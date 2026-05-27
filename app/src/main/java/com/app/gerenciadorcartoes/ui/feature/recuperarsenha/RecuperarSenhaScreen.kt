package com.app.gerenciadorcartoes.ui.feature.recuperarsenha

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.R
import com.app.gerenciadorcartoes.ui.components.AppLoading
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.components.AppTopAppBar
import com.app.gerenciadorcartoes.ui.feature.recuperarsenha.state.RecuperarSenhaUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalIconSize
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.ui.components.AppFormField
import com.app.gerenciadorcartoes.viewmodel.RecuperarSenhaViewModel

// ── Tier 1: Screen ────────────────────────────────────────────────────────────

@Composable
fun RecuperarSenhaScreen(
    navigateBack : () -> Unit,
    viewModel    : RecuperarSenhaViewModel = hiltViewModel(),
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                RecuperarSenhaUiEvent.NavigateBack        -> navigateBack()
                is RecuperarSenhaUiEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(event.mensagem)
                    navigateBack()
                }
                is RecuperarSenhaUiEvent.MostrarErro     -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    RecuperarSenhaContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
    )
}

// ── Tier 2: Content ───────────────────────────────────────────────────────────

@Composable
fun RecuperarSenhaContent(
    uiState           : RecuperarSenhaUiState = RecuperarSenhaUiState(),
    snackbarHostState : SnackbarHostState     = remember { SnackbarHostState() },
    onEvent           : (RecuperarSenhaEvent) -> Unit = {},
) {
    val spacing  = LocalSpacing.current
    val iconSize = LocalIconSize.current

    AppScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            AppTopAppBar(
                title          = stringResource(R.string.recuperar_senha_titulo),
                onNavigateBack = { onEvent(RecuperarSenhaEvent.Voltar) },
            )
        },
    ) { paddingValues ->
        when {
            uiState.enviando -> AppLoading()
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                MaterialTheme.colorScheme.background,
                            )
                        )
                    )
                    .padding(paddingValues)
                    .padding(horizontal = spacing.medium)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                Spacer(Modifier.height(spacing.extraLarge))

                Box(
                    modifier         = Modifier
                        .size(iconSize.extraLarge * 2)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Default.Email,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimary,
                        modifier           = Modifier.size(iconSize.large),
                    )
                }

                Text(
                    text      = stringResource(R.string.recuperar_senha_titulo),
                    style     = MaterialTheme.typography.headlineSmall,
                    color     = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text      = stringResource(R.string.recuperar_senha_subtitulo),
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(spacing.small))

                AppFormField(
                    value         = uiState.email,
                    onValueChange = { onEvent(RecuperarSenhaEvent.EmailAlterado(it)) },
                    label         = stringResource(R.string.recuperar_senha_campo_email),
                    errorMessage  = uiState.erroEmail,
                    leadingIcon   = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardActions = KeyboardActions(
                        onDone = { onEvent(RecuperarSenhaEvent.Enviar) }
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Done,
                    ),
                )

                Button(
                    onClick  = { onEvent(RecuperarSenhaEvent.Enviar) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text     = stringResource(R.string.recuperar_senha_btn_enviar),
                        style    = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = spacing.extraSmall),
                    )
                }

                Spacer(Modifier.height(spacing.extraLarge))
            }
        }
    }
}

// ── Tier 3: Previews ──────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "RecuperarSenha – Vazio")
@Preview(showBackground = true, name = "RecuperarSenha – Vazio Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecuperarSenhaVazioPreview() {
    GerenciadorCartoesTheme { RecuperarSenhaContent() }
}

@Preview(showBackground = true, name = "RecuperarSenha – Preenchido")
@Composable
private fun RecuperarSenhaPreenchidoPreview() {
    GerenciadorCartoesTheme {
        RecuperarSenhaContent(
            uiState = RecuperarSenhaUiState(email = "usuario@email.com")
        )
    }
}

@Preview(showBackground = true, name = "RecuperarSenha – Erro")
@Composable
private fun RecuperarSenhaErroPreview() {
    GerenciadorCartoesTheme {
        RecuperarSenhaContent(
            uiState = RecuperarSenhaUiState(
                email     = "email-invalido",
                erroEmail = "E-mail inválido",
            )
        )
    }
}

@Preview(showBackground = true, name = "RecuperarSenha – Enviando")
@Composable
private fun RecuperarSenhaEnviandoPreview() {
    GerenciadorCartoesTheme {
        RecuperarSenhaContent(uiState = RecuperarSenhaUiState(enviando = true))
    }
}
