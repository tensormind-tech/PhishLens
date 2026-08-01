package com.phishlens.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phishlens.app.R
import com.phishlens.app.security.BrowserPhishingProtectionService
import com.phishlens.app.security.PhishingNotificationHelper
import com.phishlens.app.ui.theme.PrimaryBlue
import com.phishlens.app.ui.theme.RiskRed
import com.phishlens.app.ui.theme.SafeGreen
import com.phishlens.app.viewmodel.MainViewModel
import com.phishlens.app.viewmodel.ScanUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: ScanUiState,
    viewModel: MainViewModel,
    onNavigateToResult: () -> Unit,
    onNavigateToQrScanner: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var isShieldServiceEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    // Launcher for POST_NOTIFICATIONS permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Lottie Compositions
    val splashComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash))
    val splashProgress by animateLottieCompositionAsState(
        composition = splashComposition,
        iterations = LottieConstants.IterateForever
    )

    val analyzingComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.analyzing))
    val analyzingProgress by animateLottieCompositionAsState(
        composition = analyzingComposition,
        iterations = LottieConstants.IterateForever
    )

    // Gallery Photo Picker Launcher for ML Kit OCR
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processImageUri(context, it, onSuccess = onNavigateToResult)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield Logo",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PhishLens",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "AI Threat Shield",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryBlue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Phishing & Scam Protection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Paste SMS, email, URL, screenshot, or scan QR code.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        LottieAnimation(
                            composition = splashComposition,
                            progress = { splashProgress },
                            modifier = Modifier.size(76.dp)
                        )
                    }
                }

                // Real-Time Browser Extension Shield Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isShieldServiceEnabled) SafeGreen.copy(alpha = 0.12f) else RiskRed.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Realtime Browser Shield",
                                    tint = if (isShieldServiceEnabled) SafeGreen else RiskRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Real-Time Browser Phishing Shield",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (isShieldServiceEnabled) "🟢 Active - Real-time Chrome Protection Enabled" else "🔴 Inactive - Enable Service in Accessibility Settings",
                                        fontSize = 12.sp,
                                        color = if (isShieldServiceEnabled) SafeGreen else RiskRed
                                    )
                                }
                            }

                            Switch(
                                checked = isShieldServiceEnabled,
                                onCheckedChange = { active ->
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = SafeGreen)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Test Extension System Notification Button
                        OutlinedButton(
                            onClick = {
                                val testUrl = "http://bit.ly/sbi-verify-login"
                                PhishingNotificationHelper.sendPhishingWarningNotification(
                                    context = context,
                                    url = testUrl,
                                    score = 85,
                                    label = "High Risk",
                                    groqSummary = "⚠️ High Risk Phishing Warning: This link uses a shortened URL 'bit.ly' attempting to impersonate a banking login. Groq AI recommends closing the browser tab immediately."
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Groq AI Push Notification & Alert", fontSize = 13.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Streamlined Input Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.updateInputText(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        label = { Text("Paste suspicious message or URL") },
                        placeholder = { Text("e.g. Your SBI account is suspended. Verify immediately at bit.ly/sbi-verify...") },
                        trailingIcon = {
                            if (uiState.inputText.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateInputText("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Text")
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Smart Detection Chip
                    AnimatedVisibility(
                        visible = uiState.inputText.isNotBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        val lowerText = uiState.inputText.lowercase()
                        val hintText = when {
                            lowerText.contains("bit.ly") || lowerText.contains("tinyurl") || lowerText.contains("http") -> "🌐 URL / Link detected"
                            lowerText.contains("suspended") || lowerText.contains("verify") || lowerText.contains("urgent") -> "⚠️ High-risk keyword pattern detected"
                            lowerText.contains("password") || lowerText.contains("otp") -> "🔐 Sensitive credential keyword detected"
                            else -> "📝 Text ready for scanning (${uiState.inputText.length} chars)"
                        }

                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = hintText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }

                // Error Banner if present
                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = RiskRed.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = RiskRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage,
                                color = RiskRed,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Clean Input Actions Row (Paste, Screenshot, Scan QR)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                viewModel.updateInputText(clipText)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Screenshot", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToQrScanner,
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan QR", fontSize = 13.sp)
                    }
                }

                // Main Action Button
                Button(
                    onClick = {
                        viewModel.analyzeText(uiState.inputText, onSuccess = onNavigateToResult)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Analyze")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze Phishing Risk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Animated Loading Modal with analyzing.lottie
            if (uiState.isLoading) {
                Dialog(onDismissRequest = {}) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 10.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            LottieAnimation(
                                composition = analyzingComposition,
                                progress = { analyzingProgress },
                                modifier = Modifier.size(120.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.loadingMessage.ifEmpty { "Analyzing threat patterns..." },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Checks if the PhishLens Browser Protection Accessibility Service is enabled on the device.
 */
private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedService = "${context.packageName}/${BrowserPhishingProtectionService::class.java.canonicalName}"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    return enabledServices.contains(expectedService) || enabledServices.contains(context.packageName)
}
