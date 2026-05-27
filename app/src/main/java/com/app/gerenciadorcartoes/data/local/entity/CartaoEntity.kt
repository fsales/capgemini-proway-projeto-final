package com.app.gerenciadorcartoes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cartoes",
    foreignKeys = [ForeignKey(
        entity = CadastroUsuarioEntity::class,
        parentColumns = ["id"],
        childColumns = ["cadastroUsuarioId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["cadastroUsuarioId"])]
)
data class CartaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id          : Long   = 0L,
    val nomeTitular : String,
    val finalNumero : String, // últimos 4 dígitos
    val bandeira    : String,
    val validade    : String, // MM/AA
    val limite      : Double,
    val limiteMaximo: Double,
    val template    : String = "default",
    val bloqueado   : Boolean = false,
    // Identificador gerado pelo cliente (opcional) usado para idempotência
    val clientId     : String? = null,
    // Marca registros que ainda precisam ser sincronizados com a API
    val syncPending  : Boolean = false,
    // relacionamento (nullable para migração segura)
    val cadastroUsuarioId: Long? = null,
)
