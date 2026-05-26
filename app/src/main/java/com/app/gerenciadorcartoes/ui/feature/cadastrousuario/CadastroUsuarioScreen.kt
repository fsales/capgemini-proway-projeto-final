package com.app.gerenciadorcartoes.ui.feature.cadastrousuario

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.R
import com.app.gerenciadorcartoes.ui.components.AppFormField
import com.app.gerenciadorcartoes.ui.components.AppLoading
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.components.AppSectionCard
import com.app.gerenciadorcartoes.ui.components.AppTopAppBar
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.state.CadastroUsuarioUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalIconSize
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.CadastroUsuarioViewModel

// ── Tier 1: Screen ────────────────────────────────────────────────────────────

@Composable
fun CadastroUsuarioScreen(
    navigateBack    : () -> Unit,
    navigateToLista : (mensagem: String) -> Unit,
    viewModel       : CadastroUsuarioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                CadastroUsuarioUiEvent.NavigateBack        -> navigateBack()
                is CadastroUsuarioUiEvent.NavigateToLista  -> navigateToLista(event.mensagem)
                is CadastroUsuarioUiEvent.MostrarErro      -> snackbarHostState.showSnackbar(event.mensagem)
                is CadastroUsuarioUiEvent.MostrarMensagem  -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    CadastroUsuarioContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
    )
}

// ── Tier 2: Content ───────────────────────────────────────────────────────────

