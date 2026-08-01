package com.phishlens.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phishlens.app.ui.theme.RiskRed
import com.phishlens.app.ui.theme.SafeGreen
import com.phishlens.app.ui.theme.SuspiciousYellow

@Composable
fun RiskScoreBadge(
    score: Int,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp
) {
    var animatedScorePlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animatedScorePlayed) score / 100f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "scoreProgress"
    )

    LaunchedEffect(key1 = score) {
        animatedScorePlayed = true
    }

    val meterColor: Color = when {
        score < 30 -> SafeGreen
        score <= 65 -> SuspiciousYellow
        else -> RiskRed
    }

    val trackColor = meterColor.copy(alpha = 0.15f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            // Background track
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokePx)
            )
            // Progress arc
            drawArc(
                color = meterColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = meterColor
            )
            Text(
                text = "/ 100",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
