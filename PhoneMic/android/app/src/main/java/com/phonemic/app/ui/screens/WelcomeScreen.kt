package com.phonemic.app.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LaptopChromebook
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonemic.app.ui.components.BottomWelcomeWave
import com.phonemic.app.ui.components.PhoneMicAppLogoBadge
import com.phonemic.app.ui.theme.PhoneMicBgLight
import com.phonemic.app.ui.theme.PhoneMicGreenPrimary
import com.phonemic.app.ui.theme.PhoneMicGreenSoftBg
import com.phonemic.app.ui.theme.PhoneMicGreenVibrant
import com.phonemic.app.ui.theme.PhoneMicTextPrimary
import com.phonemic.app.ui.theme.PhoneMicTextSecondary

/**
 * Exact 1:1 reproduction of Android Screen 1 (Welcome Screen) from `kk.jpeg`.
 */
@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhoneMicBgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(78.dp))

            // App Brand Row: Green Rounded-Square Mic Icon + "PhoneMic"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                PhoneMicAppLogoBadge(size = 52.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "PhoneMic",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = PhoneMicTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3-Line Headline: "Use your phone as a / wireless microphone / for your Windows PC"
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = PhoneMicTextPrimary)) {
                        append("Use your phone as a\n")
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PhoneMicTextPrimary)) {
                        append("wireless microphone\nfor your Windows PC")
                    }
                },
                fontSize = 24.sp,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(42.dp))

            // 3 Feature Rows
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                WelcomeFeatureRow(
                    icon = Icons.Default.Wifi,
                    title = "Real-time Audio",
                    subtitle = "Low latency, high quality"
                )
                WelcomeFeatureRow(
                    icon = Icons.Default.LaptopChromebook,
                    title = "Simple Connection",
                    subtitle = "Auto discover or enter IP"
                )
                WelcomeFeatureRow(
                    icon = Icons.Default.Bolt,
                    title = "Lightweight & Free",
                    subtitle = "No account, no cloud"
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // "Get Started ->" Pill Button
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PhoneMicGreenPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3 Pagination Dots (first dot green, two light gray)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PhoneMicGreenVibrant)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1D5DB))
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1D5DB))
                )
            }
        }

        // Soft Green Wave at Bottom
        BottomWelcomeWave(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun WelcomeFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(PhoneMicGreenSoftBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PhoneMicGreenVibrant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PhoneMicTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = PhoneMicTextSecondary
            )
        }
    }
}
