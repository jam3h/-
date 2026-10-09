package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SettingsManager
import com.example.model.AlertLog
import com.example.model.AlertType
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
        assertTrue(settings.highMessage.contains("أبو ظلام"))
        assertTrue(settings.highMessage.contains("85"))
        assertTrue(settings.lowMessage.contains("أبو ظلام"))
        assertTrue(settings.lowMessage.contains("اشحن"))
    }

    @Test
    fun `verify alert logs recording and clearing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsManager = SettingsManager.getInstance(context)

        settingsManager.clearLogs()
        assertEquals(0, settingsManager.logsFlow.value.size)

        val log = AlertLog(
            type = AlertType.HIGH_BATTERY,
            batteryLevel = 85,
            message = "أبو ظلام، تم شحن الهاتف 85 بالمئة، أزل الشاحن من الهاتف"
        )
        settingsManager.addLog(log)

        assertEquals(1, settingsManager.logsFlow.value.size)
        assertEquals(85, settingsManager.logsFlow.value[0].batteryLevel)
        assertEquals(AlertType.HIGH_BATTERY, settingsManager.logsFlow.value[0].type)
    }
}
