package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BillViewModel
import com.example.ui.Loc
import com.example.ui.components.CategoryDistributionChart
import com.example.ui.components.MonthlySpendBarChart

@Composable
fun TrendsScreen(
    viewModel: BillViewModel,
    modifier: Modifier = Modifier
) {
    val bills by viewModel.allBills.collectAsState()
    val payments by viewModel.selectedMonthPayments.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val calendarMode by viewModel.calendarMode.collectAsState()
    val allPayments by viewModel.allPaymentsHistory.collectAsState()

    val loans = remember(bills) {
        bills.filter { it.isLoan }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header Banner Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = Loc.t("stats_and_insights", appLanguage),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (appLanguage == "fa") "جریان‌های نقدی ماهانه و تفکیک پرداخت‌های خود را پیگیری کنید" else "Track your monthly cash flows and payment breakdowns",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // 2. Spending Trends Bar Chart
        MonthlySpendBarChart(
            payments = allPayments,
            bills = bills,
            calendarMode = calendarMode,
            appLanguage = appLanguage,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        // 3. Category Breakdown Pie / Donut Chart
        CategoryDistributionChart(
            bills = bills,
            payments = payments,
            currencySymbol = selectedCurrency,
            appLanguage = appLanguage,
            modifier = Modifier.fillMaxWidth()
        )

        // 4. Loans Tracking Progress (if any exists)
        if (loans.isNotEmpty()) {
            HorizontalDivider()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (appLanguage == "fa") "پیگیری اقساط و وام‌های فعال" else "Active Installments & Loans Tracker",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                loans.forEach { loan ->
                    val progress = if (loan.totalInstallments > 0) {
                        (loan.totalInstallments - loan.remainingInstallments).toFloat() / loan.totalInstallments
                    } else 1f

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = loan.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (appLanguage == "fa") {
                                            String.format("مبلغ %s%,.2f سررسید ماهانه", loan.currency, loan.amount).replace(".00", "")
                                        } else {
                                            String.format("%s%,.2f due monthly", loan.currency, loan.amount).replace(".00", "")
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Text(
                                    text = if (appLanguage == "fa") {
                                        "${loan.remainingInstallments} از ${loan.totalInstallments} مانده"
                                    } else {
                                        "${loan.remainingInstallments} / ${loan.totalInstallments} left"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        } else {
            // Friendly tips banner
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (appLanguage == "fa") "نکته: هنگام ثبت قرارداد اقساطی یا وام، گزینه «این یک وام است» را فعال کنید تا نمودار پیشرفت دقیق پرداخت‌ها در اینجا نمایش داده شود." else "Tip: When adding an installment or loan contract, toggle the 'This is a Loan' option to enable precise payment progress bars here.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
