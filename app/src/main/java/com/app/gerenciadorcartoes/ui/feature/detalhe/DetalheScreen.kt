package com.app.gerenciadorcartoes.ui.feature.detalhe

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.model.CartaoDetalhe
import com.app.gerenciadorcartoes.model.InstituicaoFinanceira
import com.app.gerenciadorcartoes.ui.components.AppLoading
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.components.AppTopAppBar
import com.app.gerenciadorcartoes.ui.components.CartaoTemplateCard
import com.app.gerenciadorcartoes.ui.feature.detalhe.state.DetalheUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.DetalheViewModel
import java.text.NumberFormat
import java.util.Locale

private val PtBrLocale: Locale = Locale.forLanguageTag("pt-BR")

// =============================================================================
// Screen — ponto de entrada; coleta ViewModel e roteia UiEvent.
// =============================================================================
@Composable
fun DetalheScreen(
    navigateBack              : () -> Unit,
    onNavigateToAjustarLimite : (Long) -> Unit,
    viewModel                 : DetalheViewModel = hiltViewModel(),
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

     LaunchedEffect(viewModel) {
         viewModel.uiEvent.collect { event ->
             when (event) {
                 DetalheUiEvent.NavigateBack               -> navigateBack()
                 is DetalheUiEvent.NavigateToAjustarLimite -> onNavigateToAjustarLimite(event.id)
                 is DetalheUiEvent.MostrarErro             -> snackbarHostState.showSnackbar(event.mensagem)
                 is DetalheUiEvent.MostrarMensagem         -> snackbarHostState.showSnackbar(event.mensagem)
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
                title          = tituloCartao(uiState.detalhe.cartao),
                onNavigateBack = { onEvent(DetalheEvent.Voltar) },
            )
        },
    ) { paddingValues ->
        when {
            uiState.carregando -> AppLoading(modifier = Modifier.padding(paddingValues))
            else               -> DetalheBody(
                detalhe       = uiState.detalhe,
                paddingValues = paddingValues,
                onEvent       = onEvent,
            )
        }
    }
}

// =============================================================================
// Body — exibe o cartão selecionado e o limite.
// =============================================================================
@Composable
private fun DetalheBody(
    detalhe       : CartaoDetalhe,
    paddingValues : PaddingValues,
    onEvent       : (DetalheEvent) -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.extraLarge),
    ) {
         CartaoSection(detalhe = detalhe)
         BlockCardSection(
             detalhe = detalhe,
             onBlockCard = { onEvent(DetalheEvent.BloquearCartao) }
         )
         LimiteSection(
            detalhe          = detalhe,
            onAjustarLimite = { onEvent(DetalheEvent.AjustarLimite) },
        )
    }
}

@Composable
private fun CartaoSection(detalhe: CartaoDetalhe) {
    CartaoTemplateCard(cartao = detalhe.cartao)
}

@Composable
private fun BlockCardSection(
     detalhe          : CartaoDetalhe,
     onBlockCard : () -> Unit,){
     val spacing              = LocalSpacing.current
     val isBloqueado          = detalhe.cartao.bloqueado
     val corBotao             = if (isBloqueado) Color(0xFF2E7D32)  else Color(0xFFC62828)
     val textoBotao           = if (isBloqueado) "Desbloquear" else "Bloquear"

     Column(
         modifier            = Modifier.fillMaxWidth(),
         verticalArrangement = Arrangement.spacedBy(spacing.medium),
     ) {
         Row(
             modifier              = Modifier.fillMaxWidth(),
             horizontalArrangement = Arrangement.SpaceBetween,
             verticalAlignment     = Alignment.CenterVertically,
         ) {
             Text(
                 text  = "Bloqueio",
                 style = MaterialTheme.typography.titleLarge,
             )
             Button(
                 onClick = onBlockCard,
                 colors = ButtonDefaults.buttonColors(
                     containerColor = corBotao
                 )
             ) {
                 Text(text = textoBotao)
             }
         }

     }
 }

