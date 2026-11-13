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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonemic.app.model.DiscoveredPc
import com.phonemic.app.ui.components.LaptopPhoneConnectIllustration
import com.phonemic.app.ui.theme.PhoneMicBgLight
import com.phonemic.app.ui.theme.PhoneMicBorder
import com.phonemic.app.ui.theme.PhoneMicGreenPrimary
import com.phonemic.app.ui.theme.PhoneMicGreenSoftBg
import com.phonemic.app.ui.theme.PhoneMicGreenVibrant
import com.phonemic.app.ui.theme.PhoneMicSecondarySurface
import com.phonemic.app.ui.theme.PhoneMicTextPrimary
import com.phonemic.app.ui.theme.PhoneMicTextSecondary

/**
 * Exact 1:1 reproduction of Android Screen 2 ("Connect to your PC") from `kk.jpeg`.
 */
@Composable
fun ConnectScreen(
    selectedTab: Int,
    manualIp: String,
    history: List<DiscoveredPc>,
    onTabSelected: (Int) -> Unit,
    onManualIpChanged: (String) -> Unit,
    onAutoDiscoverClick: () -> Unit,
    onConnectIpClick: (String) -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PhoneMicBgLight)
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar: Green link-mic icon + "PhoneMic" on left, (?) and (⋮) on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "PhoneMic",
                    tint = PhoneMicGreenVibrant,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PhoneMic",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = PhoneMicTextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = "Help",
                        tint = PhoneMicTextPrimary
                    )
                }
                IconButton(onClick = onOpenSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = PhoneMicTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Tab Control: [ Connect ]  [ History ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(PhoneMicSecondarySurface)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Connect Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selectedTab == 0) PhoneMicGreenSoftBg else Color.Transparent)
                    .clickable { onTabSelected(0) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = if (selectedTab == 0) PhoneMicGreenPrimary else PhoneMicTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Connect",
                        fontSize = 15.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selectedTab == 0) PhoneMicGreenPrimary else PhoneMicTextSecondary
                    )
                }
            }

            // History Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selectedTab == 1) PhoneMicGreenSoftBg else Color.Transparent)
                    .clickable { onTabSelected(1) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = if (selectedTab == 1) PhoneMicGreenPrimary else PhoneMicTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "History",
                        fontSize = 15.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selectedTab == 1) PhoneMicGreenPrimary else PhoneMicTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (selectedTab == 0) {
            // Center Laptop + Smartphone Illustration
            LaptopPhoneConnectIllustration()

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Connect to your PC",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = PhoneMicTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Find your Windows PC on the same\nWi-Fi network or enter its IP address.",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = PhoneMicTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Green Pill Button: "🔍 Auto Discover PCs"
            Button(
                onClick = onAutoDiscoverClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PhoneMicGreenPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Auto Discover PCs",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // "— OR —" Divider
            Row(
                modifier = Modifier.fillMaxWidth(0.5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = PhoneMicBorder)
                Text(
                    text = "  OR  ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PhoneMicTextSecondary
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = PhoneMicBorder)
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Manual IP Input Field with Desktop icon on left and Scanner bracket icon on right
            OutlinedTextField(
                value = manualIp,
                onValueChange = onManualIpChanged,
                placeholder = {
                    Text(
                        text = "Enter PC IP address",
                        color = PhoneMicTextSecondary,
                        fontSize = 15.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DesktopWindows,
                        contentDescription = "PC IP",
                        tint = PhoneMicTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onConnectIpClick(manualIp.ifBlank { "192.168.1.42" })
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CropFree,
                            contentDescription = "Connect or Scan",
                            tint = PhoneMicTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = { onConnectIpClick(manualIp.ifBlank { "192.168.1.42" }) }
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PhoneMicGreenVibrant,
                    unfocusedBorderColor = PhoneMicBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp))
            )
        } else {
            // History Tab List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                history.forEach { pc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, PhoneMicBorder, RoundedCornerShape(16.dp))
                            .clickable { onConnectIpClick(pc.ipAddress) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DesktopWindows,
                                contentDescription = null,
                                tint = PhoneMicGreenVibrant,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = pc.pcName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PhoneMicTextPrimary
                                )
                                Text(
                                    text = pc.ipAddress,
                                    fontSize = 14.sp,
                                    color = PhoneMicTextSecondary
                                )
                            }
                        }
                        Text(
                            text = "Connect",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PhoneMicGreenPrimary
                        )
                    }
                }
            }
        }
    }
}
