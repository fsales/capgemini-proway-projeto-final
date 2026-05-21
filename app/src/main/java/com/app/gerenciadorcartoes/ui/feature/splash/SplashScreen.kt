package com.app.gerenciadorcartoes.ui.feature.splash

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.R
import com.app.gerenciadorcartoes.ui.feature.splash.state.SplashUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.viewmodel.SplashViewModel

// Cor do fundo da splash — navy profundo, igual à splash do sistema (transição sem corte)
private val SplashBg = Color(0xFF001432)

// ── Nível 1: Screen ───────────────────────────────────────────────────────────
@Composable
fun SplashScreen(
    navigateToLogin: () -> Unit,
    viewModel       : SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                SplashUiEvent.NavigateToLogin -> navigateToLogin()
            }
        }
    }

    SplashContent(uiState = uiState)
}

// ── Nível 2: Content ──────────────────────────────────────────────────────────
@Composable
fun SplashContent(
    uiState: SplashUiState = SplashUiState(),
) {
    // Animação de entrada: fade + scale suave
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.88f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 700))
        scale.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 500))
    }

    Box(
        modifier          = Modifier
            .fillMaxSize()
            .background(SplashBg),
        contentAlignment  = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier
                .alpha(alpha.value)
                .scale(scale.value),
        ) {
            // Ícone dentro do círculo azul elétrico
            Box(
                modifier         = Modifier
                    .size(148.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter            = painterResource(R.drawable.ic_splash_logo_static),
                    contentDescription = null,
                    modifier           = Modifier.size(104.dp),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Nome do app — destaque máximo
            Text(
                text       = "G3 Bank",
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text  = "Seus cartões, no controle",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.65f),
            )
        }

        // Versão discreta no rodapé
        Text(
            text     = "G3 Bank © 2026",
            style    = MaterialTheme.typography.labelSmall,
            color    = Color.White.copy(alpha = 0.30f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(alpha.value),
        )
    }
}

// ── Nível 3: Previews ─────────────────────────────────────────────────────────
@Preview(showBackground = true, name = "Splash – Light")
@Preview(showBackground = true, name = "Splash – Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SplashPreview() {
    GerenciadorCartoesTheme {
        SplashContent()
    }
}

