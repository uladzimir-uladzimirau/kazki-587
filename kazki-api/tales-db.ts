import { DurableObject } from "cloudflare:workers";

type Env = {
  ADMIN_TOKEN?: string;
};

const sample = (n: number) => `https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${n}.mp3`;

type SeedTale = [id: string, title: string, category: string, isFree: number, minutes: number, description: string];

// Historical v1 seed, used only by the initial migration on old installs; superseded by the v4 reseed.
// Placeholder catalogue. Replace audio_url / cover_url per tale via PUT /admin/tales/:id.
const SEED_V1: SeedTale[] = [
  ["kotok", "Каток зь Пеўнікам ды Ліска", "zviarynyja", 1, 8, "Ліска ўкрала Пеўніка — але верны Каток са сваёй музыкай ужо пасыпеў на выручку. Цёплая казка пра сяброўства і спагаду."],
  ["zajac_vozyk", "Заяц і Вожык", "zviarynyja", 0, 6, "Ганарысты Заяц пасмяяўся з кароценькіх вожыкавых ног — ды прайграў спаборніцтва ў бегу. Казка пра тое, што разум хутчэйшы за ногі."],
  ["sabaka_vouk", "Сабака і Воўк", "zviarynyja", 0, 7, "Галодны Воўк пазайздросціў сытаму Сабаку, але, убачыўшы следак ад ланцуга на ягонай шыі, паляцеў назад у лес. Воля даражэйшая за міску ежы."],
  ["chvasty", "Хвасты", "zviarynyja", 0, 5, "Чаму ў Лісіцы хвост пухнаты, а ў Зайца — короткі? Забаўная казка пра тое, як звяры дзялілі хвасты і хто апынуўся хітрэйшым."],
  ["zalaty_ptach", "Залаты птах", "charounyja", 1, 10, "У царскім садзе знікалі залатыя яблыкі — то быў цудоўны Птах! Малодшага сына чакае далёкі шлях, поўны чараў і выпрабаванняў."],
  ["karalevic", "Каралевіч, чараўнік і ягоная дачка", "charounyja", 0, 12, "Каралевіч трапіў у палон да магутнага чараўніка, а ўцякаць дапамагла ягоная дачка. Казка пра адвагу, хітрасць і цудоўныя перамены."],
  ["och_tabakierka", "Ох і залатая табакерка", "charounyja", 0, 9, "Просты Ох здабыў чароўную залатую табакерку — і пачаліся дзівосы! Смешная казка пра тое, як шчасце прыходзіць да добрага сэрца."],
  ["palasuki", "Паляшукі й Палявікі", "mifalagicnyja", 1, 9, "У лесе гаспадарыць Паляшук, а ў полі — Палявік. Што будзе, калі гэтыя нябачныя духі паспрачаюцца? Загадкавая казка пра гаспадароў беларускай прыроды."],
  ["sciopka", "Як Сцёпка з панам гаварыў", "zabaunyja", 0, 7, "Сцёпка пана не баяўся і адказваў так хітра, што той толькі рот раззявіў. Вясёлая казка пра востры розум простага чалавека."],
  ["razumnaja_dacka", "Разумная дачка", "zabaunyja", 0, 8, "Бацькава дачка разгадавала загадкі, якія нікому іншаму не былі па сіле. Казка пра дзяўчынку, у якой розум — найбольшае багацце."],
  ["muzyka", "Музыка-чарадзей", "autarskija", 1, 9, "Філасофская казка пра дзівосную сілу музыкі і яе супрацьстаянне злу ды ліху."],
  ["andrej", "Андрэй за ўсіх мудрэй", "autarskija", 0, 11, "Чарадзейная казка пра чалавечую мудрасць і іспыты жыцця."],
  ["kot", "Як кот звяроў напалохаў", "autarskija", 0, 6, "Іранічная казка пра хітрасць, знаходлівасць і прыткі розум."],
  ["sapciucha", "Шаптуха", "autarskija", 0, 8, "Сацыяльна-бытавая казка пра тое, што на ўсякага хітрага знойдзецца яшчэ хітрэйшы."],
  ["alionka", "Алёнка", "autarskija", 0, 10, "Чарадзейная казка пра жаночую смеласць ды рашучасць."],
  ["barsuk", "Чаму барсук і лісіца ў норах жывуць", "autarskija", 0, 7, "Казка пра тое, чаму звяры такія розныя і што з імі здарылася на самой справе."],
];

