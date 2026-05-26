package com.app.gerenciadorcartoes.ui.feature.ajustarlimite

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.ui.components.AppLoading
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.components.AppTopAppBar
import com.app.gerenciadorcartoes.ui.feature.ajustarlimite.state.AjustarLimiteUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.AjustarLimiteViewModel
import java.text.NumberFormat
import java.util.Locale

private val PtBrLocale: Locale = Locale.forLanguageTag("pt-BR")
private const val LimiteMinimoSlider = 1.0
private const val LimiteMaximoSlider = 10_000.0

// =============================================================================
// Screen — ponto de entrada; coleta ViewModel e roteia UiEvent.
// =============================================================================
@Composable
fun AjustarLimiteScreen(
    navigateBack : () -> Unit,
    viewModel    : AjustarLimiteViewModel = hiltViewModel(),
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(onBack = navigateBack)

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                AjustarLimiteUiEvent.NavigateBack       -> navigateBack()
                is AjustarLimiteUiEvent.MostrarMensagem -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    AjustarLimiteContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
    )
}

// =============================================================================
// Content — renderiza estado puro; sem ViewModel (testável).
// =============================================================================
@Composable
fun AjustarLimiteContent(
    uiState           : AjustarLimiteUiState = AjustarLimiteUiState(),
    snackbarHostState : SnackbarHostState    = remember { SnackbarHostState() },
    onEvent           : (AjustarLimiteEvent) -> Unit = {},
) {
    val currencyFormatter = rememberCurrencyFormatter()

    AppScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            AppTopAppBar(
                title          = "Ajustar limite",
                onNavigateBack = { onEvent(AjustarLimiteEvent.Voltar) },
            )
        },
    ) { paddingValues ->
        when {
            uiState.carregando -> AppLoading(modifier = Modifier.padding(paddingValues))
            else               -> AjustarLimiteBody(
                uiState           = uiState,
                paddingValues     = paddingValues,
                currencyFormatter = currencyFormatter,
                onEvent           = onEvent,
            )
        }
    }

    if (uiState.mostrarConfirmacao) {
        ConfirmarLimiteDialog(
            limiteAtual       = currencyFormatter.format(uiState.limiteAtual),
            novoLimite        = currencyFormatter.format(uiState.limiteConfirmacao),
            onConfirmar       = { onEvent(AjustarLimiteEvent.ConfirmarSalvar) },
            onCancelar        = { onEvent(AjustarLimiteEvent.CancelarConfirmacao) },
        )
    }
}

