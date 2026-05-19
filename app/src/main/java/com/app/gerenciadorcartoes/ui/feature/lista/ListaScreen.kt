package com.app.gerenciadorcartoes.ui.feature.lista

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.ui.components.AppLoading
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.components.AppTopAppBar
import com.app.gerenciadorcartoes.ui.components.CartaoTemplateCard
import com.app.gerenciadorcartoes.ui.components.EmptyState
import com.app.gerenciadorcartoes.ui.feature.lista.state.ListaUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.ListaViewModel

// =============================================================================
// Screen — ponto de entrada; coleta ViewModel e roteia UiEvent.
// =============================================================================
@Composable
fun ListaScreen(
    onNavigateToNovo : () -> Unit,
    onNavigateToItem : (id: Long) -> Unit,
    viewModel        : ListaViewModel = hiltViewModel(),
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ListaUiEvent.NavegaParaItem  -> onNavigateToItem(event.id)
                ListaUiEvent.NavegaParaNovo     -> onNavigateToNovo()
                is ListaUiEvent.MostrarErro     -> snackbarHostState.showSnackbar(event.mensagem)
                is ListaUiEvent.MostrarMensagem -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    ListaContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
    )
}

// =============================================================================
// Content — renderiza estado puro; sem ViewModel (testável).
// =============================================================================
@Composable
fun ListaContent(
    uiState           : ListaUiState      = ListaUiState(),
    snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
    onEvent           : (ListaEvent) -> Unit = {},
) {
    var cartaoParaExcluir by remember { mutableStateOf<Cartao?>(null) }

    if (cartaoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { cartaoParaExcluir = null },
            title   = { Text("Excluir cartão") },
            text    = { Text("Deseja remover o cartão de ${cartaoParaExcluir!!.nomeTitular}?") },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(ListaEvent.ExcluirCartao(cartaoParaExcluir!!.id))
                    cartaoParaExcluir = null
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { cartaoParaExcluir = null }) { Text("Cancelar") }
            },
        )
    }

    AppScaffold(
        snackbarHostState    = snackbarHostState,
        topBar               = { AppTopAppBar(title = "Meus Cartões") },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(ListaEvent.NavegaParaNovo) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo cartão")
            }
        },
    ) { paddingValues ->
        when {
            uiState.carregando        -> AppLoading()
            uiState.cartoes.isEmpty() -> EmptyState(
                message  = "Nenhum cartão cadastrado.\nToque em + para adicionar.",
                modifier = Modifier.padding(paddingValues),
            )
            else -> LazyColumn(
                contentPadding      = paddingValues,
                verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.small),
                modifier            = Modifier.fillMaxSize(),
            ) {
                items(items = uiState.cartoes, key = { it.id }) { cartao ->
                    CartaoItem(
                        cartao    = cartao,
                        onClick   = { onEvent(ListaEvent.NavegaParaItem(cartao.id)) },
                        onExcluir = { cartaoParaExcluir = cartao },
                    )
                }
            }
        }
    }
}

// =============================================================================
// Composable interno: CartaoItem
// =============================================================================
@Composable
private fun CartaoItem(
    cartao    : Cartao,
    onClick   : () -> Unit,
    onExcluir : () -> Unit,
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.medium),
    ) {
        CartaoTemplateCard(
            cartao  = cartao,
            onClick = onClick,
        )
        IconButton(
            onClick  = onExcluir,
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Icon(
                imageVector        = Icons.Default.Delete,
                contentDescription = "Excluir",
                tint               = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

// =============================================================================
// Previews
// =============================================================================
@Preview(showBackground = true, name = "Lista – Carregando")
@Preview(showBackground = true, name = "Lista – Carregando Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListaCarregandoPreview() {
    GerenciadorCartoesTheme { ListaContent(uiState = ListaUiState(carregando = true)) }
}

@Preview(showBackground = true, name = "Lista – Vazia")
@Composable
private fun ListaVaziaPreview() {
    GerenciadorCartoesTheme { ListaContent() }
}

@Preview(showBackground = true, name = "Lista – Com cartões")
@Preview(showBackground = true, name = "Lista – Com cartões Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListaComItensPreview() {
    GerenciadorCartoesTheme {
        ListaContent(
            uiState = ListaUiState(
                cartoes = listOf(
                    Cartao(1L, "João Silva",    "1234", "Visa",       "12/28", 5_000.0),
                    Cartao(2L, "Maria Santos",  "5678", "Mastercard", "08/26", 10_000.0),
                    Cartao(3L, "Pedro Almeida", "9012", "Elo",        "03/25", 2_500.0),
                ),
            ),
        )
    }
}