@Composable
private fun LimiteSection(
    detalhe          : CartaoDetalhe,
    onAjustarLimite : () -> Unit,
) {
    val spacing              = LocalSpacing.current
    val currencyFormatter    = rememberCurrencyFormatter()
    val percentualUso        = detalhe.percentualUso
    val progressColor        = limiteProgressColor(percentualUso)
    val animatedPercentual by animateFloatAsState(
        targetValue   = percentualUso,
        animationSpec = tween(durationMillis = 900),
        label         = "limiteUsado",
    )

    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = "Limite",
                style = MaterialTheme.typography.titleLarge,
            )
            Button(onClick = onAjustarLimite) {
                Text(text = "Ajustar limite")
            }
        }

        LimiteProgressBar(
            progress = animatedPercentual,
            color    = progressColor,
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            DetalheMetric(
                label    = "Valor utilizado",
                value    = currencyFormatter.format(detalhe.limiteUtilizado),
                modifier = Modifier.weight(1f),
            )
            DetalheMetric(
                label               = "Valor disponível",
                value               = currencyFormatter.format(detalhe.limiteDisponivel),
                modifier            = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            )
        }
    }
}

@Composable
private fun LimiteProgressBar(
    progress : Float,
    color    : Color,
) {
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(shape)
                .background(color),
        )
    }
}

@Composable
private fun DetalheMetric(
    label               : String,
    value               : String,
    modifier            : Modifier = Modifier,
    horizontalAlignment : Alignment.Horizontal = Alignment.Start,
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.extraSmall),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary,
        )
        Text(
            text      = value,
            style     = MaterialTheme.typography.titleMedium,
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun rememberCurrencyFormatter(): NumberFormat =
    remember { NumberFormat.getCurrencyInstance(PtBrLocale) }

private fun tituloCartao(cartao: Cartao): String =
    if (cartao.finalNumero.isBlank()) "Cartão"
    else "Cartão •••• ${cartao.finalNumero}"

private fun limiteProgressColor(percentualUso: Float): Color =
    when {
        percentualUso < 0.50f  -> Color(0xFF2E7D32)
        percentualUso <= 0.80f -> Color(0xFFEF8C00)
        else                   -> Color(0xFFC62828)
    }

// =============================================================================
// Previews
// =============================================================================
@Preview(showBackground = true, name = "Detalhe - Uso baixo")
@Composable
private fun DetalheUsoBaixoPreview() {
    GerenciadorCartoesTheme {
        DetalheContent(uiState = DetalheUiState(detalhe = previewDetalhe(percentualUso = 0.34)))
    }
}

@Preview(showBackground = true, name = "Detalhe - Uso médio")
@Composable
private fun DetalheUsoMedioPreview() {
    GerenciadorCartoesTheme {
        DetalheContent(
            uiState = DetalheUiState(
                detalhe = previewDetalhe(
                    percentualUso = 0.67,
                    cartao        = Cartao(
                        id          = 2L,
                        nomeTitular = "Maria Santos",
                        finalNumero = "5678",
                        bandeira    = "Mastercard",
                        validade    = "08/26",
                        limite      = 10_000.0,
                        template    = "nubank",
                    ),
                    instituicao = "Nubank",
                ),
            ),
        )
    }
}

@Preview(showBackground = true, name = "Detalhe - Uso alto")
@Preview(showBackground = true, name = "Detalhe - Uso alto Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetalheUsoAltoPreview() {
    GerenciadorCartoesTheme {
        DetalheContent(
            uiState = DetalheUiState(
                detalhe = previewDetalhe(
                    percentualUso = 0.86,
                    cartao        = Cartao(
                        id          = 3L,
                        nomeTitular = "Pedro Almeida",
                        finalNumero = "9012",
                        bandeira    = "Elo",
                        validade    = "03/25",
                        limite      = 2_500.0,
                        template    = "inter",
                    ),
                    instituicao = "Inter",
                ),
            ),
        )
    }
}

private fun previewDetalhe(
    percentualUso : Double,
    cartao        : Cartao = Cartao(
        id          = 1L,
        nomeTitular = "João Silva",
        finalNumero = "1234",
        bandeira    = "Visa",
        validade    = "12/28",
        limite      = 5_000.0,
        template    = "itau",
    ),
    instituicao   : String = "Itaú",
): CartaoDetalhe =
    CartaoDetalhe(
        cartao          = cartao,
        instituicao     = InstituicaoFinanceira(instituicao),
        limiteUtilizado = cartao.limite * percentualUso,
    )
