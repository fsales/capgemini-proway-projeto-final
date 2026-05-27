package com.app.gerenciadorcartoes.ui.common.transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object CpfVisualTransformation : VisualTransformation {
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