package com.app.gerenciadorcartoes.ui.feature.cadastraralterar.state

data class CadastrarAlterarUiState(
    val nomeTitular : String  = "",
    val finalNumero : String  = "",
    val bandeira    : String  = "",
    val validade    : String  = "",
    val limite      : String  = "",
    val template    : String  = "default",
    val carregando  : Boolean = false,
    val salvando    : Boolean = false,
    // Erros de validação por campo
    val erroNome    : String? = null,
    val erroNumero  : String? = null,
    val erroBandeira: String? = null,
    val erroValidade: String? = null,
    val erroLimite  : String? = null,
    val modoEdicao  : Boolean = false,
) {
    /** true quando o formulário tem id > 0 (modo edição já carregado) */
    val isEdicao: Boolean get() = modoEdicao
}

