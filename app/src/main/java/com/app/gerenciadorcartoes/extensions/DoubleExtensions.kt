package com.app.gerenciadorcartoes.extensions

/**
 * Converte Double para String de dígitos (base do TextField)
 *
 * Ex:
 * 1111.11 -> "111111"
 */
fun Double.toCurrencyDigits(): String =
    ((this * 100).toLong()).toString()
