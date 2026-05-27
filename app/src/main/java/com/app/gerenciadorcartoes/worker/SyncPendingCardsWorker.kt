package com.app.gerenciadorcartoes.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.gerenciadorcartoes.repository.CartaoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncPendingCardsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cartaoRepository: CartaoRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("SyncPendingWorker", "Iniciando sync de cartões pendentes")
        val pendentes = cartaoRepository.buscarPendentes()
        if (pendentes.isEmpty()) {
            Log.d("SyncPendingWorker", "Nenhum cartão pendente")
            return Result.success()
        }
        Log.d("SyncPendingWorker", "Encontrados ${pendentes.size} cartões pendentes")
        var hadFailure = false
        for (cartao in pendentes) {
            runCatching { cartaoRepository.sincronizarCartao(cartao) }
                .onFailure { e ->
                    Log.w("SyncPendingWorker", "Falha ao sincronizar cartão id=${cartao.id}", e)
                    hadFailure = true
                }
        }
        Log.d("SyncPendingWorker", "Sync finalizado, hadFailure=$hadFailure")
        return if (hadFailure) Result.retry() else Result.success()
    }
}
