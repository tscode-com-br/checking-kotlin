package br.com.tscode.checking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import br.com.tscode.checking.presentation.navigation.CheckingNavHost
import br.com.tscode.checking.presentation.theme.CheckingTheme
import dagger.hilt.android.AndroidEntryPoint

// Single activity — hosts the Compose NavHost (§15).
// Edge-to-edge display matches the web PWA's viewport-fit=cover (§1.1).
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheckingTheme {
                CheckingNavHost()
            }
        }
    }
}
