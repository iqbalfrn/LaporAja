# Aset & skrip data demo

Berkas di folder ini hanya untuk mengisi proyek Supabase **pengembangan** dengan data contoh.
Nama, lokasi, dan gambar semuanya fiktif — jangan dipakai di proyek produksi dan jangan
menambah akun atau gambar nyata ke dalamnya.

| Berkas | Fungsi |
| --- | --- |
| `assets/*.jpg` | gambar hasil komposisi sisi terpanjang 1280 px, JPEG kualitas 80 (sama seperti `ImageCompressor.kt`) |
| `seed-images.mjs` | unggah avatar, 2 foto laporan, dan 1 poster; memasang `avatar_path` + `photo_path`, membuat kegiatan baru yang berposter |
| `seed-images2.mjs` | memasang 2 foto laporan sisanya |
| `check-demo.mjs` | mencetak inventaris laporan (penanda FOTO), kegiatan (penanda POSTER), dan profil (penanda AVATAR) |

Ketiganya membaca `SUPABASE_URL` dan `SUPABASE_ANON_KEY` dari `local.properties` di root project,
jadi tidak ada kredensial yang tersimpan di sini. Jalankan dari root project:

```bash
node supabase/demo/check-demo.mjs
node supabase/demo/seed-images.mjs
```

`seed-images.mjs` dan `seed-images2.mjs` **tidak idempoten**: menjalankannya dua kali
menambah kegiatan baru lagi dan menimpa path gambar yang lama. `check-demo.mjs` hanya membaca
dan aman diulang.
