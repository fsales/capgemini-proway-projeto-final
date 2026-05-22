package com.app.gerenciadorcartoes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.toRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.gerenciadorcartoes.ui.feature.ajustarlimite.AjustarLimiteScreen
import com.app.gerenciadorcartoes.ui.feature.cadastraralterar.CadastrarAlterarScreen
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.CadastroUsuarioScreen
import com.app.gerenciadorcartoes.ui.feature.detalhe.DetalheScreen
import com.app.gerenciadorcartoes.ui.feature.lista.ListaScreen
import com.app.gerenciadorcartoes.ui.feature.login.LoginScreen
import com.app.gerenciadorcartoes.ui.feature.splash.SplashScreen

@Composable
fun AppNavHost(startDestination: Any = SplashRoute) {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = startDestination,
    ) {

        composable<SplashRoute> {
            SplashScreen(
                navigateToLista = {
                    navController.navigate(ListaRoute) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                },
            )
        }

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
                onDeslogar = {
                    navController.navigate(LoginRoute) {
                        popUpTo<ListaRoute> { inclusive = true }   // limpa toda a back stack
                    }
                },
            )
        }

        composable<DetalheRoute> {
            DetalheScreen(
                navigateBack              = { navController.popBackStack() },
                onNavigateToAjustarLimite = { id ->
                    navController.navigate(AjustarLimiteRoute(id = id))
                },
            )
        }

        composable<AjustarLimiteRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AjustarLimiteRoute>()

            AjustarLimiteScreen(
                navigateBack = {
                    val voltouParaDetalhe = navController.popBackStack<DetalheRoute>(
                        inclusive = false,
                    )

                    if (!voltouParaDetalhe) {
                        navController.navigate(DetalheRoute(id = route.id)) {
                            popUpTo<AjustarLimiteRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
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
