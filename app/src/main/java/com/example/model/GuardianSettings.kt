package com.example.model

data class GuardianSettings(
    val serviceEnabled: Boolean = true,
    val highAlertEnabled: Boolean = true,
    val highThreshold: Int = 85,
    val highMessage: String = "أبو ظلام، تم شحن الهاتف 85 بالمئة، أزل الشاحن من الهاتف",
    val lowAlertEnabled: Boolean = true,
    val lowThreshold: Int = 20,
    val lowMessage: String = "أبو ظلام، اشحن هاتفك بسرعة",
    val pluggedAlertEnabled: Boolean = false,
    val pluggedMessage: String = "أبو ظلام، تم توصيل الشاحن بنجاح",
    val unpluggedAlertEnabled: Boolean = false,
    val unpluggedMessage: String = "أبو ظلام، تم فصل الشاحن",
    val vibrationEnabled: Boolean = true,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val repeatIntervalMinutes: Int = 2, // 0 = once, 1 = every min, 2 = every 2 mins
    val startOnBoot: Boolean = true
)
