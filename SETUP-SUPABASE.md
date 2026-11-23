# Persiapan Supabase untuk LaporAja

Panduan ini menyelesaikan bagian "server" dari proyek. Tanpa langkah di bawah, aplikasi tetap
terpasang dan berjalan, tetapi setiap layar data menampilkan pesan
`Kredensial Supabase belum diisi, buka local.properties`.

Acuan: PRD Subbab 6 (kredensial), Subbab 7.8 (bucket), Subbab 13 (RLS), Lampiran A/B/C.

## 0. Yang dibutuhkan

- Akun Supabase (paket gratis cukup).
- Proyek Android ini sudah terbuka di Android Studio (compileSdk 37, AGP 9.3.3, Kotlin 2.2.10).
- Waktu ± 15 menit. Skrip `supabase/setup.sql` dibuat idempoten sehingga boleh dijalankan ulang.

## 1. Buat proyek

1. Buka <https://supabase.com/dashboard> → **New project**.
2. Nama bebas (mis. `laporaja`), database password disimpan baik-baik, region terdekat.
3. Tunggu sampai proyek berstatus **Active** (± 2 menit).

## 2. Matikan konfirmasi email (khusus pengembangan)

**Authentication → Providers → Email**:
- **Confirm email**: mati. Alasannya, akun uji dibuat lewat form register dan langsung dipakai demo;
  kalau aktif, login pertama selalu tertahan.
- **Use a honeypot** dan **Enable captcha**: mati selama pengembangan. Pada dashboard generasi
  terbaru kedua sakelar itu sudah tidak muncul lagi (default nonaktif), jadi cukup pastikan
  register dari aplikasi langsung berhasil.

Catat juga **Authentication → URL Configuration**: **Site URL** diisi `http://localhost` (tidak dipakai
langsung oleh klien, hanya agar alur auth tidak menolak).

## 3. Jalankan skrip skema

1. **SQL Editor → New query**.
2. Tempel seluruh isi [`supabase/setup.sql`](supabase/setup.sql), lalu **Run**.
3. Buka **Table Editor** dan pastikan berisi 4 tabel: `profiles`, `reports`, `report_responses`,
   `activities`.
4. Buka **Authentication → Users** dan daftar **Policies** tiap tabel: kolom **RLS enabled** harus
   aktif di keempat tabel. Kebijakan dibuat oleh skrip, tidak perlu ditulis manual.

Nama tabel dan kolom pada skrip inilah yang dipakai kode klien
(`app/src/main/java/id/ac/kuliah/laporaja/data/remote/SupabaseTables.kt`). Kalau suatu saat
menambah kolom, perbarui kedua tempat itu sekaligus.

## 4. Bucket Storage

Skrip sudah membuat tiga bucket publik, tetapi cek sekali lagi lewat **Storage → Buckets** supaya
tidak ada yang tertinggal:

| Bucket | Isi | Akses |
| --- | --- | --- |
| `report-photos` | foto bukti laporan | publik baca, tulis hanya akun login |
| `activity-posters` | poster kegiatan | publik baca, tulis hanya akun login |
| `avatars` | foto profil | publik baca, tulis hanya akun login |

Path berkas dibentuk oleh `core/image/ImageUploader.kt`: `reports/<uuid>/<uuid>.jpg`,
`activities/<uuid>/<uuid>.jpg`, dan `users/<user-id>/avatar-<timestamp>.jpg` (tiga-tiganya path
relatif bucket, jadi memindahkan bucket tidak butuh migrasi data).
Ukuran gambar maksimal 12 MB sebelum dikompres ke sisi terpanjang 1280 px (JPEG kualitas 80).

## 5. Isi kredensial di `local.properties`

Salin `local.properties.example` menjadi `local.properties` (berkas ini ada di `.gitignore`), lalu isi:

```properties
sdk.dir=C:/Users/NAMA_KAMU/AppData/Local/Android/Sdk
SUPABASE_URL=https://PROYEK_ANDA.supabase.co
SUPABASE_ANON_KEY=tempel-publishable-key-di-sini
```

- **Project URL** ada di **Project Settings → API Keys**, bersama **Publishable key** (nama baru
  dari `anon key`; formatnya `sb_publishable_...`). Isilah `SUPABASE_ANON_KEY` dengan publishable
  key itu — kode klien tidak berubah karena keduanya dipakai pada header `apikey` yang sama.
  **Secret key** dan `service_role key` tidak dipakai aplikasi ini.
