package com.example.model

data class GuardianSettings(
    val serviceEnabled: Boolean = true,
    val highAlertEnabled: Boolean = true,
    val highThreshold: Int = 85,
    val highMessage: String = "أَبُو ظَلَام: تَمَّ شَحْنُ الهَاتِفِ 85 بِالمِئَة! أَزِل الشَّاحِنَ مِنَ الهَاتِفِ فَوْرَاً!",
    val lowAlertEnabled: Boolean = true,
    val lowThreshold: Int = 20,
    val lowMessage: String = "أَبُو ظَلَام: اِشْحَنْ هَاتِفَكَ بِسُرْعَة! البَطَّارِيَّةُ 20 بِالمِئَة!",
    val pluggedAlertEnabled: Boolean = false,
    val pluggedMessage: String = "أَبُو ظَلَام: تَمَّ تَوْصِيلُ الشَّاحِنِ بِنَجَاح",
    val unpluggedAlertEnabled: Boolean = false,
    val unpluggedMessage: String = "أَبُو ظَلَام: تَمَّ فَصْلُ الشَّاحِن",
    val vibrationEnabled: Boolean = true,
    val voiceProfile: String = "MONSTER", // "MONSTER", "DEEP", "STANDARD"
    val speechRate: Float = 0.88f, // Deep and deliberate
    val speechPitch: Float = 0.58f, // Deep beast / monster pitch
    val repeatIntervalMinutes: Int = 2,
    val startOnBoot: Boolean = true
) {
    /**
     * Resolves high message dynamically replacing any previous number or placeholder
     * with the current highThreshold so if user changes to 80% or 90%, it speaks that exact number!
     */
    fun resolveHighMessage(level: Int = highThreshold): String {
        return formatPercentageInText(highMessage, level, defaultPrefix = "أَبُو ظَلَام: تَمَّ شَحْنُ الهَاتِفِ $level بِالمِئَة! أَزِل الشَّاحِنَ مِنَ الهَاتِفِ فَوْرَاً!")
    }

    /**
     * Resolves low message dynamically replacing any previous number or placeholder
     * with current lowThreshold so if user changes to 15% or 20%, it speaks that exact number!
     */
    fun resolveLowMessage(level: Int = lowThreshold): String {
        return formatPercentageInText(lowMessage, level, defaultPrefix = "أَبُو ظَلَام: اِشْحَنْ هَاتِفَكَ بِسُرْعَة! البَطَّارِيَّةُ $level بِالمِئَة!")
    }

    companion object {
        fun formatPercentageInText(template: String, percentage: Int, defaultPrefix: String): String {
            if (template.isBlank()) return defaultPrefix
            return if (template.contains("%d")) {
                try {
                    String.format(template, percentage)
                } catch (e: Exception) {
                    template
                }
            } else if (template.contains("{percent}")) {
                template.replace("{percent}", percentage.toString())
            } else if (template.contains(Regex("\\b\\d{1,3}\\b"))) {
                // Replace the first occurrence of digits with current percentage
                template.replaceFirst(Regex("\\b\\d{1,3}\\b"), percentage.toString())
            } else {
                "$template ($percentage بالمئة)"
            }
        }
    }
}
