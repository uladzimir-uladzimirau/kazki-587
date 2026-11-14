import type { TalesDb } from "./tales-db";

export { TalesDb } from "./tales-db";

type Env = {
  TALES_DB: DurableObjectNamespace<TalesDb>;
  AUDIO: R2Bucket;
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

/** Streams a tale's MP3 from R2 with HTTP Range support (needed for seek in ExoPlayer). */
async function serveAudio(request: Request, env: Env, key: string): Promise<Response> {
  const rangeHeader = request.headers.get("Range");
  let range: { offset?: number; length?: number; suffix?: number } | undefined;
  let partial = false;

  if (rangeHeader) {
    const m = rangeHeader.match(/^bytes=(\d*)-(\d*)$/);
    if (!m || (m[1] === "" && m[2] === "")) {
      return new Response(null, { status: 416, headers: { ...CORS_HEADERS, "Content-Range": "bytes */*" } });
    }
    if (m[1] === "") {
      range = { suffix: parseInt(m[2]!, 10) };
    } else {
      const offset = parseInt(m[1]!, 10);
      range = m[2] !== "" ? { offset, length: parseInt(m[2]!, 10) - offset + 1 } : { offset };
    }
    partial = true;
  }

  const object = await env.AUDIO.get(key, range ? { range: range as R2Range } : undefined);
  if (!object) return json({ error: "not_found" }, 404);

  const headers = new Headers(CORS_HEADERS);
  headers.set("Content-Type", object.httpMetadata?.contentType ?? "audio/mpeg");
  headers.set("Cache-Control", "public, max-age=31536000, immutable");
  headers.set("Accept-Ranges", "bytes");
  headers.set("ETag", object.httpEtag);

  let status = 200;
  const servedRange = object.range as { offset?: number; length: number } | undefined;
  if (partial && servedRange) {
    const offset = servedRange.offset ?? object.size - servedRange.length;
    headers.set("Content-Range", `bytes ${offset}-${offset + servedRange.length - 1}/${object.size}`);
    headers.set("Content-Length", String(servedRange.length));
    status = 206;
  } else {
    headers.set("Content-Length", String(object.size));
  }

  if (request.method === "HEAD") return new Response(null, { status, headers });
  return new Response(object.body, { status, headers });
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: CORS_HEADERS });
    const url = new URL(request.url);
    const db = env.TALES_DB.getByName("main");
    const path = url.pathname.replace(/\/+$/, "") || "/";

    if (path === "/ping") return json({ ok: true });

    // Tale audio streamed from R2, with Range support for seeking.
    const audioMatch = path.match(/^\/audio\/([a-z0-9_-]{1,64}\.mp3)$/);
    if (audioMatch && (request.method === "GET" || request.method === "HEAD")) {
      return serveAudio(request, env, audioMatch[1]!);
    }

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
