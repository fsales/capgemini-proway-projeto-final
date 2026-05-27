package com.app.gerenciadorcartoes.ui.feature.fatura

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.ui.components.AppLoading
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.components.AppTopAppBar
import com.app.gerenciadorcartoes.ui.components.EmptyState
import com.app.gerenciadorcartoes.ui.feature.fatura.state.FaturaMesUiState
import com.app.gerenciadorcartoes.ui.feature.fatura.state.FaturaUiState
import com.app.gerenciadorcartoes.ui.feature.fatura.state.LancamentoUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.FaturaViewModel
import java.text.NumberFormat
import java.util.Locale

private val PtBrLocale: Locale = Locale.forLanguageTag("pt-BR")

@Composable
fun FaturaScreen(
    navigateBack : () -> Unit,
    viewModel    : FaturaViewModel = hiltViewModel(),
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                FaturaUiEvent.NavigateBack   -> navigateBack()
                is FaturaUiEvent.MostrarErro -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    FaturaContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
    )
}

@Composable
fun FaturaContent(
    uiState           : FaturaUiState     = FaturaUiState(),
    snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
    onEvent           : (FaturaEvent) -> Unit = {},
) {
    AppScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            AppTopAppBar(
                title          = uiState.titulo,
                onNavigateBack = { onEvent(FaturaEvent.Voltar) },
            )
        },
    ) { paddingValues ->
        when {
            uiState.carregando    -> AppLoading(modifier = Modifier.padding(paddingValues))
            uiState.faturas.isEmpty() -> EmptyState(
                title    = "Sem faturas",
                subtitle = "Nao ha lancamentos para este cartao no periodo.",
                modifier = Modifier.padding(paddingValues),
            )
            else -> FaturaBody(
                faturas       = uiState.faturas,
                paddingValues = paddingValues,
            )
        }
    }
}

@Composable
private fun FaturaBody(
    faturas       : List<FaturaMesUiState>,
    paddingValues : PaddingValues,
) {
    val spacing = LocalSpacing.current
    var selectedReferencia by remember(faturas) {
        mutableStateOf(faturas.first().referencia)
    }
    val selectedFatura = faturas.firstOrNull { it.referencia == selectedReferencia } ?: faturas.first()

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = paddingValues,
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        item(key = "month-selector") {
            MonthSelector(
                meses               = faturas,
                selectedReferencia  = selectedFatura.referencia,
                onReferenciaSelected = { selectedReferencia = it },
                modifier            = Modifier.fillMaxWidth(),
            )
        }

        item(key = selectedFatura.referencia) {
            FaturaMesSection(
                fatura   = selectedFatura,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.medium),
            )
        }
    }
}

@Composable
private fun MonthSelector(
    meses                : List<FaturaMesUiState>,
    selectedReferencia   : String,
    onReferenciaSelected : (String) -> Unit,
    modifier             : Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    LazyRow(
        modifier              = modifier,
        contentPadding        = PaddingValues(horizontal = spacing.medium, vertical = spacing.small),
        horizontalArrangement = Arrangement.spacedBy(spacing.smallMedium),
    ) {
        items(items = meses, key = { it.referencia }) { mes ->
            MonthSelectorItem(
                referencia = mes.referencia,
                selected   = mes.referencia == selectedReferencia,
                onClick    = { onReferenciaSelected(mes.referencia) },
            )
        }
    }
}

@Composable
private fun MonthSelectorItem(
    referencia : String,
    selected   : Boolean,
    onClick    : () -> Unit,
    modifier   : Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val shape   = RoundedCornerShape(28.dp)
    val animationSpec = tween<Color>(durationMillis = 220)

    val containerColor by animateColorAsState(
        targetValue   = if (selected) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        animationSpec = animationSpec,
        label         = "monthContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue   = if (selected) Color(0xFF111827) else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = animationSpec,
        label         = "monthContentColor",
    )
    val borderColor by animateColorAsState(
        targetValue   = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.42f) else Color.Transparent,
        animationSpec = animationSpec,
        label         = "monthBorderColor",
    )
    val shadowElevation by animateDpAsState(
        targetValue   = if (selected) 6.dp else 0.dp,
        animationSpec = tween(durationMillis = 220),
        label         = "monthShadowElevation",
    )
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1f else 0.98f,
        animationSpec = tween(durationMillis = 180),
        label         = "monthScale",
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(onClick = onClick),
        shape           = shape,
        color           = containerColor,
        contentColor    = contentColor,
        border          = BorderStroke(width = 1.dp, color = borderColor),
        shadowElevation = shadowElevation,
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 104.dp)
                .heightIn(min = 48.dp)
                .padding(horizontal = spacing.medium, vertical = spacing.smallMedium),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text      = referencia,
                style     = MaterialTheme.typography.labelLarge,
                color     = contentColor,
                maxLines  = 1,
                textAlign = TextAlign.Center,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun FaturaMesSection(
    fatura   : FaturaMesUiState,
    modifier : Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val currencyFormatter = rememberCurrencyFormatter()

    Card(modifier = modifier) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text  = fatura.referencia,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text  = currencyFormatter.format(fatura.total),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            HorizontalDivider()

            fatura.lancamentos.forEachIndexed { index, lancamento ->
                LancamentoRow(lancamento = lancamento)
                if (index != fatura.lancamentos.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun LancamentoRow(lancamento: LancamentoUiState) {
    val spacing = LocalSpacing.current
    val currencyFormatter = rememberCurrencyFormatter()

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
            Text(
                text  = lancamento.descricao,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text  = lancamento.data,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        Text(
            text  = currencyFormatter.format(lancamento.valor),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun rememberCurrencyFormatter(): NumberFormat =
    remember { NumberFormat.getCurrencyInstance(PtBrLocale) }

@Preview(showBackground = true, name = "Fatura - Carregando")
@Preview(showBackground = true, name = "Fatura - Carregando Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FaturaCarregandoPreview() {
    GerenciadorCartoesTheme {
        FaturaContent(uiState = FaturaUiState(carregando = true))
    }
}

@Preview(showBackground = true, name = "Fatura - Vazio")
@Composable
private fun FaturaVazioPreview() {
    GerenciadorCartoesTheme {
        FaturaContent(uiState = FaturaUiState())
    }
}

@Preview(showBackground = true, name = "Fatura - Com dados")
@Preview(showBackground = true, name = "Fatura - Com dados Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FaturaComDadosPreview() {
    GerenciadorCartoesTheme {
        FaturaContent(
            uiState = FaturaUiState(
                titulo = "Faturas •••• 1234",
                faturas = listOf(
                    FaturaMesUiState(
                        referencia = "Maio/2026",
                        lancamentos = listOf(
                            LancamentoUiState("Supermercado", "05/05/2026", 145.90),
                            LancamentoUiState("Combustivel", "10/05/2026", 210.30),
                            LancamentoUiState("Farmacia", "13/05/2026", 55.10),
                            LancamentoUiState("Cinema", "20/05/2026", 48.00),
                            LancamentoUiState("Restaurante", "24/05/2026", 129.90),
                        ),
                    ),
                ),
            ),
        )
    }
}
