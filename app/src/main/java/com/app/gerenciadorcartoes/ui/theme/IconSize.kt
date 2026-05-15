package com.app.gerenciadorcartoes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =============================================================================
// Tokens de tamanho de ícone — escala oficial MD3
// https://m3.material.io/styles/icons/specs
// =============================================================================
data class IconSize(
    /** 16dp — ícones em componentes compactos (chips, badges) */
    val extraSmall : Dp = 16.dp,
    /** 20dp — ícones em componentes densos (listas compactas) */
    val small      : Dp = 20.dp,
    /** 24dp — ícones padrão (NavigationBar, botões, campos) */
    val medium     : Dp = 24.dp,
    /** 40dp — ícones grandes (FAB, itens de lista com destaque) */
    val large      : Dp = 40.dp,
    /** 48dp — ícones de destaque (cards hero, onboarding) */
    val extraLarge : Dp = 48.dp,
)

val LocalIconSize = compositionLocalOf { IconSize() }

/** Acesso ao sistema de tamanho de ícone via MaterialTheme — uso: MaterialTheme.iconSize.medium */
val MaterialTheme.iconSize: IconSize
    @Composable
    @ReadOnlyComposable
    get() = LocalIconSize.current
