package com.app.gerenciadorcartoes.ui.components

import android.content.res.Configuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.app.gerenciadorcartoes.R
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme

/**
 * Diálogo de confirmação genérico — wraps [AlertDialog] com ações de confirmar e cancelar.
 *
 * @param titulo         Texto exibido como título do diálogo.
 * @param mensagem       Texto explicativo exibido no corpo do diálogo.
 * @param textConfirmar  Rótulo do botão de confirmação (padrão: [R.string.dialog_btn_confirmar]).
 * @param textCancelar   Rótulo do botão de cancelamento (padrão: [R.string.dialog_btn_cancelar]).
 * @param onConfirmar    Callback disparado ao clicar em confirmar.
 * @param onDismiss      Callback disparado ao fechar (botão cancelar ou toque fora).
 */
@Composable
fun ConfirmacaoDialog(
    titulo        : String,
    mensagem      : String,
    textConfirmar : String = stringResource(R.string.dialog_btn_confirmar),
    textCancelar  : String = stringResource(R.string.dialog_btn_cancelar),
    onConfirmar   : () -> Unit = {},
    onDismiss     : () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title            = { Text(titulo) },
        text             = { Text(mensagem) },
        confirmButton    = {
            TextButton(onClick = {
                onConfirmar()
                onDismiss()
            }) { Text(textConfirmar) }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) { Text(textCancelar) }
        },
    )
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true, name = "ConfirmacaoDialog – Light")
@Preview(showBackground = true, name = "ConfirmacaoDialog – Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConfirmacaoDialogPreview() {
    GerenciadorCartoesTheme {
        ConfirmacaoDialog(
            titulo        = stringResource(R.string.lista_dialog_deslogar_titulo),
            mensagem      = stringResource(R.string.lista_dialog_deslogar_mensagem),
            textConfirmar = stringResource(R.string.lista_dialog_deslogar_confirmar),
        )
    }
}

@Preview(showBackground = true, name = "ConfirmacaoDialog – Excluir Light")
@Preview(showBackground = true, name = "ConfirmacaoDialog – Excluir Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConfirmacaoDialogExcluirPreview() {
    GerenciadorCartoesTheme {
        ConfirmacaoDialog(
            titulo        = stringResource(R.string.lista_dialog_excluir_titulo),
            mensagem      = stringResource(R.string.lista_dialog_excluir_mensagem, "João Silva"),
            textConfirmar = stringResource(R.string.lista_dialog_excluir_confirmar),
        )
    }
}
