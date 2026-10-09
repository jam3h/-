package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GuardianSettings
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    settings: GuardianSettings,
    onSaveHighSettings: (Boolean, Int, String) -> Unit,
    onSaveLowSettings: (Boolean, Int, String) -> Unit,
    onSaveAudioAndGeneral: (
        vibration: Boolean,
        speechRate: Float,
        speechPitch: Float,
        repeatInterval: Int,
        startOnBoot: Boolean,
        pluggedAlert: Boolean,
        unpluggedAlert: Boolean
    ) -> Unit,
    onSetVoiceProfile: (String) -> Unit = {},
    onTestHigh: () -> Unit,
    onTestLow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var highEnabled by remember(settings.highAlertEnabled) { mutableStateOf(settings.highAlertEnabled) }
    var highThreshold by remember(settings.highThreshold) { mutableIntStateOf(settings.highThreshold) }
    var highMessage by remember(settings.highMessage, settings.highThreshold) {
        mutableStateOf(settings.resolveHighMessage(settings.highThreshold))
    }

    var lowEnabled by remember(settings.lowAlertEnabled) { mutableStateOf(settings.lowAlertEnabled) }
    var lowThreshold by remember(settings.lowThreshold) { mutableIntStateOf(settings.lowThreshold) }
    var lowMessage by remember(settings.lowMessage, settings.lowThreshold) {
        mutableStateOf(settings.resolveLowMessage(settings.lowThreshold))
    }

    var vibration by remember(settings.vibrationEnabled) { mutableStateOf(settings.vibrationEnabled) }
    var voiceProfile by remember(settings.voiceProfile) { mutableStateOf(settings.voiceProfile) }
    var speechRate by remember(settings.speechRate) { mutableFloatStateOf(settings.speechRate) }
    var speechPitch by remember(settings.speechPitch) { mutableFloatStateOf(settings.speechPitch) }
    var repeatInterval by remember(settings.repeatIntervalMinutes) { mutableIntStateOf(settings.repeatIntervalMinutes) }
    var startOnBoot by remember(settings.startOnBoot) { mutableStateOf(settings.startOnBoot) }
    var pluggedAlert by remember(settings.pluggedAlertEnabled) { mutableStateOf(settings.pluggedAlertEnabled) }
    var unpluggedAlert by remember(settings.unpluggedAlertEnabled) { mutableStateOf(settings.unpluggedAlertEnabled) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⚙️ إعدادات تنبيهات ونبرة صوت أبو ظلام",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        )

        // SPECIAL SECTION: Beast / Monster Voice Profile ("صوت قوي كوحش")
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.5.dp, CyberAmber.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyberAmber.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = CyberAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "نبرة صوت أبو ظلام (صوت الوحش 👹)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberAmber
                            )
                        )
                        Text(
                            text = "نبرة قوية، جهيرة وضخمة مع صدى وحش جهوري",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                // Voice Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val profiles = listOf(
                        Triple("MONSTER", "صوت وحش كاسر 👹", 0.58f to 0.88f),
                        Triple("DEEP", "حارس الظلام ⚡", 0.72f to 0.95f),
                        Triple("STANDARD", "صوت طبيعي 🎙️", 1.0f to 1.0f)
                    )

                    profiles.forEach { (key, label, params) ->
                        ElevatedFilterChip(
                            selected = voiceProfile == key,
                            onClick = {
                                voiceProfile = key
                                speechPitch = params.first
                                speechRate = params.second
                                onSetVoiceProfile(key)
                                onSaveAudioAndGeneral(
                                    vibration, params.second, params.first, repeatInterval,
                                    startOnBoot, pluggedAlert, unpluggedAlert
                                )
                            },
                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                selectedContainerColor = CyberAmber,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Pitch and speed sliders
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "طبقة الصوت (الجهارة والغلظة):",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = if (speechPitch < 0.65f) "خشن وضخم كوحش 👹 (${String.format("%.2f", speechPitch)})"
                            else "${String.format("%.2f", speechPitch)}x",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberAmber
                            )
                        )
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = {
                            speechPitch = it
                            voiceProfile = "CUSTOM"
                            onSaveAudioAndGeneral(
                                vibration, speechRate, it, repeatInterval,
                                startOnBoot, pluggedAlert, unpluggedAlert
                            )
                        },
                        valueRange = 0.5f..1.3f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberAmber,
                            activeTrackColor = CyberAmber,
                            inactiveTrackColor = DarkSurfaceVariant
                        )
                    )
                }
            }
        }

        // 1. High Battery Alert Card (Dynamic Threshold)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "تنبيه اكتمال الشحن ($highThreshold%)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "ينطق صوت أبو ظلام بالنسبة المحددة فور وصولها",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    Switch(
                        checked = highEnabled,
                        onCheckedChange = {
                            highEnabled = it
                            onSaveHighSettings(it, highThreshold, highMessage)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricCyan,
                            checkedTrackColor = ElectricCyan.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("high_alert_switch")
                    )
                }

                if (highEnabled) {
                    // Quick Level Presets (80%, 85%, 90%, 95%, 100%)
                    Text(
                        text = "اختر نسبة الشحن المستهدفة (سيتغير الصوت معها فوراً):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(80, 85, 90, 95, 100).forEach { pct ->
                            ElevatedFilterChip(
                                selected = highThreshold == pct,
                                onClick = {
                                    highThreshold = pct
                                    // Update message text dynamically so it mentions the exact new percentage!
                                    val updatedMsg = GuardianSettings.formatPercentageInText(
                                        template = highMessage,
                                        percentage = pct,
                                        defaultPrefix = "أَبُو ظَلَام: تَمَّ شَحْنُ الهَاتِفِ $pct بِالمِئَة! أَزِل الشَّاحِنَ مِنَ الهَاتِفِ فَوْرَاً!"
                                    )
                                    highMessage = updatedMsg
                                    onSaveHighSettings(highEnabled, pct, updatedMsg)
                                },
                                label = { Text("$pct%", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = ElectricCyan,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Message Text Field with live preview
                    OutlinedTextField(
                        value = highMessage,
                        onValueChange = {
                            highMessage = it
                            onSaveHighSettings(highEnabled, highThreshold, it)
                        },
                        label = { Text("العبارة المنطوقة بصوت أبو ظلام عند $highThreshold%") },
                        trailingIcon = {
                            IconButton(onClick = {
                                val resetMsg = "أَبُو ظَلَام: تَمَّ شَحْنُ الهَاتِفِ $highThreshold بِالمِئَة! أَزِل الشَّاحِنَ مِنَ الهَاتِفِ فَوْرَاً!"
                                highMessage = resetMsg
                                onSaveHighSettings(highEnabled, highThreshold, resetMsg)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "استعادة العبارة الأصلية",
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("high_message_input")
                    )

                    OutlinedButton(
                        onClick = onTestHigh,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🔊 تجربة صوت الوحش عند $highThreshold%", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Low Battery Alert Card (Dynamic Threshold)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DangerRed.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryAlert,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "تنبيه انخفاض الشحن ($lowThreshold%)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "ينطق صوت أبو ظلام بالنسبة المحددة فور انخفاضها",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    Switch(
                        checked = lowEnabled,
                        onCheckedChange = {
                            lowEnabled = it
                            onSaveLowSettings(it, lowThreshold, lowMessage)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DangerRed,
                            checkedTrackColor = DangerRed.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("low_alert_switch")
                    )
                }

                if (lowEnabled) {
                    Text(
                        text = "اختر نسبة الانخفاض المستهدفة:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = DangerRed,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(10, 15, 20, 25).forEach { pct ->
                            ElevatedFilterChip(
                                selected = lowThreshold == pct,
                                onClick = {
                                    lowThreshold = pct
                                    val updatedMsg = GuardianSettings.formatPercentageInText(
                                        template = lowMessage,
                                        percentage = pct,
                                        defaultPrefix = "أَبُو ظَلَام: اِشْحَنْ هَاتِفَكَ بِسُرْعَة! البَطَّارِيَّةُ $pct بِالمِئَة!"
                                    )
                                    lowMessage = updatedMsg
                                    onSaveLowSettings(lowEnabled, pct, updatedMsg)
                                },
                                label = { Text("$pct%", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = DangerRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = lowMessage,
                        onValueChange = {
                            lowMessage = it
                            onSaveLowSettings(lowEnabled, lowThreshold, it)
                        },
                        label = { Text("العبارة المنطوقة بصوت أبو ظلام عند $lowThreshold%") },
                        trailingIcon = {
                            IconButton(onClick = {
                                val resetMsg = "أَبُو ظَلَام: اِشْحَنْ هَاتِفَكَ بِسُرْعَة! البَطَّارِيَّةُ $lowThreshold بِالمِئَة!"
                                lowMessage = resetMsg
                                onSaveLowSettings(lowEnabled, lowThreshold, resetMsg)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "استعادة العبارة الأصلية",
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DangerRed,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("low_message_input")
                    )

                    OutlinedButton(
                        onClick = onTestLow,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🔊 تجربة صوت الوحش عند $lowThreshold%", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Audio & General Behaviors Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "الاهتزاز مع التنبيه الصوتي",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                        )
                    }
                    Switch(
                        checked = vibration,
                        onCheckedChange = {
                            vibration = it
                            onSaveAudioAndGeneral(
                                it, speechRate, speechPitch, repeatInterval,
                                startOnBoot, pluggedAlert, unpluggedAlert
                            )
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberAmber)
                    )
                }

                // Charger Connected announcement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Power,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "نطق صوت عند توصيل الشاحن",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                        )
                    }
                    Switch(
                        checked = pluggedAlert,
                        onCheckedChange = {
                            pluggedAlert = it
                            onSaveAudioAndGeneral(
                                vibration, speechRate, speechPitch, repeatInterval,
                                startOnBoot, it, unpluggedAlert
                            )
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberAmber)
                    )
                }

                // Charger Disconnected announcement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Power,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "نطق صوت عند فصل الشاحن",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                        )
                    }
                    Switch(
                        checked = unpluggedAlert,
                        onCheckedChange = {
                            unpluggedAlert = it
                            onSaveAudioAndGeneral(
                                vibration, speechRate, speechPitch, repeatInterval,
                                startOnBoot, pluggedAlert, it
                            )
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberAmber)
                    )
                }

                // Auto Start on Boot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "التشغيل التلقائي عند تشغيل الهاتف",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                        )
                        Text(
                            text = "يبدأ درع أبو ظلام بالعمل فور إقلاع النظام",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                    Switch(
                        checked = startOnBoot,
                        onCheckedChange = {
                            startOnBoot = it
                            onSaveAudioAndGeneral(
                                vibration, speechRate, speechPitch, repeatInterval,
                                it, pluggedAlert, unpluggedAlert
                            )
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberAmber)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
