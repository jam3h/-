package com.example.model

data class BatteryInfo(
    val level: Int = 100,
    val isCharging: Boolean = false,
    val plugType: String = "غير متصل",
    val health: String = "ممتازة",
    val temperatureCelsius: Float = 25.0f,
    val voltageVolts: Float = 4.0f,
    val technology: String = "Li-ion",
    val statusText: String = "جيد"
)
