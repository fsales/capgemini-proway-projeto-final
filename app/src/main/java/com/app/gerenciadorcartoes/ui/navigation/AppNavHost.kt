package com.app.gerenciadorcartoes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.gerenciadorcartoes.ui.feature.cadastraralterar.CadastrarAlterarScreen
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.CadastroUsuarioScreen
import com.app.gerenciadorcartoes.ui.feature.detalhe.DetalheScreen
import com.app.gerenciadorcartoes.ui.feature.lista.ListaScreen
import com.app.gerenciadorcartoes.ui.feature.login.LoginScreen

@Composable
fun AppNavHost(startDestination: Any = LoginRoute) {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = startDestination,
    ) {

        composable<LoginRoute> {
            LoginScreen(
                onNavigateToLista      = {
                    navController.navigate(ListaRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
                onNavigateParaCadastro = {
                    navController.navigate(CadastroUsuarioRoute)
                },
            )
        }

        composable<ListaRoute> {
            ListaScreen(
                onNavigateToNovo = {
                    navController.navigate(CadastrarAlterarRoute(id = 0L))
                },
                onNavigateToItem = { id ->
                    navController.navigate(DetalheRoute(id = id))
                },
                onNavigateToEditar = { id ->
                    navController.navigate(CadastrarAlterarRoute(id = id))
                },
            )
        }

        composable<DetalheRoute> {
            DetalheScreen(
                navigateBack = { navController.popBackStack() },
            )
        }

        composable<CadastrarAlterarRoute> {
            CadastrarAlterarScreen(
                navigateBack = { navController.popBackStack() },
            )
        }

        composable<CadastroUsuarioRoute> {
            CadastroUsuarioScreen(
                navigateBack = { navController.popBackStack() },
            )
        }
    }
}
