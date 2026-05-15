package com.app.gerenciadorcartoes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =============================================================================
// Tokens de espaçamento — escala de 4dp seguindo MD3
// https://m3.material.io/foundations/layout/understanding-layout/spacing
// =============================================================================
data class Spacing(
    /** 4dp  — separação mínima entre elementos relacionados */
    val extraSmall  : Dp = 4.dp,
    /** 8dp  — separação interna de componentes compactos */
    val small       : Dp = 8.dp,
    /** 12dp — padding interno de componentes (ex: chips, badges) */
    val smallMedium : Dp = 12.dp,
    /** 16dp — padding padrão de tela e cards (MD3 compact margin) */
    val medium      : Dp = 16.dp,
    /** 24dp — padding vertical de seções de tela */
    val large       : Dp = 24.dp,
    /** 32dp — separação entre seções distintas */
    val extraLarge  : Dp = 32.dp,
)

val LocalSpacing = compositionLocalOf { Spacing() }

/** Acesso ao sistema de espaçamento via MaterialTheme — uso: MaterialTheme.spacing.medium */
val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
