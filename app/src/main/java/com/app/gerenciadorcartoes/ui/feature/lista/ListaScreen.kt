package com.app.gerenciadorcartoes.ui.feature.lista

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gerenciadorcartoes.R
import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.model.ChatMessage
import com.app.gerenciadorcartoes.model.ChatSender
import com.app.gerenciadorcartoes.ui.components.AppLoading
import com.app.gerenciadorcartoes.ui.components.AppScaffold
import com.app.gerenciadorcartoes.ui.components.AppTopAppBar
import com.app.gerenciadorcartoes.ui.components.CartaoTemplateCard
import com.app.gerenciadorcartoes.ui.components.ConfirmacaoDialog
import com.app.gerenciadorcartoes.ui.components.EmptyState
import com.app.gerenciadorcartoes.ui.feature.lista.chat.ChatEvent
import com.app.gerenciadorcartoes.ui.feature.lista.chat.state.ChatUiState
import com.app.gerenciadorcartoes.ui.feature.lista.state.ListaUiState
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalIconSize
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing
import com.app.gerenciadorcartoes.viewmodel.ChatViewModel
import com.app.gerenciadorcartoes.viewmodel.ListaViewModel

// =============================================================================
// Screen — ponto de entrada; coleta ViewModel e roteia UiEvent.
// =============================================================================
@Composable
fun ListaScreen(
    onNavigateToNovo   : () -> Unit,
    onNavigateToItem   : (id: Long) -> Unit,
    onNavigateToEditar : (id: Long) -> Unit,
    onDeslogar         : () -> Unit,
    onNavigateToPerfil : (userId: String) -> Unit,
    viewModel          : ListaViewModel = hiltViewModel(),
    chatViewModel      : ChatViewModel = hiltViewModel(),
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val chatUiState      by chatViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ListaUiEvent.NavegaParaItem   -> onNavigateToItem(event.id)
                is ListaUiEvent.NavegaParaEditar -> onNavigateToEditar(event.id)
                ListaUiEvent.NavegaParaNovo      -> onNavigateToNovo()
                ListaUiEvent.NavegaParaLogin     -> onDeslogar()
                is ListaUiEvent.NavegaParaPerfil -> onNavigateToPerfil(event.userId)
                is ListaUiEvent.MostrarErro      -> snackbarHostState.showSnackbar(event.mensagem)
                is ListaUiEvent.MostrarMensagem  -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    ListaContent(
        uiState           = uiState,
        chatUiState       = chatUiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
        onChatEvent       = chatViewModel::onEvent,
    )
}

