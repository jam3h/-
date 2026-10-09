package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.SettingsManager
import com.example.service.BatteryGuardianService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val settings = SettingsManager.getInstance(context).getSettings()
            if (settings.serviceEnabled && settings.startOnBoot) {
                BatteryGuardianService.start(context)
            }
        }
    }
}
