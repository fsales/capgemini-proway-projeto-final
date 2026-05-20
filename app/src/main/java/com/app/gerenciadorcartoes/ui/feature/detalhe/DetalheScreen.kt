package com.app.gerenciadorcartoes.ui.feature.detalhe

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.ui.components.AppLoading
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.components.AppTopAppBar
import com.app.gerenciadorcartoes.ui.feature.detalhe.state.DetalheUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.DetalheViewModel

// =============================================================================
// Screen — ponto de entrada; coleta ViewModel e roteia UiEvent.
// =============================================================================
@Composable
fun DetalheScreen(
    navigateBack : () -> Unit,
    viewModel    : DetalheViewModel = hiltViewModel(),
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                DetalheUiEvent.NavigateBack    -> navigateBack()
                is DetalheUiEvent.MostrarErro  -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    DetalheContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
    )
}

// =============================================================================
// Content — renderiza estado puro; sem ViewModel (testável).
// =============================================================================
@Composable
fun DetalheContent(
    uiState           : DetalheUiState    = DetalheUiState(),
    snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
    onEvent           : (DetalheEvent) -> Unit = {},
) {
    AppScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            AppTopAppBar(
                title          = "Detalhes do Cartão",
                onNavigateBack = { onEvent(DetalheEvent.Voltar) },
            )
        },
    ) { paddingValues ->
        when {
            uiState.carregando -> AppLoading()
            else               -> DetalheBody(
                cartao        = uiState.cartao,
                paddingValues = paddingValues,
            )
        }
    }
}

// =============================================================================
// Body — exibe os campos do cartão.
// =============================================================================
@Composable
private fun DetalheBody(
    cartao        : Cartao,
    paddingValues : PaddingValues,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        DetalheRow(label = "Titular",  value = cartao.nomeTitular)
        DetalheRow(label = "Número",   value = "•••• •••• •••• ${cartao.finalNumero}")
        DetalheRow(label = "Bandeira", value = cartao.bandeira)
        DetalheRow(label = "Validade", value = cartao.validade)
        DetalheRow(label = "Limite",   value = "R$ ${"%.2f".format(cartao.limite)}")
    }
}

@Composable
private fun DetalheRow(label: String, value: String) {
    val spacing = LocalSpacing.current
    Column {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.titleMedium,
        )
        HorizontalDivider(modifier = Modifier.padding(top = spacing.small))
    }
}

// =============================================================================
// Previews
// =============================================================================
@Preview(showBackground = true, name = "Detalhe – Carregando")
@Preview(showBackground = true, name = "Detalhe – Carregando Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetalheCarregandoPreview() {
    GerenciadorCartoesTheme { DetalheContent(uiState = DetalheUiState(carregando = true)) }
}

@Preview(showBackground = true, name = "Detalhe – Com dados")
@Preview(showBackground = true, name = "Detalhe – Com dados Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetalheComDadosPreview() {
    GerenciadorCartoesTheme {
        DetalheContent(
            uiState = DetalheUiState(
                cartao = Cartao(1L, "João Silva", "1234", "Visa", "12/28", 5_000.0),
            ),
        )
    }
}
