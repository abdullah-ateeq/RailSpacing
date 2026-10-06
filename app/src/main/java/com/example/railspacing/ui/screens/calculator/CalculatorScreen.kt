package com.example.railspacing.ui.screens.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railspacing.calculator.UnitConverter
import com.example.railspacing.data.GapLimitStatus
import com.example.railspacing.data.Orientation
import com.example.railspacing.data.RailSpacingMode
import com.example.railspacing.ui.components.DisclaimerBanner
import com.example.railspacing.ui.components.RailVisualization
import com.example.railspacing.ui.components.UnitSelector
import com.example.railspacing.ui.theme.StatusExceedsColor
import com.example.railspacing.ui.theme.StatusWithinColor

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val view = LocalView.current

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.hapticEvent.collect {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 700.dp

            if (isWideScreen) {
                // Two column layout for tablet / landscape
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            CalculatorHeader()
                        }
                        item {
                            CalculatorInputsCard(uiState = uiState, viewModel = viewModel)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            CalculatorResultsSection(
                                uiState = uiState,
                                viewModel = viewModel,
                                context = context
                            )
                        }
                        item {
                            DisclaimerBanner()
                        }
                    }
                }
            } else {
                // Single column layout for phone portrait
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
                ) {
                    item {
                        CalculatorHeader()
                    }
                    item {
                        CalculatorInputsCard(uiState = uiState, viewModel = viewModel)
                    }
                    item {
                        CalculatorResultsSection(
                            uiState = uiState,
                            viewModel = viewModel,
                            context = context
                        )
                    }
                    item {
                        DisclaimerBanner()
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
}

@Composable
private fun CalculatorHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "RAILSPACING",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Calculate rail count and clear spacing.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun CalculatorInputsCard(
    uiState: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mode Selector: Compact Material 3 Segmented Control
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = uiState.mode == RailSpacingMode.MAX_GAP,
                    onClick = { viewModel.setMode(RailSpacingMode.MAX_GAP) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(
                        text = "MAX GAP",
                        fontWeight = if (uiState.mode == RailSpacingMode.MAX_GAP) FontWeight.Bold else FontWeight.Normal
                    )
                }
                SegmentedButton(
                    selected = uiState.mode == RailSpacingMode.RAIL_COUNT,
                    onClick = { viewModel.setMode(RailSpacingMode.RAIL_COUNT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(
                        text = "RAIL COUNT",
                        fontWeight = if (uiState.mode == RailSpacingMode.RAIL_COUNT) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Unit Selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "DIMENSION UNIT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
                UnitSelector(
                    selectedUnit = uiState.unit,
                    onUnitSelected = { viewModel.setUnit(it) }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Span Input
            OutlinedTextField(
                value = uiState.spanInput,
                onValueChange = { viewModel.updateSpan(it) },
                label = { Text("AVAILABLE SPAN (${uiState.unit.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Available span in ${uiState.unit.displayName}" }
            )

            // Rail Width Input
            OutlinedTextField(
                value = uiState.railWidthInput,
                onValueChange = { viewModel.updateRailWidth(it) },
                label = { Text("RAIL WIDTH (${uiState.unit.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Rail width in ${uiState.unit.displayName}" }
            )

            // Mode Specific Inputs
            if (uiState.mode == RailSpacingMode.MAX_GAP) {
                OutlinedTextField(
                    value = uiState.maxGapInput,
                    onValueChange = { viewModel.updateMaxGap(it) },
                    label = { Text("MAXIMUM CLEAR GAP (${uiState.unit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Maximum clear gap in ${uiState.unit.displayName}" }
                )

                // Quick Gap Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickGaps = listOf(5.0, 10.0, 15.0, 20.0)
                    quickGaps.forEach { gapCm ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.applyQuickGap(gapCm) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${gapCm.toInt()} cm",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = uiState.railCountInput,
                    onValueChange = { viewModel.updateRailCount(it) },
                    label = { Text("NUMBER OF RAILS (1-1000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Number of rails" }
                )

                // Optional Maximum Gap Check in Rail Count Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "CHECK MAXIMUM GAP",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Verify clear gap limit",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Switch(
                        checked = uiState.maxGapCheckEnabledInRailCount,
                        onCheckedChange = { viewModel.toggleMaxGapCheck(it) }
                    )
                }

                if (uiState.maxGapCheckEnabledInRailCount) {
                    OutlinedTextField(
                        value = uiState.maxGapInput,
                        onValueChange = { viewModel.updateMaxGap(it) },
                        label = { Text("MAXIMUM CLEAR GAP (${uiState.unit.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Maximum clear gap check limit" }
                    )
                }
            }

            // Start Offset Input
            OutlinedTextField(
                value = uiState.startOffsetInput,
                onValueChange = { viewModel.updateStartOffset(it) },
                label = { Text("START OFFSET TO FIRST RAIL (${uiState.unit.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Start offset to first rail" }
            )

            // End Offset Input
            OutlinedTextField(
                value = uiState.endOffsetInput,
                onValueChange = { viewModel.updateEndOffset(it) },
                label = { Text("END OFFSET AFTER LAST RAIL (${uiState.unit.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "End offset after last rail" }
            )

            // Optional Informational Rail Depth
            OutlinedTextField(
                value = uiState.railDepthInput,
                onValueChange = { viewModel.updateRailDepth(it) },
                label = { Text("RAIL DEPTH (OPTIONAL INFORMATIONAL)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Optional rail depth" }
            )

            // Orientation Selector: Compact Segmented Control
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "VISUAL ORIENTATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = uiState.orientation == Orientation.HORIZONTAL,
                        onClick = { viewModel.setOrientation(Orientation.HORIZONTAL) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Horizontal")
                    }
                    SegmentedButton(
                        selected = uiState.orientation == Orientation.VERTICAL,
                        onClick = { viewModel.setOrientation(Orientation.VERTICAL) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Vertical")
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorResultsSection(
    uiState: CalculatorUiState,
    viewModel: CalculatorViewModel,
    context: Context
) {
    val result = uiState.result
    val unit = uiState.unit
    val dec = uiState.decimalPlaces

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Error or No-fit Banner
        if (!result.isValid) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = result.errorMessage ?: "Invalid Layout Dimensions",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }

                    if (result.shortByMm != null) {
                        val reqStr = result.requiredRailWidthMm?.let {
                            UnitConverter.formatMeasurement(it, unit, dec)
                        } ?: ""
                        val availStr = result.availableMm?.let {
                            UnitConverter.formatMeasurement(it, unit, dec)
                        } ?: ""
                        val shortStr = UnitConverter.formatMeasurement(result.shortByMm, unit, dec)

                        Text(
                            text = "Required rail width: $reqStr\nAvailable span: $availStr\nShort by: $shortStr",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
            }
        } else {
            // Primary Result Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.mode == RailSpacingMode.MAX_GAP) "RAILS FIT" else "RAILS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        if (result.isExactFit) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "EXACT FIT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Prominent Rail Count
                    Text(
                        text = "${result.railCount}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = "${result.railCount} rails"
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Clear Gap & Center-to-Center Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CLEAR GAP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                            val gapText = if (result.isSingleRail) {
                                "One rail — no internal gap."
                            } else {
                                UnitConverter.formatMeasurement(result.actualClearGapMm, unit, dec)
                            }
                            Text(
                                text = gapText,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.semantics {
                                    contentDescription = "Clear gap $gapText"
                                }
                            )
                        }

                        if (uiState.showCenterToCenter) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CENTER-TO-CENTER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                )
                                val c2cText = UnitConverter.formatMeasurement(result.centerToCenterMm, unit, dec)
                                Text(
                                    text = c2cText,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.semantics {
                                        contentDescription = "Center to center spacing $c2cText"
                                    }
                                )
                            }
                        }
                    }

                    // Maximum Gap Status in Max Gap mode or when toggled in Rail Count
                    if (result.gapLimitStatus != GapLimitStatus.NOT_APPLICABLE) {
                        val isWithin = result.gapLimitStatus == GapLimitStatus.WITHIN_MAXIMUM
                        val statusColor = if (isWithin) StatusWithinColor else StatusExceedsColor
                        val statusText = if (isWithin) "WITHIN MAXIMUM" else "GAP EXCEEDS LIMIT"

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusColor.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (isWithin) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = statusColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "STATUS: $statusText",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                    )
                                    if (!isWithin) {
                                        Text(
                                            text = "Reduce the spacing or increase the rail count.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = statusColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Visualization
            if (uiState.showVisualization) {
                RailVisualization(
                    result = result,
                    unit = unit,
                    decimalPlaces = dec,
                    orientation = uiState.orientation
                )
            }

            // Collapsible Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleBreakdownExpanded() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CALCULATION BREAKDOWN",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(
                            imageVector = if (uiState.isBreakdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (uiState.isBreakdownExpanded) "Collapse breakdown" else "Expand breakdown"
                        )
                    }

                    AnimatedVisibility(visible = uiState.isBreakdownExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BreakdownRow("TOTAL SPAN", UnitConverter.formatMeasurement(result.totalSpanMm, unit, dec))
                            BreakdownRow("START OFFSET TO FIRST RAIL", UnitConverter.formatMeasurement(result.startOffsetMm, unit, dec))
                            BreakdownRow("END OFFSET AFTER LAST RAIL", UnitConverter.formatMeasurement(result.endOffsetMm, unit, dec))
                            BreakdownRow("USABLE SPAN", UnitConverter.formatMeasurement(result.usableSpanMm, unit, dec))
                            BreakdownRow("RAIL WIDTH", UnitConverter.formatMeasurement(result.railWidthMm, unit, dec))
                            BreakdownRow("RAILS", "${result.railCount}")
                            BreakdownRow("INTERNAL GAPS", "${result.internalGapCount}")
                            BreakdownRow("CLEAR GAP BETWEEN RAILS", UnitConverter.formatMeasurement(result.actualClearGapMm, unit, dec))
                            BreakdownRow("CENTER-TO-CENTER", UnitConverter.formatMeasurement(result.centerToCenterMm, unit, dec))
                            BreakdownRow("USED SPAN", UnitConverter.formatMeasurement(result.usedSpanMm, unit, dec))
                            BreakdownRow(
                                "REMAINING",
                                if (result.isExactFit) "0 ${unit.symbol} (EXACT FIT)" else UnitConverter.formatMeasurement(result.remainingMm, unit, dec)
                            )
                        }
                    }
                }
            }

            // Positions List
            if (uiState.showPositionList && result.positions.isNotEmpty()) {
                var isPositionsExpanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPositionsExpanded = !isPositionsExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RAIL POSITIONS (${result.positions.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Icon(
                                imageVector = if (isPositionsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isPositionsExpanded) "Collapse positions" else "Expand positions"
                            )
                        }

                        AnimatedVisibility(visible = isPositionsExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Positions from span start (outer start edge & center):",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                result.positions.forEach { pos ->
                                    val outer = UnitConverter.formatMeasurement(pos.outerEdgePositionMm, unit, dec)
                                    val center = UnitConverter.formatMeasurement(pos.centerPositionMm, unit, dec)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (pos.index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                                else MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Rail ${pos.index}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = "Edge: $outer | Center: $center",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("RailSpacing Result", viewModel.generateResultCopyText())
                            clipboard.setPrimaryClip(clip)
                            // ViewModel sends snackbar
                            viewModel.saveCurrentCalculation() // Saves calculation & triggers snackbar
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("SAVE")
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("RailSpacing Result", viewModel.generateResultCopyText())
                            clipboard.setPrimaryClip(clip)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("COPY RESULT")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Rail Positions", viewModel.generatePositionsCopyText())
                            clipboard.setPrimaryClip(clip)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("COPY POSITIONS")
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetToDefaults() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("RESET")
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
