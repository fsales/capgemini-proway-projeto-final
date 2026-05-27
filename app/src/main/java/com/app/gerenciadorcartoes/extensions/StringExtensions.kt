package com.app.gerenciadorcartoes.extensions


/**
 * Converte qualquer String (com ou sem máscara) para Double.
 *
 * Regras:
 * - Remove tudo que não é dígito
 * - Últimos 2 dígitos = centavos
 *
 * Exemplos:
 * "1"        -> 0.01
 * "12"       -> 0.12
 * "123"      -> 1.23
 * "1234"     -> 12.34
 * "1.111,11" -> 1111.11
 * ""         -> 0.0
 */
fun String.toCurrencyDouble(): Double {
    val digits = filter(Char::isDigit)

    if (digits.isEmpty()) return 0.0

    val padded = digits.padStart(3, '0')

    val inteiro = padded
        .dropLast(2)
        .trimStart('0')
        .ifEmpty { "0" }

    val centavos = padded.takeLast(2)

    return "$inteiro.$centavos".toDouble()
}

/**
 * Converte qualquer String (com ou sem máscara) para uma representação
 * numérica de moeda em formato decimal (padrão técnico).
 *
 * Regras aplicadas:
 * - Remove todos os caracteres não numéricos
 * - Considera os dois últimos dígitos como centavos
 * - Retorna no formato: "1111.11"
 *
 * Exemplos:
 * "1"         -> "0.01"
 * "12"        -> "0.12"
 * "123"       -> "1.23"
 * "1234"      -> "12.34"
 * "1.111,11"  -> "1111.11"
 * ""          -> "0.0"
 *
 * Uso:
 * Ideal para conversão antes de persistência ou envio para API.
 */


fun String.toCurrencyDecimalString(): String {
    val digits = filter(Char::isDigit)

    if (digits.isEmpty()) return "0.0"

    val padded = digits.padStart(3, '0')

    val inteiro = padded
        .dropLast(2)
        .trimStart('0')
        .ifEmpty { "0" }

    val centavos = padded.takeLast(2)

    return "$inteiro.$centavos"
}




/**
 * Formata uma String como validade de cartão no padrão "MM/AA".
 *
 * Regras aplicadas:
 * - Remove todos os caracteres não numéricos
 * - Limita a 4 dígitos (MMYY)
 * - Insere automaticamente "/" após os dois primeiros dígitos
 *
 * Exemplos:
 * "1"     -> "1"
 * "12"    -> "12"
 * "123"   -> "12/3"
 * "1234"  -> "12/34"
 * "12345" -> "12/34"
 *
 * Uso:
 * - Entrada de usuário em campos de validade
 * - Pode ser usado no onValueChange e/or VisualTransformation
 */
fun String.toValidadeFormatada(): String {
    val digits = filter(Char::isDigit).take(4)

    return buildString {
        digits.forEachIndexed { index, c ->
            if (index == 2) append('/')
            append(c)
        }
    }
}


/**
 * Formata uma String como valor monetário no padrão brasileiro (BRL).
 *
 * Regras aplicadas:
 * - Remove todos os caracteres não numéricos
 * - Considera os dois últimos dígitos como centavos
 * - Garante ao menos 3 dígitos (ex: "1" → "0,01")
 * - Insere separador de milhar (.) e decimal (,)
 *
 * Formato final: "1.234,56"
 *
 * Exemplos:
 * "1"         -> "0,01"
 * "12"        -> "0,12"
 * "123"       -> "1,23"
 * "1234"      -> "12,34"
 * "111111"    -> "1.111,11"
 * "1.111,11"  -> "1.111,11"
 * ""          -> "0,00"
 *
 * Uso:
 * - Exibição de valores monetários na UI
 * - Pode ser usado em conjunto com VisualTransformation
 * - Ideal para formatação consistente em todo o app
 */
fun String.toCurrencyFormatted(): String {
    val digits = filter(Char::isDigit)

    if (digits.isEmpty()) return "0,00"

    val padded = digits.padStart(3, '0')
    val inteiro = padded.dropLast(2).trimStart('0').ifEmpty { "0" }
    val centavos = padded.takeLast(2)

    val inteiroFormatado = inteiro
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return "$inteiroFormatado,$centavos"
}

