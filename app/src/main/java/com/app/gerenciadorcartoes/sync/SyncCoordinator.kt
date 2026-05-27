package com.app.gerenciadorcartoes.sync

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.app.gerenciadorcartoes.worker.SyncPendingCardsWorker
import java.util.UUID
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncCoordinator @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    fun scheduleSync(): UUID? {
        return runCatching {
            val work = OneTimeWorkRequestBuilder<SyncPendingCardsWorker>().build()
            val id = work.id
            WorkManager.getInstance(appContext)
                .enqueueUniqueWork("sync_pending_cards", ExistingWorkPolicy.KEEP, work)
            android.util.Log.d("SyncCoordinator", "Sync agendado, workId=$id")
            id
        }.onFailure { e ->
            android.util.Log.w("SyncCoordinator", "Não foi possível agendar WorkManager para sync", e)
        }.getOrNull()
    }
}