// =============================================================================
// Body
// =============================================================================
@Composable
private fun AjustarLimiteBody(
    uiState           : AjustarLimiteUiState,
    paddingValues     : PaddingValues,
    currencyFormatter : NumberFormat,
    onEvent           : (AjustarLimiteEvent) -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        LimiteAtualCard(
            limiteAtual       = currencyFormatter.format(uiState.limiteAtual),
            cartaoFinalNumero = uiState.cartaoFinalNumero,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            LimiteSliderCard(
                uiState           = uiState,
                currencyFormatter = currencyFormatter,
                onValueChange     = { onEvent(AjustarLimiteEvent.NovoLimiteAlterado(it)) },
            )

            if (uiState.erroLimite != null) {
                Text(
                    text  = uiState.erroLimite,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (uiState.mensagem != null) {
                MensagemSucesso(text = uiState.mensagem)
            }

            if (uiState.aviso != null) {
                MensagemAviso(text = uiState.aviso)
            }
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            OutlinedButton(
                onClick  = { onEvent(AjustarLimiteEvent.Voltar) },
                modifier = Modifier.weight(1f),
                enabled  = !uiState.salvando,
            ) {
                Text("Cancelar")
            }
            Button(
                onClick  = { onEvent(AjustarLimiteEvent.Salvar) },
                modifier = Modifier.weight(1f),
                enabled  = !uiState.salvando && uiState.cartaoEncontrado,
            ) {
                Icon(
                    imageVector        = Icons.Default.Check,
                    contentDescription = null,
                )
                Text(
                    text     = if (uiState.salvando) "Salvando" else "Salvar",
                    modifier = Modifier.padding(start = spacing.small),
                )
            }
        }
    }
}

@Composable
private fun LimiteSliderCard(
    uiState           : AjustarLimiteUiState,
    currencyFormatter : NumberFormat,
    onValueChange     : (Double) -> Unit,
) {
    val spacing       = LocalSpacing.current
    val limiteMaximo  = limiteMaximoSlider(uiState)
    val sliderValue   = uiState.novoLimite.coerceIn(LimiteMinimoSlider, limiteMaximo)
    var editandoManual by remember { mutableStateOf(false) }
    var textoManual by remember { mutableStateOf(sliderValue.formatarEntradaMonetaria()) }
    var validacaoManual by remember { mutableStateOf<ValidacaoManual?>(null) }
    val focusRequester    = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val animatedValue by animateFloatAsState(
        targetValue   = sliderValue.toFloat(),
        animationSpec = tween(durationMillis = 220),
        label         = "limiteSelecionado",
    )

    LaunchedEffect(sliderValue, editandoManual) {
        if (!editandoManual) {
            textoManual = sliderValue.formatarEntradaMonetaria()
        }
    }

    LaunchedEffect(editandoManual) {
        if (editandoManual) {
            textoManual = sliderValue.formatarEntradaMonetaria()
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    if (validacaoManual != null) {
        ValidacaoManualDialog(
            validacao = validacaoManual!!,
            onDismiss = { validacaoManual = null },
        )
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier            = Modifier.padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text       = "Novo limite",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = { editandoManual = true },
                    enabled = !uiState.salvando,
                ) {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = "Digitar limite manualmente",
                    )
                }
            }
            if (editandoManual) {
                OutlinedTextField(
                    value         = textoManual,
                    onValueChange = { novoTexto ->
                        val textoFiltrado = novoTexto.filtrarEntradaMonetaria()
                        val valorDigitado = textoFiltrado.toValorMonetarioOrNull()

                        when {
                            textoFiltrado != novoTexto -> Unit
                            valorDigitado != null && valorDigitado <= 0.0 -> {
                                textoManual = sliderValue.formatarEntradaMonetaria()
                                validacaoManual = ValidacaoManual.ValorMinimo
                            }
                            valorDigitado != null && valorDigitado > LimiteMaximoSlider -> {
                                textoManual = sliderValue.formatarEntradaMonetaria()
                                validacaoManual = ValidacaoManual.ValorMaximo
                            }
                            else -> {
                                textoManual = textoFiltrado
                                valorDigitado?.let(onValueChange)
                            }
                        }
                    },
                    prefix        = { Text("R$") },
                    singleLine    = true,
                    isError       = textoManual.isNotBlank() && textoManual.toValorMonetarioOrNull() == null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction    = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            editandoManual = false
                            keyboardController?.hide()
                        },
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color     = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            } else {
                Text(
                    text      = currencyFormatter.format(animatedValue.toDouble()),
                    style     = MaterialTheme.typography.headlineMedium,
                    color     = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth(),
                )
            }
            Slider(
                value         = sliderValue.toFloat(),
                onValueChange = { value ->
                    val novoValor = value.toDouble().arredondarParaPasso()
                    onValueChange(novoValor)
                    if (editandoManual) {
                        textoManual = novoValor.formatarEntradaMonetaria()
                    }
                },
                valueRange    = LimiteMinimoSlider.toFloat()..limiteMaximo.toFloat(),
                enabled       = !uiState.salvando,
                modifier      = Modifier.fillMaxWidth(),
            )
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text  = currencyFormatter.format(LimiteMinimoSlider),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                )
                Text(
                    text  = currencyFormatter.format(limiteMaximo),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                )
            }
        }
    }
}

@Composable
private fun ValidacaoManualDialog(
    validacao : ValidacaoManual,
    onDismiss : () -> Unit,
) {
    val mensagem = when (validacao) {
        ValidacaoManual.ValorMaximo -> "O limite máximo permitido é R$ 10.000,00."
        ValidacaoManual.ValorMinimo -> "O limite precisa ser maior que zero."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title            = { Text("Valor inválido") },
        text             = { Text(mensagem) },
        confirmButton    = {
            Button(onClick = onDismiss) {
                Text("Entendi")
            }
        },
    )
}

