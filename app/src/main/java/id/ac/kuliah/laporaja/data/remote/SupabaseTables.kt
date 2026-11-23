package id.ac.kuliah.laporaja.data.remote

/**
 * Satu-satunya tempat penulisan nama tabel dan kolom Supabase (Subbab 12.5).
 * Acuan: skema Lampiran A.
 */
internal object SupabaseTables {

    const val PROFILES = "profiles"
    const val REPORTS = "reports"
    const val REPORT_RESPONSES = "report_responses"
    const val ACTIVITIES = "activities"

    object Column {
        const val ID = "id"
        const val EMAIL = "email"
        const val FULL_NAME = "full_name"
        const val PHONE = "phone"
        const val RT_RW = "rt_rw"
        const val ROLE = "role"
        const val AVATAR_PATH = "avatar_path"
        const val USER_ID = "user_id"
        const val REPORT_ID = "report_id"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val CATEGORY = "category"
        const val LOCATION = "location"
        const val PHOTO_PATH = "photo_path"
        const val POSTER_PATH = "poster_path"
        const val STATUS = "status"
        const val STATUS_NOTE = "status_note"
        const val RESOLVED_AT = "resolved_at"
        const val MESSAGE = "message"
        const val IS_OFFICIAL = "is_official"
        const val SCHEDULE_AT = "schedule_at"
        const val QUOTA = "quota"
        const val CREATED_BY = "created_by"
        const val IS_PUBLISHED = "is_published"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
    }

    private fun columns(vararg names: String): String = names.joinToString(", ")

    private val profileFields = columns(
        Column.ID,
        Column.EMAIL,
        Column.FULL_NAME,
        Column.PHONE,
        Column.RT_RW,
        Column.ROLE,
        Column.AVATAR_PATH,
        Column.CREATED_AT,
    )

    private val reportFields = columns(
        Column.ID,
        Column.USER_ID,
        Column.TITLE,
        Column.DESCRIPTION,
        Column.CATEGORY,
        Column.LOCATION,
        Column.PHOTO_PATH,
        Column.STATUS,
        Column.STATUS_NOTE,
        Column.RESOLVED_AT,
        Column.CREATED_AT,
        Column.UPDATED_AT,
    )

    private val activityFields = columns(
        Column.ID,
        Column.TITLE,
        Column.DESCRIPTION,
        Column.CATEGORY,
        Column.LOCATION,
        Column.SCHEDULE_AT,
        Column.QUOTA,
        Column.POSTER_PATH,
        Column.IS_PUBLISHED,
        Column.CREATED_AT,
        Column.CREATED_BY,
    )

    val profileProjection: String = profileFields

    /** Daftar laporan sekalian jumlah tanggapan (Subbab 10.1). */
    val reportProjection: String = "$reportFields, $REPORT_RESPONSES(count)"

    /** Tanggapan memakai nama, peran, dan avatar pengirim; nomor HP tidak boleh ikut (Subbab 13). */
    val responseProjection: String = columns(
        Column.ID,
        Column.REPORT_ID,
        Column.USER_ID,
        Column.MESSAGE,
        Column.IS_OFFICIAL,
        Column.CREATED_AT,
    ) + ", $PROFILES(" + columns(Column.FULL_NAME, Column.ROLE, Column.AVATAR_PATH) + ")"

    val activityProjection: String = "$activityFields, $PROFILES(" + columns(Column.FULL_NAME) + ")"

    /** Aggregate embed untuk jumlah laporan milik sendiri pada S13 (Subbab 9.2). */
    val ownReportCountProjection: String = "$REPORTS(count)"
}

internal object SupabaseBuckets {
    const val REPORT_PHOTOS = "report-photos"
    const val ACTIVITY_POSTERS = "activity-posters"
    const val AVATARS = "avatars"
}
