package com.example.railspacing.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railspacing.calculator.UnitConverter
import com.example.railspacing.data.MeasurementUnit
import com.example.railspacing.data.Orientation
import com.example.railspacing.data.RailSpacingResult
import com.example.railspacing.ui.theme.DimensionLineColorDark
import com.example.railspacing.ui.theme.DimensionLineColorLight
import java.math.BigDecimal

@Composable
fun RailVisualization(
    result: RailSpacingResult,
    unit: MeasurementUnit,
    decimalPlaces: Int,
    orientation: Orientation,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val railFillColor = MaterialTheme.colorScheme.primaryContainer
    val railBorderColor = MaterialTheme.colorScheme.primary
    val dimLineColor = if (isDark) DimensionLineColorDark else DimensionLineColorLight
    val boundaryColor = MaterialTheme.colorScheme.outline
    val offsetColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
    val textMeasurer = rememberTextMeasurer()

    val formattedSpan = UnitConverter.formatMeasurement(result.totalSpanMm, unit, decimalPlaces)
    val formattedRailWidth = UnitConverter.formatMeasurement(result.railWidthMm, unit, decimalPlaces)
    val formattedGap = UnitConverter.formatMeasurement(result.actualClearGapMm, unit, decimalPlaces)

    val contentDesc = "Visual rail layout showing ${result.railCount} rails with $formattedGap clear gaps across $formattedSpan total span"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = contentDesc },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val canvasHeight = if (orientation == Orientation.HORIZONTAL) 150.dp else 220.dp

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(canvasHeight)
            ) {
                val width = size.width
                val height = size.height

                if (!result.isValid || result.railCount <= 0 || result.totalSpanMm <= BigDecimal.ZERO) {
                    // Empty or invalid state placeholder
                    drawRoundRect(
                        color = boundaryColor.copy(alpha = 0.3f),
                        topLeft = Offset(10f, 10f),
                        size = Size(width - 20f, height - 20f),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                    )
                    return@Canvas
                }

                if (orientation == Orientation.HORIZONTAL) {
                    drawHorizontalLayout(
                        result = result,
                        width = width,
                        height = height,
                        railFillColor = railFillColor,
                        railBorderColor = railBorderColor,
                        boundaryColor = boundaryColor,
                        dimLineColor = dimLineColor,
                        offsetColor = offsetColor,
                        formattedSpan = formattedSpan,
                        textMeasurer = textMeasurer
                    )
                } else {
                    drawVerticalLayout(
                        result = result,
                        width = width,
                        height = height,
                        railFillColor = railFillColor,
                        railBorderColor = railBorderColor,
                        boundaryColor = boundaryColor,
                        dimLineColor = dimLineColor,
                        offsetColor = offsetColor,
                        formattedSpan = formattedSpan,
                        textMeasurer = textMeasurer
                    )
                }
            }

            // Overlay badge if more than 30 rails
            if (result.railCount > 30) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${result.railCount} Rails • Clear Gap: $formattedGap",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHorizontalLayout(
    result: RailSpacingResult,
    width: Float,
    height: Float,
    railFillColor: Color,
    railBorderColor: Color,
    boundaryColor: Color,
    dimLineColor: Color,
    offsetColor: Color,
    formattedSpan: String,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val padX = 28f
    val spanPx = width - padX * 2
    val railTop = 42f
    val railBottom = height - 36f
    val railH = railBottom - railTop

    // Draw Boundary Posts / Posts at ends
    drawLine(
        color = boundaryColor,
        start = Offset(padX, railTop - 8f),
        end = Offset(padX, railBottom + 8f),
        strokeWidth = 3f
    )
    drawLine(
        color = boundaryColor,
        start = Offset(width - padX, railTop - 8f),
        end = Offset(width - padX, railBottom + 8f),
        strokeWidth = 3f
    )

    // Top Span Dimension Line
    val dimY = 16f
    drawLine(
        color = dimLineColor,
        start = Offset(padX, dimY),
        end = Offset(width - padX, dimY),
        strokeWidth = 1.5f
    )
    // Dimension tick marks
    drawLine(dimLineColor, Offset(padX, dimY - 6f), Offset(padX, dimY + 6f), 1.5f)
    drawLine(dimLineColor, Offset(width - padX, dimY - 6f), Offset(width - padX, dimY + 6f), 1.5f)

    // Dimension text
    val spanMeasured = textMeasurer.measure(
        text = formattedSpan,
        style = TextStyle(fontSize = 11.sp, color = dimLineColor, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
    )
    drawText(
        textLayoutResult = spanMeasured,
        topLeft = Offset((width - spanMeasured.size.width) / 2f, 2f)
    )

    val spanMmFloat = result.totalSpanMm.toFloat()
    if (spanMmFloat <= 0f) return

    val scale = spanPx / spanMmFloat

    // Start offset region
    val startOffsetMm = result.startOffsetMm.toFloat()
    if (startOffsetMm > 0f) {
        val startOffsetPx = startOffsetMm * scale
        drawRect(
            color = offsetColor,
            topLeft = Offset(padX, railTop),
            size = Size(startOffsetPx, railH)
        )
    }

    // End offset region
    val endOffsetMm = result.endOffsetMm.toFloat()
    if (endOffsetMm > 0f) {
        val endOffsetPx = endOffsetMm * scale
        drawRect(
            color = offsetColor,
            topLeft = Offset(width - padX - endOffsetPx, railTop),
            size = Size(endOffsetPx, railH)
        )
    }

    if (result.railCount > 30) {
        // Simplified rendering for > 30 rails
        val sampleRails = 8
        val itemW = spanPx / (sampleRails * 2f)
        for (i in 0 until sampleRails) {
            val x = padX + i * itemW * 2f
            drawRoundRect(
                color = railFillColor,
                topLeft = Offset(x, railTop),
                size = Size(itemW.coerceAtLeast(3f), railH),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = railBorderColor,
                topLeft = Offset(x, railTop),
                size = Size(itemW.coerceAtLeast(3f), railH),
                cornerRadius = CornerRadius(3f, 3f),
                style = Stroke(1.5f)
            )
        }
    } else {
        // Exact rails
        val railWidthPx = (result.railWidthMm.toFloat() * scale).coerceAtLeast(4f)
        result.positions.forEach { pos ->
            val railX = padX + pos.outerEdgePositionMm.toFloat() * scale
            drawRoundRect(
                color = railFillColor,
                topLeft = Offset(railX, railTop),
                size = Size(railWidthPx, railH),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = railBorderColor,
                topLeft = Offset(railX, railTop),
                size = Size(railWidthPx, railH),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(1.5f)
            )
        }
    }

    // Bottom baseline
    drawLine(
        color = boundaryColor.copy(alpha = 0.5f),
        start = Offset(padX, railBottom),
        end = Offset(width - padX, railBottom),
        strokeWidth = 1.5f
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawVerticalLayout(
    result: RailSpacingResult,
    width: Float,
    height: Float,
    railFillColor: Color,
    railBorderColor: Color,
    boundaryColor: Color,
    dimLineColor: Color,
    offsetColor: Color,
    formattedSpan: String,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val padY = 24f
    val spanPx = height - padY * 2
    val railLeft = 48f
    val railRight = width - 24f
    val railW = railRight - railLeft

    // Boundary at top and bottom
    drawLine(
        color = boundaryColor,
        start = Offset(railLeft - 8f, padY),
        end = Offset(railRight + 8f, padY),
        strokeWidth = 3f
    )
    drawLine(
        color = boundaryColor,
        start = Offset(railLeft - 8f, height - padY),
        end = Offset(railRight + 8f, height - padY),
        strokeWidth = 3f
    )

    // Left Span Dimension Line
    val dimX = 18f
    drawLine(
        color = dimLineColor,
        start = Offset(dimX, padY),
        end = Offset(dimX, height - padY),
        strokeWidth = 1.5f
    )
    drawLine(dimLineColor, Offset(dimX - 6f, padY), Offset(dimX + 6f, padY), 1.5f)
    drawLine(dimLineColor, Offset(dimX - 6f, height - padY), Offset(dimX + 6f, height - padY), 1.5f)

    val spanMeasured = textMeasurer.measure(
        text = formattedSpan,
        style = TextStyle(fontSize = 10.sp, color = dimLineColor, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
    )
    drawText(
        textLayoutResult = spanMeasured,
        topLeft = Offset(dimX + 4f, (height - spanMeasured.size.height) / 2f)
    )

    val spanMmFloat = result.totalSpanMm.toFloat()
    if (spanMmFloat <= 0f) return

    val scale = spanPx / spanMmFloat

    // Start offset region (top)
    val startOffsetMm = result.startOffsetMm.toFloat()
    if (startOffsetMm > 0f) {
        val startOffsetPx = startOffsetMm * scale
        drawRect(
            color = offsetColor,
            topLeft = Offset(railLeft, padY),
            size = Size(railW, startOffsetPx)
        )
    }

    // End offset region (bottom)
    val endOffsetMm = result.endOffsetMm.toFloat()
    if (endOffsetMm > 0f) {
        val endOffsetPx = endOffsetMm * scale
        drawRect(
            color = offsetColor,
            topLeft = Offset(railLeft, height - padY - endOffsetPx),
            size = Size(railW, endOffsetPx)
        )
    }

    if (result.railCount > 30) {
        val sampleRails = 8
        val itemH = spanPx / (sampleRails * 2f)
        for (i in 0 until sampleRails) {
            val y = padY + i * itemH * 2f
            drawRoundRect(
                color = railFillColor,
                topLeft = Offset(railLeft, y),
                size = Size(railW, itemH.coerceAtLeast(3f)),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = railBorderColor,
                topLeft = Offset(railLeft, y),
                size = Size(railW, itemH.coerceAtLeast(3f)),
                cornerRadius = CornerRadius(3f, 3f),
                style = Stroke(1.5f)
            )
        }
    } else {
        val railHeightPx = (result.railWidthMm.toFloat() * scale).coerceAtLeast(4f)
        result.positions.forEach { pos ->
            val railY = padY + pos.outerEdgePositionMm.toFloat() * scale
            drawRoundRect(
                color = railFillColor,
                topLeft = Offset(railLeft, railY),
                size = Size(railW, railHeightPx),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = railBorderColor,
                topLeft = Offset(railLeft, railY),
                size = Size(railW, railHeightPx),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(1.5f)
            )
        }
    }
}
