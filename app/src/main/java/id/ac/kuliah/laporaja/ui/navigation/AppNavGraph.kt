package id.ac.kuliah.laporaja.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import id.ac.kuliah.laporaja.di.AppContainer
import id.ac.kuliah.laporaja.di.LocalAppContainer
import id.ac.kuliah.laporaja.ui.account.AboutScreen
import id.ac.kuliah.laporaja.ui.account.AccountScreen
import id.ac.kuliah.laporaja.ui.account.EditProfileScreen
import id.ac.kuliah.laporaja.ui.activity.ActivityDetailScreen
import id.ac.kuliah.laporaja.ui.activity.ActivityFormScreen
import id.ac.kuliah.laporaja.ui.activity.ActivityListScreen
import id.ac.kuliah.laporaja.ui.auth.LoginScreen
import id.ac.kuliah.laporaja.ui.auth.RegisterScreen
import id.ac.kuliah.laporaja.ui.auth.SplashScreen
import id.ac.kuliah.laporaja.ui.gate.PetugasGateScreen
import id.ac.kuliah.laporaja.ui.home.HomeScreen
import id.ac.kuliah.laporaja.ui.report.ReportDetailScreen
import id.ac.kuliah.laporaja.ui.report.ReportFormScreen
import id.ac.kuliah.laporaja.ui.report.ReportListScreen
import id.ac.kuliah.laporaja.ui.response.ResponseFormScreen

/** Kunci SavedStateHandle untuk pesan singkat yang dibawa balik ke layar sebelumnya. */
private const val KEY_NOTICE = "notice"

/**
 * Semua rute aplikasi (Subbab 5). Perlindungan rute dilakukan di sini lewat satu sumber kebenaran
 * yaitu [AppContainer.sessionHolder]; tombol yang tidak boleh tampil cukup disembunyikan, sementara
 * otorisasi sebenarnya tetap dijaga RLS di server (Subbab 13).
 */
@Composable
fun AppNavGraph(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    val session by container.sessionHolder.user.collectAsStateWithLifecycle()
    val unauthorizedTick by container.sessionHolder.unauthorizedTick.collectAsStateWithLifecycle()
    val isSignedIn = session != null

    // Sesi kedaluwarsa (401 dari panggilan mana pun): tumpukan dibersihkan lalu ke S1 (Subbab 9.1).
    LaunchedEffect(unauthorizedTick) {
        if (unauthorizedTick > 0) {
            navController.openLogin(container.sessionHolder.takeExpiredMessage())
        }
    }

    CompositionLocalProvider(LocalAppContainer provides container) {
        NavHost(navController = navController, startDestination = Routes.SPLASH) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onSignedIn = { navController.openHome() },
                    onSignedOut = { navController.openLogin(null) },
                )
            }

            composable(Routes.LOGIN) { entry ->
                LoginScreen(
                    notice = entry.noticeState(),
                    onNoticeShown = { entry.savedStateHandle.clearNotice() },
                    onOpenRegister = { navController.navigate(Routes.REGISTER) },
                    onSuccess = { navController.openHome() },
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.openHome() },
                )
            }

            composable(Routes.HOME) {
                if (!isSignedIn) {
                    LoginRedirect(navController)
                } else {
                    HomeScreen(
                        onSelectRoute = navController::selectTab,
                        onOpenReport = { navController.navigate(Routes.reportDetail(it)) },
                        onOpenActivity = { navController.navigate(Routes.activityDetail(it)) },
                        onOpenNewReport = { navController.navigate(Routes.REPORT_NEW) },
                    )
                }
            }

            composable(Routes.REPORTS) { entry ->
                if (!isSignedIn) {
                    LoginRedirect(navController)
                } else {
                    ReportListScreen(
                        notice = entry.noticeState(),
                        onNoticeShown = { entry.savedStateHandle.clearNotice() },
                        onSelectRoute = navController::selectTab,
                        onOpenDetail = { navController.navigate(Routes.reportDetail(it)) },
                        onOpenNew = { navController.navigate(Routes.REPORT_NEW) },
                    )
                }
            }

            composable(Routes.REPORT_NEW) {
                if (!isSignedIn) {
                    LoginRedirect(navController)
                } else {
                    ReportFormScreen(
                        reportId = null,
                        onBack = { navController.popBackStack() },
                        onSaved = navController::popWithNotice,
                    )
                }
            }

            reportIdRoute(Routes.REPORT_DETAIL) { reportId, entry ->
                if (!isSignedIn) {
                    LoginRedirect(navController)
                } else {
                    ReportDetailScreen(
                        reportId = reportId,
                        notice = entry.noticeState(),
                        onNoticeShown = { entry.savedStateHandle.clearNotice() },
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(Routes.reportEdit(reportId)) },
                        onRespond = { navController.navigate(Routes.reportRespond(reportId)) },
                    )
                }
            }

            reportIdRoute(Routes.REPORT_EDIT) { reportId, _ ->
                if (!isSignedIn) {
                    LoginRedirect(navController)
                } else {
                    ReportFormScreen(
                        reportId = reportId,
                        onBack = { navController.popBackStack() },
                        onSaved = navController::popWithNotice,
                    )
                }
            }

            reportIdRoute(Routes.REPORT_RESPOND) { reportId, _ ->
                if (!isSignedIn) {
                    LoginRedirect(navController)
                } else {
                    ResponseFormScreen(
                        reportId = reportId,
                        onBack = { navController.popBackStack() },
                        onSent = navController::popWithNotice,
                    )
                }
            }

            composable(Routes.ACTIVITIES) { entry ->
                if (!isSignedIn) {
                    LoginRedirect(navController)
                } else {
                    ActivityListScreen(
                        notice = entry.noticeState(),
                        onNoticeShown = { entry.savedStateHandle.clearNotice() },
                        onSelectRoute = navController::selectTab,
                        onOpenDetail = { navController.navigate(Routes.activityDetail(it)) },
                        onOpenNew = { navController.navigate(Routes.ACTIVITY_NEW) },
                    )
                }
            }

            composable(Routes.ACTIVITY_NEW) {
                when {
                    !isSignedIn -> LoginRedirect(navController)

                    // Rute khusus petugas yang dimasuki akun warga (Subbab 10.3).
                    session?.isPetugas != true -> PetugasGateScreen(onBack = { navController.popBackStack() })

                    else -> ActivityFormScreen(
                        onBack = { navController.popBackStack() },
                        onSaved = navController::popWithNotice,
                    )
                }
            }

            activityIdRoute(Routes.ACTIVITY_DETAIL) { activityId ->
                if (!isSignedIn) {
                    LoginRedirect(navController)
                } else {
                    ActivityDetailScreen(
                        activityId = activityId,
                        onBack = { navController.popBackStack() },
                    )
                }
            }

            composable(Routes.ACCOUNT) { entry ->
                if (!isSignedIn) {
                    LoginRedirect(navController)
                } else {
                    AccountScreen(
                        notice = entry.noticeState(),
                        onNoticeShown = { entry.savedStateHandle.clearNotice() },
                        onSelectRoute = navController::selectTab,
                        onEditProfile = { navController.navigate(Routes.ACCOUNT_EDIT) },
                        onAbout = { navController.navigate(Routes.ABOUT) },
                        onLoggedOut = { navController.openLogin(null) },
                    )
                }
            }

            composable(Routes.ACCOUNT_EDIT) {
                if (!isSignedIn) {
                    LoginRedirect(navController)
                } else {
                    EditProfileScreen(
                        onBack = { navController.popBackStack() },
                        onSaved = navController::popWithNotice,
                    )
                }
            }

            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Pindah antar tab tanpa menumpuk entri (Subbab 5.1). */
