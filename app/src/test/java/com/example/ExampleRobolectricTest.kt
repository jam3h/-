package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SettingsManager
import com.example.model.AlertLog
import com.example.model.AlertType
import com.example.model.GuardianSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource is Abu Thalam`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("أبو ظلام", appName)
    }

    @Test
    fun `verify default battery alert settings meet user requirements`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsManager = SettingsManager.getInstance(context)
        val settings = settingsManager.getSettings()

        // Requirements: 85% high alert, 20% low alert
        assertEquals(85, settings.highThreshold)
        assertEquals(20, settings.lowThreshold)
        assertTrue(settings.highAlertEnabled)
        assertTrue(settings.lowAlertEnabled)
        assertTrue(settings.highMessage.contains("ظَلَام") || settings.highMessage.contains("ظلام"))
        assertTrue(settings.lowMessage.contains("ظَلَام") || settings.lowMessage.contains("ظلام"))
    }

    @Test
    fun `verify dynamic percentage resolution when changing to 80 or 90`() {
        val settings = GuardianSettings(
            highThreshold = 80,
            highMessage = "أَبُو ظَلَام: تَمَّ شَحْنُ الهَاتِفِ 85 بِالمِئَة! أَزِل الشَّاحِنَ مِنَ الهَاتِفِ فَوْرَاً!"
        )

        // When threshold is changed to 80, resolved message must say 80!
        val msg80 = settings.resolveHighMessage(80)
        assertTrue("Message should contain 80 but was: $msg80", msg80.contains("80"))

        // When threshold is changed to 90, resolved message must say 90!
        val msg90 = settings.resolveHighMessage(90)
        assertTrue("Message should contain 90 but was: $msg90", msg90.contains("90"))

        // For low threshold 15, resolved message must say 15!
        val lowSettings = GuardianSettings(
            lowThreshold = 15,
            lowMessage = "أَبُو ظَلَام: اِشْحَنْ هَاتِفَكَ بِسُرْعَة! البَطَّارِيَّةُ 20 بِالمِئَة!"
        )
        val msg15 = lowSettings.resolveLowMessage(15)
        assertTrue("Message should contain 15 but was: $msg15", msg15.contains("15"))
    }

    @Test
    fun `verify monster voice profile has deep beast pitch`() {
        val settings = GuardianSettings()
        assertEquals("MONSTER", settings.voiceProfile)
        assertTrue("Pitch should be deep beast pitch (<0.65) but was ${settings.speechPitch}", settings.speechPitch < 0.65f)
        assertTrue("Speech rate should be deliberate (<0.95) but was ${settings.speechRate}", settings.speechRate < 0.95f)
    }

    @Test
    fun `verify alert logs recording and clearing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsManager = SettingsManager.getInstance(context)

        settingsManager.clearLogs()
        assertEquals(0, settingsManager.logsFlow.value.size)

        val log = AlertLog(
            type = AlertType.HIGH_BATTERY,
            batteryLevel = 90,
            message = "أَبُو ظَلَام: تَمَّ شَحْنُ الهَاتِفِ 90 بِالمِئَة! أَزِل الشَّاحِنَ مِنَ الهَاتِفِ فَوْرَاً!"
        )
        settingsManager.addLog(log)

        assertEquals(1, settingsManager.logsFlow.value.size)
        assertEquals(90, settingsManager.logsFlow.value[0].batteryLevel)
        assertEquals(AlertType.HIGH_BATTERY, settingsManager.logsFlow.value[0].type)
    }
}
