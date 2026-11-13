package com.phonemic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LaptopChromebook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonemic.app.ui.components.GlowingMicCircle
import com.phonemic.app.ui.components.GreenAudioWaveformBars
import com.phonemic.app.ui.theme.PhoneMicBgLight
import com.phonemic.app.ui.theme.PhoneMicBorder
import com.phonemic.app.ui.theme.PhoneMicGreenVibrant
import com.phonemic.app.ui.theme.PhoneMicRedDisconnect
import com.phonemic.app.ui.theme.PhoneMicSecondarySurface
import com.phonemic.app.ui.theme.PhoneMicTextPrimary
import com.phonemic.app.ui.theme.PhoneMicTextSecondary

/**
 * Exact 1:1 reproduction of Android Screen 3 (Live Microphone Dashboard) from `kk.jpeg`.
 */
@Composable
fun MicDashboardScreen(
    pcName: String,
    pcIp: String,
    isMuted: Boolean,
    rmsDb: Float,
    spectrumBars: List<Float>,
    wifiSsid: String,
    sampleRateLabel: String,
    onBackClick: () -> Unit,
    onToggleMute: () -> Unit,
    onDisconnectClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PhoneMicBgLight)
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top App Bar: Back Arrow <- | "PhoneMic" | (⋮)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PhoneMicTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PhoneMic",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = PhoneMicTextPrimary
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = PhoneMicTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Connected Status Card: Laptop icon | ● Connected / Windows PC (192.168.1.42) | >
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.dp, PhoneMicBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PhoneMicSecondarySurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LaptopChromebook,
                        contentDescription = null,
                        tint = PhoneMicTextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(PhoneMicGreenVibrant)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "Connected",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PhoneMicGreenVibrant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$pcName ($pcIp)",
                        fontSize = 14.sp,
                        color = PhoneMicTextSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = PhoneMicTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Center Glowing Microphone Circle
        GlowingMicCircle(
            isMuted = isMuted,
            rmsDb = rmsDb
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Full-Width 33-Bar Green Waveform Visualizer
        GreenAudioWaveformBars(
            bars = spectrumBars,
            maxHeight = 64.dp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(34.dp))

        // 3 Circular Control Buttons: Mute | Disconnect | Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardRoundAction(
                icon = if (isMuted) Icons.Default.Mic else Icons.Default.MicOff,
                label = if (isMuted) "Unmute" else "Mute",
                circleColor = if (isMuted) Color(0xFFFEE2E2) else PhoneMicSecondarySurface,
                iconTint = if (isMuted) PhoneMicRedDisconnect else PhoneMicTextPrimary,
                onClick = onToggleMute
            )

            DashboardRoundAction(
                icon = Icons.Default.Stop,
                label = "Disconnect",
                circleColor = PhoneMicRedDisconnect,
                iconTint = Color.White,
                circleSize = 68.dp,
                onClick = onDisconnectClick
            )

            DashboardRoundAction(
                icon = Icons.Default.Settings,
                label = "Settings",
                circleColor = PhoneMicSecondarySurface,
                iconTint = PhoneMicTextPrimary,
                onClick = onSettingsClick
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Status Pill: Wi-Fi: Home_5G | 48 kHz · PCM
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 26.dp)
                .shadow(1.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, PhoneMicBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = PhoneMicGreenVibrant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wi-Fi: $wifiSsid",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PhoneMicTextPrimary
                )
            }

            VerticalDivider(
                modifier = Modifier.height(18.dp),
                color = PhoneMicBorder
            )

            Text(
                text = "$sampleRateLabel · PCM",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PhoneMicTextPrimary
            )
        }
    }
}

@Composable
private fun DashboardRoundAction(
    icon: ImageVector,
    label: String,
    circleColor: Color,
    iconTint: Color,
    circleSize: androidx.compose.ui.unit.Dp = 62.dp,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(circleColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PhoneMicTextPrimary
        )
    }
}
