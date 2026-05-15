package com.app.gerenciadorcartoes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.gerenciadorcartoes.ui.feature.cadastraralterar.CadastrarAlterarScreen
import com.app.gerenciadorcartoes.ui.feature.detalhe.DetalheScreen
import com.app.gerenciadorcartoes.ui.feature.lista.ListaScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = ListaRoute,
    ) {

        composable<ListaRoute> {
            ListaScreen(
                onNavigateToNovo = {
                    navController.navigate(CadastrarAlterarRoute(id = 0L))
                },
                onNavigateToItem = { id ->
                    navController.navigate(DetalheRoute(id = id))
                },
            )
        }

        composable<DetalheRoute> {
            DetalheScreen(
                navigateBack       = { navController.popBackStack() },
                onNavigateToEditar = { id ->
                    navController.navigate(CadastrarAlterarRoute(id = id))
                },
            )
        }

        composable<CadastrarAlterarRoute> {
            CadastrarAlterarScreen(
                navigateBack = { navController.popBackStack() },
            )
        }
    }
}
