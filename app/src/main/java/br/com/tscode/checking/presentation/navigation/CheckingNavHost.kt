package br.com.tscode.checking.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.tscode.checking.i18n.rememberT
import br.com.tscode.checking.presentation.check.CheckScreen
import br.com.tscode.checking.presentation.manual.ManualScreen
import br.com.tscode.checking.presentation.manual.ManualViewModel
import br.com.tscode.checking.presentation.splash.AppSplashScreen

// Route identifiers (§15).
object Routes {
    const val SPLASH = "splash"
    const val CHECK = "check"
    const val MANUAL = "manual"
}

// Single-activity NavHost (§15).
// Transport and Accident are full-screen modal surfaces managed as Compose state within CheckScreen,
// not as separate nav destinations, to preserve the underlying check state exactly like the web.
@Composable
fun CheckingNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            AppSplashScreen(
                onFinished = {
                    navController.navigate(Routes.CHECK) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.CHECK) {
            CheckScreen(onNavigateToManual = { navController.navigate(Routes.MANUAL) })
        }
        composable(Routes.MANUAL) {
            val manualVm: ManualViewModel = hiltViewModel()
            val t = rememberT(manualVm.languageFlow)
            ManualScreen(
                onBack = { navController.popBackStack() },
                t = t,
            )
        }
    }
}