@Composable
fun CadastroUsuarioContent(
    uiState           : CadastroUsuarioUiState = CadastroUsuarioUiState(),
    snackbarHostState : SnackbarHostState      = remember { SnackbarHostState() },
    onEvent           : (CadastroUsuarioEvent) -> Unit = {},
) {
    val spacing      = LocalSpacing.current
    val iconSize     = LocalIconSize.current
    val focusManager = LocalFocusManager.current
    val steps = when {
        uiState.isFluxoExterno || uiState.isModoEdicao ->
            CadastroUsuarioTab.entries.filter { it != CadastroUsuarioTab.Seguranca }
        else ->
            CadastroUsuarioTab.entries
    }

    val nomeFocusRequester           = remember { FocusRequester() }
    val cpfFocusRequester            = remember { FocusRequester() }
    val emailFocusRequester          = remember { FocusRequester() }
    val cepFocusRequester            = remember { FocusRequester() }
    val enderecoFocusRequester       = remember { FocusRequester() }
    val numberFocusRequester         = remember { FocusRequester() }
    val bairroFocusRequester         = remember { FocusRequester() }
    val cidadeFocusRequester         = remember { FocusRequester() }
    val estadoFocusRequester         = remember { FocusRequester() }
    val senhaFocusRequester          = remember { FocusRequester() }
    val confirmarSenhaFocusRequester = remember { FocusRequester() }

    var senhaVisivel          by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }
    var estavaBuscandoCep     by remember { mutableStateOf(false) }
    var cpfFieldValue by remember { mutableStateOf(TextFieldValue(uiState.cpf)) }
    var cepFieldValue by remember { mutableStateOf(TextFieldValue(uiState.cep)) }

    LaunchedEffect(uiState.etapaAtual) {
        when (steps[uiState.etapaAtual]) {
            CadastroUsuarioTab.DadosPessoais -> nomeFocusRequester.requestFocus()
            CadastroUsuarioTab.Endereco      -> cepFocusRequester.requestFocus()
            CadastroUsuarioTab.Seguranca     -> senhaFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(uiState.buscandoCep) {
        if (estavaBuscandoCep && !uiState.buscandoCep && uiState.erroCep == null) {
            numberFocusRequester.requestFocus()
        }
        estavaBuscandoCep = uiState.buscandoCep
    }

    LaunchedEffect(uiState.focarPrimeiroCampoComErro) {
        if (!uiState.focarPrimeiroCampoComErro) return@LaunchedEffect
        focarPrimeiroCampoComErro(
            etapaAtual               = uiState.etapaAtual,
            uiState                  = uiState,
            steps                    = steps,
            nomeFocusRequester       = nomeFocusRequester,
            cpfFocusRequester        = cpfFocusRequester,
            emailFocusRequester      = emailFocusRequester,
            cepFocusRequester        = cepFocusRequester,
            enderecoFocusRequester   = enderecoFocusRequester,
            numberFocusRequester     = numberFocusRequester,
            bairroFocusRequester     = bairroFocusRequester,
            cidadeFocusRequester     = cidadeFocusRequester,
            estadoFocusRequester     = estadoFocusRequester,
            senhaFocusRequester      = senhaFocusRequester,
            confirmarSenhaFocusRequester = confirmarSenhaFocusRequester,
        )
        onEvent(CadastroUsuarioEvent.FocoRealizado)
    }

    LaunchedEffect(uiState.cpf) {
        if (cpfFieldValue.text != uiState.cpf)
            cpfFieldValue = TextFieldValue(uiState.cpf, TextRange(uiState.cpf.length))
    }
    LaunchedEffect(uiState.cep) {
        if (cepFieldValue.text != uiState.cep)
            cepFieldValue = TextFieldValue(uiState.cep, TextRange(uiState.cep.length))
    }

    AppScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            AppTopAppBar(
                title          = stringResource(R.string.app_name),
                subtitle       = stringResource(R.string.cadastro_usuario_subtitulo),
                large          = true,
                onNavigateBack = { onEvent(CadastroUsuarioEvent.Voltar) },
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
        ) {
            val contentWidth = if (maxWidth >= 600.dp) 560.dp else maxWidth

            AnimatedContent(
                targetState    = uiState.carregando,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label          = "CadastroCarregando",
            ) { carregando ->
            if (carregando) {
                AppLoading()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                                    MaterialTheme.colorScheme.background,
                                ),
                            ),
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = spacing.medium, vertical = spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = contentWidth),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium),
                    ) {
                        Surface(
                            shape           = MaterialTheme.shapes.large,
                            tonalElevation  = spacing.small,
                            shadowElevation = spacing.extraSmall,
                            modifier        = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier                = Modifier.padding(spacing.medium),
                                verticalArrangement     = Arrangement.spacedBy(spacing.small),
                                horizontalAlignment     = Alignment.CenterHorizontally,
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Box(
                                        modifier         = Modifier
                                            .size(iconSize.extraLarge * 2)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector        = Icons.Default.Person,
                                            contentDescription = null,
                                            tint               = MaterialTheme.colorScheme.onPrimary,
                                            modifier           = Modifier.size(iconSize.large),
                                        )
                                    }
                                    if (uiState.isFluxoExterno) {
                                        Box(
                                            modifier         = Modifier
                                                .size(iconSize.large)
                                                .background(Color.White, CircleShape)
                                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Image(
                                                painter            = painterResource(R.drawable.ic_google_logo),
                                                contentDescription = null,
                                                modifier           = Modifier.size(iconSize.small),
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text  = stringResource(
                                        if (uiState.isFluxoExterno) R.string.cadastro_usuario_externo_titulo
                                        else                        R.string.cadastro_usuario_header_titulo
                                    ),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text  = stringResource(
                                        if (uiState.isFluxoExterno) R.string.cadastro_usuario_externo_subtitulo
                                        else                        R.string.cadastro_usuario_header_subtitulo
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Surface(
                            shape           = MaterialTheme.shapes.large,
                            tonalElevation  = spacing.small,
                            shadowElevation = spacing.extraSmall,
                            modifier        = Modifier.fillMaxWidth(),
                        ) {
                            Column {
                                CadastroUsuarioStepperHeader(
                                    steps          = steps,
                                    etapaAtual     = uiState.etapaAtual,
                                    temErroNaEtapa = { step ->
                                        when (step) {
                                            CadastroUsuarioTab.DadosPessoais -> uiState.temErroNaEtapa0
                                            CadastroUsuarioTab.Endereco      -> uiState.temErroNaEtapa1
                                            CadastroUsuarioTab.Seguranca     -> uiState.temErroNaEtapa2
                                        }
                                    },
                                    onStepClick = { index ->
                                        if (index < uiState.etapaAtual) {
                                            repeat(uiState.etapaAtual - index) {
                                                onEvent(CadastroUsuarioEvent.VoltarEtapa)
                                            }
                                        }
                                    },
                                )

                                AnimatedContent(
                                    targetState = steps[uiState.etapaAtual],
                                    label       = "CadastroUsuarioTabContent",
                                ) { tab ->
                                    when (tab) {

                                        CadastroUsuarioTab.DadosPessoais -> AppSectionCard(
                                            title    = stringResource(R.string.cadastro_usuario_secao_dados_pessoais),
                                            subtitle = stringResource(R.string.cadastro_usuario_secao_dados_pessoais_subtitulo),
                                        ) {
                                            AppFormField(
                                                value           = uiState.nome,
                                                onValueChange   = { onEvent(CadastroUsuarioEvent.NomeAlterado(it)) },
                                                label           = stringResource(R.string.cadastro_usuario_nome),
                                                errorMessage    = uiState.erroNome,
                                                focusRequester  = nomeFocusRequester,
                                                leadingIcon     = { Icon(Icons.Default.Person, null) },
                                                keyboardActions = KeyboardActions(onNext = { cpfFocusRequester.requestFocus() }),
                                                keyboardOptions = KeyboardOptions(
                                                    capitalization = KeyboardCapitalization.Words,
                                                    keyboardType   = KeyboardType.Text,
                                                    imeAction      = ImeAction.Next,
                                                ),
                                            )
                                            if (uiState.isFluxoExterno && uiState.nome.isNotBlank()) {
                                                Text(
                                                    text     = stringResource(R.string.cadastro_usuario_nome_google),
                                                    style    = MaterialTheme.typography.bodySmall,
                                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.fillMaxWidth(),
                                                )
                                            }
                                            AppFormField(
                                                value                = cpfFieldValue,
                                                onValueChange        = { newValue ->
                                                    val digits = newValue.text
                                                        .filter { it.isDigit() }.take(11)
                                                    val cursor = newValue.selection.start
                                                        .coerceAtMost(digits.length)
                                                    cpfFieldValue = TextFieldValue(
                                                        text      = digits,
                                                        selection = TextRange(cursor),
                                                    )
                                                    onEvent(CadastroUsuarioEvent.CpfAlterado(digits))
                                                },
                                                label                = stringResource(R.string.cadastro_usuario_cpf),
                                                hintText             = stringResource(R.string.cadastro_usuario_cpf_hint),
                                                errorMessage         = uiState.erroCpf,
                                                focusRequester       = cpfFocusRequester,
                                                visualTransformation = CpfVisualTransformation,
                                                keyboardActions      = KeyboardActions(onNext = { emailFocusRequester.requestFocus() }),
                                                keyboardOptions      = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction    = ImeAction.Next,
                                                ),
                                            )
                                            AppFormField(
                                                value           = uiState.email,
                                                onValueChange   = { onEvent(CadastroUsuarioEvent.EmailAlterado(it)) },
                                                label           = stringResource(R.string.cadastro_usuario_email),
                                                errorMessage    = uiState.erroEmail,
                                                focusRequester  = emailFocusRequester,
                                                leadingIcon     = { Icon(Icons.Default.Email, null) },
                                                readOnly        = uiState.isFluxoExterno || uiState.isModoEdicao,
                                                showValidationIcon = !uiState.isFluxoExterno && !uiState.isModoEdicao,
                                                trailingIcon    = if (uiState.isFluxoExterno || uiState.isModoEdicao) {
                                                    {
                                                        Icon(
                                                            imageVector        = Icons.Default.Lock,
                                                            contentDescription = null,
                                                            tint               = MaterialTheme.colorScheme.outline,
                                                        )
                                                    }
                                                } else null,
                                                keyboardActions = KeyboardActions(onNext = { onEvent(CadastroUsuarioEvent.AvancarEtapa) }),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Email,
                                                    imeAction    = ImeAction.Next,
                                                ),
                                            )
                                            if (uiState.isFluxoExterno) {
                                                Text(
                                                    text     = stringResource(R.string.cadastro_usuario_email_google),
                                                    style    = MaterialTheme.typography.bodySmall,
                                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.fillMaxWidth(),
                                                )
                                            }
                                        }

                                        CadastroUsuarioTab.Endereco -> AppSectionCard(
                                            title    = stringResource(R.string.cadastro_usuario_secao_endereco),
                                            subtitle = stringResource(R.string.cadastro_usuario_cep_dica),
                                        ) {
                                            AppFormField(
                                                value                = cepFieldValue,
                                                onValueChange        = { newValue ->
                                                    val digits = newValue.text
                                                        .filter { it.isDigit() }.take(8)
                                                    val cursor = newValue.selection.start
                                                        .coerceAtMost(digits.length)
                                                    cepFieldValue = TextFieldValue(
                                                        text      = digits,
                                                        selection = TextRange(cursor),
                                                    )
                                                    onEvent(CadastroUsuarioEvent.CepAlterado(digits))
                                                },
                                                label                = stringResource(R.string.cadastro_usuario_cep),
                                                hintText             = stringResource(R.string.cadastro_usuario_cep_hint),
                                                errorMessage         = uiState.erroCep,
                                                focusRequester       = cepFocusRequester,
                                                visualTransformation = CepVisualTransformation,
                                                showValidationIcon   = !uiState.buscandoCep,
                                                trailingIcon       = {
                                                    if (uiState.buscandoCep) {
                                                        CircularProgressIndicator(
                                                            modifier    = Modifier
                                                                .padding(spacing.extraSmall)
                                                                .size(iconSize.small),
                                                            strokeWidth = 2.dp,
                                                            color       = MaterialTheme.colorScheme.primary,
                                                        )
                                                    }
                                                },
                                                keyboardActions = KeyboardActions(onNext = { enderecoFocusRequester.requestFocus() }),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction    = ImeAction.Next,
                                                ),
                                            )
                                            if (uiState.buscandoCep) {
                                                Text(
                                                    text  = stringResource(R.string.cadastro_usuario_buscando_cep),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                            AppFormField(
                                                value           = uiState.endereco,
                                                onValueChange   = { onEvent(CadastroUsuarioEvent.EnderecoAlterado(it)) },
                                                label           = stringResource(R.string.cadastro_usuario_endereco),
                                                errorMessage    = uiState.erroEndereco,
                                                focusRequester  = enderecoFocusRequester,
                                                readOnly        = uiState.buscandoCep,
                                                keyboardActions = KeyboardActions(onNext = { numberFocusRequester.requestFocus() }),
                                                keyboardOptions = KeyboardOptions(
                                                    capitalization = KeyboardCapitalization.Words,
                                                    keyboardType   = KeyboardType.Text,
                                                    imeAction      = ImeAction.Next,
                                                ),
                                            )
                                            AppFormField(
                                                value           = uiState.number,
                                                onValueChange   = { onEvent(CadastroUsuarioEvent.NumberAlterado(it)) },
                                                label           = stringResource(R.string.cadastro_usuario_numero),
                                                errorMessage    = uiState.erroNumber,
                                                focusRequester  = numberFocusRequester,
                                                keyboardActions = KeyboardActions(onNext = { bairroFocusRequester.requestFocus() }),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction    = ImeAction.Next,
                                                ),
                                            )
                                            AppFormField(
                                                value           = uiState.bairro,
                                                onValueChange   = { onEvent(CadastroUsuarioEvent.BairroAlterado(it)) },
                                                label           = stringResource(R.string.cadastro_usuario_bairro),
                                                errorMessage    = uiState.erroBairro,
                                                focusRequester  = bairroFocusRequester,
                                                readOnly        = uiState.buscandoCep,
                                                keyboardActions = KeyboardActions(onNext = { cidadeFocusRequester.requestFocus() }),
                                                keyboardOptions = KeyboardOptions(
                                                    capitalization = KeyboardCapitalization.Words,
                                                    keyboardType   = KeyboardType.Text,
                                                    imeAction      = ImeAction.Next,
                                                ),
                                            )
                                            AppFormField(
                                                value           = uiState.cidade,
                                                onValueChange   = { onEvent(CadastroUsuarioEvent.CidadeAlterada(it)) },
                                                label           = stringResource(R.string.cadastro_usuario_cidade),
                                                errorMessage    = uiState.erroCidade,
                                                focusRequester  = cidadeFocusRequester,
                                                readOnly        = uiState.buscandoCep,
                                                keyboardActions = KeyboardActions(onNext = { estadoFocusRequester.requestFocus() }),
                                                keyboardOptions = KeyboardOptions(
                                                    capitalization = KeyboardCapitalization.Words,
                                                    keyboardType   = KeyboardType.Text,
                                                    imeAction      = ImeAction.Next,
                                                ),
                                            )
                                            AppFormField(
                                                value           = uiState.estado,
                                                onValueChange   = { onEvent(CadastroUsuarioEvent.EstadoAlterado(it)) },
                                                label           = stringResource(R.string.cadastro_usuario_estado),
                                                hintText        = stringResource(R.string.cadastro_usuario_estado_hint),
                                                errorMessage    = uiState.erroEstado,
                                                focusRequester  = estadoFocusRequester,
                                                readOnly        = uiState.buscandoCep,
                                                keyboardActions = KeyboardActions(onNext = { onEvent(CadastroUsuarioEvent.AvancarEtapa) }),
                                                keyboardOptions = KeyboardOptions(
                                                    capitalization = KeyboardCapitalization.Characters,
                                                    keyboardType   = KeyboardType.Text,
                                                    imeAction      = ImeAction.Next,
                                                ),
                                            )
                                        }

                                        CadastroUsuarioTab.Seguranca -> AppSectionCard(
                                            title    = stringResource(R.string.cadastro_usuario_secao_seguranca),
                                            subtitle = stringResource(R.string.cadastro_usuario_secao_seguranca_subtitulo),
                                        ) {
                                            AppFormField(
                                                value                 = uiState.senha,
                                                onValueChange         = { onEvent(CadastroUsuarioEvent.SenhaAlterada(it)) },
                                                label                 = stringResource(R.string.cadastro_usuario_senha),
                                                hintText              = stringResource(R.string.cadastro_usuario_senha_hint),
                                                errorMessage          = uiState.erroSenha,
                                                focusRequester        = senhaFocusRequester,
                                                leadingIcon           = { Icon(Icons.Default.Lock, null) },
                                                trailingIcon          = {
                                                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                                        Icon(
                                                            imageVector        = if (senhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                            contentDescription = stringResource(
                                                                if (senhaVisivel) R.string.cadastro_usuario_cd_ocultar_senha
                                                                else R.string.cadastro_usuario_cd_mostrar_senha,
                                                            ),
                                                        )
                                                    }
                                                },
                                                visualTransformation  = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                                                keyboardActions       = KeyboardActions(onNext = { confirmarSenhaFocusRequester.requestFocus() }),
                                                keyboardOptions       = KeyboardOptions(
                                                    keyboardType = KeyboardType.Password,
                                                    imeAction    = ImeAction.Next,
                                                ),
                                            )
                                            AppFormField(
                                                value                 = uiState.confirmarSenha,
                                                onValueChange         = { onEvent(CadastroUsuarioEvent.ConfirmarSenhaAlterada(it)) },
                                                label                 = stringResource(R.string.cadastro_usuario_confirmar_senha),
                                                errorMessage          = uiState.erroConfirmarSenha,
                                                focusRequester        = confirmarSenhaFocusRequester,
                                                leadingIcon           = { Icon(Icons.Default.Lock, null) },
                                                trailingIcon          = {
                                                    IconButton(onClick = { confirmarSenhaVisivel = !confirmarSenhaVisivel }) {
                                                        Icon(
                                                            imageVector        = if (confirmarSenhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                            contentDescription = stringResource(
                                                                if (confirmarSenhaVisivel) R.string.cadastro_usuario_cd_ocultar_senha
                                                                else R.string.cadastro_usuario_cd_mostrar_senha,
                                                            ),
                                                        )
                                                    }
                                                },
                                                visualTransformation  = if (confirmarSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                                                keyboardActions       = KeyboardActions(onDone = {
                                                    focusManager.clearFocus()
                                                    onEvent(CadastroUsuarioEvent.AvancarEtapa)
                                                }),
                                                keyboardOptions       = KeyboardOptions(
                                                    keyboardType = KeyboardType.Password,
                                                    imeAction    = ImeAction.Done,
                                                ),
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider()

                                val isUltimoPasso = uiState.etapaAtual == steps.lastIndex
                                val proximaAcao = if (isUltimoPasso) {
                                    stringResource(R.string.cadastro_usuario_microtexto_acao_final)
                                } else {
                                    stringResource(
                                        R.string.cadastro_usuario_microtexto_proxima_acao,
                                        stringResource(steps[uiState.etapaAtual + 1].titleRes),
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = spacing.medium, vertical = spacing.medium),
                                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                                ) {
                                    OutlinedButton(
                                        onClick  = { onEvent(CadastroUsuarioEvent.VoltarEtapa) },
                                        enabled  = uiState.etapaAtual > 0,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text(stringResource(R.string.cadastro_usuario_btn_anterior))
                                    }

                                    Button(
                                        onClick  = {
                                            focusManager.clearFocus()
                                            onEvent(CadastroUsuarioEvent.AvancarEtapa)
                                        },
                                        enabled  = !uiState.buscandoCep,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text(
                                            stringResource(
                                                when {
                                                    isUltimoPasso && uiState.isModoEdicao -> R.string.cadastro_usuario_btn_salvar
                                                    isUltimoPasso                         -> R.string.cadastro_usuario_btn_cadastrar
                                                    else                                  -> R.string.cadastro_usuario_btn_proximo
                                                }
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text     = proximaAcao,
                                    style    = MaterialTheme.typography.bodySmall,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = spacing.medium)
                                        .padding(bottom = spacing.medium),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.small))
                    }
                }
            }
            } // fecha AnimatedContent
        }
    }
}

private fun focarPrimeiroCampoComErro(
    etapaAtual               : Int,
    uiState                  : CadastroUsuarioUiState,
    steps                    : List<CadastroUsuarioTab>,
    nomeFocusRequester       : FocusRequester,
    cpfFocusRequester        : FocusRequester,
    emailFocusRequester      : FocusRequester,
    cepFocusRequester        : FocusRequester,
    enderecoFocusRequester   : FocusRequester,
    numberFocusRequester     : FocusRequester,
    bairroFocusRequester     : FocusRequester,
    cidadeFocusRequester     : FocusRequester,
    estadoFocusRequester     : FocusRequester,
    senhaFocusRequester      : FocusRequester,
    confirmarSenhaFocusRequester: FocusRequester,
) {
    when (steps[etapaAtual]) {
        CadastroUsuarioTab.DadosPessoais -> when {
            uiState.nome.isBlank()  || uiState.erroNome  != null -> nomeFocusRequester.requestFocus()
            uiState.cpf.isBlank()   || uiState.erroCpf   != null -> cpfFocusRequester.requestFocus()
            uiState.email.isBlank() || uiState.erroEmail != null -> emailFocusRequester.requestFocus()
            else                                                  -> Unit
        }
        CadastroUsuarioTab.Endereco -> when {
            uiState.buscandoCep                                         -> Unit
            uiState.cep.isBlank()      || uiState.erroCep      != null -> cepFocusRequester.requestFocus()
            uiState.endereco.isBlank() || uiState.erroEndereco != null -> enderecoFocusRequester.requestFocus()
            uiState.number.isBlank()   || uiState.erroNumber   != null -> numberFocusRequester.requestFocus()
            uiState.bairro.isBlank()   || uiState.erroBairro   != null -> bairroFocusRequester.requestFocus()
            uiState.cidade.isBlank()   || uiState.erroCidade   != null -> cidadeFocusRequester.requestFocus()
            uiState.estado.isBlank()   || uiState.erroEstado   != null -> estadoFocusRequester.requestFocus()
            else                                                        -> Unit
        }
        CadastroUsuarioTab.Seguranca -> when {
            uiState.senha.isBlank()          || uiState.erroSenha          != null -> senhaFocusRequester.requestFocus()
            uiState.confirmarSenha.isBlank() || uiState.erroConfirmarSenha != null -> confirmarSenhaFocusRequester.requestFocus()
            else                                                                    -> Unit
        }
    }
}

// ── Tier 3: Previews ──────────────────────────────────────────────────────────

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
                cpf            = "12345678900",
                cep            = "01310100",
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

private object CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(11)
        val out = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 3 || i == 6) append('.')
                if (i == 9) append('-')
                append(c)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                return when {
                    o <= 2 -> o
                    o <= 5 -> o + 1
                    o <= 8 -> o + 2
                    else   -> o + 3
                }.coerceAtMost(out.length)
            }
            override fun transformedToOriginal(offset: Int): Int {
                val o = offset.coerceIn(0, out.length)
                return when {
                    o <= 3  -> o
                    o <= 7  -> o - 1
                    o <= 11 -> o - 2
                    else    -> o - 3
                }.coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

private object CepVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(8)
        val out = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 5) append('-')
                append(c)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                return when {
                    o <= 4 -> o
                    else   -> o + 1
                }.coerceAtMost(out.length)
            }
            override fun transformedToOriginal(offset: Int): Int {
                val o = offset.coerceIn(0, out.length)
                return when {
                    o <= 5 -> o
                    else   -> o - 1
                }.coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

