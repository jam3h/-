package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AlertLog
import com.example.model.AlertType
import com.example.model.GuardianSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("abu_thalam_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<GuardianSettings> = _settingsFlow.asStateFlow()

    private val _logsFlow = MutableStateFlow(loadLogs())
    val logsFlow: StateFlow<List<AlertLog>> = _logsFlow.asStateFlow()

    fun getSettings(): GuardianSettings {
        return _settingsFlow.value
    }

    fun saveSettings(settings: GuardianSettings) {
        prefs.edit()
            .putBoolean("service_enabled", settings.serviceEnabled)
            .putBoolean("high_alert_enabled", settings.highAlertEnabled)
            .putInt("high_threshold", settings.highThreshold)
            .putString("high_message", settings.highMessage)
            .putBoolean("low_alert_enabled", settings.lowAlertEnabled)
            .putInt("low_threshold", settings.lowThreshold)
            .putString("low_message", settings.lowMessage)
            .putBoolean("plugged_alert_enabled", settings.pluggedAlertEnabled)
            .putString("plugged_message", settings.pluggedMessage)
            .putBoolean("unplugged_alert_enabled", settings.unpluggedAlertEnabled)
            .putString("unplugged_message", settings.unpluggedMessage)
            .putBoolean("vibration_enabled", settings.vibrationEnabled)
            .putString("voice_profile", settings.voiceProfile)
            .putFloat("speech_rate", settings.speechRate)
            .putFloat("speech_pitch", settings.speechPitch)
            .putInt("repeat_interval", settings.repeatIntervalMinutes)
            .putBoolean("start_on_boot", settings.startOnBoot)
            .apply()
        _settingsFlow.value = settings
    }

    private fun loadSettings(): GuardianSettings {
        val highThresh = prefs.getInt("high_threshold", 85)
        val lowThresh = prefs.getInt("low_threshold", 20)
        val defaultHighMsg = "أَبُو ظَلَام: تَمَّ شَحْنُ الهَاتِفِ $highThresh بِالمِئَة! أَزِل الشَّاحِنَ مِنَ الهَاتِفِ فَوْرَاً!"
        val defaultLowMsg = "أَبُو ظَلَام: اِشْحَنْ هَاتِفَكَ بِسُرْعَة! البَطَّارِيَّةُ $lowThresh بِالمِئَة!"

        return GuardianSettings(
            serviceEnabled = prefs.getBoolean("service_enabled", true),
            highAlertEnabled = prefs.getBoolean("high_alert_enabled", true),
            highThreshold = highThresh,
            highMessage = prefs.getString("high_message", defaultHighMsg) ?: defaultHighMsg,
            lowAlertEnabled = prefs.getBoolean("low_alert_enabled", true),
            lowThreshold = lowThresh,
            lowMessage = prefs.getString("low_message", defaultLowMsg) ?: defaultLowMsg,
            pluggedAlertEnabled = prefs.getBoolean("plugged_alert_enabled", false),
            pluggedMessage = prefs.getString(
                "plugged_message",
                "أَبُو ظَلَام: تَمَّ تَوْصِيلُ الشَّاحِنِ بِنَجَاح"
            ) ?: "أَبُو ظَلَام: تَمَّ تَوْصِيلُ الشَّاحِنِ بِنَجَاح",
            unpluggedAlertEnabled = prefs.getBoolean("unplugged_alert_enabled", false),
            unpluggedMessage = prefs.getString(
                "unplugged_message",
                "أَبُو ظَلَام: تَمَّ فَصْلُ الشَّاحِن"
            ) ?: "أَبُو ظَلَام: تَمَّ فَصْلُ الشَّاحِن",
            vibrationEnabled = prefs.getBoolean("vibration_enabled", true),
            voiceProfile = prefs.getString("voice_profile", "MONSTER") ?: "MONSTER",
            speechRate = prefs.getFloat("speech_rate", 0.88f),
            speechPitch = prefs.getFloat("speech_pitch", 0.58f),
            repeatIntervalMinutes = prefs.getInt("repeat_interval", 2),
            startOnBoot = prefs.getBoolean("start_on_boot", true)
        )
    }

    fun addLog(log: AlertLog) {
        val current = _logsFlow.value.toMutableList()
        current.add(0, log)
        if (current.size > 50) {
            current.removeAt(current.lastIndex)
        }
        _logsFlow.value = current
        saveLogs(current)
    }

    fun clearLogs() {
        _logsFlow.value = emptyList()
        prefs.edit().remove("alert_logs_json").apply()
    }

    private fun saveLogs(logs: List<AlertLog>) {
        val array = JSONArray()
        for (log in logs) {
            val obj = JSONObject()
            obj.put("id", log.id)
            obj.put("timestamp", log.timestamp)
            obj.put("type", log.type.name)
            obj.put("level", log.batteryLevel)
            obj.put("message", log.message)
            array.put(obj)
        }
        prefs.edit().putString("alert_logs_json", array.toString()).apply()
    }

    private fun loadLogs(): List<AlertLog> {
        val jsonStr = prefs.getString("alert_logs_json", null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<AlertLog>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeName = obj.optString("type", AlertType.HIGH_BATTERY.name)
                val alertType = try {
                    AlertType.valueOf(typeName)
                } catch (e: Exception) {
                    AlertType.HIGH_BATTERY
                }
                list.add(
                    AlertLog(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        type = alertType,
                        batteryLevel = obj.optInt("level", 85),
                        message = obj.optString("message", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
