package com.example.model

data class AlertLog(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: AlertType = AlertType.HIGH_BATTERY,
    val batteryLevel: Int = 85,
    val message: String = ""
)

enum class AlertType(val titleAr: String) {
    HIGH_BATTERY("اكتمال الشحن (85%)"),
    LOW_BATTERY("انخفاض البطارية (20%)"),
    CHARGER_CONNECTED("توصيل الشاحن"),
    CHARGER_DISCONNECTED("فصل الشاحن"),
    TEST_ALERT("تجربة الصوت")
}
