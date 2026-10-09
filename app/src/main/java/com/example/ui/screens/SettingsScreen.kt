package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GuardianSettings
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

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
    onTestHigh: () -> Unit,
    onTestLow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var highEnabled by remember(settings.highAlertEnabled) { mutableStateOf(settings.highAlertEnabled) }
    var highThreshold by remember(settings.highThreshold) { mutableIntStateOf(settings.highThreshold) }
    var highMessage by remember(settings.highMessage) { mutableStateOf(settings.highMessage) }

    var lowEnabled by remember(settings.lowAlertEnabled) { mutableStateOf(settings.lowAlertEnabled) }
    var lowThreshold by remember(settings.lowThreshold) { mutableIntStateOf(settings.lowThreshold) }
    var lowMessage by remember(settings.lowMessage) { mutableStateOf(settings.lowMessage) }

    var vibration by remember(settings.vibrationEnabled) { mutableStateOf(settings.vibrationEnabled) }
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
            text = "⚙️ إعدادات تنبيهات أبو ظلام",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        )

        // 1. High Battery Alert Card (85%)
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
                                text = "ينبهك لإزالة الشاحن وحماية البطارية",
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
                    // Quick Level Presets
                    Text(
                        text = "نسبة الشحن المستهدفة:",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(80, 85, 90, 95, 100).forEach { pct ->
                            ElevatedFilterChip(
                                selected = highThreshold == pct,
                                onClick = {
                                    highThreshold = pct
                                    onSaveHighSettings(highEnabled, pct, highMessage)
                                },
                                label = { Text("$pct%") },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = ElectricCyan,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }

                    // Message Text Field
                    OutlinedTextField(
                        value = highMessage,
                        onValueChange = {
                            highMessage = it
                            onSaveHighSettings(highEnabled, highThreshold, it)
                        },
                        label = { Text("العبارة الصوتية المنطوقة") },
                        trailingIcon = {
                            IconButton(onClick = {
                                highMessage = "أبو ظلام، تم شحن الهاتف 85 بالمئة، أزل الشاحن من الهاتف"
                                onSaveHighSettings(highEnabled, highThreshold, highMessage)
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
                        Text("استماع لصوت أبو ظلام عند $highThreshold%")
                    }
                }
            }
        }

        // 2. Low Battery Alert Card (20%)
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
                                text = "ينبهك لشحن الهاتف قبل نفاد الطاقة",
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
                        text = "نسبة التنبيه المنخفضة:",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 15, 20, 25).forEach { pct ->
                            ElevatedFilterChip(
                                selected = lowThreshold == pct,
                                onClick = {
                                    lowThreshold = pct
                                    onSaveLowSettings(lowEnabled, pct, lowMessage)
                                },
                                label = { Text("$pct%") },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = DangerRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = lowMessage,
                        onValueChange = {
                            lowMessage = it
                            onSaveLowSettings(lowEnabled, lowThreshold, it)
                        },
                        label = { Text("العبارة الصوتية المنطوقة") },
                        trailingIcon = {
                            IconButton(onClick = {
                                lowMessage = "أبو ظلام، اشحن هاتفك بسرعة"
                                onSaveLowSettings(lowEnabled, lowThreshold, lowMessage)
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
                        Text("استماع لصوت أبو ظلام عند $lowThreshold%")
                    }
                }
            }
        }

        // 3. Audio & Voice Settings
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberAmber.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = CyberAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "خصائص الصوت والنطق",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                // Speech Rate Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "سرعة نطق الصوت",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                        Text(
                            text = String.format("%.1fx", speechRate),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberAmber
                            )
                        )
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = {
                            speechRate = it
                            onSaveAudioAndGeneral(
                                vibration, speechRate, speechPitch, repeatInterval,
                                startOnBoot, pluggedAlert, unpluggedAlert
                            )
                        },
                        valueRange = 0.6f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberAmber,
                            activeTrackColor = CyberAmber,
                            inactiveTrackColor = DarkSurfaceVariant
                        )
                    )
                }

                // Speech Pitch Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "طبقة الصوت (الحدة والجهارة)",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                        Text(
                            text = String.format("%.1fx", speechPitch),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberAmber
                            )
                        )
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = {
                            speechPitch = it
                            onSaveAudioAndGeneral(
                                vibration, speechRate, speechPitch, repeatInterval,
                                startOnBoot, pluggedAlert, unpluggedAlert
                            )
                        },
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberAmber,
                            activeTrackColor = CyberAmber,
                            inactiveTrackColor = DarkSurfaceVariant
                        )
                    )
                }

                // Repeat Interval
                Column {
                    Text(
                        text = "تكرار التنبيه إذا لم يتم فصل/توصيل الشاحن:",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0 to "مرة واحدة",
                            1 to "كل دقيقة",
                            2 to "كل دقيقتين",
                            5 to "كل 5 دقائق"
                        ).forEach { (minutes, label) ->
                            ElevatedFilterChip(
                                selected = repeatInterval == minutes,
                                onClick = {
                                    repeatInterval = minutes
                                    onSaveAudioAndGeneral(
                                        vibration, speechRate, speechPitch, minutes,
                                        startOnBoot, pluggedAlert, unpluggedAlert
                                    )
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = CyberAmber,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }
                }

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
                            text = "يبدأ درع أبو ظلام بالعمل فور تشغيل الجهاز",
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
