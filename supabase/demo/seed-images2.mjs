import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
const HERE = path.dirname(fileURLToPath(import.meta.url));

const PROJ = path.resolve(HERE, "..", "..");
const ASSETS = path.join(HERE, "assets");
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
const uuid = () => crypto.randomUUID();
const log = [];

async function login(email) {
  const r = await fetch(`${U}/auth/v1/token?grant_type=password`, {
    method: "POST",
    headers: { apikey: K, "Content-Type": "application/json" },
    body: JSON.stringify({ email, password: "LaporAja2026!" }),
  });
  if (!r.ok) throw new Error(`login ${email} -> ${r.status}`);
  return { email, token: (await r.json()).access_token };
}

async function upload(bucket, path, file, acct) {
  const bytes = fs.readFileSync(`${ASSETS}/${file}`);
  const r = await fetch(`${U}/storage/v1/object/${bucket}/${path}?upsert=true`, {
    method: "POST",
    headers: { apikey: K, Authorization: `Bearer ${acct.token}`, "Content-Type": "image/jpeg" },
    body: bytes,
  });
  if (!r.ok) throw new Error(`upload ${path} -> ${r.status} ${await r.text()}`);
  log.push(`upload ${bucket}/${path} <- ${file} (${bytes.length} B) oleh ${acct.email}`);
  return path;
}

async function patchReport(title, photoPath, acct) {
  const r = await fetch(`${U}/rest/v1/reports?title=eq.${encodeURIComponent(title)}`, {
    method: "PATCH",
    headers: {
      apikey: K,
      Authorization: `Bearer ${acct.token}`,
      "Content-Type": "application/json",
      Prefer: "return=representation",
    },
    body: JSON.stringify({ photo_path: photoPath }),
  });
  const body = await r.json();
  if (!r.ok || !Array.isArray(body) || body.length === 0)
    throw new Error(`patch "${title}" -> ${r.status} ${JSON.stringify(body).slice(0, 200)}`);
  log.push(`reports "${title}" status=${body[0].status} <- ${photoPath}`);
}

const warga1 = await login("warga.demo@laporaja.test");
const warga2 = await login("warga.dua@laporaja.test");
const petugas = await login("petugas.rw@laporaja.test");

// Laporan status baru: pemilik boleh melengkapi datanya sendiri.
await patchReport(
  "Pagar fasum taman baca roboh",
  await upload("report-photos", `reports/${uuid()}/${uuid()}.jpg`, "report-fasum.jpg", warga2),
  warga2
);

// Laporan status selesai: hanya petugas yang masih punya policy UPDATE,
// jadi foto bukti dipasang memakai sesi petugas.
await patchReport(
  "Lampu penerangan jalan mati",
  await upload("report-photos", `reports/${uuid()}/${uuid()}.jpg`, "report-lampu.jpg", warga1),
  petugas
);

console.log(log.join("\n"));
