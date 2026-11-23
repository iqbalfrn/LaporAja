-- LaporAja - skrip setup database Supabase (Lampiran A PRD, Subbab 17)
--
-- Cara pakai: Supabase Dashboard -> SQL Editor -> New query -> tempel seluruh berkas -> Run.
-- Skrip ini idempoten (boleh dijalankan ulang): setiap policy di-drop lebih dulu,
-- sesuai catatan implementasi di akhir Lampiran A.
--
-- Nama tabel dan kolom di berkas ini adalah acuan kode aplikasi
-- (app/src/main/java/id/ac/kuliah/laporaja/data/remote/SupabaseTables.kt).
-- Service_role key tidak dipakai di mana pun; cukup jalankan skrip dari Dashboard.

-- A1. Fungsi bantu
create or replace function public.touch_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end; $$;

-- A2. Tabel profiles + trigger pembuatan otomatis saat register
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text not null,
  full_name text not null check (char_length(full_name) between 3 and 60),
  phone text,
  rt_rw text,
  role text not null default 'warga' check (role in ('warga','petugas')),
  avatar_path text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  insert into public.profiles (id, email, full_name, phone, rt_rw)
  values (
    new.id,
    new.email,
    coalesce(nullif(new.raw_user_meta_data ->> 'full_name', ''), 'Warga Baru'),
    nullif(new.raw_user_meta_data ->> 'phone', ''),
    nullif(new.raw_user_meta_data ->> 'rt_rw', '')
  )
  on conflict (id) do nothing;
  return new;
end; $$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- A2b. Fungsi pemeriksa peran (dibuat setelah tabel profiles agar validasi body fungsi lolos)
create or replace function public.is_petugas()
returns boolean language sql stable security definer set search_path = public as $$
  select coalesce((select role = 'petugas' from public.profiles where id = auth.uid()), false);
$$;

