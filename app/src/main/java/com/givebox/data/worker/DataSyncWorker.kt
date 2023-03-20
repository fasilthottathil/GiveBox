package com.givebox.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.givebox.R
import com.givebox.data.repository.CategoryRepositoryImpl
import com.givebox.data.repository.ProductRepositoryImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Fasil on 24/11/22.
 */
@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val productRepository: ProductRepositoryImpl,
    private val categoryRepository: CategoryRepositoryImpl
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {

        showNotification()

        CoroutineScope(Dispatchers.IO).launch {
            productRepository.getProductsFromServer()
            categoryRepository.getAllCategoriesFromServer()
        }.join()

        return Result.success()
    }

    private fun showNotification() {
        val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setSound(null,null)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)

            }
            builder.setChannelId(NOTIFICATION_CHANNEL_ID)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        builder.apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setOngoing(true)
            setSound(null)
            setContentTitle("Syncing")
            setContentText("Updating data...")
        }

        notificationManager.notify(NOTIFICATION_ID,builder.build())

    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "givebox-sync-channel"
        const val NOTIFICATION_CHANNEL_NAME = "Sync update"
        const val NOTIFICATION_ID = 1
    }

}