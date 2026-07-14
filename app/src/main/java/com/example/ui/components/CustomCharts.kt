package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Bill
import com.example.data.BillPaymentHistory
import com.example.ui.Loc

// Define cohesive theme colors for categories
val CategoryColors = mapOf(
    "Housing" to Color(0xFF4F46E5),      // Indigo
    "Utilities" to Color(0xFF0EA5E9),    // Sky Blue
    "Subscription" to Color(0xFFF43F5E), // Rose
    "Loan" to Color(0xFFEAB308),         // Yellow
    "Insurance" to Color(0xFF10B981),    // Emerald
    "Other" to Color(0xFF8B5CF6)         // Violet
)

@Composable
fun MonthlySpendBarChart(
    payments: List<BillPaymentHistory>,
    bills: List<Bill>,
    calendarMode: String = "Gregorian",
    appLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    // Group payments by month (for the current year, e.g. Jan to Dec)
    val monthlyTotals = remember(payments) {
        val totals = DoubleArray(12) { 0.0 }
        payments.forEach { payment ->
            if (payment.month in 1..12) {
                totals[payment.month - 1] += payment.paidAmount
            }
        }
        totals
    }

    val maxAmount = remember(monthlyTotals) {
        val max = monthlyTotals.maxOrNull() ?: 0.0
        if (max == 0.0) 100.0 else max * 1.15 // 15% padding at top
    }

    // Animation state
    var startAnimation by remember { mutableStateOf(false) }
    val animationScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    LaunchedEffect(payments) {
        startAnimation = true
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = Loc.t("monthly_spending_trend", appLanguage),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val barWidth = (width / 12) * 0.6f
                val spacing = (width / 12) * 0.4f

                // Draw Y-axis grid lines (3 divisions)
                for (i in 0..3) {
                    val y = height - (height / 3) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Bars
                for (i in 0..11) {
                    val amount = monthlyTotals[i]
                    val barHeight = (amount / maxAmount) * height * animationScale
                    val x = i * (barWidth + spacing) + (spacing / 2)
                    val y = height - barHeight

                    if (amount > 0) {
                        drawRect(
                            color = barColor,
                            topLeft = Offset(x, y.toFloat()),
                            size = Size(barWidth, barHeight.toFloat())
                        )
                    }
                }
            }
        }

        // Labels Row
        val months = remember(calendarMode, appLanguage) {
            if (calendarMode == "Jalali") {
                if (appLanguage == "fa") {
                    Loc.faMonths
                } else {
                    Loc.enMonths.map { it.take(3) }
                }
            } else {
                Loc.gregMonths.map { it.take(3) }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            months.forEach { month ->
                Text(
                    text = month,
                    fontSize = 11.sp,
                    color = labelColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CategoryDistributionChart(
    bills: List<Bill>,
    payments: List<BillPaymentHistory>,
    currencySymbol: String = "$",
    appLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    // Map of Category -> Sum of payments in selected month
    val categoryTotals = remember(bills, payments) {
        val totals = mutableMapOf<String, Double>()
        payments.forEach { payment ->
            val bill = bills.find { it.id == payment.billId }
            val cat = bill?.category ?: "Other"
            totals[cat] = (totals[cat] ?: 0.0) + payment.paidAmount
        }
        totals
    }

    val totalSpent = remember(categoryTotals) {
        categoryTotals.values.sum()
    }

    var startAnimation by remember { mutableStateOf(false) }
    val animationScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    LaunchedEffect(payments) {
        startAnimation = true
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = Loc.t("category_distribution", appLanguage),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (totalSpent == 0.0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (appLanguage == "fa") "هنوز هیچ هزینه‌ای برای این ماه پرداخت نشده است." else "No expenses paid yet for this month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart Canvas
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        categoryTotals.forEach { (cat, amount) ->
                            val sweepAngle = ((amount / totalSpent) * 360f).toFloat() * animationScale
                            val color = CategoryColors[cat] ?: CategoryColors["Other"]!!

                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (appLanguage == "fa") "کل پرداختی" else "Total Paid",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = String.format("%s%.0f", currencySymbol, totalSpent),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legend Column
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryTotals.forEach { (cat, amount) ->
                        val percent = (amount / totalSpent) * 100
                        val color = CategoryColors[cat] ?: CategoryColors["Other"]!!

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = String.format("%.0f%%", percent),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
