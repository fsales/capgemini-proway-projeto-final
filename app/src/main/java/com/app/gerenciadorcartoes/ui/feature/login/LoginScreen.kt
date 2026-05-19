package com.app.gerenciadorcartoes.ui.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.components.AppTopAppBar
import com.app.gerenciadorcartoes.ui.feature.login.state.LoginUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onNavigateToLista     : () -> Unit,
    onNavigateParaCadastro: () -> Unit,
    viewModel             : LoginViewModel = hiltViewModel(),
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                LoginUiEvent.NavegaParaLista      -> onNavigateToLista()
                LoginUiEvent.NavegaParaCadastro   -> onNavigateParaCadastro()
                is LoginUiEvent.MostrarErro       -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    LoginContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
    )
}

@Composable
fun LoginContent(
    uiState           : LoginUiState      = LoginUiState(),
    snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
    onEvent           : (LoginEvent) -> Unit = {},
) {
    var senhaVisivel by remember { mutableStateOf(false) }
    val spacing       = LocalSpacing.current

    AppScaffold(
        snackbarHostState = snackbarHostState,
        topBar            = { AppTopAppBar(title = "Login") },
    ) { paddingValues ->
        when {
            else -> Column(
                modifier              = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = spacing.medium),
                verticalArrangement   = Arrangement.Center,
                horizontalAlignment   = Alignment.CenterHorizontally,
            ) {
                // Título da tela
                Text(
                    text      = "Bem-vindo!",
                    style     = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(spacing.small))


                // Campo usuário
                OutlinedTextField(
                    value         = uiState.usuario,
                    onValueChange = { onEvent(LoginEvent.Usuario(it)) },
                    label         = { Text("Usuário") },
                    leadingIcon   = {
                        Icon(
                            imageVector        = Icons.Default.Person,
                            contentDescription = null,
                        )
                    },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction    = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(spacing.small))

                // Campo senha
                OutlinedTextField(
                    value         = uiState.senha,
                    onValueChange = { onEvent(LoginEvent.Senha(it)) },
                    label         = { Text("Senha") },
                    leadingIcon   = {
                        Icon(
                            imageVector        = Icons.Default.Lock,
                            contentDescription = null,
                        )
                    },
                    trailingIcon  = {
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
                    singleLine     = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(spacing.large))

                // Botões Entrar e Cadastrar
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                ) {
                    OutlinedButton(
                        onClick  = { onEvent(LoginEvent.NavegaParaCadastro) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cadastrar")
                    }

                    Button(
                        onClick  = { onEvent(LoginEvent.Entrar) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Entrar")
                    }
                }
                Text(
                    text      = "Esqueceu a senha !",
                    textAlign = TextAlign.Center,
                    color = Color.Blue,
                    style = TextStyle(textDecoration = TextDecoration.Underline)
                )
            }
        }
    }
}

// =============================================================================
// Previews
// =============================================================================


@Preview(showBackground = true, name = "Login – Preenchido")
@Composable
private fun LoginPreenchidoPreview() {
    GerenciadorCartoesTheme {
        LoginContent(
            uiState = LoginUiState(
                usuario = "gustavo",
                senha   = "1234",
            ),
        )
    }
}

