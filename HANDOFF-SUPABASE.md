# HANDOFF — lanjutan progres setup Supabase LaporAja

Dokumen ini untuk AI/sesi baru yang melanjutkan pekerjaan integrasi Supabase. Bacalah
[`SETUP-SUPABASE.md`](SETUP-SUPABASE.md) sebagai acuan langkah manual, dokumen ini sebagai
"di mana kita berhenti dan apa yang sudah terbukti".

Terakhir diperbarui: 2026-11-23.

## 1. Status: server-side SUDAH SELESAI DAN TERUJI

| Aspek | Status |
| --- | --- |
| Proyek Supabase | `laporaja`, ref `aghtczoujceugtwtnrmk`, region `ap-southeast-2` (Sydney), Nano, Healthy |
| Project URL | `https://aghtczoujceugtwtnrmk.supabase.co` |
| Skema `supabase/setup.sql` | sudah dijalankan, hasil: 4 tabel, RLS aktif di semuanya, 10 policy `public`, 3 policy `storage.objects`, 5 fungsi, trigger `on_auth_user_created` / `stamp_official` / `guard_role` / `touch_profiles` / `touch_reports` / `touch_activities` |
| Bucket | `report-photos`, `activity-posters`, `avatars` — ketiganya publik dan **terbukti** terbaca lewat URL publik tanpa token |
| Auth | Confirm email **mati**, Site URL `http://localhost`. Honeypot & captcha tidak lagi ada di dashboard versi baru (default nonaktif) |
| Kredensial klien | `local.properties` terisi (`SUPABASE_URL`, `SUPABASE_ANON_KEY` = *publishable key*), berkas ini ada di `.gitignore` |
| Akun uji | 3 akun dibuat lewat `POST /auth/v1/signup` (jalur yang sama dengan form register app) sehingga trigger `handle_new_user` teruji dan baris `profiles` terisi otomatis |
| Data demo | 5 laporan (status: 2 baru, 1 diproses, 1 selesai, 1 ditolak), 5 kegiatan dalam 4 kategori (3 jadwal mendatang), 1 tanggapan warga + 1 tanggapan resmi |
| Aset gambar | 4 foto laporan, 1 poster kegiatan, 1 foto profil — diunggah ke Storage dan tampil di UI (Coil) |
| Build | `assembleDebug` + `testDebugUnitTest` **BUILD SUCCESSFUL**, 38 tes / 0 gagal |
| Ikon & label | `application-label:'LaporAja'`, adaptive icon lonceng hijau `#146C43` (`res/mipmap-anydpi-v26` + fallback vektor `res/mipmap-anydpi` untuk API 24-25) |
| Dua perangkat | terbukti: `emulator-5554` (Rina, 3 laporannya) dan `emulator-5556` (Bagas, 2 laporannya) saling tidak melihat laporan satu sama lain |
| UI nyata | login `warga.demo` → Beranda "Halo, Rina" + 3 laporannya (2 berfoto), tab Kegiatan menampilkan 5 kartu + chip filter, tab Akun menampilkan avatar dari Storage |

Detail data demo (semua nama & lokasi fiktif, tidak ada foto wajah nyata):

- Milik `warga.demo` (Rina Kusuma): sampah "Tumpukan sampah liar di Gang Melati" (baru, 2 tanggapan,
  **berfoto**), saluran "Saluran air tersumbat di Blok C" (diproses, tanpa foto),
  penerangan "Lampu penerangan jalan mati" (selesai, **berfoto**). Profilnya punya **avatar**.
- Milik `warga.dua` (Bagas Prakoso): jalan "Aspal jalan lingkungan mengelupas" (**ditolak**,
  **berfoto**), fasum "Pagar fasum taman baca roboh" (baru, **berfoto**).
- Kegiatan: kerja bakti 29 Nov 2026, edukasi 6 Des 2026, **kerja bakti penanaman pohon 13 Des 2026
  (punya poster)**, bank sampah 15 Nov 2026, kunjungan 8 Nov 2026.
