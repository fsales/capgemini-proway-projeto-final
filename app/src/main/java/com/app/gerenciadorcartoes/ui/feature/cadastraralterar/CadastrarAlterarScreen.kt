package com.app.gerenciadorcartoes.ui.feature.cadastraralterar

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.app.gerenciadorcartoes.ui.components.ValidadeVisualTransformation
import com.app.gerenciadorcartoes.ui.components.rememberCurrencyVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.extensions.toCurrencyDigits
import com.app.gerenciadorcartoes.extensions.toCurrencyDouble
import com.app.gerenciadorcartoes.extensions.toValidadeFormatada
import com.app.gerenciadorcartoes.ui.components.AppLoading
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.components.AppTopAppBar
import com.app.gerenciadorcartoes.ui.components.CartaoTemplateMini
import com.app.gerenciadorcartoes.ui.components.CartaoTemplateCard
import com.app.gerenciadorcartoes.ui.components.todosTemplates
import com.app.gerenciadorcartoes.ui.feature.cadastraralterar.state.CadastrarAlterarUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.CadastrarAlterarViewModel
import com.app.gerenciadorcartoes.model.Cartao

// =============================================================================
// Screen — ponto de entrada; coleta ViewModel e roteia UiEvent.
// =============================================================================
@Composable
fun CadastrarAlterarScreen(
    navigateBack : () -> Unit,
    viewModel    : CadastrarAlterarViewModel = hiltViewModel(),
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                CadastrarAlterarUiEvent.NavigateBack     -> navigateBack()
                is CadastrarAlterarUiEvent.MostrarErro   -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    CadastrarAlterarContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
    )
}

// =============================================================================
// Content — renderiza estado puro; sem ViewModel (testável).
// =============================================================================
@Composable
fun CadastrarAlterarContent(
    uiState           : CadastrarAlterarUiState = CadastrarAlterarUiState(),
    snackbarHostState : SnackbarHostState        = remember { SnackbarHostState() },
    onEvent           : (CadastrarAlterarEvent) -> Unit = {},
) {
    val titulo = if (uiState.isEdicao) "Editar Cartão" else "Novo Cartão"

    AppScaffold(
        snackbarHostState    = snackbarHostState,
        topBar               = {
            AppTopAppBar(
                title          = titulo,
                onNavigateBack = { onEvent(CadastrarAlterarEvent.Voltar) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick          = { onEvent(CadastrarAlterarEvent.Salvar) },
                containerColor   = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor     = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Salvar")
            }
        },
    ) { paddingValues ->
        when {
            uiState.carregando -> AppLoading()
            else               -> FormularioBody(
                uiState         = uiState,
                onEvent         = onEvent,
                modifier        = Modifier.padding(paddingValues),
            )
        }
    }
}

// =============================================================================
// Formulário
// =============================================================================
@Composable
private fun FormularioBody(
    uiState  : CadastrarAlterarUiState,
    onEvent  : (CadastrarAlterarEvent) -> Unit,
    modifier : Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.medium)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        // ── Card preview ────────────────────────────────────────────────────
        CartaoTemplateCard(
            cartao = Cartao(
                nomeTitular = uiState.nomeTitular.ifBlank { "Nome do Titular" },
                finalNumero = uiState.finalNumero.ifBlank { "0000" },
                bandeira    = uiState.bandeira.ifBlank { "Bandeira" },
                validade    = uiState.validade.ifBlank { "--/--" },
                template    = uiState.template,
            ),
            modifier = Modifier.padding(vertical = spacing.small),
        )

        // ── Template picker ─────────────────────────────────────────────────
        Text(
            text  = "Escolha o template",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            items(todosTemplates) { config ->
                CartaoTemplateMini(
                    config   = config,
                    selected = uiState.template == config.id,
                    onClick  = { onEvent(CadastrarAlterarEvent.TemplateAlterado(config.id)) },
                )
            }
        }

        // Nome do titular
        OutlinedTextField(
            value         = uiState.nomeTitular,
            onValueChange = { onEvent(CadastrarAlterarEvent.NomeTitularAlterado(it)) },
            label         = { Text("Nome do titular") },
            isError       = uiState.erroNome != null,
            supportingText = uiState.erroNome?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction      = ImeAction.Next,
            ),
            singleLine = true,
            modifier   = Modifier.fillMaxWidth(),
        )

        // Últimos 4 dígitos
        OutlinedTextField(
            value         = uiState.finalNumero,
            onValueChange = { onEvent(CadastrarAlterarEvent.FinalNumeroAlterado(it.filter(Char::isDigit))) },
            label         = { Text("4 últimos dígitos do cartão") },
            isError       = uiState.erroNumero != null,
            supportingText = uiState.erroNumero?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction    = ImeAction.Next,
            ),
            singleLine = true,
            modifier   = Modifier.fillMaxWidth(),
        )

        // Bandeira
        OutlinedTextField(
            value         = uiState.bandeira,
            onValueChange = { onEvent(CadastrarAlterarEvent.BandeiraAlterada(it)) },
            label         = { Text("Bandeira (ex: Visa, Mastercard, Elo)") },
            isError       = uiState.erroBandeira != null,
            supportingText = uiState.erroBandeira?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction      = ImeAction.Next,
            ),
            singleLine = true,
            modifier   = Modifier.fillMaxWidth(),
        )

        // Validade
        OutlinedTextField(
            value         = uiState.validade,
            onValueChange = { onEvent(CadastrarAlterarEvent.ValidadeAlterada(it.toValidadeFormatada())) },
            label         = { Text("Validade (MM/AA)") },
            visualTransformation = ValidadeVisualTransformation,
            isError       = uiState.erroValidade != null,
            supportingText = uiState.erroValidade?.let { { Text(it) } },
            placeholder   = { Text("12/28") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction    = ImeAction.Next,
            ),
            singleLine = true,
            modifier   = Modifier.fillMaxWidth(),
        )

        // Limite
        OutlinedTextField(
            value         = uiState.limite.toCurrencyDigits(),
            onValueChange = { onEvent(CadastrarAlterarEvent.LimiteAlterado(it.toCurrencyDouble())) },
            label         = { Text("Limite máximo (R$)") },
            visualTransformation = rememberCurrencyVisualTransformation(),
            isError       = uiState.erroLimite != null,
            supportingText = uiState.erroLimite?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction    = ImeAction.Done,
            ),
            singleLine = true,
            modifier   = Modifier.fillMaxWidth(),
        )
    }
}

// VisualTransformations moved to ui/components/MaskVisualTransformations.kt for reuse

// =============================================================================
// Previews
// =============================================================================
@Preview(showBackground = true, name = "Cadastrar – Novo")
@Preview(showBackground = true, name = "Cadastrar – Novo Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CadastrarNovoPreview() {
    GerenciadorCartoesTheme { CadastrarAlterarContent() }
}

@Preview(showBackground = true, name = "Editar – Com dados")
@Composable
private fun EditarComDadosPreview() {
    GerenciadorCartoesTheme {
        CadastrarAlterarContent(
            uiState = CadastrarAlterarUiState(
                nomeTitular = "João Silva",
                finalNumero = "1234",
                bandeira    = "Visa",
                validade    = "12/28",
                limite      = 5000.0,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Cadastrar – Com erros")
@Composable
private fun CadastrarComErrosPreview() {
    GerenciadorCartoesTheme {
        CadastrarAlterarContent(
            uiState = CadastrarAlterarUiState(
                erroNome     = "Nome é obrigatório",
                erroNumero   = "Informe os 4 últimos dígitos",
                erroValidade = "Formato MM/AA",
            ),
        )
    }
}
