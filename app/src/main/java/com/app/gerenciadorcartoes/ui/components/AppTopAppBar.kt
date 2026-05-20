@file:OptIn(ExperimentalMaterial3Api::class)
package com.app.gerenciadorcartoes.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme

// TopAppBar padrão — Material 3.
// onNavigateBack: omita para telas raiz (sem botão de voltar).
// large: true → título em headlineSmall Bold (visual premium sem altura extra).
// actions: RowScope — use para ícones extras na direita (Editar, Excluir, etc.).
@Composable
fun AppTopAppBar(
    title          : String,
    subtitle       : String?                          = null,
    large          : Boolean                          = false,
    onNavigateBack : (() -> Unit)?                    = null,
    actions        : @Composable RowScope.() -> Unit  = {},
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text       = title,
                    style      = if (large) MaterialTheme.typography.headlineSmall
                                 else       MaterialTheme.typography.titleLarge,
                    fontWeight = if (large) FontWeight.Bold else null,
                )
                if (subtitle != null) {
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            }
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                    )
                }
            }
        },
        actions = actions,
    )
}

// =============================================================================
// Previews
// =============================================================================

/** Tela raiz — sem botão de voltar, sem ações. */
@Preview(showBackground = true, name = "TopAppBar – Raiz Light")
@Preview(showBackground = true, name = "TopAppBar – Raiz Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopAppBarRaizPreview() {
    GerenciadorCartoesTheme {
        AppTopAppBar(
            title    = "Meus Cartões",
            subtitle = "Gerencie seus cartões cadastrados",
            large    = true,
        )
    }
}

/** Tela interna — com botão de voltar, sem ações. */
@Preview(showBackground = true, name = "TopAppBar – Com voltar Light")
@Preview(showBackground = true, name = "TopAppBar – Com voltar Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopAppBarVoltarPreview() {
    GerenciadorCartoesTheme {
        AppTopAppBar(
            title          = "Detalhes do Cartão",
            onNavigateBack = {},
        )
    }
}

/** Tela de detalhe — com botão de voltar e ações (Editar + Excluir). */
@Preview(showBackground = true, name = "TopAppBar – Com ações Light")
@Preview(showBackground = true, name = "TopAppBar – Com ações Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopAppBarAcoesPreview() {
    GerenciadorCartoesTheme {
        AppTopAppBar(
            title          = "Detalhes do Cartão",
            onNavigateBack = {},
            actions        = {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector        = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint               = MaterialTheme.colorScheme.error,
                    )
                }
            },
        )
    }
}
