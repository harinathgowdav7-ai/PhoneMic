package com.phonemic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonemic.app.model.AudioSettings
import com.phonemic.app.ui.theme.PhoneMicBgLight
import com.phonemic.app.ui.theme.PhoneMicBorder
import com.phonemic.app.ui.theme.PhoneMicGreenVibrant
import com.phonemic.app.ui.theme.PhoneMicTextPrimary
import com.phonemic.app.ui.theme.PhoneMicTextSecondary

/**
 * Exact 1:1 reproduction of Android Screen 4 (Settings Screen) from `kk.jpeg`.
 */
@Composable
fun SettingsScreen(
    settings: AudioSettings,
    onBackClick: () -> Unit,
    onSampleRateClick: () -> Unit,
    onAutoReconnectChange: (Boolean) -> Unit,
    onSecureConnectionChange: (Boolean) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onAppThemeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PhoneMicBgLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top Bar: <- Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PhoneMicTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PhoneMicTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 1: Audio
        SectionHeader("Audio")
        SettingsGroupCard {
            SettingsValueRow(
                icon = Icons.Default.GraphicEq,
                title = "Sample Rate",
                value = settings.sampleRateLabel,
                onClick = onSampleRateClick
            )
            SettingsDivider()
            SettingsValueRow(
                icon = Icons.Default.Layers,
                title = "Audio Format",
                value = settings.audioFormat,
                onClick = {}
            )
            SettingsDivider()
            SettingsValueRow(
                icon = Icons.Default.Tune,
                title = "Channels",
                value = settings.channels,
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Connection
        SectionHeader("Connection")
        SettingsGroupCard {
            SettingsToggleRow(
                icon = Icons.Default.Wifi,
                title = "Auto Reconnect",
                checked = settings.autoReconnect,
                onCheckedChange = onAutoReconnectChange
            )
            SettingsDivider()
            SettingsToggleRow(
                icon = Icons.Outlined.VerifiedUser,
                title = "Secure Connection",
                checked = settings.secureConnection,
                onCheckedChange = onSecureConnectionChange
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Appearance
        SectionHeader("Appearance")
        SettingsGroupCard {
            SettingsToggleRow(
                icon = Icons.Outlined.DarkMode,
                title = "Dark Mode",
                checked = settings.darkMode,
                onCheckedChange = onDarkModeChange
            )
            SettingsDivider()
            SettingsValueRow(
                icon = Icons.Default.Palette,
                title = "App Theme",
                value = settings.appTheme,
                onClick = onAppThemeClick
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 4: About
        SectionHeader("About")
        SettingsGroupCard {
            SettingsValueRow(
                icon = Icons.Outlined.Info,
                title = "About PhoneMic",
                value = "",
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = PhoneMicTextPrimary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsGroupCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, PhoneMicBorder, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color(0xFFF1F5F9)
    )
}

@Composable
private fun SettingsValueRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PhoneMicTextSecondary,
                modifier = Modifier.size(21.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = PhoneMicTextPrimary
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = PhoneMicTextSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = PhoneMicTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PhoneMicTextSecondary,
                modifier = Modifier.size(21.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = PhoneMicTextPrimary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PhoneMicGreenVibrant,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1)
            )
        )
    }
}