- Nilai dibaca `app/build.gradle.kts` masuk ke `BuildConfig`, jadi **Sync Project with Gradle Files**
  wajib dilakukan setelah mengubahnya.
- `service_role key` tidak pernah dipakai di klien dan tidak boleh muncul di kode, komentar,
  maupun contoh. Ia hanya boleh dipakai di server, dan proyek ini tidak punya server sendiri.

## 6. Buat akun uji

Buat lewat aplikasi (register di S1), bukan lewat Dashboard, supaya trigger `handle_new_user` ikut
teruji dan baris `profiles` terbentuk otomatis.

| Sebutan | Email | Peran | Kegunaan demo |
| --- | --- | --- | --- |
| Warga utama | `warga.demo@laporaja.test` | warga | buat laporan, edit laporan, kirim tanggapan, unggah foto profil |
| Warga kedua | `warga.dua@laporaja.test` | warga | membuktikan warga lain tidak membaca laporan akun pertama (U10) |
| Petugas | `petugas.rw@laporaja.test` | petugas | ubah status, tanggapan resmi, buat kegiatan |

Password uji untuk ketiganya: `LaporAja2026!` (khusus akun demo).

Setelah ketiga akun dibuat, naikkan peran petugas dengan SQL berikut di **SQL Editor**
(peran dikunci oleh trigger sehingga tidak bisa diubah dari aplikasi):

```sql
update public.profiles set role = 'petugas' where email = 'petugas.rw@laporaja.test';
select email, role from public.profiles order by created_at;  -- wajib menampilkan 'petugas'
```

`guard_profile_role` hanya mengunci peran bila request membawa JWT (`auth.uid()` terisi), sehingga
UPDATE dari SQL Editor tetap bekerja. Kalau SELECT di atas masih menunjukkan `warga`, berarti
versi `setup.sql` lama yang dijalankan — perbarui fungsi itu lebih dulu, jangan menambah
`role` manual lewat Table Editor.

Data contoh mengikuti aturan privasi: nama samaran dan lokasi fiktif (mis. "Gang Melati RT 003",
"Lapangan Seruni"), minimal 5 laporan menutupi 4 status dan 4 kegiatan dengan kategori berbeda
(2 di antaranya jadwal mendatang). Jangan memakai foto wajah orang.

## 7. Daftar Periksa (Lampiran C)

- [ ] Proyek Supabase aktif, URL dan anon key sudah di `local.properties` masing-masing mesin.
- [ ] `local.properties` tidak terlacak Git (sudah ada di `.gitignore`).
- [ ] Email provider aktif, Confirm Email mati, honeypot dan captcha mati.
- [ ] 4 tabel terlihat di Table Editor dan RLS aktif di semuanya.
- [ ] 3 bucket publik ada dan bisa dibaca dari browser lewat URL publik.
- [ ] 3 akun uji dibuat lewat aplikasi; peran petugas dinaikkan lewat SQL.
- [ ] `./gradlew assembleDebug` hijau dan `./gradlew testDebugUnitTest` hijau (38 pengujian).
- [ ] Verifikasi di dua perangkat berbeda sebelum lanjut ke fitur berikutnya.
- [ ] Ikon dan nama launcher "LaporAja" sudah terpasang.

## 8. Bila muncul masalah

| Gejala | Penyebab umum | Yang dilakukan |
| --- | --- | --- |
| `Kredensial Supabase belum diisi, buka local.properties` | `local.properties` kosong atau belum di-Gradle Sync | isi nilai lalu **Sync Project with Gradle Files** |
| `Sesi berakhir, silakan masuk kembali` muncul terus | token kedaluwarsa atau proyek diganti | login ulang; pastikan URL dan anon key satu proyek yang sama |
| `Akun kamu belum punya hak untuk aksi ini` (403) | kebijakan RLS kurang | tambahkan policy yang sesuai di `supabase/setup.sql`; **jangan** menonaktifkan RLS |
| `Email atau password salah` padahal baru daftar | Confirm Email masih aktif | matikan Confirm Email lalu daftarkan ulang akunnya |
| `Tidak ada koneksi internet, coba lagi` | perangkat tanpa jaringan atau URL salah | cek `SUPABASE_URL` (harus `https://...supabase.co`) |
| `column ... does not exist` (42703) | skrip belum dijalankan atau beda proyek | jalankan ulang `supabase/setup.sql` di proyek yang benar |
| Galat `policy ... already exists` | skrip lama (non-idempoten) pernah dijalankan | skrip sekarang sudah memakai `drop policy if exists`; jalankan ulang |
