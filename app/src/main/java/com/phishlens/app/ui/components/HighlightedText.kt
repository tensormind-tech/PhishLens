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
import com.phishlens.app.ui.theme.RiskRed
import com.phishlens.app.ui.theme.RiskRedBackground

@Composable
fun HighlightedText(
    text: String,
    suspiciousPhrases: List<String>,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp
) {
    val annotatedString = buildAnnotatedString {
        if (suspiciousPhrases.isEmpty()) {
            append(text)
        } else {
            val lowercaseText = text.lowercase()
            // Collect all matching ranges for all suspicious phrases
            val matches = mutableListOf<IntRange>()
            for (phrase in suspiciousPhrases) {
                if (phrase.isBlank()) continue
                var startIndex = 0
                val lowerPhrase = phrase.lowercase()
                while (startIndex < lowercaseText.length) {
                    val foundIndex = lowercaseText.indexOf(lowerPhrase, startIndex)
                    if (foundIndex != -1) {
                        matches.add(foundIndex until (foundIndex + lowerPhrase.length))
                        startIndex = foundIndex + lowerPhrase.length
                    } else {
                        break
                    }
                }
            }

            // Merge overlapping or adjacent ranges
            val sortedMatches = matches.sortedBy { it.first }
            val mergedMatches = mutableListOf<IntRange>()
            for (match in sortedMatches) {
                if (mergedMatches.isEmpty()) {
                    mergedMatches.add(match)
                } else {
                    val last = mergedMatches.last()
                    if (match.first <= last.last + 1) {
                        mergedMatches[mergedMatches.size - 1] = last.first..maxOf(last.last, match.last)
                    } else {
                        mergedMatches.add(match)
                    }
                }
            }

            var currentIdx = 0
            for (range in mergedMatches) {
                if (range.first > currentIdx) {
                    append(text.substring(currentIdx, range.first))
                }
                withStyle(
                    style = SpanStyle(
                        color = RiskRed,
                        background = RiskRedBackground,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(text.substring(range.first, minOf(range.last + 1, text.length)))
                }
                currentIdx = range.last + 1
            }
            if (currentIdx < text.length) {
                append(text.substring(currentIdx))
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        fontSize = fontSize,
        lineHeight = (fontSize.value * 1.4).sp,
        color = MaterialTheme.colorScheme.onSurface
    )
}
