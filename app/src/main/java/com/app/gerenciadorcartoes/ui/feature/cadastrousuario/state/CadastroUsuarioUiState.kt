package com.app.gerenciadorcartoes.ui.feature.cadastrousuario.state


data class CadastroUsuarioUiState(
    /** Etapa atual do wizard (0-based index). */
    val etapaAtual                : Int     = 0,
    /**
     * Quando `true`, a UI deve focar o primeiro campo com erro na etapa atual.
     * Deve ser limpo despachando [com.app.gerenciadorcartoes.ui.feature.cadastrousuario.CadastroUsuarioEvent.FocoRealizado].
     */
    val focarPrimeiroCampoComErro : Boolean = false,
    /**
     * `true` quando o usuário veio de um provedor externo (Google etc.).
     * Oculta a aba de Segurança — a conta no provedor já existe.
     */
    val isFluxoExterno            : Boolean = false,
    /**
     * `true` quando o usuário acessou a tela para editar o próprio perfil já cadastrado.
     * Oculta a aba de Segurança e e-mail fica somente-leitura.
     */
    val isModoEdicao              : Boolean = false,

    val nome           : String  = "",
    val erroNome       : String? = null,
    val cpf            : String  = "",
    val erroCpf        : String? = null,
    val cep            : String  = "",
    val erroCep        : String? = null,
    val endereco       : String  = "",
    val erroEndereco   : String? = null,
    val number         : String  = "",
    val erroNumber     : String? = null,
    val bairro         : String  = "",
    val erroBairro     : String? = null,
    val cidade         : String  = "",
    val erroCidade     : String? = null,
    val estado         : String  = "",
    val erroEstado     : String? = null,
    val email          : String  = "",
    val erroEmail      : String? = null,
    val senha          : String  = "",
    val erroSenha      : String? = null,
    val confirmarSenha : String  = "",
    val erroConfirmarSenha : String? = null,
    val carregando     : Boolean = false,
    val buscandoCep    : Boolean = false,
) {
    /** Etapa 0 — Dados pessoais possui algum campo com erro. */
    val temErroNaEtapa0: Boolean
        get() = erroNome != null || erroCpf != null || erroEmail != null

    /** Etapa 1 — Endereço possui algum campo com erro. */
    val temErroNaEtapa1: Boolean
        get() = erroCep != null || erroEndereco != null || erroNumber != null ||
                erroBairro != null || erroCidade != null || erroEstado != null

    /** Etapa 2 — Segurança possui algum campo com erro. */
    val temErroNaEtapa2: Boolean
        get() = erroSenha != null || erroConfirmarSenha != null
}