// Current starter catalogue (v4): 6 tales, narration not recorded yet — audio_url stays NULL.
// Set audioUrl / coverUrl / durationSec per tale via PUT /admin/tales/:id once audio is ready.
type SeedTaleV2 = [id: string, title: string, category: string, description: string];
const SEED: SeedTaleV2[] = [
  ["muzyka", "Музыка-чарадзей", "charounyja", "Філасофская казка пра дзівосную сілу музыкі і яе супрацьстаянне злу ды ліху."],
  ["andrej", "Андрэй за ўсіх мудрэй", "charounyja", "Чарадзейная казка пра чалавечую мудрасць і іспыты жыцця."],
  ["kot", "Як кот звяроў напалохаў", "zviarynyja", "Іранічная казка пра хітрасць, знаходлівасць і прыткі розум."],
  ["sapciucha", "Шаптуха", "zabaunyja", "Сацыяльна-бытавая казка пра тое, што на ўсякага хітрага знойдзецца яшчэ хітрэйшы."],
  ["alionka", "Алёнка", "charounyja", "Чарадзейная казка пра жаночую смеласць ды рашучасць."],
  ["barsuk", "Чаму барсук і лісіца ў норах жывуць", "mifalagicnyja", "Казка пра тое, чаму звяры такія розныя і што з імі здарылася на самой справе."],
];

const CATEGORIES = new Set(["zviarynyja", "charounyja", "mifalagicnyja", "zabaunyja", "autarskija"]);

// Append new steps; never edit one that has shipped.
const MIGRATIONS: ((sql: SqlStorage) => void)[] = [
  (sql) => {
    sql.exec(`CREATE TABLE tales (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      category TEXT NOT NULL,
      cover_url TEXT,
      audio_url TEXT NOT NULL,
      duration_sec INTEGER NOT NULL,
      is_free INTEGER NOT NULL DEFAULT 0,
      description TEXT NOT NULL DEFAULT '',
      sort_order INTEGER NOT NULL DEFAULT 0,
      published INTEGER NOT NULL DEFAULT 1,
      updated_at INTEGER NOT NULL
    )`);
    SEED_V1.forEach(([id, title, category, isFree, minutes, description], i) => {
      sql.exec(
        "INSERT INTO tales (id, title, category, cover_url, audio_url, duration_sec, is_free, description, sort_order, published, updated_at) VALUES (?, ?, ?, NULL, ?, ?, ?, ?, ?, 1, ?)",
        id, title, category, sample((i % 16) + 1), minutes * 60, isFree, description, (i + 1) * 10, Date.now(),
      );
    });
  },
  (sql) => {
    // Anonymous RevenueCat app user ids only; no personal data.
    sql.exec(`CREATE TABLE subscriptions (
      app_user_id TEXT PRIMARY KEY,
      active INTEGER NOT NULL,
      expires_at INTEGER,
      last_event TEXT NOT NULL,
      updated_at INTEGER NOT NULL
    )`);
  },
  (sql) => {
    // KAZKI is fully free: every tale is open, and subscription tracking is gone.
    sql.exec("UPDATE tales SET is_free = 1");
    sql.exec("DROP TABLE IF EXISTS subscriptions");
  },
  (sql) => {
    // New 6-tale starter catalogue replaces the placeholder 16. Narration is not
    // recorded yet, so audio_url becomes nullable and stays NULL until uploaded.
    sql.exec("ALTER TABLE tales RENAME TO tales_old");
    sql.exec(`CREATE TABLE tales (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      category TEXT NOT NULL,
      cover_url TEXT,
      audio_url TEXT,
      duration_sec INTEGER NOT NULL,
      is_free INTEGER NOT NULL DEFAULT 0,
      description TEXT NOT NULL DEFAULT '',
      sort_order INTEGER NOT NULL DEFAULT 0,
      published INTEGER NOT NULL DEFAULT 1,
      updated_at INTEGER NOT NULL
    )`);
    sql.exec("DROP TABLE tales_old");
    SEED.forEach(([id, title, category, description], i) => {
      sql.exec(
        "INSERT INTO tales (id, title, category, cover_url, audio_url, duration_sec, is_free, description, sort_order, published, updated_at) VALUES (?, ?, ?, NULL, NULL, 0, 1, ?, ?, 1, ?)",
        id, title, category, description, (i + 1) * 10, Date.now(),
      );
    });
  },
  (sql) => {
    // First batch of narrations: m4a (AAC 80k mono) uploaded to the kazki-audio R2
    // bucket, served via its public r2.dev URL.
    const AUDIO: [id: string, audioUrl: string, durationSec: number][] = [
      ["muzyka", "https://pub-1a65051c16c1434c80f6e16db46fe497.r2.dev/muzyka.m4a", 449],
      ["kot", "https://pub-1a65051c16c1434c80f6e16db46fe497.r2.dev/kot.m4a", 439],
      ["barsuk", "https://pub-1a65051c16c1434c80f6e16db46fe497.r2.dev/barsuk.m4a", 433],
    ];
    AUDIO.forEach(([id, url, durationSec]) => {
      sql.exec(
        "UPDATE tales SET audio_url = ?, duration_sec = ?, updated_at = ? WHERE id = ?",
        url, durationSec, Date.now(), id,
      );
    });
  },
  (sql) => {
    // Second batch of narrations: the remaining three tales.
    const AUDIO: [id: string, audioUrl: string, durationSec: number][] = [
      ["andrej", "https://pub-1a65051c16c1434c80f6e16db46fe497.r2.dev/andrej.m4a", 499],
      ["sapciucha", "https://pub-1a65051c16c1434c80f6e16db46fe497.r2.dev/sapciucha.m4a", 302],
      ["alionka", "https://pub-1a65051c16c1434c80f6e16db46fe497.r2.dev/alionka.m4a", 646],
    ];
    AUDIO.forEach(([id, url, durationSec]) => {
      sql.exec(
        "UPDATE tales SET audio_url = ?, duration_sec = ?, updated_at = ? WHERE id = ?",
        url, durationSec, Date.now(), id,
      );
    });
  },
  (sql) => {
    // Two original tales (autarskija): mp3 narrations and webp covers on R2.
    const R2 = "https://pub-1a65051c16c1434c80f6e16db46fe497.r2.dev";
    const TALES: [id: string, title: string, durationSec: number, sortOrder: number][] = [
      ["dyvanok", "Кошка Мышка і жоўты дыванок", 676, 70],
      ["kupalle", "Купалле, чупакабра і рэдкія ружы", 1391, 80],
    ];
    TALES.forEach(([id, title, durationSec, sortOrder]) => {
      sql.exec(
        `INSERT INTO tales (id, title, category, cover_url, audio_url, duration_sec, is_free, description, sort_order, published, updated_at)
         VALUES (?, ?, 'autarskija', ?, ?, ?, 1, '', ?, 1, ?)
         ON CONFLICT(id) DO NOTHING`,
        id, title, `${R2}/cover_${id}.webp`, `${R2}/${id}.mp3`, durationSec, sortOrder, Date.now(),
      );
    });
  },
];

