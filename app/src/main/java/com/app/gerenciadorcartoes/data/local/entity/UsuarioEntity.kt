package com.app.gerenciadorcartoes.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices   = [
        Index(value = ["email"],  unique = true),
        Index(value = ["userId"], unique = true),
    ],
)
data class UsuarioEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val userId   : String = "",   // id opaco do provedor de autenticação

    val nome     : String = "",

    val cpf      : String = "",

    val cep      : String = "",

    val endereco : String = "",

    val number   : String = "",

    val bairro   : String = "",

    val cidade   : String = "",

    val estado   : String = "",

    val email    : String = "",
)
