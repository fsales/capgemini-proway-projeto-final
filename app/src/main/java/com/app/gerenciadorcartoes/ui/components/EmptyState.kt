package com.app.gerenciadorcartoes.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme
import com.app.gerenciadorcartoes.ui.theme.LocalSpacing

@Composable
fun EmptyState(
    message  : String? = null,
    title    : String? = null,
    subtitle : String? = null,
    modifier : Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val fallbackTitle = message?.lineSequence()?.firstOrNull().orEmpty()
    val fallbackSubtitle = message
        ?.lineSequence()
        ?.drop(1)
        ?.joinToString("\n")
        ?.ifBlank { null }

    val titleText = title ?: fallbackTitle
    val subtitleText = subtitle ?: fallbackSubtitle

    Column(
        modifier            = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector        = Icons.Outlined.Inbox,
            contentDescription = null,
            modifier           = Modifier.size(64.dp),
            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        )
        Spacer(modifier = Modifier.height(spacing.medium))
        if (titleText.isNotBlank()) {
            Text(
                text      = titleText,
                style     = MaterialTheme.typography.titleLarge,
                color     = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        if (!subtitleText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(spacing.small))
            Text(
                text      = subtitleText,
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// =============================================================================
// Previews
// =============================================================================
@Preview(showBackground = true, name = "EmptyState – Light")
@Preview(showBackground = true, name = "EmptyState – Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyStatePreview() {
    GerenciadorCartoesTheme {
        EmptyState(
            title    = "Você ainda não tem cartões",
            subtitle = "Adicione seu primeiro cartão para começar",
        )
    }
}