type TaleRow = {
  id: string; title: string; category: string; cover_url: string | null; audio_url: string | null;
  duration_sec: number; is_free: number; description: string; sort_order: number;
};

const toTale = (r: TaleRow) => ({
  id: r.id,
  title: r.title,
  category: r.category,
  coverUrl: r.cover_url,
  audioUrl: r.audio_url,
  durationSec: r.duration_sec,
  isFree: r.is_free === 1,
  description: r.description,
  sortOrder: r.sort_order,
});

export class TalesDb extends DurableObject<Env> {
  constructor(ctx: DurableObjectState, env: Env) {
    super(ctx, env);
    ctx.blockConcurrencyWhile(async () => this.migrate());
  }

  private migrate(): void {
    const sql = this.ctx.storage.sql;
    sql.exec("CREATE TABLE IF NOT EXISTS _migrations (version INTEGER NOT NULL)");
    const done = sql.exec<{ version: number }>("SELECT version FROM _migrations").toArray()[0]?.version ?? 0;
    for (let v = done; v < MIGRATIONS.length; v += 1) {
      this.ctx.storage.transactionSync(() => {
        MIGRATIONS[v]!(sql);
        sql.exec("DELETE FROM _migrations");
        sql.exec("INSERT INTO _migrations (version) VALUES (?)", v + 1);
      });
    }
  }

  listTales() {
    return this.ctx.storage.sql
      .exec<TaleRow>("SELECT * FROM tales WHERE published = 1 ORDER BY sort_order, id")
      .toArray()
      .map(toTale);
  }

  upsertTale(id: string, body: Record<string, unknown>): { ok: true } | { error: string } {
    const existing = this.ctx.storage.sql.exec<TaleRow>("SELECT * FROM tales WHERE id = ?", id).toArray()[0];
    const str = (k: string, fallback?: string | null) => (typeof body[k] === "string" ? (body[k] as string) : fallback);
    const num = (k: string, fallback?: number) => (typeof body[k] === "number" ? (body[k] as number) : fallback);
    const bool = (k: string, fallback?: number) => (typeof body[k] === "boolean" ? (body[k] ? 1 : 0) : fallback);

    const title = str("title", existing?.title);
    const category = str("category", existing?.category);
    const audioUrl = str("audioUrl", existing?.audio_url) ?? null;
    const durationSec = num("durationSec", existing?.duration_sec) ?? 0;
    if (!title || !category) {
      return { error: "title and category are required for a new tale" };
    }
    if (!CATEGORIES.has(category)) return { error: `category must be one of ${[...CATEGORIES].join(", ")}` };

    this.ctx.storage.sql.exec(
      `INSERT INTO tales (id, title, category, cover_url, audio_url, duration_sec, is_free, description, sort_order, published, updated_at)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
       ON CONFLICT(id) DO UPDATE SET title=excluded.title, category=excluded.category, cover_url=excluded.cover_url,
         audio_url=excluded.audio_url, duration_sec=excluded.duration_sec, is_free=excluded.is_free,
         description=excluded.description, sort_order=excluded.sort_order, published=excluded.published, updated_at=excluded.updated_at`,
      id, title, category,
      str("coverUrl", existing?.cover_url ?? null) ?? null,
      audioUrl, durationSec,
      1, // every tale is free
      str("description", existing?.description ?? "") ?? "",
      num("sortOrder", existing?.sort_order ?? 1000) ?? 1000,
      bool("published", 1) ?? 1,
      Date.now(),
    );
    return { ok: true };
  }

  deleteTale(id: string) {
    this.ctx.storage.sql.exec("DELETE FROM tales WHERE id = ?", id);
  }
}
