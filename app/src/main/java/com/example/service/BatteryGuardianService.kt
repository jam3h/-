package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.audio.AbuThalamVoiceEngine
import com.example.data.SettingsManager
import com.example.model.AlertLog
import com.example.model.AlertType
import com.example.model.BatteryInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BatteryGuardianService : Service() {

    private val tag = "BatteryGuardianService"
    private val notificationChannelId = "abu_thalam_battery_guardian"
    private val notificationId = 1001

    private lateinit var settingsManager: SettingsManager
    private lateinit var voiceEngine: AbuThalamVoiceEngine

    private var wakeLock: PowerManager.WakeLock? = null
    private var isReceiverRegistered = false

    // State tracking to prevent continuous repeating unless repeat interval is set
    private var highAlertTriggeredCycle = false
    private var lastHighAlertTime = 0L

    private var lowAlertTriggeredCycle = false
    private var lastLowAlertTime = 0L

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> handleBatteryChanged(intent)
                Intent.ACTION_POWER_CONNECTED -> handlePowerConnected()
                Intent.ACTION_POWER_DISCONNECTED -> handlePowerDisconnected()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager.getInstance(this)
        voiceEngine = AbuThalamVoiceEngine.getInstance(this)

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AbuThalam::GuardianWakeLock"
        )

        createNotificationChannel()
        startInForeground()
        registerBatteryReceiver()
        _isServiceRunning.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TEST_HIGH_ALERT -> {
                val settings = settingsManager.getSettings()
                triggerAlert(
                    AlertType.HIGH_BATTERY,
                    settings.highThreshold,
                    settings.highMessage
                )
            }
            ACTION_TEST_LOW_ALERT -> {
                val settings = settingsManager.getSettings()
                triggerAlert(
                    AlertType.LOW_BATTERY,
                    settings.lowThreshold,
                    settings.lowMessage
                )
            }
        }
        return START_STICKY
    }

    private fun registerBatteryReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            registerReceiver(batteryReceiver, filter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterBatteryReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                Log.e(tag, "Error unregistering receiver", e)
            }
            isReceiverRegistered = false
        }
    }

    private fun handleBatteryChanged(intent: Intent) {
        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val level = if (rawLevel >= 0 && scale > 0) (rawLevel * 100) / scale else rawLevel

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val plugType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "شاحن جداري AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "منفذ USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "شاحن لاسلكي"
            else -> if (isCharging) "متصل بالشاحن" else "غير متصل"
        }

        val healthCode = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "ممتازة"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "حرارة مرتفعة!"
            BatteryManager.BATTERY_HEALTH_DEAD -> "تالفة"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "جهد كهربائي زائد"
            BatteryManager.BATTERY_HEALTH_COLD -> "باردة جداً"
            else -> "جيدة"
        }

        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val tempCelsius = tempRaw / 10.0f

        val voltageRaw = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val voltageVolts = if (voltageRaw > 100) voltageRaw / 1000.0f else voltageRaw.toFloat()

        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        val statusText = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "جاري الشحن الآن"
            BatteryManager.BATTERY_STATUS_FULL -> "ممتلئة بالكامل"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "قيد الاستخدام"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "متصل دون شحن"
            else -> "حالة مستقرة"
        }

        val info = BatteryInfo(
            level = level,
            isCharging = isCharging,
            plugType = plugType,
            health = health,
            temperatureCelsius = tempCelsius,
            voltageVolts = voltageVolts,
            technology = tech,
            statusText = statusText
        )

        _batteryInfoState.value = info
        updateNotification(info)
        checkAlertRules(info)
    }

    private fun handlePowerConnected() {
        val settings = settingsManager.getSettings()
        // Reset low alert cycle when user plugs in charger
        lowAlertTriggeredCycle = false

        if (settings.pluggedAlertEnabled) {
            triggerAlert(AlertType.CHARGER_CONNECTED, _batteryInfoState.value.level, settings.pluggedMessage)
        }
    }

    private fun handlePowerDisconnected() {
        val settings = settingsManager.getSettings()
        // Reset high alert cycle when user unplugs charger
        highAlertTriggeredCycle = false

        if (settings.unpluggedAlertEnabled) {
            triggerAlert(AlertType.CHARGER_DISCONNECTED, _batteryInfoState.value.level, settings.unpluggedMessage)
        }
    }

    private fun checkAlertRules(info: BatteryInfo) {
        val settings = settingsManager.getSettings()
        val now = System.currentTimeMillis()

        // 1. HIGH BATTERY ALERT (default 85% when charging)
        if (settings.highAlertEnabled && info.isCharging && info.level >= settings.highThreshold) {
            val repeatIntervalMillis = settings.repeatIntervalMinutes * 60 * 1000L
            val shouldAlert = !highAlertTriggeredCycle ||
                    (settings.repeatIntervalMinutes > 0 && (now - lastHighAlertTime >= repeatIntervalMillis))

            if (shouldAlert) {
                highAlertTriggeredCycle = true
                lastHighAlertTime = now
                val message = formatMessage(settings.highMessage, info.level)
                triggerAlert(AlertType.HIGH_BATTERY, info.level, message)
            }
        } else if (!info.isCharging || info.level < (settings.highThreshold - 2)) {
            // Reset cycle if user unplugs or battery drops
            highAlertTriggeredCycle = false
        }

        // 2. LOW BATTERY ALERT (default 20% when discharging)
        if (settings.lowAlertEnabled && !info.isCharging && info.level <= settings.lowThreshold) {
            val repeatIntervalMillis = settings.repeatIntervalMinutes * 60 * 1000L
            val shouldAlert = !lowAlertTriggeredCycle ||
                    (settings.repeatIntervalMinutes > 0 && (now - lastLowAlertTime >= repeatIntervalMillis))

            if (shouldAlert) {
                lowAlertTriggeredCycle = true
                lastLowAlertTime = now
                val message = formatMessage(settings.lowMessage, info.level)
                triggerAlert(AlertType.LOW_BATTERY, info.level, message)
            }
        } else if (info.isCharging || info.level > (settings.lowThreshold + 2)) {
            // Reset cycle if user starts charging or battery rises
            lowAlertTriggeredCycle = false
        }
    }

    private fun formatMessage(template: String, level: Int): String {
        return if (template.contains("%d")) {
            try {
                String.format(template, level)
            } catch (e: Exception) {
                template
            }
        } else {
            template
        }
    }

    private fun triggerAlert(type: AlertType, level: Int, message: String) {
        val settings = settingsManager.getSettings()

        // Acquire wake lock briefly so CPU remains awake while voice is speaking
        wakeLock?.acquire(8000)

        // Haptic feedback
        if (settings.vibrationEnabled) {
            vibrateAlert()
        }

        // Voice announcement
        voiceEngine.speak(
            text = message,
            speechRate = settings.speechRate,
            pitch = settings.speechPitch,
            playChimeFirst = true
        )

        // Record in history log
        settingsManager.addLog(
            AlertLog(
                type = type,
                batteryLevel = level,
                message = message
            )
        )
    }

    private fun vibrateAlert() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                val pattern = longArrayOf(0, 400, 200, 400, 200, 400)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val pattern = longArrayOf(0, 400, 200, 400, 200, 400)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Vibration failed", e)
        }
    }

    private fun startInForeground() {
        val notification = buildNotification(_batteryInfoState.value)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(notificationId, notification)
        }
    }

    private fun updateNotification(info: BatteryInfo) {
        val notification = buildNotification(info)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(notificationId, notification)
    }

    private fun buildNotification(info: BatteryInfo): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, BatteryGuardianService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = "أبو ظلام • ${info.level}% (${if (info.isCharging) "جاري الشحن ⚡" else "حارس نشط 🛡️"})"
        val content = if (info.isCharging) {
            "درع أبو ظلام يراقب الشحن: سينبهك صوتياً عند وصول ${settingsManager.getSettings().highThreshold}%"
        } else {
            "درع أبو ظلام يراقب البطارية: سينبهك صوتياً عند انخفاضها إلى ${settingsManager.getSettings().lowThreshold}%"
        }

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setProgress(100, info.level, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إيقاف الحارس", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "درع أبو ظلام لمراقبة البطارية",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار مستمر لحارس بطارية أبو ظلام وتنبيهات الصوت العربية"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBatteryReceiver()
        _isServiceRunning.value = false
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP_SERVICE = "com.example.service.ACTION_STOP_SERVICE"
        const val ACTION_TEST_HIGH_ALERT = "com.example.service.ACTION_TEST_HIGH_ALERT"
        const val ACTION_TEST_LOW_ALERT = "com.example.service.ACTION_TEST_LOW_ALERT"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _batteryInfoState = MutableStateFlow(BatteryInfo())
        val batteryInfoState: StateFlow<BatteryInfo> = _batteryInfoState.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, BatteryGuardianService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BatteryGuardianService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