private fun NavController.selectTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Masuk ke S3 dan membuang seluruh tumpukan autentikasi. */
private fun NavController.openHome() {
    navigate(Routes.HOME) {
        popUpTo(Routes.SPLASH) { inclusive = true }
        popUpTo(Routes.LOGIN) { inclusive = true }
        popUpTo(Routes.REGISTER) { inclusive = true }
        launchSingleTop = true
    }
}

/** Keluar sesi atau sesi kedaluwarsa: back tidak boleh mengembalikan layar yang sudah kosong (Subbab 9.1). */
private fun NavController.openLogin(notice: String?) {
    navigate(Routes.LOGIN) {
        popUpTo(Routes.SPLASH) { inclusive = true }
        popUpTo(Routes.HOME) { inclusive = true }
        launchSingleTop = true
    }
    if (notice != null) getBackStackEntry(Routes.LOGIN).savedStateHandle.setNotice(notice)
}

/** Pesan sukses dititipkan ke entri di bawahnya sebelum kembali (Subbab 10.1). */
private fun NavController.popWithNotice(message: String) {
    previousBackStackEntry?.savedStateHandle?.setNotice(message)
    popBackStack()
}

/** Rute dengan satu argumen laporan. */
private fun NavGraphBuilder.reportIdRoute(
    route: String,
    content: @Composable (String, NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        arguments = listOf(navArgument(Routes.ARG_REPORT_ID) { type = NavType.StringType }),
    ) { entry ->
        content(entry.arguments?.getString(Routes.ARG_REPORT_ID).orEmpty(), entry)
    }
}

/** Rute dengan satu argumen kegiatan. */
private fun NavGraphBuilder.activityIdRoute(
    route: String,
    content: @Composable (String) -> Unit,
) {
    composable(
        route = route,
        arguments = listOf(navArgument(Routes.ARG_ACTIVITY_ID) { type = NavType.StringType }),
    ) { entry ->
        content(entry.arguments?.getString(Routes.ARG_ACTIVITY_ID).orEmpty())
    }
}

@Composable
private fun LoginRedirect(navController: NavController) {
    LaunchedEffect(Unit) { navController.openLogin(null) }
}

/** Notice dibaca sebagai state supaya Snackbar muncul saat entri tujuan disusun ulang. */
@Composable
private fun NavBackStackEntry.noticeState(): String? {
    val flow = remember(this) { savedStateHandle.getStateFlow(KEY_NOTICE, null as String?) }
    val value by flow.collectAsStateWithLifecycle()
    return value
}

private fun SavedStateHandle.setNotice(message: String) {
    set(KEY_NOTICE, message)
}

private fun SavedStateHandle.clearNotice() {
    set(KEY_NOTICE, null)
}
