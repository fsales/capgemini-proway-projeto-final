package com.app.gerenciadorcartoes.ui.common.transformation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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