-- A3. Tabel reports
create table if not exists public.reports (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  title text not null check (char_length(title) between 5 and 80),
  description text not null check (char_length(description) between 20 and 1000),
  category text not null check (category in
    ('sampah','saluran','jalan','penerangan','fasum','lainnya')),
  location text not null check (char_length(location) between 5 and 120),
  photo_path text,
  status text not null default 'baru' check (status in
    ('baru','diproses','selesai','ditolak')),
  status_note text check (status_note is null or char_length(status_note) <= 300),
  resolved_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index if not exists reports_user_created_idx on public.reports (user_id, created_at desc);
create index if not exists reports_status_idx on public.reports (status);

-- A4. Tabel report_responses dan pengisian is_official otomatis
create table if not exists public.report_responses (
  id uuid primary key default gen_random_uuid(),
  report_id uuid not null references public.reports(id) on delete cascade,
  user_id uuid not null references public.profiles(id) on delete cascade,
  message text not null check (char_length(message) between 3 and 500),
  is_official boolean not null default false,
  created_at timestamptz not null default now()
);
create index if not exists responses_report_created_idx
  on public.report_responses (report_id, created_at desc);

create or replace function public.stamp_official_response()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  select role = 'petugas' into new.is_official from public.profiles where id = new.user_id;
  return new;
end; $$;

drop trigger if exists stamp_official on public.report_responses;
create trigger stamp_official
  before insert on public.report_responses
  for each row execute function public.stamp_official_response();

-- A5. Tabel activities
create table if not exists public.activities (
  id uuid primary key default gen_random_uuid(),
  title text not null check (char_length(title) between 5 and 80),
  description text not null check (char_length(description) between 30 and 2000),
  category text not null check (category in
    ('kerja_bakti','edukasi','bank_sampah','kunjungan')),
  location text not null check (char_length(location) between 5 and 120),
  schedule_at timestamptz not null,
  quota integer check (quota is null or quota between 1 and 999),
  poster_path text,
  created_by uuid references public.profiles(id) on delete set null,
  is_published boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index if not exists activities_schedule_idx on public.activities (schedule_at);

-- A6. Trigger updated_at untuk tiga tabel data
drop trigger if exists touch_profiles on public.profiles;
create trigger touch_profiles before update on public.profiles
  for each row execute function public.touch_updated_at();
drop trigger if exists touch_reports on public.reports;
create trigger touch_reports before update on public.reports
  for each row execute function public.touch_updated_at();
drop trigger if exists touch_activities on public.activities;
create trigger touch_activities before update on public.activities
  for each row execute function public.touch_updated_at();

-- A7. Kunci peran agar tidak bisa diubah dari aplikasi
-- auth.uid() bernilai null saat skrip dijalankan tanpa JWT (Dashboard/SQL Editor),
-- jadi promosi peran oleh admin tetap mungkin; klien selalu membawa JWT.
create or replace function public.guard_profile_role()
returns trigger language plpgsql as $$
begin
  if auth.uid() is not null and new.role is distinct from old.role then
    new.role = old.role;
  end if;
  return new;
end; $$;

drop trigger if exists guard_role on public.profiles;
create trigger guard_role before update on public.profiles
  for each row execute function public.guard_profile_role();

-- A8. Row Level Security
alter table public.profiles enable row level security;
alter table public.reports enable row level security;
alter table public.report_responses enable row level security;
alter table public.activities enable row level security;

drop policy if exists profiles_read on public.profiles;
create policy profiles_read on public.profiles
  for select to authenticated using (true);

drop policy if exists profiles_update_own on public.profiles;
create policy profiles_update_own on public.profiles
  for update to authenticated using (id = auth.uid()) with check (id = auth.uid());

drop policy if exists reports_read on public.reports;
create policy reports_read on public.reports
  for select to authenticated
  using (user_id = auth.uid() or public.is_petugas());

drop policy if exists reports_insert_own on public.reports;
create policy reports_insert_own on public.reports
  for insert to authenticated with check (user_id = auth.uid());

drop policy if exists reports_edit_own_content on public.reports;
create policy reports_edit_own_content on public.reports
  for update to authenticated
  using (user_id = auth.uid() and status in ('baru','ditolak'))
  with check (user_id = auth.uid());

drop policy if exists reports_petugas_status on public.reports;
create policy reports_petugas_status on public.reports
  for update to authenticated using (public.is_petugas());

drop policy if exists responses_read on public.report_responses;
create policy responses_read on public.report_responses
  for select to authenticated using (exists (
    select 1 from public.reports r
    where r.id = report_id and (r.user_id = auth.uid() or public.is_petugas())));

drop policy if exists responses_insert on public.report_responses;
create policy responses_insert on public.report_responses
  for insert to authenticated with check (
    user_id = auth.uid() and exists (
      select 1 from public.reports r
      where r.id = report_id and (r.user_id = auth.uid() or public.is_petugas())));

drop policy if exists activities_read on public.activities;
create policy activities_read on public.activities
  for select to authenticated using (is_published = true);

drop policy if exists activities_insert_petugas on public.activities;
create policy activities_insert_petugas on public.activities
  for insert to authenticated with check (public.is_petugas());

-- A9. Bucket Storage (jalankan juga lewat Dashboard bila perlu)
insert into storage.buckets (id, name, public) values
  ('report-photos','report-photos', true),
  ('activity-posters','activity-posters', true),
  ('avatars','avatars', true)
on conflict (id) do nothing;

drop policy if exists storage_read on storage.objects;
create policy storage_read on storage.objects
  for select using (bucket_id in ('report-photos','activity-posters','avatars'));

drop policy if exists storage_write_auth on storage.objects;
create policy storage_write_auth on storage.objects
  for insert to authenticated
  with check (bucket_id in ('report-photos','activity-posters','avatars'));

drop policy if exists storage_update_own on storage.objects;
create policy storage_update_own on storage.objects
  for update to authenticated using (owner = auth.uid()) with check (owner = auth.uid());

-- Selesai. Catatan Lampiran A: kebijakan memakai public.is_petugas() yang security definer
-- supaya tidak terjadi rekursi RLS saat membaca tabel profiles. RLS tidak boleh dinonaktifkan
-- sebagai solusi galat akses; perbaiki kebijakan yang kurang, bukan matikan RLS.
