package com.phonemic.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.phonemic.app.ui.theme.PhoneMicGreenBright
import com.phonemic.app.ui.theme.PhoneMicGreenPrimary
import com.phonemic.app.ui.theme.PhoneMicGreenVibrant

/**
 * Replicates the rounded-square green PhoneMic app badge with white classic microphone + sound wave arcs.
 */
@Composable
fun PhoneMicAppLogoBadge(size: Dp = 52.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(4.dp, RoundedCornerShape(size * 0.26f))
            .clip(RoundedCornerShape(size * 0.26f))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF22C55E), Color(0xFF15803D))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "PhoneMic Logo",
            tint = Color.White,
            modifier = Modifier.size(size * 0.62f)
        )
    }
}

/**
 * Replicates the green-screen Windows laptop + smartphone Wi-Fi illustration
 * on the "Connect to your PC" screen (`kk.jpeg` Screen 2).
 */
@Composable
fun LaptopPhoneConnectIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(175.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp, 160.dp)) {
            val w = size.width
            val h = size.height

            // Soft radial green glow behind laptop
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3322C55E), Color.Transparent),
                    center = Offset(w * 0.48f, h * 0.50f),
                    radius = w * 0.48f
                ),
                center = Offset(w * 0.48f, h * 0.50f),
                radius = w * 0.48f
            )

            // Laptop Lid (Dark Bezel)
            val lidLeft = w * 0.14f
            val lidTop = h * 0.15f
            val lidW = w * 0.58f
            val lidH = h * 0.60f
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(lidLeft, lidTop),
                size = Size(lidW, lidH),
                cornerRadius = CornerRadius(14f, 14f)
            )

            // Laptop Green Display
            val pad = 7f
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A)),
                    start = Offset(lidLeft, lidTop),
                    end = Offset(lidLeft + lidW, lidTop + lidH)
                ),
                topLeft = Offset(lidLeft + pad, lidTop + pad),
                size = Size(lidW - pad * 2, lidH - pad * 2),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Windows 4-Pane Logo inside Laptop Screen
            val winSize = 14f
            val winGap = 3f
            val winCx = lidLeft + lidW * 0.48f
            val winCy = lidTop + lidH * 0.48f
            val paneColor = Color.White.copy(alpha = 0.78f)
            drawRect(paneColor, Offset(winCx - winSize - winGap / 2, winCy - winSize - winGap / 2), Size(winSize, winSize))
            drawRect(paneColor, Offset(winCx + winGap / 2, winCy - winSize - winGap / 2), Size(winSize, winSize))
            drawRect(paneColor, Offset(winCx - winSize - winGap / 2, winCy + winGap / 2), Size(winSize, winSize))
            drawRect(paneColor, Offset(winCx + winGap / 2, winCy + winGap / 2), Size(winSize, winSize))

            // Laptop Base
            drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(w * 0.07f, lidTop + lidH - 2f),
                size = Size(w * 0.66f, 10f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Foreground Smartphone Body (Right side overlapping laptop)
            val phoneLeft = w * 0.58f
            val phoneTop = h * 0.25f
            val phoneW = w * 0.24f
            val phoneH = h * 0.62f
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(phoneLeft, phoneTop),
                size = Size(phoneW, phoneH),
                cornerRadius = CornerRadius(18f, 18f)
            )
            // Phone White Screen
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(phoneLeft + 5f, phoneTop + 6f),
                size = Size(phoneW - 10f, phoneH - 12f),
                cornerRadius = CornerRadius(13f, 13f)
            )
            // Soft Green Circle inside Phone Screen
            val pCx = phoneLeft + phoneW / 2f
            val pCy = phoneTop + phoneH / 2f
            drawCircle(
                color = Color(0xFFE8F8EE),
                radius = phoneW * 0.30f,
                center = Offset(pCx, pCy)
            )
            // Green Wi-Fi arcs inside phone circle
            drawArc(
                color = Color(0xFF16A34A),
                startAngle = 225f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(pCx - 12f, pCy - 10f),
                size = Size(24f, 24f),
                style = Stroke(width = 3.2f, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFF16A34A),
                startAngle = 225f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(pCx - 7f, pCy - 5f),
                size = Size(14f, 14f),
                style = Stroke(width = 3.2f, cap = StrokeCap.Round)
            )
            drawCircle(
                color = Color(0xFF16A34A),
                radius = 2.5f,
                center = Offset(pCx, pCy + 5f)
            )
        }
    }
}

/**
 * Replicates the glowing circular Microphone emblem on the Android Dashboard (`kk.jpeg` Screen 3).
 */
@Composable
fun GlowingMicCircle(
    isMuted: Boolean,
    rmsDb: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(210.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer soft radial green aura
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x4422C55E),
                        Color(0x1822C55E),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius
            )
            // Outer subtle ring
            drawCircle(
                color = Color(0x3322C55E),
                radius = radius * 0.80f,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Inner crisp white circle with bright green border
        Box(
            modifier = Modifier
                .size(142.dp)
                .shadow(10.dp, CircleShape, ambientColor = PhoneMicGreenBright, spotColor = PhoneMicGreenBright)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.5.dp, if (isMuted) Color(0xFF94A3B8) else PhoneMicGreenBright, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Live Microphone",
                tint = if (isMuted) Color(0xFF64748B) else PhoneMicGreenVibrant,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

/**
 * Replicates the vertical green audio spectrum bars with rounded top caps and soft bottom fade.
 */
@Composable
fun GreenAudioWaveformBars(
    bars: List<Float>,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 68.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight)
    ) {
        val count = bars.size.coerceAtLeast(1)
        val totalW = size.width
        val totalH = size.height
        val barWidth = (totalW / (count * 1.85f)).coerceIn(5f, 14f)
        val gap = (totalW - (barWidth * count)) / (count + 1).coerceAtLeast(1)

        for (i in 0 until count) {
            val normalized = bars[i].coerceIn(0.08f, 1.0f)
            val barH = (totalH * normalized).coerceAtLeast(barWidth * 1.2f)
            val x = gap + i * (barWidth + gap)
            val topY = totalH - barH

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF16A34A),
                        Color(0xFF22C55E),
                        Color(0x2222C55E)
                    ),
                    startY = topY,
                    endY = totalH
                ),
                topLeft = Offset(x, topY),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

/**
 * Soft decorative green wave at the bottom of the Welcome screen (`kk.jpeg` Screen 1).
 */
@Composable
fun BottomWelcomeWave(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
    ) {
        val w = size.width
        val h = size.height

        val path1 = Path().apply {
            moveTo(0f, h * 0.35f)
            cubicTo(
                w * 0.28f, h * 0.05f,
                w * 0.65f, h * 0.85f,
                w, h * 0.30f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = path1,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE2F9EB), Color(0xFFC6F4D6))
            )
        )

        val path2 = Path().apply {
            moveTo(0f, h * 0.62f)
            cubicTo(
                w * 0.35f, h * 0.95f,
                w * 0.72f, h * 0.25f,
                w, h * 0.55f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = path2,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x88BBF7D0), Color(0xFFB4F1C9))
            )
        )
    }
}
