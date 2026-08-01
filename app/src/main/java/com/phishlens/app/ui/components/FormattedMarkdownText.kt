package com.phishlens.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.phishlens.app.ui.theme.BodyFontFamily

@Composable
fun FormattedMarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp
) {
    val annotatedString = buildAnnotatedString {
        val parts = text.split("**")
        for (i in parts.indices) {
            val part = parts[i]
            if (i % 2 == 1) {
                // Bold markdown text
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        fontFamily = BodyFontFamily,
        fontSize = fontSize,
        lineHeight = (fontSize.value * 1.5).sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    )
}
