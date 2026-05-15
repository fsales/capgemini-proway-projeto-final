package com.app.gerenciadorcartoes.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme

// Scaffold padrão do aplicativo — Material 3.
// O SnackbarHostState gerencia Snackbar: use snackbarHostState.showSnackbar() nas Screens.
@Composable
fun AppScaffold(
    snackbarHostState    : SnackbarHostState = remember { SnackbarHostState() },
    topBar               : @Composable () -> Unit = {},
    floatingActionButton : @Composable () -> Unit = {},
    content              : @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        snackbarHost         = { SnackbarHost(hostState = snackbarHostState) },
        topBar               = topBar,
        floatingActionButton = floatingActionButton,
        content              = content,
    )
}

// =============================================================================
// Previews
// =============================================================================

/** Scaffold mínimo — só topBar. */
@Preview(showBackground = true, name = "AppScaffold – Só TopBar Light")
@Preview(showBackground = true, name = "AppScaffold – Só TopBar Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppScaffoldTopBarPreview() {
    GerenciadorCartoesTheme {
        AppScaffold(
            topBar = { AppTopAppBar(title = "Meus Cartões") },
        ) { paddingValues ->
            Box(
                modifier         = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) { Text("Conteúdo da tela") }
        }
    }
}

/** Scaffold completo — topBar + FAB + conteúdo. */
@Preview(showBackground = true, name = "AppScaffold – Com FAB Light")
@Preview(showBackground = true, name = "AppScaffold – Com FAB Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppScaffoldFabPreview() {
    GerenciadorCartoesTheme {
        AppScaffold(
            topBar = { AppTopAppBar(title = "Meus Cartões") },
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Novo")
                }
            },
        ) { paddingValues ->
            Box(
                modifier         = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) { Text("Conteúdo da tela") }
        }
    }
}
