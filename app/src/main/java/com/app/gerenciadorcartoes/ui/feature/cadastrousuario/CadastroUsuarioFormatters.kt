package com.app.gerenciadorcartoes.ui.feature.cadastrousuario

// ── Formatadores puros — sem dependência de Compose ou Android ────────────────
// Declarados como `internal` para ficarem visíveis nos testes unitários do módulo.

/** Remove tudo que não é dígito, limita a 11 e aplica a máscara ###.###.###-##. */
internal fun formatCpf(text: String): String {
    val digits = text.filter { it.isDigit() }.take(11)
    return buildString {
        digits.forEachIndexed { i, c ->
            if (i == 3 || i == 6) append('.')
            if (i == 9) append('-')
            append(c)
        }
    }
}

/** Remove tudo que não é dígito, limita a 8 e aplica a máscara #####-###. */
internal fun formatCep(text: String): String {
    val digits = text.filter { it.isDigit() }.take(8)
    return buildString {
        digits.forEachIndexed { i, c ->
            if (i == 5) append('-')
            append(c)
        }
    }
}

/** Mantém apenas dígitos e limita a 8 caracteres (número do endereço). */
internal fun formatNumero(text: String): String =
    text.filter { it.isDigit() }.take(8)

/** Mantém apenas letras, converte para maiúsculas e limita a 2 (sigla UF). */
internal fun formatUf(text: String): String =
    text.filter { it.isLetter() }.uppercase().take(2)

/** Valida estruturalmente o e-mail: deve ter @, domínio e TLD com ≥ 2 chars. */
internal fun emailEstruturalmenteValido(email: String): Boolean =
    email.isNotBlank() && Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$").matches(email)

