package id.ac.kuliah.laporaja

import android.app.Application
import id.ac.kuliah.laporaja.di.AppContainer

class LaporAjaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
