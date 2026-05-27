package com.app.gerenciadorcartoes.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.gerenciadorcartoes.extensions.toCurrencyFormatted
import com.app.gerenciadorcartoes.extensions.toValidadeFormatada

// --- Validade (MM/AA) ──────────────────────────────────────────────────────

object ValidadeVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = text.text.toValidadeFormatada()

        val digitsOnly = text.text.filter { it.isDigit() }.take(4)

        val offsetMapping = object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {
                val clampedOffset = offset.coerceIn(0, digitsOnly.length)

                var digitCount = 0
                formatted.forEachIndexed { index, c ->
                    if (digitCount == clampedOffset) return index
                    if (c.isDigit()) digitCount++
                }

                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return formatted
                    .take(offset)
                    .count { it.isDigit() }
                    .coerceIn(0, digitsOnly.length)
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}



// --- Currency (BRL) ─────────────────────────────────────────────────────────
/**
 * VisualTransformation que exibe String de dígitos (centavos) no formato BRL.
 *
 * Armazenamento: "150050"  → dígitos puros representando centavos
 * Exibição:      "1.500,50"
 *
 * Uso:
 *   OutlinedTextField(
 *       keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
 *       visualTransformation = rememberCurrencyVisualTransformation(),
 *   )
 *
 * Converter para BigDecimal no ViewModel via `digitos.padStart(3, '0')` e parse manual.
 */

class CurrencyVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter(Char::isDigit)
        val formatted = digits.toCurrencyFormatted()

        val offsetMapping = object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {
                val clampedOffset = offset.coerceIn(0, digits.length)

                var digitsCount = 0
                formatted.forEachIndexed { index, c ->
                    if (digitsCount == clampedOffset) return index
                    if (c.isDigit()) digitsCount++
                }

                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return formatted
                    .take(offset)
                    .count(Char::isDigit)
                    .coerceIn(0, digits.length)
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}


@Composable
fun rememberCurrencyVisualTransformation(): VisualTransformation {
    return remember { CurrencyVisualTransformation() }
}

// ── Previews ───────────────────────────────────────────────────────────────
@Preview(showBackground = true, name = "Currency Transformation Preview")
@Composable
private fun CurrencyTransformationPreview() {
    Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
        var text by remember { mutableStateOf("") }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Limite") },
            visualTransformation = rememberCurrencyVisualTransformation(),
        )
    }
}

@Preview(showBackground = true, name = "Validade Transformation Preview")
@Composable
private fun ValidadeTransformationPreview() {
    Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
        var text by remember { mutableStateOf("") }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Validade") },
            visualTransformation = ValidadeVisualTransformation,
        )
    }
}
