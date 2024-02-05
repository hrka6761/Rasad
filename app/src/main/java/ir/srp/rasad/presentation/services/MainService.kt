package ir.srp.rasad.presentation.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import ir.srp.rasad.R
import ir.srp.rasad.core.Constants.SERVICE_INTENT_EXTRA_KEY
import ir.srp.rasad.core.Constants.START_SERVICE_DATA
import ir.srp.rasad.core.Constants.STOP_SERVICE_DATA

class MainService : Service() {

    private val CHANNEL_ID = "Rasad"
    private val NOTIFICATION_ID = 110
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private val serviceMessenger: Messenger? = null
    private lateinit var homeMessenger: Messenger
    private val handler = Handler()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
        homeMessenger = Messenger(handler)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val data = intent?.getStringExtra(SERVICE_INTENT_EXTRA_KEY)
        when (data) {
            START_SERVICE_DATA -> {
                startForeground(
                    NOTIFICATION_ID,
                    createNotification(
                        getString(R.string.app_name),
                        "App is running in the background.",
                        true
                        )
                )
            }

            STOP_SERVICE_DATA -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification(
                        getString(R.string.app_name),
                        "App is stoped from the background.",
                        false
                    )
                )
            }
            else -> {}
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = homeMessenger.binder

    override fun onDestroy() {
        super.onDestroy()
    }


    private fun createNotification(title: String, message: String, onGoing: Boolean): Notification {
        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setOngoing(onGoing)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
    }


    @SuppressLint("HandlerLeak")
    private inner class Handler : android.os.Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg) {
                else -> super.handleMessage(msg)
            }
        }
    }
}