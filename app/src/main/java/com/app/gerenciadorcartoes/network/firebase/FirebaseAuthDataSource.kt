package com.app.gerenciadorcartoes.network.firebase

import com.app.gerenciadorcartoes.model.ColisaoContaException
import com.app.gerenciadorcartoes.model.UsuarioAuth
import com.app.gerenciadorcartoes.network.auth.AuthDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : AuthDataSource {

    override suspend fun cadastrar(email: String, senha: String): UsuarioAuth =
        firebaseAuth.createUserWithEmailAndPassword(email, senha).await()
            .user?.toUsuarioAuth() ?: throw Exception("Falha ao criar conta")

    override suspend fun entrar(email: String, senha: String): UsuarioAuth =
        firebaseAuth.signInWithEmailAndPassword(email, senha).await()
            .user?.toUsuarioAuth() ?: throw Exception("Credenciais inválidas")

    override suspend fun autenticarComToken(idToken: String): UsuarioAuth {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        try {
            return firebaseAuth.signInWithCredential(credential).await()
                .user?.toUsuarioAuth() ?: throw Exception("Falha ao autenticar com provedor externo")
        } catch (e: FirebaseAuthUserCollisionException) {
            throw ColisaoContaException(
                email   = e.email ?: "",
                idToken = idToken,
            )
        } catch (e: FirebaseAuthException) {
            throw Exception(
                e.message?.takeIf { it.isNotBlank() } ?: "Falha ao autenticar com Google. Tente novamente."
            )
        }
    }

    override suspend fun enviarRecuperacaoSenha(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    override suspend fun sair() = firebaseAuth.signOut()

    override suspend fun deletarContaAtual() {
        firebaseAuth.currentUser?.delete()?.await()
    }

    override fun usuarioAtual(): UsuarioAuth? = firebaseAuth.currentUser?.toUsuarioAuth()

    override fun observarEstadoAuth(): Flow<UsuarioAuth?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUsuarioAuth())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    private fun FirebaseUser.toUsuarioAuth() = UsuarioAuth(
        userId = uid,
        email  = email,
        nome   = displayName,
    )
}
