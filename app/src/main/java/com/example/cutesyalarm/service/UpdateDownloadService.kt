package com.example.cutesyalarm.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.cutesyalarm.MainActivity
import com.example.cutesyalarm.R
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateDownloadService : Service() {

    companion object {
        const val CHANNEL_ID = "ota_update_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_DOWNLOAD_UPDATE = "com.example.cutesyalarm.DOWNLOAD_UPDATE"
        const val EXTRA_DOWNLOAD_URL = "download_url"
        const val EXTRA_VERSION_NAME = "version_name"

        fun startDownload(context: Context, downloadUrl: String, versionName: String) {
            val intent = Intent(context, UpdateDownloadService::class.java).apply {
                action = ACTION_DOWNLOAD_UPDATE
                putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                putExtra(EXTRA_VERSION_NAME, versionName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DOWNLOAD_UPDATE) {
            val downloadUrl = intent.getStringExtra(EXTRA_DOWNLOAD_URL) ?: return START_NOT_STICKY
            val versionName = intent.getStringExtra(EXTRA_VERSION_NAME) ?: "New Version"

            startForeground(NOTIFICATION_ID, createProgressNotification(versionName, 0))

            serviceScope.launch {
                try {
                    downloadAndInstallApk(downloadUrl, versionName)
                } catch (e: Exception) {
                    showErrorNotification(versionName, e.message ?: "Download failed")
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun downloadAndInstallApk(downloadUrl: String, versionName: String) {
        withContext(Dispatchers.IO) {
            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 30000
                readTimeout = 30000
                setRequestProperty("Accept", "application/vnd.android.package-archive")
            }

            connection.connect()
            val fileLength = connection.contentLength

            // Create update directory
            val updateDir = File(externalCacheDir, "updates").apply {
                if (!exists()) mkdirs()
            }

            val apkFile = File(updateDir, "update_$versionName.apk")

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var totalBytes: Long = 0
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead

                        // Update progress notification every 5%
                        val progress = if (fileLength > 0) {
                            ((totalBytes * 100) / fileLength).toInt()
                        } else 0

                        if (progress % 5 == 0) {
                            updateNotification(versionName, progress)
                        }
                    }
                }
            }

            connection.disconnect()

            // Download complete, show install notification
            showInstallNotification(versionName, apkFile)
            stopSelf()
        }
    }

    private fun createProgressNotification(versionName: String, progress: Int): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading Ira's Cute Alarm $versionName")
            .setContentText("Progress: $progress%")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(versionName: String, progress: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading Ira's Cute Alarm $versionName")
            .setContentText("Progress: $progress%")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showInstallNotification(versionName: String, apkFile: File) {
        val apkUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Complete!")
            .setContentText("Tap to install Ira's Cute Alarm $versionName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "Install Now", pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 1, notification)

        // Also trigger the install automatically
        try {
            startActivity(installIntent)
        } catch (e: Exception) {
            // User will need to tap notification
        }
    }

    private fun showErrorNotification(versionName: String, error: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Update Failed")
            .setContentText("Could not download version $versionName: $error")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(false)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for app updates"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