@Composable
private fun LimiteAtualCard(
    limiteAtual       : String,
    cartaoFinalNumero : String,
) {
    val spacing = LocalSpacing.current
    val cartao  = if (cartaoFinalNumero.isBlank()) "Cartão" else "Cartão •••• $cartaoFinalNumero"

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier            = Modifier.padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Text(
                text  = cartao,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
            )
            Text(
                text       = "Limite atual",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text  = limiteAtual,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MensagemAviso(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.errorContainer,
    ) {
        Text(
            text     = text,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(LocalSpacing.current.medium),
        )
    }
}

@Composable
private fun MensagemSucesso(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text     = text,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(LocalSpacing.current.medium),
        )
    }
}

@Composable
private fun ConfirmarLimiteDialog(
    limiteAtual : String,
    novoLimite  : String,
    onConfirmar : () -> Unit,
    onCancelar  : () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title            = { Text("Confirmar ajuste de limite") },
        text             = {
            Column(
                verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.small),
            ) {
                Text("Limite atual: $limiteAtual")
                Text("Novo limite: $novoLimite")
            }
        },
        confirmButton    = {
            Button(onClick = onConfirmar) {
                Text("Confirmar")
            }
        },
        dismissButton    = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
private fun rememberCurrencyFormatter(): NumberFormat =
    remember { NumberFormat.getCurrencyInstance(PtBrLocale) }

private fun limiteMaximoSlider(uiState: AjustarLimiteUiState): Double =
    LimiteMaximoSlider

private fun Double.arredondarParaPasso(): Double =
    maxOf(LimiteMinimoSlider, kotlin.math.round(this / 50.0) * 50.0)

private fun Double.formatarEntradaMonetaria(): String =
    String.format(Locale.US, "%.2f", this)

private fun String.toValorMonetarioOrNull(): Double? {
    val semMoeda = trim()
        .replace("R$", "")
        .replace(" ", "")
        .replace("\u00A0", "")

    if (semMoeda.isBlank()) return null

    val normalizado = if (semMoeda.contains(",")) {
        semMoeda.replace(".", "").replace(",", ".")
    } else if (semMoeda.temPontosDeMilhar()) {
        semMoeda.replace(".", "")
    } else {
        semMoeda
    }

    return normalizado.toDoubleOrNull()
}

private fun String.filtrarEntradaMonetaria(): String {
    val builder = StringBuilder()
    var temSeparador = false

    forEachIndexed { index, char ->
        when {
            char.isDigit() -> builder.append(char)
            char == '-' && index == 0 && !builder.contains("-") -> builder.append(char)
            (char == ',' || char == '.') && !temSeparador -> {
                builder.append(char)
                temSeparador = true
            }
        }
    }

    return builder.toString()
}

private fun String.temPontosDeMilhar(): Boolean {
    val grupos = split(".")
    return grupos.size > 1 &&
        grupos.first().length in 1..3 &&
        grupos.first().all(Char::isDigit) &&
        grupos.drop(1).all { it.length == 3 && it.all(Char::isDigit) }
}

private enum class ValidacaoManual {
    ValorMaximo,
    ValorMinimo,
}

// =============================================================================
// Previews
// =============================================================================
@Preview(showBackground = true, name = "Ajustar limite")
@Preview(showBackground = true, name = "Ajustar limite Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AjustarLimitePreview() {
    GerenciadorCartoesTheme {
        AjustarLimiteContent(
            uiState = AjustarLimiteUiState(
                limiteAtual       = 5_000.0,
                novoLimite        = 7_500.0,
                cartaoFinalNumero = "1234",
            ),
        )
    }
}

@Preview(showBackground = true, name = "Ajustar limite - erro")
@Composable
private fun AjustarLimiteErroPreview() {
    GerenciadorCartoesTheme {
        AjustarLimiteContent(
            uiState = AjustarLimiteUiState(
                limiteAtual       = 5_000.0,
                novoLimite        = 100.0,
                cartaoFinalNumero = "1234",
                erroLimite        = "O limite deve ser maior que zero.",
            ),
        )
    }
}
