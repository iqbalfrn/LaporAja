import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
const HERE = path.dirname(fileURLToPath(import.meta.url));

const PROJ = path.resolve(HERE, "..", "..");
const ASSETS = path.join(HERE, "assets");
const PW = "LaporAja2026!";

const props = Object.fromEntries(
  fs
    .readFileSync(`${PROJ}/local.properties`, "utf8")
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l && !l.startsWith("#"))
    .map((l) => {
      const i = l.indexOf("=");
      return [l.slice(0, i), l.slice(i + 1)];
    })
);
const U = props.SUPABASE_URL;
const K = props.SUPABASE_ANON_KEY;
if (!U || !K) throw new Error("local.properties tidak lengkap");

const uuid = () => crypto.randomUUID();
const out = [];
const say = (s) => out.push(s);

async function login(email) {
  const r = await fetch(`${U}/auth/v1/token?grant_type=password`, {
    method: "POST",
    headers: { apikey: K, "Content-Type": "application/json" },
    body: JSON.stringify({ email, password: PW }),
  });
  if (!r.ok) throw new Error(`login ${email} -> ${r.status} ${await r.text()}`);
  const j = await r.json();
  return { email, token: j.access_token, id: j.user.id };
}

async function upload(bucket, path, file, acct) {
  const bytes = fs.readFileSync(`${ASSETS}/${file}`);
  const r = await fetch(`${U}/storage/v1/object/${bucket}/${path}?upsert=true`, {
    method: "POST",
    headers: {
      apikey: K,
      Authorization: `Bearer ${acct.token}`,
      "Content-Type": "image/jpeg",
      "x-upsert": "true",
    },
    body: bytes,
  });
  const text = await r.text();
  if (!r.ok) throw new Error(`upload ${bucket}/${path} -> ${r.status} ${text}`);
  say(`upload ${bucket} ${path} (${bytes.length} B) oleh ${acct.email}`);
  return path;
}

async function rest(method, table, filter, body, acct, representation = false) {
  const r = await fetch(`${U}/rest/v1/${table}${filter}`, {
    method,
    headers: {
      apikey: K,
      Authorization: `Bearer ${acct.token}`,
      "Content-Type": "application/json",
      Prefer: representation ? "return=representation" : "return=minimal",
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await r.text();
  if (!r.ok) throw new Error(`${method} ${table}${filter} -> ${r.status} ${text}`);
  return text ? JSON.parse(text) : null;
}

const warga1 = await login("warga.demo@laporaja.test");
const warga2 = await login("warga.dua@laporaja.test");
const petugas = await login("petugas.rw@laporaja.test");
say(`login ok: warga=${warga1.id} petugas=${petugas.id}`);

// 1. Foto profil warga.demo (jalur asli: users/<id>/avatar-<ms>.jpg, update oleh pemilik baris)
const avatarPath = await upload(
  "avatars",
  `users/${warga1.id}/avatar-${Date.now()}.jpg`,
  "avatar-rina.jpg",
  warga1
);
await rest("PATCH", "profiles", `?id=eq.${warga1.id}`, { avatar_path: avatarPath }, warga1);
say(`profiles.avatar_path <- ${avatarPath}`);

// 2. Foto laporan milik masing-masing warga (status baru => pemilik boleh update)
const r1 = await upload("report-photos", `reports/${uuid()}/${uuid()}.jpg`, "report-sampah.jpg", warga1);
const rep1 = await rest(
  "PATCH",
  "reports",
  `?title=eq.${encodeURIComponent("Tumpukan sampah liar di Gang Melati")}`,
  { photo_path: r1 },
  warga1,
  true
);
say(`reports[0] "${rep1[0].title}" status=${rep1[0].status} <- ${r1}`);

const r2 = await upload("report-photos", `reports/${uuid()}/${uuid()}.jpg`, "report-jalan.jpg", warga2);
const rep2 = await rest(
  "PATCH",
  "reports",
  `?title=eq.${encodeURIComponent("Aspal jalan lingkungan mengelupas")}`,
  { photo_path: r2 },
  warga2,
  true
);
say(`reports[1] "${rep2[0].title}" status=${rep2[0].status} <- ${r2}`);

// 3. Poster kegiatan: tabel activities hanya punya policy INSERT, jadi dibuat baru lewat petugas
const poster = await upload("activity-posters", `activities/${uuid()}/${uuid()}.jpg`, "poster-kerja-bakti.jpg", petugas);
const act = await rest(
  "POST",
  "activities",
  "",
  {
    title: "Kerja bakti penanaman pohon di taman baca",
    description:
      "Warga bersama remaja karang taruna menanam sepuluh bibit pohon di taman baca RW 04. Sarung tangan dan alat kebersihan disediakan panitia.",
    category: "kerja_bakti",
    location: "Taman Baca RW 04, Lingkungan Maju",
    schedule_at: "2026-12-13T08:00:00+07:00",
    quota: 40,
    poster_path: poster,
    created_by: petugas.id,
    is_published: true,
  },
  petugas,
  true
);
say(`activities baru "${act[0].title}" id=${act[0].id} <- ${poster}`);

// 4. Bukti bahwa URL publik bisa dibaca tanpa token
for (const [bucket, path] of [
  ["avatars", avatarPath],
  ["report-photos", r1],
  ["report-photos", r2],
  ["activity-posters", poster],
]) {
  const r = await fetch(`${U}/storage/v1/object/public/${bucket}/${path}`);
  const buf = r.ok ? await r.arrayBuffer() : null;
  say(
    `public ${bucket}/${path.split("/").slice(-1)} -> ${r.status} ${r.headers.get("content-type")} ${buf ? buf.byteLength : 0} B`
  );
}

console.log(out.join("\n"));
