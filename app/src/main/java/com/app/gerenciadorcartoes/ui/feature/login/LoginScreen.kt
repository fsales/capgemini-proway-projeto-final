package com.app.gerenciadorcartoes.ui.feature.login

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.R
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.feature.login.state.LoginUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalIconSize
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.LoginViewModel

// ── Nível 1: Screen ───────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onNavigateToLista      : () -> Unit,
    onNavigateParaCadastro : () -> Unit,
    viewModel              : LoginViewModel = hiltViewModel(),
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                LoginUiEvent.NavegaParaLista    -> onNavigateToLista()
                LoginUiEvent.NavegaParaCadastro -> onNavigateParaCadastro()
                is LoginUiEvent.MostrarErro     -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    LoginContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
    )
}

// ── Nível 2: Content ──────────────────────────────────────────────────────────
@Composable
fun LoginContent(
    uiState           : LoginUiState      = LoginUiState(),
    snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
    onEvent           : (LoginEvent) -> Unit = {},
) {
    var senhaVisivel by remember { mutableStateOf(false) }
    val spacing  = LocalSpacing.current

    AppScaffold(snackbarHostState = snackbarHostState) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
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
        ) {
            Spacer(Modifier.height(spacing.extraLarge))

            // ── Seção de marca ───────────────────────────────────────────────
            BrandHeader()

            Spacer(Modifier.height(spacing.extraLarge))

            // ── Campo usuário ─────────────────────────────────────────────────
            OutlinedTextField(
                value          = uiState.usuario,
                onValueChange  = { onEvent(LoginEvent.Usuario(it)) },
                label          = { Text(stringResource(R.string.login_campo_usuario)) },
                leadingIcon    = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                isError        = uiState.erroUsuario != null,
                supportingText = uiState.erroUsuario?.let { { Text(it) } },
                singleLine     = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction    = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(spacing.small))

            // ── Campo senha ───────────────────────────────────────────────────
            OutlinedTextField(
                value          = uiState.senha,
                onValueChange  = { onEvent(LoginEvent.Senha(it)) },
                label          = { Text(stringResource(R.string.login_campo_senha)) },
                leadingIcon    = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon   = {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            imageVector        = if (senhaVisivel) Icons.Default.VisibilityOff
                                                 else Icons.Default.Visibility,
                            contentDescription = if (senhaVisivel) "Ocultar senha"
                                                 else "Mostrar senha",
                        )
                    }
                },
                visualTransformation = if (senhaVisivel) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                isError        = uiState.erroSenha != null,
                supportingText = uiState.erroSenha?.let { { Text(it) } },
                singleLine     = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onEvent(LoginEvent.Entrar) }
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Esqueceu a senha — alinhado à direita ─────────────────────────
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { /* TODO: recuperar senha */ }) {
                    Text(
                        text  = stringResource(R.string.login_esqueceu_senha),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            Spacer(Modifier.height(spacing.medium))

            // ── Botão primário: Entrar ────────────────────────────────────────
            Button(
                onClick  = { onEvent(LoginEvent.Entrar) },
                enabled  = !uiState.carregando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.carregando) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(LocalIconSize.current.medium),
                        color     = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = LocalSpacing.current.extraSmall / 2,
                    )
                } else {
                    Text(
                        text      = stringResource(R.string.login_btn_entrar),
                        style     = MaterialTheme.typography.labelLarge,
                        modifier  = Modifier.padding(vertical = spacing.extraSmall),
                    )
                }
            }

            Spacer(Modifier.height(spacing.small))

            // ── Link de cadastro ──────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text  = stringResource(R.string.login_nao_tem_conta),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                )
                TextButton(onClick = { onEvent(LoginEvent.NavegaParaCadastro) }) {
                    Text(
                        text  = stringResource(R.string.login_btn_cadastrar),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            Spacer(Modifier.height(spacing.extraLarge))
        }
    }
}

// ── Brand header: mesma linguagem visual da splash ────────────────────────────
@Composable
private fun BrandHeader() {
    val spacing  = LocalSpacing.current
    val iconSize = LocalIconSize.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.smallMedium),
    ) {
        // Círculo azul com ícone do cartão — 2 × iconSize.extraLarge = 96dp
        Box(
            modifier         = Modifier
                .size(iconSize.extraLarge * 2)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter            = painterResource(R.drawable.ic_splash_logo_static),
                contentDescription = null,
                modifier           = Modifier.size(iconSize.extraLarge + iconSize.large),
            )
        }

        Spacer(Modifier.height(spacing.small))

        // Nome do app
        Text(
            text       = stringResource(R.string.app_name),
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary,
        )

        // Saudação
        Text(
            text      = stringResource(R.string.login_bem_vindo),
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        // Subtítulo
        Text(
            text      = stringResource(R.string.login_subtitulo),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

// ── Nível 3: Previews ─────────────────────────────────────────────────────────
@Preview(showBackground = true, name = "Login – Vazio")
@Preview(showBackground = true, name = "Login – Vazio Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginVazioPreview() {
    GerenciadorCartoesTheme { LoginContent() }
}

@Preview(showBackground = true, name = "Login – Preenchido")
@Preview(showBackground = true, name = "Login – Preenchido Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginPreenchidoPreview() {
    GerenciadorCartoesTheme {
        LoginContent(uiState = LoginUiState(usuario = "gustavo", senha = "1234"))
    }
}

@Preview(showBackground = true, name = "Login – Erros")
@Composable
private fun LoginErrosPreview() {
    GerenciadorCartoesTheme {
        LoginContent(
            uiState = LoginUiState(
                erroUsuario = "Informe o usuário",
                erroSenha   = "Informe a senha",
            )
        )
    }
}

@Preview(showBackground = true, name = "Login – Carregando")
@Composable
private fun LoginCarregandoPreview() {
    GerenciadorCartoesTheme {
        LoginContent(uiState = LoginUiState(usuario = "gustavo", senha = "1234", carregando = true))
    }
}
