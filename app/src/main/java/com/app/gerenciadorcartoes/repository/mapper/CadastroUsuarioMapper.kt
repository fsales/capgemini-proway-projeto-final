package com.app.gerenciadorcartoes.repository.mapper

import com.app.gerenciadorcartoes.data.local.entity.CadastroUsuarioEntity
import com.app.gerenciadorcartoes.model.CadastroUsuario

fun CadastroUsuarioEntity.toDomain(): CadastroUsuario = CadastroUsuario(
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

fun CadastroUsuario.toEntity(): CadastroUsuarioEntity = CadastroUsuarioEntity(
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
