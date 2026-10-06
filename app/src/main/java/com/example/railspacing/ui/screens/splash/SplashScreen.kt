package com.example.railspacing.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
        )
        delay(100)
        onSplashFinished()
    }

    val p = progress.value
    // Animation stages:
    // 0.0 -> 0.25: Span line appears
    // 0.25 -> 0.50: First rail appears
    // 0.50 -> 0.75: Additional rails distribute
    // 0.75 -> 0.90: Gap dimension appears
    // 0.85 -> 1.00: Center-to-center markers appear and Title fades in

    val spanLineProgress = (p / 0.25f).coerceIn(0f, 1f)
    val firstRailProgress = ((p - 0.25f) / 0.25f).coerceIn(0f, 1f)
    val distributeProgress = ((p - 0.50f) / 0.25f).coerceIn(0f, 1f)
    val gapMarkerProgress = ((p - 0.75f) / 0.15f).coerceIn(0f, 1f)
    val titleProgress = ((p - 0.70f) / 0.30f).coerceIn(0f, 1f)

    val primaryColor = MaterialTheme.colorScheme.primary
    val railColor = MaterialTheme.colorScheme.primaryContainer
    val borderColor = MaterialTheme.colorScheme.primary
    val dimLineColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Technical Blueprint Graphic Animation
            Canvas(
                modifier = Modifier
                    .width(260.dp)
                    .height(110.dp)
            ) {
                val w = size.width
                val h = size.height
                val railH = 50f
                val railY = 32f
                val railW = 20f

                // 1. Span line
                if (spanLineProgress > 0f) {
                    val currentSpanW = (w - 20f) * spanLineProgress
                    drawLine(
                        color = dimLineColor.copy(alpha = 0.5f),
                        start = Offset(10f, railY + railH + 12f),
                        end = Offset(10f + currentSpanW, railY + railH + 12f),
                        strokeWidth = 2f
                    )
                    // End ticks
                    drawLine(dimLineColor, Offset(10f, railY + railH + 6f), Offset(10f, railY + railH + 18f), 2f)
                    if (spanLineProgress >= 1f) {
                        drawLine(dimLineColor, Offset(w - 10f, railY + railH + 6f), Offset(w - 10f, railY + railH + 18f), 2f)
                    }
                }

                // 2. First rail
                if (firstRailProgress > 0f) {
                    val alpha = firstRailProgress
                    drawRoundRect(
                        color = railColor.copy(alpha = alpha),
                        topLeft = Offset(10f, railY),
                        size = Size(railW, railH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = borderColor.copy(alpha = alpha),
                        topLeft = Offset(10f, railY),
                        size = Size(railW, railH),
                        cornerRadius = CornerRadius(4f, 4f),
                        style = Stroke(2f)
                    )
                }

                // 3. Additional rails distribute (Rails 2, 3, 4, 5)
                if (distributeProgress > 0f) {
                    val totalRails = 5
                    val step = (w - 20f - railW) / (totalRails - 1)
                    val alpha = distributeProgress

                    for (i in 1 until totalRails) {
                        val currentIProgress = ((distributeProgress - (i - 1) * 0.2f) / 0.3f).coerceIn(0f, 1f)
                        if (currentIProgress > 0f) {
                            val railX = 10f + i * step
                            drawRoundRect(
                                color = railColor.copy(alpha = currentIProgress * alpha),
                                topLeft = Offset(railX, railY),
                                size = Size(railW, railH),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                            drawRoundRect(
                                color = borderColor.copy(alpha = currentIProgress * alpha),
                                topLeft = Offset(railX, railY),
                                size = Size(railW, railH),
                                cornerRadius = CornerRadius(4f, 4f),
                                style = Stroke(2f)
                            )
                        }
                    }
                }

                // 4. Gap dimension and Center-to-center markers
                if (gapMarkerProgress > 0f) {
                    val totalRails = 5
                    val step = (w - 20f - railW) / (totalRails - 1)
                    val gapStartX = 10f + railW
                    val gapEndX = 10f + step
                    val dimY = railY - 14f

                    // Clear gap marker
                    drawLine(
                        color = primaryColor.copy(alpha = gapMarkerProgress),
                        start = Offset(gapStartX, dimY),
                        end = Offset(gapEndX, dimY),
                        strokeWidth = 2f
                    )
                    drawLine(primaryColor.copy(alpha = gapMarkerProgress), Offset(gapStartX, dimY - 4f), Offset(gapStartX, dimY + 4f), 2f)
                    drawLine(primaryColor.copy(alpha = gapMarkerProgress), Offset(gapEndX, dimY - 4f), Offset(gapEndX, dimY + 4f), 2f)

                    // Center-to-center dimension marker (between rail 2 and 3)
                    val c2cStartX = 10f + step + railW / 2f
                    val c2cEndX = 10f + 2 * step + railW / 2f
                    val c2cY = railY - 24f

                    drawLine(
                        color = dimLineColor.copy(alpha = gapMarkerProgress),
                        start = Offset(c2cStartX, c2cY),
                        end = Offset(c2cEndX, c2cY),
                        strokeWidth = 1.5f
                    )
                    drawLine(dimLineColor.copy(alpha = gapMarkerProgress), Offset(c2cStartX, c2cY - 4f), Offset(c2cStartX, c2cY + 4f), 1.5f)
                    drawLine(dimLineColor.copy(alpha = gapMarkerProgress), Offset(c2cEndX, c2cY - 4f), Offset(c2cEndX, c2cY + 4f), 1.5f)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title & Subtitle Fade In
            Text(
                text = "RAILSPACING",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = primaryColor.copy(alpha = titleProgress)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Plan evenly spaced rails and bars",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = titleProgress)
                )
            )
        }
    }
}
