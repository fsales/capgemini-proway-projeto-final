package com.app.gerenciadorcartoes.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.gerenciadorcartoes.R
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalIconSize
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing

private val GoogleButtonBackground        = Color(0xFFFFFFFF)
private val GoogleButtonContentColor      = Color(0xFF1F1F1F)
private val GoogleButtonBorderColor       = Color(0xFF747775)
private val GoogleButtonDisabledAlpha     = 0.38f

/**
 * Botão de autenticação Google seguindo as
 * [Google Sign-In Button Guidelines](https://developers.google.com/identity/branding-guidelines).
 *
 * - Fundo branco fixo (`#FFFFFF`) independente do tema
 * - Borda `1dp #747775`
 * - Ícone Google à esquerda + texto centralizado
 * - Cores nunca derivadas de `MaterialTheme` — exigência do branding Google
 */
@Composable
fun GoogleSignInButton(
    onClick  : () -> Unit,
    modifier : Modifier = Modifier,
    enabled  : Boolean  = true,
) {
    val spacing  = LocalSpacing.current
    val iconSize = LocalIconSize.current

    OutlinedButton(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier,
        colors   = ButtonDefaults.outlinedButtonColors(
            containerColor         = GoogleButtonBackground,
            contentColor           = GoogleButtonContentColor,
            disabledContainerColor = GoogleButtonBackground.copy(alpha = GoogleButtonDisabledAlpha),
            disabledContentColor   = GoogleButtonContentColor.copy(alpha = GoogleButtonDisabledAlpha),
        ),
        border   = BorderStroke(
            width = 1.dp,
            color = if (enabled) GoogleButtonBorderColor
                    else         GoogleButtonBorderColor.copy(alpha = GoogleButtonDisabledAlpha),
        ),
    ) {
        Image(
            painter            = painterResource(R.drawable.ic_google_logo),
            contentDescription = null,
            modifier           = Modifier.size(iconSize.medium),
        )
        Spacer(Modifier.width(spacing.smallMedium))
        Text(
            text  = stringResource(R.string.google_sign_in_continuar),
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) GoogleButtonContentColor
                    else         GoogleButtonContentColor.copy(alpha = GoogleButtonDisabledAlpha),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "GoogleSignInButton – Ativo")
@Preview(showBackground = true, name = "GoogleSignInButton – Ativo Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GoogleSignInButtonAtivoPreview() {
    GerenciadorCartoesTheme {
        GoogleSignInButton(onClick = {}, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true, name = "GoogleSignInButton – Desabilitado")
@Composable
private fun GoogleSignInButtonDesabilitadoPreview() {
    GerenciadorCartoesTheme {
        GoogleSignInButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
    }
}
