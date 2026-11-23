package id.ac.kuliah.laporaja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.ac.kuliah.laporaja.ui.navigation.AppNavGraph
import id.ac.kuliah.laporaja.ui.theme.LaporAjaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as LaporAjaApp).container
        setContent {
            LaporAjaTheme {
                AppNavGraph(container = container)
            }
        }
    }
}
