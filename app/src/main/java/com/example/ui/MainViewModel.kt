package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AbuThalamVoiceEngine
import com.example.data.SettingsManager
import com.example.model.AlertLog
import com.example.model.AlertType
import com.example.model.BatteryInfo
import com.example.model.GuardianSettings
import com.example.service.BatteryGuardianService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager.getInstance(application)
    private val voiceEngine = AbuThalamVoiceEngine.getInstance(application)

    val settings: StateFlow<GuardianSettings> = settingsManager.settingsFlow
    val logs: StateFlow<List<AlertLog>> = settingsManager.logsFlow
    val voiceEngineState: StateFlow<AbuThalamVoiceEngine.VoiceEngineState> = voiceEngine.engineState
    val isServiceRunning: StateFlow<Boolean> = BatteryGuardianService.isServiceRunning

    private val _currentBattery = MutableStateFlow(readCurrentBatteryInfo(application))
    val currentBattery: StateFlow<BatteryInfo> = _currentBattery.asStateFlow()

    init {
        // Collect live updates from service if active
        viewModelScope.launch {
            BatteryGuardianService.batteryInfoState.collect { info ->
                if (info.level >= 0) {
                    _currentBattery.value = info
                }
            }
        }

        // Auto start service if enabled
        if (settings.value.serviceEnabled && !isServiceRunning.value) {
            startGuardianService()
        }
    }

    fun refreshBatteryInfo() {
        _currentBattery.value = readCurrentBatteryInfo(getApplication())
    }

    fun toggleService(enabled: Boolean) {
        val updated = settings.value.copy(serviceEnabled = enabled)
        settingsManager.saveSettings(updated)
        if (enabled) {
            startGuardianService()
        } else {
            stopGuardianService()
        }
    }

    fun startGuardianService() {
        BatteryGuardianService.start(getApplication())
    }

    fun stopGuardianService() {
        BatteryGuardianService.stop(getApplication())
    }

    fun updateHighSettings(enabled: Boolean, threshold: Int, message: String) {
        val updated = settings.value.copy(
            highAlertEnabled = enabled,
            highThreshold = threshold,
            highMessage = message
        )
        settingsManager.saveSettings(updated)
    }

    fun updateLowSettings(enabled: Boolean, threshold: Int, message: String) {
        val updated = settings.value.copy(
            lowAlertEnabled = enabled,
            lowThreshold = threshold,
            lowMessage = message
        )
        settingsManager.saveSettings(updated)
    }

    fun updateAudioAndGeneralSettings(
        vibration: Boolean,
        speechRate: Float,
        speechPitch: Float,
        repeatInterval: Int,
        startOnBoot: Boolean,
        pluggedAlert: Boolean,
        unpluggedAlert: Boolean
    ) {
        val updated = settings.value.copy(
            vibrationEnabled = vibration,
            speechRate = speechRate,
            speechPitch = speechPitch,
            repeatIntervalMinutes = repeatInterval,
            startOnBoot = startOnBoot,
            pluggedAlertEnabled = pluggedAlert,
            unpluggedAlertEnabled = unpluggedAlert
        )
        settingsManager.saveSettings(updated)
    }

    fun testHighAlert() {
        val currentSettings = settings.value
        val formatted = if (currentSettings.highMessage.contains("%d")) {
            String.format(currentSettings.highMessage, currentSettings.highThreshold)
        } else {
            currentSettings.highMessage
        }

        voiceEngine.speak(
            text = formatted,
            speechRate = currentSettings.speechRate,
            pitch = currentSettings.speechPitch,
            playChimeFirst = true
        )

        settingsManager.addLog(
            AlertLog(
                type = AlertType.TEST_ALERT,
                batteryLevel = currentSettings.highThreshold,
                message = formatted
            )
        )
    }

    fun testLowAlert() {
        val currentSettings = settings.value
        val formatted = if (currentSettings.lowMessage.contains("%d")) {
            String.format(currentSettings.lowMessage, currentSettings.lowThreshold)
        } else {
            currentSettings.lowMessage
        }

        voiceEngine.speak(
            text = formatted,
            speechRate = currentSettings.speechRate,
            pitch = currentSettings.speechPitch,
            playChimeFirst = true
        )

        settingsManager.addLog(
            AlertLog(
                type = AlertType.TEST_ALERT,
                batteryLevel = currentSettings.lowThreshold,
                message = formatted
            )
        )
    }

    fun stopSpeaking() {
        voiceEngine.stop()
    }

    fun clearLogs() {
        settingsManager.clearLogs()
    }

    private fun readCurrentBatteryInfo(context: Context): BatteryInfo {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

        if (batteryStatus != null) {
            val rawLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val level = if (rawLevel >= 0 && scale > 0) (rawLevel * 100) / scale else 85

            val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val plugType = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "شاحن جداري AC"
                BatteryManager.BATTERY_PLUGGED_USB -> "منفذ USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "شاحن لاسلكي"
                else -> if (isCharging) "متصل بالشاحن" else "غير متصل"
            }

            val healthCode = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val health = when (healthCode) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "ممتازة"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "حرارة مرتفعة!"
                BatteryManager.BATTERY_HEALTH_DEAD -> "تالفة"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "جهد كهربائي زائد"
                BatteryManager.BATTERY_HEALTH_COLD -> "باردة جداً"
                else -> "جيدة"
            }

            val tempRaw = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val tempCelsius = if (tempRaw > 0) tempRaw / 10.0f else 31.0f

            val voltageRaw = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val voltageVolts = if (voltageRaw > 100) voltageRaw / 1000.0f else 4.05f

            val tech = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

            val statusText = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "جاري الشحن الآن ⚡"
                BatteryManager.BATTERY_STATUS_FULL -> "ممتلئة بالكامل"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "قيد الاستخدام"
                else -> "حالة مستقرة"
            }

            return BatteryInfo(
                level = level,
                isCharging = isCharging,
                plugType = plugType,
                health = health,
                temperatureCelsius = tempCelsius,
                voltageVolts = voltageVolts,
                technology = tech,
                statusText = statusText
            )
        }

        return BatteryInfo()
    }
}
