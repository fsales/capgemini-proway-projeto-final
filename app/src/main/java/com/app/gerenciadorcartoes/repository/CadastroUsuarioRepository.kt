package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.CadastroUsuario
import com.app.gerenciadorcartoes.model.Endereco

interface CadastroUsuarioRepository {

    /**
     * Persiste um novo registro de usuário e retorna o `id` gerado pelo banco.
     * Lança exceção se a operação falhar (ex: constraint violation).
     */
    suspend fun salvar(usuario: CadastroUsuario): Long

    /**
     * Atualiza todos os campos de um perfil já existente (identificado pelo `id`).
     */
    suspend fun atualizar(usuario: CadastroUsuario)

    /**
     * Retorna o usuário cujo e-mail corresponde ao informado, ou `null` se não existir.
     * Use para verificar duplicidade antes de salvar.
     */
    suspend fun buscarPorEmail(email: String): CadastroUsuario?

    /** Retorna o usuário com o [id] informado, ou `null` se não existir. */
    suspend fun buscarPorId(id: Long): CadastroUsuario?

    /** Retorna o usuário com o [userId] informado, ou `null` se não existir. */
    suspend fun buscarPorUserId(userId: String): CadastroUsuario?

    /**
     * Substitui o userId de um perfil existente pelo [novoUserId].
     *
     * Necessário quando a conta Firebase é deletada e recriada via Google:
     * o UID gerado pelo Firebase muda, mas o perfil no Room (com o mesmo e-mail)
     * deve ser aproveitado sem exigir re-cadastro do usuário.
     */
    suspend fun atualizarUserId(id: Long, novoUserId: String)

    /**
     * Busca endereço por CEP usando a API ViaCEP.
     * Retorna `null` se não encontrar ou se houve falha na requisição.
     */
    suspend fun buscarEnderecoPorCep(cep: String): Endereco?
}
