package com.app.gerenciadorcartoes

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.app.gerenciadorcartoes.ui.navigation.AppNavHost
import com.app.gerenciadorcartoes.ui.navigation.DetalheRoute
import com.app.gerenciadorcartoes.ui.navigation.ListaRoute
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // A lib já usa OnPreDrawListener internamente: quando a condição retorna false,
        // o splash é removido no mesmo vsync em que o Compose desenha o 1º frame →
        // zero gap, zero tela em branco. Só precisamos garantir que a animação AVD
        // (450ms) termine antes de liberar.
        val launchTime = SystemClock.elapsedRealtime()
        splashScreen.setKeepOnScreenCondition {
            (SystemClock.elapsedRealtime() - launchTime) < 450L
        }

        setContent {

            GerenciadorCartoesTheme {
                AppNavHost()
            }
        }
    }
}
