import type { TalesDb } from "./tales-db";

export { TalesDb } from "./tales-db";

type Env = {
  TALES_DB: DurableObjectNamespace<TalesDb>;
  ADMIN_TOKEN?: string;
};

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, PUT, DELETE, OPTIONS",
  "Access-Control-Allow-Headers": "Authorization, Content-Type",
};

function json(body: unknown, status = 200, extra: Record<string, string> = {}): Response {
  return Response.json(body, { status, headers: { ...CORS_HEADERS, ...extra } });
}

function bearer(request: Request): string | null {
  const h = request.headers.get("Authorization") ?? "";
  return h.startsWith("Bearer ") ? h.slice(7) : h || null;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: CORS_HEADERS });
    const url = new URL(request.url);
    const db = env.TALES_DB.getByName("main");
    const path = url.pathname.replace(/\/+$/, "") || "/";

    if (path === "/ping") return json({ ok: true });

    // Public catalogue read by the app.
    if (path === "/tales" && request.method === "GET") {
      return json({ tales: await db.listTales() }, 200, { "Cache-Control": "public, max-age=60" });
    }

    // Admin: add or edit tales without an app update. Requires the ADMIN_TOKEN project env.
    const adminMatch = path.match(/^\/admin\/tales\/([a-z0-9_-]{1,64})$/);
    if (adminMatch) {
      if (!env.ADMIN_TOKEN || bearer(request) !== env.ADMIN_TOKEN) return json({ error: "unauthorized" }, 401);
      const id = adminMatch[1]!;
      if (request.method === "PUT") {
        const body = (await request.json().catch(() => null)) as Record<string, unknown> | null;
        if (!body || typeof body !== "object") return json({ error: "JSON body required" }, 400);
        const result = await db.upsertTale(id, body);
        return "error" in result ? json(result, 400) : json(result);
      }
      if (request.method === "DELETE") {
        await db.deleteTale(id);
        return json({ ok: true });
      }
      return json({ error: "method_not_allowed" }, 405);
    }

    return json({ error: "not_found" }, 404);
  },
} satisfies ExportedHandler<Env>;
