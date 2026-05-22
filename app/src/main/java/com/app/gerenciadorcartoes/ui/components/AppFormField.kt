package com.app.gerenciadorcartoes.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalIconSize

/**
 * Campo de formulário padronizado para o aplicativo.
 *
 * Wraps [OutlinedTextField] adicionando:
 * - Ícone de validação inline (✓ ou ✗) controlado por [showValidationIcon].
 * - [errorMessage] exibido no `supportingText` quando presente.
 * - [hintText] exibido como `placeholder` dentro do campo quando está vazio.
 * - Vinculação opcional a um [FocusRequester].
 * - `singleLine = true` enforçado.
 *
 * @param value              Valor atual do campo.
 * @param onValueChange      Callback chamado a cada mudança de texto.
 * @param label              Label flutuante do campo.
 * @param errorMessage       Mensagem de erro; `null` = sem erro.
 * @param hintText           Dica exibida como `placeholder` dentro do campo quando está vazio.
 * @param showValidationIcon `true` exibe ícone ✓/✗ no trailing; `false` omite.
 * @param focusRequester     FocusRequester para gerenciamento de foco via código.
 */
@Composable
fun AppFormField(
    value                : String,
    onValueChange        : (String) -> Unit,
    label                : String,
    modifier             : Modifier                     = Modifier,
    leadingIcon          : @Composable (() -> Unit)?    = null,
    trailingIcon         : @Composable (() -> Unit)?    = null,
    errorMessage         : String?                      = null,
    hintText             : String?                      = null,
    visualTransformation : VisualTransformation         = VisualTransformation.None,
    keyboardOptions      : KeyboardOptions              = KeyboardOptions.Default,
    keyboardActions      : KeyboardActions              = KeyboardActions.Default,
    readOnly             : Boolean                      = false,
    focusRequester       : FocusRequester?              = null,
    showValidationIcon   : Boolean                      = true,
) {
    val fieldModifier = modifier
        .fillMaxWidth()
        .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }

    OutlinedTextField(
        value            = value,
        onValueChange    = onValueChange,
        label            = { Text(label) },
        placeholder      = hintText?.let { hint -> { Text(hint, color = MaterialTheme.colorScheme.outline) } },
        leadingIcon      = leadingIcon,
        trailingIcon     = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showValidationIcon) {
                    AppFormFieldValidationIcon(
                        hasValue = value.isNotBlank(),
                        isError  = errorMessage != null,
                    )
                }
                trailingIcon?.invoke()
            }
        },
        isError          = errorMessage != null,
        supportingText   = errorMessage?.let { msg -> { Text(msg) } },
        visualTransformation = visualTransformation,
        keyboardOptions  = keyboardOptions,
        keyboardActions  = keyboardActions,
        readOnly         = readOnly,
        singleLine       = true,
        modifier         = fieldModifier,
    )
}

// ── Ícone de validação — auxiliar privada ─────────────────────────────────────

@Composable
private fun AppFormFieldValidationIcon(
    hasValue : Boolean,
    isError  : Boolean,
) {
    if (!hasValue && !isError) return

    Icon(
        imageVector        = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
        contentDescription = null,
        tint               = if (isError) MaterialTheme.colorScheme.error
                             else         MaterialTheme.colorScheme.primary,
        modifier           = Modifier.size(LocalIconSize.current.small),
    )
}

// ── Previews ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "AppFormField – Vazio")
@Preview(showBackground = true, name = "AppFormField – Vazio Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppFormFieldVazioPreview() {
    GerenciadorCartoesTheme {
        AppFormField(value = "", onValueChange = {}, label = "Nome")
    }
}

@Preview(showBackground = true, name = "AppFormField – Preenchido")
@Composable
private fun AppFormFieldPreenchidoPreview() {
    GerenciadorCartoesTheme {
        AppFormField(value = "João Silva", onValueChange = {}, label = "Nome")
    }
}

@Preview(showBackground = true, name = "AppFormField – Erro")
@Composable
private fun AppFormFieldErroPreview() {
    GerenciadorCartoesTheme {
        AppFormField(
            value         = "jo",
            onValueChange = {},
            label         = "Nome",
            errorMessage  = "Nome é obrigatório",
        )
    }
}
