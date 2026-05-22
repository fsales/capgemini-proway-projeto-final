package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.CadastroUsuario

interface CadastroUsuarioRepository {

    /**
     * Persiste um novo registro de usuário e retorna o `id` gerado pelo banco.
     * Lança exceção se a operação falhar (ex: constraint violation).
     */
    suspend fun salvar(usuario: CadastroUsuario): Long

    /**
     * Retorna o usuário cujo e-mail corresponde ao informado, ou `null` se não existir.
     * Use para verificar duplicidade antes de salvar.
     */
    suspend fun buscarPorEmail(email: String): CadastroUsuario?

    /** Retorna o usuário com o [id] informado, ou `null` se não existir. */
    suspend fun buscarPorId(id: Long): CadastroUsuario?

    /**
     * Verifica se [email] e [senha] correspondem a um registro cadastrado.
     *
     * A comparação de senha é feita em tempo constante contra o hash armazenado.
     * @return `true` se as credenciais são válidas; `false` caso contrário.
     */
    suspend fun verificarCredenciais(email: String, senha: String): Boolean
}

