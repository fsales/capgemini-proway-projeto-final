@file:OptIn(ExperimentalMaterial3Api::class)
package com.app.gerenciadorcartoes.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.app.gerenciadorcartoes.R
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalIconSize
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing

// TopAppBar padrão — Material 3.
// onNavigateBack : omita em telas raiz (sem botão de voltar).
// leadingIcon    : ícone/logo de marca para telas raiz — exibido quando onNavigateBack == null.
// large          : true → título em headlineSmall Bold (visual premium sem altura extra).
// actions        : RowScope — use para ícones extras na direita.
@Composable
fun AppTopAppBar(
    title          : String,
    subtitle       : String?                          = null,
    large          : Boolean                          = false,
    onNavigateBack : (() -> Unit)?                    = null,
    leadingIcon    : (@Composable () -> Unit)?        = null,
    actions        : @Composable RowScope.() -> Unit  = {},
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text       = title,
                    style      = if (large) MaterialTheme.typography.headlineSmall
                                 else       MaterialTheme.typography.titleLarge,
                    fontWeight = if (large) FontWeight.Bold else FontWeight.SemiBold,
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
            when {
                onNavigateBack != null -> IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.topbar_voltar),
                    )
                }
                leadingIcon != null -> leadingIcon()
            }
        },
        actions = actions,
    )
}

// =============================================================================
// Previews
// =============================================================================

/** Tela raiz com marca — logo + app name, sem botão de voltar. */
@Preview(showBackground = true, name = "TopAppBar – Marca Light")
@Preview(showBackground = true, name = "TopAppBar – Marca Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopAppBarMarcaPreview() {
    GerenciadorCartoesTheme {
        val spacing  = LocalSpacing.current
        val iconSize = LocalIconSize.current
        AppTopAppBar(
            title       = stringResource(R.string.app_name),
            leadingIcon = {
                Box(
                    modifier         = Modifier
                        .padding(start = spacing.small)
                        .size(spacing.extraLarge)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimary,
                        modifier           = Modifier.size(iconSize.small),
                    )
                }
            },
        )
    }
}

/** Tela raiz — sem botão de voltar, sem ações. */
@Preview(showBackground = true, name = "TopAppBar – Raiz Light")
@Preview(showBackground = true, name = "TopAppBar – Raiz Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopAppBarRaizPreview() {
    GerenciadorCartoesTheme {
        AppTopAppBar(
            title    = stringResource(R.string.lista_titulo),
            subtitle = stringResource(R.string.lista_subtitulo),
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
            title          = stringResource(R.string.detalhe_titulo),
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
            title          = stringResource(R.string.detalhe_titulo),
            onNavigateBack = {},
            actions        = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.cd_editar),
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector        = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.cd_excluir),
                        tint               = MaterialTheme.colorScheme.error,
                    )
                }
            },
        )
    }
}
