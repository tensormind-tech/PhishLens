package com.phishlens.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phishlens.app.R
import com.phishlens.app.ui.components.FormattedMarkdownText
import com.phishlens.app.ui.components.HighlightedText
import com.phishlens.app.ui.components.RiskScoreBadge
import com.phishlens.app.ui.theme.HeadlineFontFamily
import com.phishlens.app.ui.theme.PrimaryBlue
import com.phishlens.app.ui.theme.RiskRed
import com.phishlens.app.ui.theme.RiskRedBackground
import com.phishlens.app.ui.theme.SafeGreen
import com.phishlens.app.ui.theme.SafeGreenBackground
import com.phishlens.app.ui.theme.SuspiciousYellow
import com.phishlens.app.ui.theme.SuspiciousYellowBackground
import com.phishlens.app.viewmodel.MainViewModel
import com.phishlens.app.viewmodel.ScanUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    uiState: ScanUiState,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val result = uiState.analysisResult

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    // Lottie Compositions for result states
    val successComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success))
    val successProgress by animateLottieCompositionAsState(
        composition = successComposition,
        iterations = LottieConstants.IterateForever
    )

    val alertComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.phishing_alertoranyalert))
    val alertProgress by animateLottieCompositionAsState(
        composition = alertComposition,
        iterations = LottieConstants.IterateForever
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Analysis Result", fontFamily = HeadlineFontFamily, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        result?.let {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "PhishLens Threat Warning")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🚨 PhishLens Security Scan Report:\nRisk Score: ${it.score}/100 (${it.label})\nMessage: \"${uiState.inputText}\"\n\nExplanation: ${uiState.aiExplanation}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share PhishLens Report"))
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (result == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No scan result available.")
            }
            return@Scaffold
        }

        val labelColor: Color = when (result.label) {
            "Safe" -> SafeGreen
            "Suspicious" -> SuspiciousYellow
            else -> RiskRed
        }

        val labelBg: Color = when (result.label) {
            "Safe" -> SafeGreenBackground
            "Suspicious" -> SuspiciousYellowBackground.copy(alpha = 0.2f)
            else -> RiskRedBackground
        }

        val labelIcon = when (result.label) {
            "Safe" -> Icons.Default.CheckCircle
            "Suspicious" -> Icons.Default.Warning
            else -> Icons.Default.Warning
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Header Score Gauge Card with Soft Gradient & Shadow
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, labelColor.copy(alpha = 0.25f)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        labelColor.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Display Lottie Status Animation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (result.label == "Safe") {
                                    LottieAnimation(
                                        composition = successComposition,
                                        progress = { successProgress },
                                        modifier = Modifier.size(90.dp)
                                    )
                                } else {
                                    LottieAnimation(
                                        composition = alertComposition,
                                        progress = { alertProgress },
                                        modifier = Modifier.size(90.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            RiskScoreBadge(score = result.score, label = result.label)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Risk Label Pill Badge with Border
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(labelBg)
                                    .border(1.dp, labelColor.copy(alpha = 0.4f), CircleShape)
                                    .padding(horizontal = 22.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = labelIcon,
                                        contentDescription = null,
                                        tint = labelColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = result.label.uppercase(),
                                        fontFamily = HeadlineFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = labelColor,
                                        fontSize = 16.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Original Analyzed Message & Flagged Trigger Pills
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { 150 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "Analyzed Message & Triggers",
                            fontFamily = HeadlineFontFamily,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        HighlightedText(
                            text = uiState.inputText,
                            suspiciousPhrases = result.suspiciousPhrases,
                            fontSize = 15.sp
                        )

                        if (result.suspiciousPhrases.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Detected Threat Triggers:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RiskRed
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                result.suspiciousPhrases.forEach { phrase ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(RiskRedBackground)
                                            .border(1.dp, RiskRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "⚠️ $phrase",
                                            color = RiskRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // AI Threat Assessment Card with Rich Markdown Formatter & Shadow
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { 200 }, animationSpec = tween(600)) + fadeIn(animationSpec = tween(600))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f)),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryBlue.copy(alpha = 0.06f)
                    )
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Breakdown",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Groq AI Threat Breakdown",
                                    fontFamily = HeadlineFontFamily,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }

                            IconButton(onClick = {
                                uiState.aiExplanation?.let { text ->
                                    clipboardManager.setText(AnnotatedString(text))
                                    Toast.makeText(context, "AI explanation copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy AI text", tint = PrimaryBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render Groq AI response with rich markdown formatting
                        FormattedMarkdownText(
                            text = uiState.aiExplanation ?: "Generating AI threat breakdown...",
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Security Lesson Tip Card
            uiState.securityTip?.let { tip ->
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(initialOffsetY = { 250 }, animationSpec = tween(700)) + fadeIn(animationSpec = tween(700))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        border = BorderStroke(1.dp, SuspiciousYellow.copy(alpha = 0.3f)),
                        colors = CardDefaults.cardColors(
                            containerColor = SuspiciousYellow.copy(alpha = 0.08f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(22.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Security Tip",
                                    tint = SuspiciousYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cybersecurity Lesson: ${tip.category}",
                                    fontFamily = HeadlineFontFamily,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SuspiciousYellow
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = tip.title,
                                fontFamily = HeadlineFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tip.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Action Buttons Row (Share & Analyze Another)
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { 300 }, animationSpec = tween(800)) + fadeIn(animationSpec = tween(800))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "PhishLens Threat Warning")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🚨 PhishLens Security Scan Report:\nRisk Score: ${result.score}/100 (${result.label})\nMessage: \"${uiState.inputText}\"\n\nExplanation: ${uiState.aiExplanation}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share PhishLens Report"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Report", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.resetScan()
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze Another", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
