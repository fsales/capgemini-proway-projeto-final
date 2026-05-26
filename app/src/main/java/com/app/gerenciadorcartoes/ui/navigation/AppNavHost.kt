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
import com.app.gerenciadorcartoes.ui.feature.fatura.FaturaScreen
import com.app.gerenciadorcartoes.ui.feature.lista.ListaScreen
import com.app.gerenciadorcartoes.ui.feature.login.LoginScreen
import com.app.gerenciadorcartoes.ui.feature.recuperarsenha.RecuperarSenhaScreen
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
                    navController.navigate(ListaRoute()) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                },
                navigateToCadastroIncompleto = { userId, email, nome ->
                    navController.navigate(
                        CadastroUsuarioRoute(
                            userId       = userId,
                            emailExterno = email,
                            nomeExterno  = nome,
                        )
                    ) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                navigateToLista           = {
                    navController.navigate(ListaRoute()) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
                navigateToCadastro        = {
                    navController.navigate(CadastroUsuarioRoute())
                },
                navigateToCadastroExterno = { userId, email, nome ->
                    navController.navigate(
                        CadastroUsuarioRoute(
                            userId       = userId,
                            emailExterno = email,
                            nomeExterno  = nome,
                        )
                    )
                },
                navigateToRecuperarSenha  = { email ->
                    navController.navigate(RecuperarSenhaRoute(emailInicial = email))
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
                        popUpTo<ListaRoute> { inclusive = true }
                    }
                },
                onNavigateToPerfil = { userId ->
                    navController.navigate(
                        CadastroUsuarioRoute(userId = userId, modoEdicao = true)
                    )
                },
            )
        }

        composable<DetalheRoute> {
            DetalheScreen(
                navigateBack              = { navController.popBackStack() },
                onNavigateToAjustarLimite = { id ->
                    navController.navigate(AjustarLimiteRoute(id = id))
                },
                onNavigateToFatura = { id ->
                    navController.navigate(FaturaRoute(id = id))
                },
            )
        }

        composable<FaturaRoute> {
            FaturaScreen(
                navigateBack = { navController.popBackStack() },
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

        composable<CadastroUsuarioRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<CadastroUsuarioRoute>()
            CadastroUsuarioScreen(
                navigateBack    = { navController.navigateUp() },
                navigateToLista = { _ ->
                    if (route.modoEdicao) {
                        navController.navigate(ListaRoute(exibirConfirmacao = true)) {
                            popUpTo<ListaRoute> { inclusive = true }
                        }
                    } else {
                        navController.navigate(ListaRoute(exibirConfirmacao = true)) {
                            popUpTo<LoginRoute> { inclusive = true }
                        }
                    }
                },
            )
        }

        composable<RecuperarSenhaRoute> {
            RecuperarSenhaScreen(
                navigateBack = { navController.popBackStack() },
            )
        }
    }
}
