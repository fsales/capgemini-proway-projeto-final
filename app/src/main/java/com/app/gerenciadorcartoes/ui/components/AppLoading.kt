package com.app.gerenciadorcartoes.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme

@Composable
fun AppLoading(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

// =============================================================================
// Previews
// =============================================================================
@Preview(showBackground = true, name = "AppLoading – Light")
@Preview(showBackground = true, name = "AppLoading – Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppLoadingPreview() {
    GerenciadorCartoesTheme { AppLoading() }
}
