package com.app.gerenciadorcartoes.repository.mapper

import com.app.gerenciadorcartoes.data.local.entity.UsuarioEntity
import com.app.gerenciadorcartoes.model.CadastroUsuario

fun UsuarioEntity.toDomain(): CadastroUsuario = CadastroUsuario(
    id       = id,
    userId   = userId,
    nome     = nome,
    cpf      = cpf,
    cep      = cep,
    endereco = endereco,
    number   = number,
    bairro   = bairro,
    cidade   = cidade,
    estado   = estado,
    email    = email,
)

fun CadastroUsuario.toEntity(): UsuarioEntity = UsuarioEntity(
    id       = id,
    userId   = userId,
    nome     = nome,
    cpf      = cpf,
    cep      = cep,
    endereco = endereco,
    number   = number,
    bairro   = bairro,
    cidade   = cidade,
    estado   = estado,
    email    = email,
)
