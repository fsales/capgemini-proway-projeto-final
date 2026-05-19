package com.app.gerenciadorcartoes.ui.feature.lista

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
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
import com.app.gerenciadorcartoes.ui.theme.LocalIconSize
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.ListaViewModel

// =============================================================================
// Screen — ponto de entrada; coleta ViewModel e roteia UiEvent.
// =============================================================================
@Composable
fun ListaScreen(
    onNavigateToNovo : () -> Unit,
    onNavigateToItem : (id: Long) -> Unit,
    onNavigateToEditar : (id: Long) -> Unit,
    viewModel        : ListaViewModel = hiltViewModel(),
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ListaUiEvent.NavegaParaItem  -> onNavigateToItem(event.id)
                is ListaUiEvent.NavegaParaEditar -> onNavigateToEditar(event.id)
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaContent(
    uiState           : ListaUiState      = ListaUiState(),
    snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
    onEvent           : (ListaEvent) -> Unit = {},
) {
    var cartaoParaExcluir by remember { mutableStateOf<Cartao?>(null) }
    var cartaoParaAcoes by remember { mutableStateOf<Cartao?>(null) }
    val spacing = LocalSpacing.current
    val iconSize = LocalIconSize.current

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

    if (cartaoParaAcoes != null) {
        val cartaoSelecionado = cartaoParaAcoes!!
        ModalBottomSheet(
            onDismissRequest = { cartaoParaAcoes = null },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.medium),
            ) {
                ListItem(
                    headlineContent = { Text("Editar cartão") },
                    leadingContent  = {
                        Icon(
                            imageVector        = Icons.Default.Edit,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.clickable {
                        cartaoParaAcoes = null
                        onEvent(ListaEvent.NavegaParaEditar(cartaoSelecionado.id))
                    },
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = {
                        Text(
                            text  = "Excluir cartão",
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    leadingContent  = {
                        Icon(
                            imageVector        = Icons.Default.Delete,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.error,
                        )
                    },
                    modifier = Modifier.clickable {
                        cartaoParaAcoes = null
                        cartaoParaExcluir = cartaoSelecionado
                    },
                )
            }
        }
    }

    AppScaffold(
        snackbarHostState    = snackbarHostState,
        topBar               = {
            AppTopAppBar(
                title    = "Meus Cartões",
                subtitle = "Gerencie seus cartões cadastrados",
                large    = true,
                actions  = {
                    FilledIconButton(
                        onClick = { onEvent(ListaEvent.NavegaParaNovo) },
                        modifier = Modifier
                            .shadow(
                                elevation = spacing.smallMedium,
                                shape     = CircleShape,
                                clip      = false,
                            )
                            .border(
                                width = spacing.extraSmall / 4,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.28f),
                                shape = CircleShape,
                            ),
                        colors  = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor   = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Add,
                            contentDescription = "Novo cartão",
                            modifier           = Modifier.size(iconSize.medium),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when {
            uiState.carregando        -> AppLoading()
            uiState.cartoes.isEmpty() -> EmptyState(
                title    = "Você ainda não tem cartões",
                subtitle = "Adicione seu primeiro cartão para começar",
                modifier = Modifier
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.09f),
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
                            ),
                        ),
                    ),
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
                        onMenuClick = { cartaoParaAcoes = cartao },
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
    cartao      : Cartao,
    onClick     : () -> Unit,
    onMenuClick : () -> Unit,
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
            onClick  = onMenuClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = spacing.small, end = spacing.small)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.24f),
                    shape = CircleShape,
                )
                .border(
                    width = spacing.extraSmall / 4,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector        = Icons.Default.MoreVert,
                contentDescription = "Menu do cartão",
                tint               = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.95f),
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