- Cara aset dipasang (boleh diulang, semuanya lewat jalur asli aplikasi):
  unggah ke Storage dengan token pemilik → `PATCH profiles.avatar_path` (diri sendiri),
  `PATCH reports.photo_path` (oleh pemilik selama status `baru`/`ditolak`, sisanya oleh petugas),
  dan poster dipasang lewat `POST activities` karena tabel itu hanya punya policy INSERT.
  Skrip dan gambarnya ada di repo: [`supabase/demo/`](supabase/demo/README.md)
  (`seed-images.mjs`, `seed-images2.mjs` tidak idempoten; `check-demo.mjs` hanya membaca dan
  aman diulang).

## 2. DUA BUG YANG SAYA PERBAIKI — JANGAN DI-REVERT

Keduanya membuat panduan lama mustahil berhasil di proyek baru. Sudah diterapkan di berkas
**dan** di database.

1. **Urutan pembuatan fungsi** ([supabase/setup.sql:52](supabase/setup.sql#L52)).
   `is_petugas()` berbahasa `sql` semula dibuat di bagian A1, sebelum tabel `profiles` ada.
   PostgreSQL memvalidasi body fungsi `language sql` saat `CREATE`, jadi skrip mati dengan
   `ERROR 42P01: relation "public.profiles" does not exist`. Fungsi dipindah ke bagian **A2b**,
   tepat setelah `profiles` dibuat.
2. **Kunci peran terlalu ketat** ([supabase/setup.sql:137](supabase/setup.sql#L137)).
   `guard_profile_role` semula membatalkan perubahan `role` untuk **semua** UPDATE, termasuk dari
   SQL Editor. Instruksi promosi petugas di `SETUP-SUPABASE.md` §6 berjalan "sukses" tanpa
   mengubah apa pun (saya ukur: `role` tetap `warga`). Sekarang kondisi menjadi
   `if auth.uid() is not null and new.role is distinct from old.role`. Efek: aplikasi tetap
   terkunci (request klien selalu membawa JWT), admin masih bisa menaikkan peran dari Dashboard.

Konsekuensi: kalau nanti menambah kolom/tabel, jalankan ulang `setup.sql` — skripnya idempoten
dan urutannya sudah benar.

## 3. Aturan yang wajib dipatuhi AI berikutnya

- **Jangan** menyalin nilai anon/publishable key ke berkas yang terlacak Git. Sumber kebenaran
  hanya `local.properties`. Jangan cetak key di log, screenshot, atau percakapan.
- **Jangan** memakai `service_role` / secret key di klien, contoh, atau komentar.
- **Jangan** menonaktifkan RLS untuk menyelesaikan galat 403 — tambahkan policy yang kurang.
- **Jangan** menghapus guard `auth.uid() is not null` pada `guard_profile_role` dan **jangan**
  memindahkan `is_petugas()` kembali ke A1 (lihat §2).
- Nama tabel/kolom adalah kontrak dua arah: ubah `supabase/setup.sql` **dan**
  [SupabaseTables.kt](app/src/main/java/id/ac/kuliah/laporaja/data/remote/SupabaseTables.kt) sekaligus.

## 4. Cara memverifikasi ulang (tanpa membuka dashboard)

Baca kredensial dari `local.properties`, jangan hardcode.

```bash
# dari root project LaporAja
node -e '
const fs=require("fs");
const p=fs.readFileSync("local.properties","utf8").split("\n").map(l=>l.trim()).filter(l=>l&&!l.startsWith("#"));
const get=k=>{const i=p.find(l=>l.startsWith(k+"="));return i?i.slice(k.length+1):""};
const U=get("SUPABASE_URL"),K=get("SUPABASE_ANON_KEY");
const H={apikey:K,Authorization:"Bearer "+K};
(async()=>{
  const r=await fetch(U+"/rest/v1/profiles?select=email,role",{headers:H});
  console.log(r.status,await r.text());
})();'
```

Yang diharapkan:

- `GET /rest/v1/profiles` dengan **anon/publishable key saja** → `200 []` (RLS menolak anonim).
- `GET /rest/v1/` (daftar OpenAPI) → `401 Secret API key required`. Ini **normal** untuk
  publishable key, bukan tanda kredensial rusak.
- Login: `POST /auth/v1/token?grant_type=password` → `200` + `access_token`.
- Matriks privasi (sudah terbukti, uji ulang bila policy berubah): warga.demo melihat 3 laporan,
  warga.dua 2, petugas 5, anonim 0. Warga membuat kegiatan → `403`. Warga menaikkan
  `role`-nya sendiri → tetap `warga`.
- Storage: upload sebagai akun login → `200`; baca tanpa token
  `GET /storage/v1/object/public/<bucket>/<path>` dengan path mengikuti `ImageUploader.kt`:
  `reports/<uuid>/<uuid>.jpg`, `activities/<uuid>/<uuid>.jpg`,
  `users/<user-id>/avatar-<timestamp>.jpg` → `200 image/jpeg`.
  Menghapus objek dari klien → `400` (memang tidak ada policy DELETE; hapus lewat UI Storage).
- Inventaris cepat (tanpa membuka dashboard): `node supabase/demo/check-demo.mjs` dari root
  project — mencetak laporan + penanda FOTO, kegiatan + penanda POSTER, dan profil + penanda AVATAR.

## 5. Gotchas teknis yang memakan waktu di sesi ini

**Gradle / terminal**

- `java` tidak ada di PATH Git Bash. `./gradlew` gagal dengan `ERROR: JAVA_HOME is not set`,
  dan karena outputnya saya pipe ke `tail`, exit code tetap 0 — saya sempat salah melaporkan
  "build hijau". Set `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"` dan **periksa
  isi output**, bukan hanya exit code.

**Supabase Dashboard lewat otomasi browser**

- Panel browser bawaan bisa tidak terlihat; klik pointer gagal dengan
  `NATIVE_BROWSER_VIEWPORT_UNAVAILABLE`, sedangkan `evaluate_script` tetap jalan.
- Editor SQL = Monaco. Isi lewat `window.monaco.editor.getModels()[0].setValue(...)`, jalankan
  dengan menekan tombol yang teksnya diawali `Run Ctrl`. Muncul dialog *destructive operations*
  (karena `drop policy/trigger if exists`) → tombolnya `Run query`.
- `fill`/mengetik 9,6 KB ke editor dan `navigator.clipboard.readText()` **ditolak**; membaca key
  dari halaman API Keys juga ditolak kebijakan keamanan. Jalur yang benar: minta pengguna
  menempelkan publishable key, atau biarkan pengguna yang mengisinya.
- Kalau menempel teks hasil salinan, **verifikasi hash** sebelum menjalankan
  (`crypto.subtle.digest('SHA-256', ...)` dibandingkan `sha256sum` berkas). Saya menangkap dua
  kesalahan transkripsi cara ini. Hati-hati: `String.replace` dengan replacement berisi `$$`
  akan menelan satu `$` — pakai function replacer.
- Menghapus baris `storage.objects` via SQL diblokir trigger `storage.protect_delete`;
  gunakan UI Storage (menu ⋯ pada berkas → Delete).

**adb / emulator**

- `adb` ada di `C:/Users/LENOVO/AppData/Local/Android/Sdk/platform-tools/adb.exe`.
- Di Git Bash set `MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL='*'`, kalau tidak `/sdcard/...`
  berubah menjadi path Git.
- `adb exec-out screencap -p > file.png` menghasilkan PNG rusak. Yang berhasil:
  `adb shell "screencap -p /sdcard/x.png"` lalu `adb pull`.
- Untuk mengetuk elemen Compose, jangan menebak koordinat dari gambar:
  `uiautomator dump` lalu ambil atribut `bounds`. Keyboard yang muncul menggeser layout,
  jadi tombol bisa berpindah.
- Emulator yang tersedia: `emulator-5554` (1080x2400, 420 dpi). AVD: `Resizable_Experimental`
  (hanya satu; `Resizable_Experimental_2` hanyalah clone multi-instance, tidak terdaftar di
  `emulator -list-avds`).
- **Dua perangkat dari satu AVD**: instance kedua hanya bisa jalan kalau **semua** instance
  memakai `-read-only`; kalau ada yang tidak, muncul
  `FATAL | Running multiple emulators with the same AVD is an experimental feature` lalu
  `ERROR | Another emulator instance is running`. Jadi: matikan yang sedang jalan
  (`adb -s <dev> emu kill`), lalu jalankan dua kali
  `emulator -avd Resizable_Experimental -read-only -no-boot-anim -no-snapshot-save -gpu auto -port 5554|5556`.
  `avdmanager` tidak terpasang di mesin ini, jadi membuat AVD baru bukan opsi cepat.
- Instance read-only kadang radio-nya belum naik saat boot selesai: app menampilkan
  "Tidak ada koneksi internet, coba lagi" padahal `sys.boot_completed=1`. Cek
  `dumpsys connectivity | grep 'Active default network'` sampai `IS_VALIDATED`, lalu ulangi
  tap Masuk. `ping 8.8.8.8` bisa gagal walau HTTPS lancar, jadi jangan pakai ping sebagai bukti.
- `adb pull` dan `adb install` butuh path **Windows** (`C:/...`) untuk sisi host; dengan
  `MSYS_NO_PATHCONV=1` path `/c/...` tidak dikonversi dan gagal dengan "No such file or directory".
- `input text` dapat menggandakan karakter pertama (mis. `wwarga@...`). Kalau login gagal tanpa
  galat, dump hierarchy dan baca ulang isi field. Mengosongkan field:
  `input keyevent 123` (MOVE_END) lalu `input keyevent 67` (DEL) sebanyak panjang teks.
- Tombol **Ganti akun demo** di tab Akun (khusus build debug) memindahkan sesi ke akun demo lain
  tanpa mengetik ulang — praktis untuk berpindah warga/petugas.
- Membuktikan label/ikon dari artefak:
  `build-tools/36.0.0/aapt2 dump badging app/build/outputs/apk/debug/app-debug.apk`.

**Aset gambar demo**

- Yang tersedia di mesin ini hanya **Pillow 11.3** (`python`); ImageMagick dan ffmpeg tidak ada,
  dan `/c/WINDOWS/System32/convert` adalah utilitas disk, bukan ImageMagick.
- Meniru pipeline app: sisi terpanjang 1280 px, JPEG kualitas 80 (`ImageCompressor.kt`).

## 6. Yang BELUM dikerjakan

Sudah selesai sejak versi sebelumnya dokumen ini: ikon & label launcher, aset gambar, verifikasi
dua perangkat, dan koreksi `SETUP-SUPABASE.md` (§2 honeypot/captcha sudah hilang dari dashboard,
§4 path Storage yang sebenarnya, §5 publishable key menggantikan anon key, §6 promosi peran +
SELECT pembanding). Yang tersisa:

1. **Poster untuk 4 kegiatan lama.** Tabel `activities` sengaja tidak punya policy UPDATE (app hanya
   membuat kegiatan), jadi `poster_path` baris lama tidak bisa diisi dari klien. Pilih salah satu:
   isi lewat SQL Editor, atau biarkan dan buat kegiatan baru beserta poster seperti yang sudah
   dilakukan untuk kegiatan 13 Des 2026.
2. **Foto laporan "Saluran air tersumbat di Blok C"** (status `diproses`). Pemilik tidak lagi boleh
   PATCH pada status itu, jadi foto dipasang memakai sesi petugas — sama seperti foto laporan
   `selesai`.
3. **Satu akun didaftarkan lewat UI Register**, bukan lewat `POST /auth/v1/signup`. Jalurnya identik
   (saya pakai REST yang sama), tetapi daftar periksa Lampiran C berbunyi "dibuat lewat aplikasi".
4. **Belum ada repository Git** di folder ini (`git rev-parse` gagal), jadi perubahan
   `supabase/setup.sql`, `SETUP-SUPABASE.md`, dan `local.properties` belum tercatat di riwayat.
5. **HP fisik** (opsional). Kebutuhan "dua perangkat" sudah terpenuhi lewat dua emulator read-only;
   perangkat keras asli hanya perlu bila ingin menguji ukuran layar/kinerja nyata.

Catatan kondisi mesin: dua emulator read-only (`emulator-5554` = Rina, `emulator-5556` = Bagas)
masih berjalan ketika dokumen ini diperbarui. Menutupnya:
`adb -s emulator-5554 emu kill && adb -s emulator-5556 emu kill`.

## 7. Kredensial akun uji

| Sebutan | Email | Peran |
| --- | --- | --- |
| Warga utama | `warga.demo@laporaja.test` | warga |
| Warga kedua | `warga.dua@laporaja.test` | warga |
| Petugas | `petugas.rw@laporaja.test` | petugas |

Password ketiganya `LaporAja2026!` (khusus akun demo, seperti di `SETUP-SUPABASE.md` §6).
Publishable key tidak diduplikasi ke dokumen ini — ambil dari `local.properties`.
