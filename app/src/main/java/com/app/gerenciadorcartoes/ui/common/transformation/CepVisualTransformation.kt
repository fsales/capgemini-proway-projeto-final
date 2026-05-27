package com.app.gerenciadorcartoes.ui.common.transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object CepVisualTransformation : VisualTransformation {
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