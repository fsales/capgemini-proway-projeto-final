package com.app.gerenciadorcartoes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.app.gerenciadorcartoes.ui.navigation.AppNavHost
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GerenciadorCartoesTheme {
                AppNavHost()
            }
        }
    }
}
