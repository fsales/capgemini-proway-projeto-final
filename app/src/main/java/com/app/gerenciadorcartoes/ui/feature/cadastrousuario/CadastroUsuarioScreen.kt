package com.app.gerenciadorcartoes.ui.feature.cadastrousuario

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.unit.dp
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.ui.components.AppLoading
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.components.AppTopAppBar
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.state.CadastroUsuarioUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.CadastroUsuarioViewModel

@Composable
fun CadastroUsuarioScreen(
    navigateBack : () -> Unit,
    viewModel    : CadastroUsuarioViewModel = hiltViewModel(),
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                CadastroUsuarioUiEvent.NavigateBack       -> navigateBack()
                is CadastroUsuarioUiEvent.MostrarErro     -> snackbarHostState.showSnackbar(event.mensagem)
                is CadastroUsuarioUiEvent.MostrarMensagem -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    CadastroUsuarioContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
    )
}

@Composable
fun CadastroUsuarioContent(
    uiState           : CadastroUsuarioUiState     = CadastroUsuarioUiState(),
    snackbarHostState : SnackbarHostState          = remember { SnackbarHostState() },
    onEvent           : (CadastroUsuarioEvent) -> Unit = {},
) {
    var senhaVisivel          by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }
    val spacing                = LocalSpacing.current

    AppScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            AppTopAppBar(
                title          = "Cadastro de Usuário",
                onNavigateBack = { onEvent(CadastroUsuarioEvent.Voltar) },
            )
        },
    ) { paddingValues ->
        when {
            uiState.carregando -> AppLoading()
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = spacing.medium)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.small),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(spacing.small))

                // Nome
                OutlinedTextField(
                    value           = uiState.nome,
                    onValueChange   = { onEvent(CadastroUsuarioEvent.NomeAlterado(it)) },
                    label           = { Text("Nome") },
                    leadingIcon     = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier        = Modifier.fillMaxWidth(),
                )

                // CPF
                OutlinedTextField(
                    value           = uiState.cpf,
                    onValueChange   = { onEvent(CadastroUsuarioEvent.CpfAlterado(it)) },
                    label           = { Text("CPF") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction    = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // CEP
                OutlinedTextField(
                    value           = uiState.cep,
                    onValueChange   = { onEvent(CadastroUsuarioEvent.CepAlterado(it)) },
                    label           = { Text("CEP") },
                    singleLine      = true,
                    trailingIcon    = {
                        if (uiState.buscandoCep) {
                            CircularProgressIndicator(
                                modifier  = Modifier.padding(spacing.extraSmall),
                                strokeWidth = 2.dp,
                                color     = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction    = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Endereço
                OutlinedTextField(
                    value           = uiState.endereco,
                    onValueChange   = { onEvent(CadastroUsuarioEvent.EnderecoAlterado(it)) },
                    label           = { Text("Endereço") },
                    singleLine      = true,
                    readOnly        = uiState.buscandoCep,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier        = Modifier.fillMaxWidth(),
                )

                // Número

                OutlinedTextField(
                    value           = uiState.number,
                    onValueChange   = { onEvent(CadastroUsuarioEvent.NumberAlterado(it)) },
                    label           = { Text("Número") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction    = ImeAction.Next,
                    ),
                    modifier        = Modifier.fillMaxWidth(),
                )

                //Bairro
                OutlinedTextField(
                    value           = uiState.bairro,
                    onValueChange   = { onEvent(CadastroUsuarioEvent.BairroAlterado(it)) },
                    label           = { Text("Bairro") },
                    singleLine      = true,
                    readOnly        = uiState.buscandoCep,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier        = Modifier.fillMaxWidth(),
                )

                // Estado
                OutlinedTextField(
                    value           = uiState.estado,
                    onValueChange   = { onEvent(CadastroUsuarioEvent.EstadoAlterado(it)) },
                    label           = { Text("Estado") },
                    singleLine      = true,
                    readOnly        = uiState.buscandoCep,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier        = Modifier.fillMaxWidth(),
                )

                // E-mail
                OutlinedTextField(
                    value           = uiState.email,
                    onValueChange   = { onEvent(CadastroUsuarioEvent.EmailAlterado(it)) },
                    label           = { Text("E-mail") },
                    leadingIcon     = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Senha
                OutlinedTextField(
                    value           = uiState.senha,
                    onValueChange   = { onEvent(CadastroUsuarioEvent.SenhaAlterada(it)) },
                    label           = { Text("Senha") },
                    leadingIcon     = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon    = {
                        IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                            Icon(
                                imageVector        = if (senhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha",
                            )
                        }
                    },
                    visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Confirmar Senha
                OutlinedTextField(
                    value           = uiState.confirmarSenha,
                    onValueChange   = { onEvent(CadastroUsuarioEvent.ConfirmarSenhaAlterada(it)) },
                    label           = { Text("Confirmar Senha") },
                    leadingIcon     = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon    = {
                        IconButton(onClick = { confirmarSenhaVisivel = !confirmarSenhaVisivel }) {
                            Icon(
                                imageVector        = if (confirmarSenhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmarSenhaVisivel) "Ocultar senha" else "Mostrar senha",
                            )
                        }
                    },
                    visualTransformation = if (confirmarSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(spacing.small))

                Button(
                    onClick  = { onEvent(CadastroUsuarioEvent.Cadastrar) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cadastrar")
                }

                Spacer(modifier = Modifier.height(spacing.medium))
            }
        }
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true, name = "CadastroUsuario – Carregando")
@Preview(showBackground = true, name = "CadastroUsuario – Carregando Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CadastroUsuarioCarregandoPreview() {
    GerenciadorCartoesTheme {
        CadastroUsuarioContent(uiState = CadastroUsuarioUiState(carregando = true))
    }
}

@Preview(showBackground = true, name = "CadastroUsuario – Vazio")
@Composable
private fun CadastroUsuarioVazioPreview() {
    GerenciadorCartoesTheme { CadastroUsuarioContent() }
}

@Preview(showBackground = true, name = "CadastroUsuario – Preenchido")
@Composable
private fun CadastroUsuarioPreenchidoPreview() {
    GerenciadorCartoesTheme {
        CadastroUsuarioContent(
            uiState = CadastroUsuarioUiState(
                nome           = "João Silva",
                cpf            = "123.456.789-00",
                cep            = "01310-100",
                endereco       = "Av. Paulista",
                number         = "1000",
                bairro         = "Bela Vista",
                estado         = "SP",
                email          = "joao@email.com",
                senha          = "senha123",
                confirmarSenha = "senha123",
            ),
        )
    }
}
