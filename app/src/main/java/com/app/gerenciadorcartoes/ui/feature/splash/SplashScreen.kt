package com.app.gerenciadorcartoes.ui.feature.splash

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.R
import com.app.gerenciadorcartoes.ui.feature.splash.state.SplashUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.viewmodel.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashBg = Color(0xFF001432)

// ── Nível 1: Screen ───────────────────────────────────────────────────────────
@Composable
fun SplashScreen(
    navigateToLista: () -> Unit,
    navigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                SplashUiEvent.NavigateToLista -> navigateToLista()
                SplashUiEvent.NavigateToLogin -> navigateToLogin()
            }
        }
    }

    SplashContent(uiState = uiState)
}

// ── Nível 2: Content ─────────────────────────────────────────────────────────
@Composable
fun SplashContent(
    uiState: SplashUiState = SplashUiState(),
) {
    // ── Fase 1: ícone sobe com spring (mesmo tema da animação AVD do system splash)
    val iconOffsetY = remember { Animatable(80f) }

    // ── Fase 2: título desliza para cima + aparece
    val titleOffsetY = remember { Animatable(30f) }
    val titleAlpha   = remember { Animatable(0f) }

    // ── Fase 3: tagline apenas aparece
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fase 1 — ícone: sobe com spring natural (inicia imediatamente, sem tela vazia)
        launch {
            iconOffsetY.animateTo(
                targetValue   = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness    = Spring.StiffnessMedium,
                ),
            )
        }

        // Fase 2 — título (150ms depois do ícone)
        delay(150)
        launch {
            titleOffsetY.animateTo(0f, tween(300, easing = FastOutSlowInEasing))
        }
        launch {
            titleAlpha.animateTo(1f, tween(280))
        }

        // Fase 3 — tagline (mais 200ms)
        delay(200)
        taglineAlpha.animateTo(1f, tween(250))
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(SplashBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Ícone sobe com spring
            Box(
                modifier         = Modifier
                    .graphicsLayer { translationY = iconOffsetY.value * density }
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

            // Título desliza para cima + aparece
            Text(
                text       = stringResource(R.string.app_name),
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                modifier   = Modifier
                    .graphicsLayer { translationY = titleOffsetY.value * density; alpha = titleAlpha.value },
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline aparece por último
            Text(
                text     = stringResource(R.string.splash_tagline),
                style    = MaterialTheme.typography.bodyLarge,
                color    = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.alpha(taglineAlpha.value),
            )
        }

        // Copyright — fixo, sem animação
        Text(
            text     = stringResource(R.string.splash_copyright),
            style    = MaterialTheme.typography.labelSmall,
            color    = Color.White.copy(alpha = 0.30f),
            modifier = Modifier.align(Alignment.BottomCenter),
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
