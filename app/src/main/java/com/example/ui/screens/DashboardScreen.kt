package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Bill
import com.example.data.BillPaymentHistory
import com.example.ui.BillViewModel
import com.example.ui.Loc
import com.example.ui.components.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BillViewModel,
    modifier: Modifier = Modifier
) {
    val bills by viewModel.allBills.collectAsState()
    val payments by viewModel.selectedMonthPayments.collectAsState()
    val constantSum by viewModel.constantExpensesSum.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val calendarMode by viewModel.calendarMode.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var billToEdit by remember { mutableStateOf<Bill?>(null) }
    var billDetailsToShow by remember { mutableStateOf<Bill?>(null) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var billToDeleteWithOptions by remember { mutableStateOf<Bill?>(null) }
    var billForVariablePayment by remember { mutableStateOf<Bill?>(null) }

    val calendar = remember { Calendar.getInstance() }
    val gregYear = calendar.get(Calendar.YEAR)
    val gregMonth = calendar.get(Calendar.MONTH) + 1
    val gregDay = calendar.get(Calendar.DAY_OF_MONTH)

    val currentYear = remember(calendarMode, gregYear, gregMonth, gregDay) {
        if (calendarMode == "Jalali") {
            com.example.ui.JalaliHelper.gregorianToJalali(gregYear, gregMonth, gregDay).year
        } else gregYear
    }

    val currentMonth = remember(calendarMode, gregYear, gregMonth, gregDay) {
        if (calendarMode == "Jalali") {
            com.example.ui.JalaliHelper.gregorianToJalali(gregYear, gregMonth, gregDay).month
        } else gregMonth
    }

    val currentDay = remember(calendarMode, gregYear, gregMonth, gregDay) {
        if (calendarMode == "Jalali") {
            com.example.ui.JalaliHelper.gregorianToJalali(gregYear, gregMonth, gregDay).day
        } else gregDay
    }

    // Filter bills to show only active ones for the selected month/year (or those actually paid in this month/year)
    val visibleBills = remember(bills, payments, selectedYear, selectedMonth) {
        bills.filter { bill ->
            val isPaid = payments.any { it.billId == bill.id }
            if (isPaid) return@filter true

            // Check if bill has started
            val startYear = bill.startYear
            val startMonth = bill.startMonth
            if (startYear != null && startMonth != null) {
                val hasStarted = (selectedYear > startYear) || (selectedYear == startYear && selectedMonth >= startMonth)
                if (!hasStarted) return@filter false
            }

            if (!bill.isArchived) return@filter true
            val archYear = bill.archiveYear ?: return@filter true
            val archMonth = bill.archiveMonth ?: return@filter true
            (selectedYear < archYear) || (selectedYear == archYear && selectedMonth < archMonth)
        }
    }

    // Calculate Summary Stats for Selected Month grouped by Currency
    val paymentsWithCurrency = remember(payments, bills) {
        payments.map { payment ->
            val bill = bills.find { it.id == payment.billId }
            val curr = bill?.currency ?: "$"
            payment to curr
        }
    }

    val paidSumByCurrency = remember(paymentsWithCurrency) {
        paymentsWithCurrency.groupBy { it.second }
            .mapValues { entry -> entry.value.sumOf { it.first.paidAmount } }
    }

    val pendingSumByCurrency = remember(visibleBills, payments) {
        val paidBillIds = payments.map { it.billId }.toSet()
        visibleBills.filter { it.id !in paidBillIds }
            .groupBy { it.currency }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val constantSumByCurrency = remember(visibleBills) {
        visibleBills.filter { it.isRecurring && !it.isLoan }
            .groupBy { it.currency }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    // Helper: list of month names based on calendar and language
    val monthsList: List<String> = remember(calendarMode, appLanguage) {
        if (calendarMode == "Jalali") {
            if (appLanguage == "fa") Loc.faMonths else Loc.enMonths
        } else {
            Loc.gregMonths
        }
    }

    // Determine upcoming bill alerts (only applicable for current month & year view)
    val isViewingCurrentMonth = (selectedYear == currentYear && selectedMonth == currentMonth)
    val alertBills = remember(visibleBills, payments, isViewingCurrentMonth, currentDay) {
        if (!isViewingCurrentMonth) emptyList()
        else {
            val paidBillIds = payments.map { it.billId }.toSet()
            visibleBills.filter { bill ->
                bill.id !in paidBillIds && (
                    // Overdue or due within 3 days
                    bill.dueDateDay < currentDay || (bill.dueDateDay - currentDay) in 0..3
                )
            }.sortedBy { it.dueDateDay }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Month Switcher Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.changeMonth(-1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Month")
                }

                // Month-Year label with a dropdown button indicating it's clickable
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showMonthPicker = true }
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Select Month",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${monthsList[selectedMonth - 1]} $selectedYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { viewModel.changeMonth(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Budget / Spending Summary Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.t("stats_and_insights", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Row 1: Paid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF10B981), shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Loc.t("paid_bills", appLanguage),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = Loc.formatCurrencyMap(paidSumByCurrency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981) // Emerald Green
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Row 2: Remaining
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.error, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Loc.t("pending_bills", appLanguage),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = Loc.formatCurrencyMap(pendingSumByCurrency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Row 3: Constant Budget
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Loc.t("constant_expenses", appLanguage),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = Loc.formatCurrencyMap(constantSumByCurrency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 3. Alerts section for upcoming / overdue deadlines
            AnimatedVisibility(visible = alertBills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationImportant,
                                contentDescription = "Alert",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Loc.t("upcoming_alerts", appLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        alertBills.take(2).forEach { bill ->
                            val daysLeft = bill.dueDateDay - currentDay
                            val alertText = if (appLanguage == "fa") {
                                if (daysLeft < 0) {
                                    "• ${bill.name} به مدت ${-daysLeft} روز به تاخیر افتاده است (${bill.currency}${bill.amount})"
                                } else if (daysLeft == 0) {
                                    "• ${bill.name} سررسید امروز است (${bill.currency}${bill.amount})"
                                } else {
                                    "• ${bill.name} سررسید در $daysLeft روز آینده (${bill.currency}${bill.amount})"
                                }
                            } else {
                                if (daysLeft < 0) {
                                    "• ${bill.name} is OVERDUE by ${-daysLeft} days (${bill.currency}${bill.amount})"
                                } else if (daysLeft == 0) {
                                    "• ${bill.name} is due TODAY (${bill.currency}${bill.amount})"
                                } else {
                                    "• ${bill.name} is due in $daysLeft days (${bill.currency}${bill.amount})"
                                }
                            }
                            Text(
                                text = alertText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 28.dp, bottom = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Bills list header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Loc.t("bills_list", appLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (appLanguage == "fa") "${visibleBills.size} مورد" else "${visibleBills.size} total items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Bills list
            if (visibleBills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Loc.t("no_bills", appLanguage),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(visibleBills.sortedBy { it.dueDateDay }) { bill ->
                        val isPaid = payments.any { it.billId == bill.id }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPaid) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { billDetailsToShow = bill }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status Circle/Checkbox
                                IconButton(
                                    onClick = {
                                        if (!isPaid && bill.isVariable) {
                                            billForVariablePayment = bill
                                        } else {
                                            viewModel.togglePayment(bill, selectedYear, selectedMonth, !isPaid)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Toggle Paid",
                                        tint = if (isPaid) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Bill info
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = bill.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isPaid) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (!bill.remindersEnabled) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.NotificationsOff,
                                                contentDescription = "Reminders Disabled",
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))

                                    val daysLeftText = remember(bill.dueDateDay, isViewingCurrentMonth, currentDay, appLanguage) {
                                        if (isPaid) {
                                            Loc.t("paid_bills", appLanguage)
                                        } else if (isViewingCurrentMonth) {
                                            val diff = bill.dueDateDay - currentDay
                                            if (appLanguage == "fa") {
                                                when {
                                                    diff < 0 -> "${-diff} روز تاخیر!"
                                                    diff == 0 -> "امروز سررسید"
                                                    diff == 1 -> "سررسید فردا"
                                                    else -> "$diff روز مانده"
                                                }
                                            } else {
                                                when {
                                                    diff < 0 -> "Overdue by ${-diff} days!"
                                                    diff == 0 -> "Due TODAY"
                                                    diff == 1 -> "Due tomorrow"
                                                    else -> "Due in $diff days"
                                                }
                                            }
                                        } else {
                                            if (appLanguage == "fa") "روز سررسید ${bill.dueDateDay}" else "Due Day ${bill.dueDateDay}"
                                        }
                                    }

                                    val dueDateColor = when {
                                        isPaid -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        daysLeftText.contains("Overdue") || daysLeftText.contains("TODAY") -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    color = CategoryColors[bill.category] ?: Color.Gray,
                                                    shape = CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${bill.category} • $daysLeftText",
                                            fontSize = 12.sp,
                                            color = dueDateColor,
                                            fontWeight = if (daysLeftText.contains("Overdue") && !isPaid) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }

                                // Amount & Icon
                                Column(horizontalAlignment = Alignment.End) {
                                    val amountText = if (isPaid) {
                                        val paidAmt = payments.find { it.billId == bill.id }?.paidAmount ?: bill.amount
                                        val formatted = String.format("%,.2f", paidAmt).replace(".00", "")
                                        "${bill.currency}$formatted"
                                    } else if (bill.isVariable) {
                                        if (bill.amount > 0.0) {
                                            val formatted = String.format("%,.2f", bill.amount).replace(".00", "")
                                            if (appLanguage == "fa") "تخمینی ${bill.currency}$formatted" else "Est. ${bill.currency}$formatted"
                                        } else {
                                            Loc.t("variable_label", appLanguage)
                                        }
                                    } else {
                                        val formatted = String.format("%,.2f", bill.amount).replace(".00", "")
                                        "${bill.currency}$formatted"
                                    }

                                    Text(
                                        text = amountText,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = if (isPaid) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                    )

                                    if (bill.isLoan) {
                                        Text(
                                            text = "${bill.remainingInstallments} remaining",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Dialogs ---

        // Dialog: Add Bill
        if (showAddDialog) {
            AddEditBillDialog(
                defaultStartYear = selectedYear,
                defaultStartMonth = selectedMonth,
                calendarMode = calendarMode,
                currencySymbol = selectedCurrency,
                appLanguage = appLanguage,
                onDismiss = { showAddDialog = false },
                onSave = { name, amount, isRecurring, isLoan, total, remaining, day, category, methodType, methodValue, notes, remindersEnabled, currency, startYear, startMonth, isVariable ->
                    viewModel.addBill(name, amount, isRecurring, isLoan, total, remaining, day, category, methodType, methodValue, notes, remindersEnabled, currency, startYear, startMonth, isVariable)
                    showAddDialog = false
                }
            )
        }

        // Dialog: Edit Bill
        if (billToEdit != null) {
            AddEditBillDialog(
                billToEdit = billToEdit,
                defaultStartYear = selectedYear,
                defaultStartMonth = selectedMonth,
                calendarMode = calendarMode,
                currencySymbol = selectedCurrency,
                appLanguage = appLanguage,
                onDismiss = { billToEdit = null },
                onSave = { name, amount, isRecurring, isLoan, total, remaining, day, category, methodType, methodValue, notes, remindersEnabled, currency, startYear, startMonth, isVariable ->
                    val updated = billToEdit!!.copy(
                        name = name,
                        amount = amount,
                        isRecurring = isRecurring,
                        isLoan = isLoan,
                        totalInstallments = total,
                        remainingInstallments = remaining,
                        dueDateDay = day,
                        category = category,
                        paymentMethodType = methodType,
                        paymentMethodValue = methodValue,
                        notes = notes,
                        remindersEnabled = remindersEnabled,
                        currency = currency,
                        startYear = startYear,
                        startMonth = startMonth,
                        isVariable = isVariable
                    )
                    viewModel.updateBill(updated)
                    billToEdit = null
                }
            )
        }

        // Dialog: Bill Details
        if (billDetailsToShow != null) {
            val bill = billDetailsToShow!!
            val isPaid = payments.any { it.billId == bill.id }
            val paidAmount = payments.find { it.billId == bill.id }?.paidAmount

            BillDetailsDialog(
                bill = bill,
                isPaid = isPaid,
                paidAmount = paidAmount,
                calendarMode = calendarMode,
                currencySymbol = selectedCurrency,
                appLanguage = appLanguage,
                onDismiss = { billDetailsToShow = null },
                onEdit = {
                    billToEdit = bill
                    billDetailsToShow = null
                },
                onDelete = {
                    billToDeleteWithOptions = bill
                    billDetailsToShow = null
                },
                onTogglePayment = {
                    if (!isPaid && bill.isVariable) {
                        billForVariablePayment = bill
                    } else {
                        viewModel.togglePayment(bill, selectedYear, selectedMonth, !isPaid)
                    }
                    billDetailsToShow = null
                }
            )
        }

        // Dialog: Pay Variable Amount
        if (billForVariablePayment != null) {
            val bill = billForVariablePayment!!
            PayVariableAmountDialog(
                bill = bill,
                appLanguage = appLanguage,
                onDismiss = { billForVariablePayment = null },
                onSave = { actualPaidAmount ->
                    viewModel.togglePayment(bill, selectedYear, selectedMonth, true, actualPaidAmount)
                    billForVariablePayment = null
                }
            )
        }

        // Dialog: Delete Options
        if (billToDeleteWithOptions != null) {
            val billToDel = billToDeleteWithOptions!!
            AlertDialog(
                onDismissRequest = { billToDeleteWithOptions = null },
                title = { 
                    Text(
                        text = "Delete Options",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "How would you like to delete \"${billToDel.name}\"?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Option 1: Archive/Finish
                        Button(
                            onClick = {
                                viewModel.archiveBill(billToDel.id, selectedYear, selectedMonth)
                                billToDeleteWithOptions = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Finish & Stop Tracking",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Stop tracking from this month onwards; past history stays.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    lineHeight = 12.sp
                                )
                            }
                        }

                        // Option 2: Delete completely
                        FilledTonalButton(
                            onClick = {
                                viewModel.deleteBill(billToDel.id)
                                billToDeleteWithOptions = null
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Delete Completely",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Remove all history across all months permanently.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { billToDeleteWithOptions = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog: Calendar Month/Year Quick Picker
        if (showMonthPicker) {
            Dialog(onDismissRequest = { showMonthPicker = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (appLanguage == "fa") "انتخاب ماه و سال" else "Select Month & Year",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Year Selector Controls
                        var pickerYear by remember { mutableStateOf(selectedYear) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { pickerYear-- }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Year")
                            }
                            Text(
                                text = pickerYear.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { pickerYear++ }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Year")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid of 12 Months
                        val pickerMonthLabels = remember(calendarMode, appLanguage, monthsList) {
                            if (calendarMode == "Jalali") {
                                if (appLanguage == "fa") {
                                    monthsList
                                } else {
                                    monthsList.map { it.take(3) }
                                }
                            } else {
                                monthsList.map { it.take(3) }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (row in 0..3) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (col in 0..2) {
                                        val index = row * 3 + col
                                        val monthNum = index + 1
                                        val isSelected = (selectedMonth == monthNum && selectedYear == pickerYear)

                                        Button(
                                            onClick = {
                                                viewModel.setMonthAndYear(monthNum, pickerYear)
                                                showMonthPicker = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                },
                                                contentColor = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            ),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = pickerMonthLabels[index],
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = {
                                viewModel.setMonthAndYear(currentMonth, currentYear)
                                showMonthPicker = false
                            }
                        ) {
                            Text(if (appLanguage == "fa") "بازنشانی به ماه جاری" else "Reset to Current Month")
                        }
                    }
                }
            }
        }
    }
}
