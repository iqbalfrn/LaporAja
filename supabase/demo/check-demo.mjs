import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
const HERE = path.dirname(fileURLToPath(import.meta.url));

const p = fs
  .readFileSync(path.resolve(HERE, "..", "..", "local.properties"), "utf8")
  .split(/\r?\n/)
  .map((l) => l.trim())
  .filter((l) => l && !l.startsWith("#"));
const get = (k) => {
  const i = p.find((l) => l.startsWith(k + "="));
  return i ? i.slice(k.length + 1) : "";
};
const U = get("SUPABASE_URL");
const K = get("SUPABASE_ANON_KEY");

const lg = await fetch(`${U}/auth/v1/token?grant_type=password`, {
  method: "POST",
  headers: { apikey: K, "Content-Type": "application/json" },
  body: JSON.stringify({ email: "petugas.rw@laporaja.test", password: "LaporAja2026!" }),
});
console.log("login", lg.status);
const t = await lg.json();
if (!t.access_token) {
  console.log("login body:", JSON.stringify(t).slice(0, 300));
  process.exit(1);
}
const H = { apikey: K, Authorization: `Bearer ${t.access_token}` };

const r1 = await fetch(`${U}/rest/v1/reports?select=title,status,photo_path,profiles(full_name)&order=created_at.asc`, { headers: H });
console.log("reports", r1.status);
if (r1.status !== 200) console.log((await r1.text()).slice(0, 300));
else
  for (const r of await r1.json())
  console.log(` ${r.status.padEnd(8)} ${(r.profiles || {}).full_name || "?"}`.padEnd(22), r.photo_path ? "FOTO" : "----", r.title);

const r2 = await fetch(`${U}/rest/v1/activities?select=title,schedule_at,poster_path&order=schedule_at.asc`, { headers: H });
console.log("activities", r2.status);
for (const a of await r2.json())
  console.log(` ${a.schedule_at.slice(0, 10)}`.padEnd(22), a.poster_path ? "POSTER" : "------", a.title);

const r3 = await fetch(`${U}/rest/v1/profiles?select=full_name,role,avatar_path`, { headers: H });
console.log("profiles", r3.status);
for (const f of await r3.json())
  console.log(` ${f.role.padEnd(8)} ${f.full_name}`.padEnd(22), f.avatar_path ? "AVATAR" : "------");
