package id.ac.kuliah.laporaja.ui.navigation

/**
 * Nama rute mengikat karena dipakai juga di dokumen pengujian (Subbab 5).
 */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val REPORTS = "reports"
    const val REPORT_NEW = "reports/new"
    const val REPORT_DETAIL = "reports/{reportId}"
    const val REPORT_EDIT = "reports/{reportId}/edit"
    const val REPORT_RESPOND = "reports/{reportId}/respond"
    const val ACTIVITIES = "activities"
    const val ACTIVITY_NEW = "activities/new"
    const val ACTIVITY_DETAIL = "activities/{activityId}"
    const val ACCOUNT = "account"
    const val ACCOUNT_EDIT = "account/edit"
    const val ABOUT = "about"

    const val ARG_REPORT_ID = "reportId"
    const val ARG_ACTIVITY_ID = "activityId"

    fun reportDetail(reportId: String): String = "reports/$reportId"

    fun reportEdit(reportId: String): String = "reports/$reportId/edit"

    fun reportRespond(reportId: String): String = "reports/$reportId/respond"

    fun activityDetail(activityId: String): String = "activities/$activityId"
}