// =============================================================================
// Content — renderiza estado puro; sem ViewModel (testável).
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaContent(
    uiState           : ListaUiState      = ListaUiState(),
    chatUiState       : ChatUiState       = ChatUiState(),
    snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
    onEvent           : (ListaEvent) -> Unit = {},
    onChatEvent       : (ChatEvent) -> Unit = {},
) {
    var cartaoParaExcluir by remember { mutableStateOf<Cartao?>(null) }
    var cartaoParaAcoes   by remember { mutableStateOf<Cartao?>(null) }
    var confirmarDeslogar by remember { mutableStateOf(false) }
    var menuExpandido     by remember { mutableStateOf(false) }
    val spacing  = LocalSpacing.current
    val iconSize = LocalIconSize.current

    // ── Diálogo: confirmar logout ─────────────────────────────────────────────
    if (confirmarDeslogar) {
        ConfirmacaoDialog(
            titulo        = stringResource(R.string.lista_dialog_deslogar_titulo),
            mensagem      = stringResource(R.string.lista_dialog_deslogar_mensagem),
            textConfirmar = stringResource(R.string.lista_dialog_deslogar_confirmar),
            onConfirmar   = { onEvent(ListaEvent.Deslogar) },
            onDismiss     = { confirmarDeslogar = false },
        )
    }

    cartaoParaExcluir?.let { cartao ->
        ConfirmacaoDialog(
            titulo        = stringResource(R.string.lista_dialog_excluir_titulo),
            mensagem      = stringResource(R.string.lista_dialog_excluir_mensagem, cartao.nomeTitular),
            textConfirmar = stringResource(R.string.lista_dialog_excluir_confirmar),
            onConfirmar   = { onEvent(ListaEvent.ExcluirCartao(cartao.id)) },
            onDismiss     = { cartaoParaExcluir = null },
        )
    }

    cartaoParaAcoes?.let { cartaoSelecionado ->
        ModalBottomSheet(
            onDismissRequest = { cartaoParaAcoes = null },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.medium),
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.lista_acao_editar)) },
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
                            text  = stringResource(R.string.lista_dialog_excluir_titulo),
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

    if (chatUiState.aberto) {
        CartoesChatBottomSheet(
            uiState = chatUiState,
            onEvent = onChatEvent,
            onClose = { onChatEvent(ChatEvent.FecharChat) },
        )
    }

    AppScaffold(
        snackbarHostState    = snackbarHostState,
        // FAB posicionado aqui seguindo o padrão Material Design 3:
        // a ação primária de criação pertence ao FAB, não à TopAppBar.
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { onEvent(ListaEvent.NavegaParaNovo) },
                shape          = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = stringResource(R.string.lista_fab_novo_cartao),
                )
            }
        },
        topBar = {
            AppTopAppBar(
                title         = stringResource(R.string.app_name),
                subtitle      = stringResource(R.string.lista_subtitulo),
                subtitleLine2 = uiState.primeiroNome
                    ?.let { stringResource(R.string.lista_saudacao, it) },
                large         = true,
                leadingIcon = {
                    // Círculo azul (primary) sobre fundo lavanda (primaryContainer) em light,
                    // e sobre fundo navy em dark — contraste adequado nos dois temas.
                    Box(
                        modifier         = Modifier
                            .padding(start = spacing.small)
                            .size(spacing.extraLarge)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onPrimary,
                            modifier           = Modifier.size(iconSize.small),
                        )
                    }
                },
                actions  = {
                    // Menu overflow ⋮ — ações secundárias (logout, etc.)
                    Box {
                        IconButton(onClick = { menuExpandido = true }) {
                            Icon(
                                imageVector        = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.lista_menu_mais_opcoes),
                            )
                        }
                        DropdownMenu(
                            expanded         = menuExpandido,
                            onDismissRequest = { menuExpandido = false },
                        ) {
                            DropdownMenuItem(
                                text        = { Text(stringResource(R.string.lista_menu_meu_perfil)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector        = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    menuExpandido = false
                                    onEvent(ListaEvent.NavegaParaPerfil)
                                },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text        = { Text(stringResource(R.string.lista_dialog_deslogar_titulo)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector        = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    menuExpandido = false
                                    confirmarDeslogar = true
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.carregando        -> AppLoading()
                uiState.cartoes.isEmpty() -> EmptyState(
                    title    = stringResource(R.string.lista_vazia_titulo),
                    subtitle = stringResource(R.string.lista_vazia_subtitulo),
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

            FloatingActionButton(
                onClick        = { onChatEvent(ChatEvent.AbrirChat) },
                shape          = CircleShape,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier       = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(
                        start  = spacing.medium,
                        bottom = spacing.medium,
                    ),
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = stringResource(R.string.lista_fab_chat),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartoesChatBottomSheet(
    uiState : ChatUiState,
    onEvent : (ChatEvent) -> Unit,
    onClose : () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val spacing = LocalSpacing.current

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState        = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            ChatHeader(onClose = onClose)
            HorizontalDivider()
            ChatMessages(
                mensagens = uiState.mensagens,
                digitando = uiState.digitando,
                modifier  = Modifier.weight(1f),
            )
            HorizontalDivider()
            ChatInputBar(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(
                    start  = spacing.medium,
                    top    = spacing.small,
                    end    = spacing.medium,
                    bottom = spacing.medium,
                ),
            )
        }
    }
}

@Composable
private fun ChatHeader(onClose: () -> Unit) {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.medium, vertical = spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector        = Icons.Default.SmartToy,
                    contentDescription = stringResource(R.string.chat_bot_avatar),
                    modifier           = Modifier.size(22.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(spacing.smallMedium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = stringResource(R.string.chat_titulo),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text  = stringResource(R.string.chat_subtitulo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onClose) {
            Icon(
                imageVector        = Icons.Default.Close,
                contentDescription = stringResource(R.string.chat_fechar),
            )
        }
    }
}

@Composable
private fun ChatMessages(
    mensagens : List<ChatMessage>,
    digitando : Boolean,
    modifier  : Modifier = Modifier,
) {
    val mensagensVisiveis = mensagens.filter { it.text.isNotBlank() }
    val listState = rememberLazyListState()
    val spacing = LocalSpacing.current

    LaunchedEffect(mensagensVisiveis.size, mensagensVisiveis.lastOrNull()?.text, digitando) {
        val totalItems = mensagensVisiveis.size + if (digitando) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start  = spacing.medium,
            top    = spacing.medium,
            end    = spacing.medium,
            bottom = spacing.medium,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        items(items = mensagensVisiveis, key = { it.id }) { mensagem ->
            ChatMessageBubble(message = mensagem)
        }

        if (digitando) {
            item(key = "typing") {
                TypingIndicator()
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.sender == ChatSender.USER
    val spacing = LocalSpacing.current
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomEnd = if (isUser) 2.dp else 8.dp,
                bottomStart = if (isUser) 8.dp else 2.dp,
            ),
            color = bubbleColor,
            contentColor = contentColor,
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    horizontal = spacing.smallMedium,
                    vertical = spacing.small,
                ),
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = spacing.smallMedium,
                    vertical = spacing.small,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(spacing.small))
                Text(
                    text  = stringResource(R.string.chat_digitando),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    uiState  : ChatUiState,
    onEvent  : (ChatEvent) -> Unit,
    modifier : Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    Column(modifier = modifier.fillMaxWidth()) {
        uiState.erro?.let { mensagem ->
            Text(
                text = mensagem,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = spacing.small),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = uiState.textoAtual,
                onValueChange = { onEvent(ChatEvent.AlterarTexto(it)) },
                modifier = Modifier.weight(1f),
                enabled = !uiState.digitando,
                placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (uiState.podeEnviar) {
                            onEvent(ChatEvent.EnviarMensagem)
                        }
                    },
                ),
            )
            Spacer(modifier = Modifier.width(spacing.small))
            FilledIconButton(
                onClick = { onEvent(ChatEvent.EnviarMensagem) },
                enabled = uiState.podeEnviar,
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.chat_enviar),
                )
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
        // Surface MD3: fundo claro elevado sobre o cartão colorido —
        // tonalElevation adiciona tinte primário; shadowElevation cria sombra física.
        // Padrão recomendado pelo Material Design 3 para ações sobre conteúdo colorido.
        Surface(
            modifier        = Modifier
                .align(Alignment.TopEnd)
                .padding(top = spacing.small, end = spacing.small),
            shape           = CircleShape,
            tonalElevation  = spacing.extraSmall,
            shadowElevation = spacing.extraSmall / 2,
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector        = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.lista_menu_cartao),
                )
            }
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
                nomeUsuario = "João Silva",
                cartoes = listOf(
                    Cartao(1L, "João Silva",    "1234", "Visa",       "12/28", 5_000.0),
                    Cartao(2L, "Maria Santos",  "5678", "Mastercard", "08/26", 10_000.0),
                    Cartao(3L, "Pedro Almeida", "9012", "Elo",        "03/25", 2_500.0),
                ),
            ),
        )
    }
}

@Preview(showBackground = true, name = "Lista – Sem nome (fallback)")
@Composable
private fun ListaSemNomePreview() {
    GerenciadorCartoesTheme {
        ListaContent(
            uiState = ListaUiState(
                nomeUsuario = null,
                cartoes = listOf(
                    Cartao(1L, "Ana Costa", "4321", "Visa", "06/27", 3_000.0),
                ),
            ),
        )
    }
}
