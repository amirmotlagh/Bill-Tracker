package com.example.ui.components

import com.example.ui.Loc
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Bill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBillDialog(
    billToEdit: Bill? = null,
    defaultStartYear: Int,
    defaultStartMonth: Int,
    calendarMode: String = "Gregorian",
    currencySymbol: String = "$",
    appLanguage: String = "en",
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        amount: Double,
        isRecurring: Boolean,
        isLoan: Boolean,
        totalInstallments: Int,
        remainingInstallments: Int,
        dueDateDay: Int,
        category: String,
        paymentMethodType: String,
        paymentMethodValue: String,
        notes: String,
        remindersEnabled: Boolean,
        currency: String,
        startYear: Int?,
        startMonth: Int?,
        isVariable: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(billToEdit?.name ?: "") }
    var isVariable by remember { mutableStateOf(billToEdit?.isVariable ?: false) }
    var amountStr by remember { mutableStateOf(if (billToEdit != null && billToEdit.amount == 0.0 && billToEdit.isVariable) "" else billToEdit?.amount?.toString() ?: "") }
    var isRecurring by remember { mutableStateOf(billToEdit?.isRecurring ?: true) }
    var isLoan by remember { mutableStateOf(billToEdit?.isLoan ?: false) }
    var selectedType by remember {
        mutableStateOf(
            when {
                billToEdit?.isLoan == true -> "loan"
                billToEdit?.isRecurring == true -> "recurring"
                else -> "regular"
            }
        )
    }
    var totalInstallmentsStr by remember { mutableStateOf(billToEdit?.totalInstallments?.toString() ?: "12") }
    var remainingInstallmentsStr by remember { mutableStateOf(billToEdit?.remainingInstallments?.toString() ?: "12") }
    var dueDateDayStr by remember { mutableStateOf(billToEdit?.dueDateDay?.toString() ?: "1") }
    var remindersEnabled by remember { mutableStateOf(billToEdit?.remindersEnabled ?: true) }
    var startYear by remember { mutableStateOf(billToEdit?.startYear ?: defaultStartYear) }
    var startMonth by remember { mutableStateOf(billToEdit?.startMonth ?: defaultStartMonth) }

    val categories = listOf("Housing", "Utilities", "Subscription", "Loan", "Insurance", "Other")
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(billToEdit?.category ?: "Utilities") }

    val paymentMethodTypes = listOf("Bank Account", "Mobile App", "Website URL", "Cash", "Other")
    var paymentMethodTypeExpanded by remember { mutableStateOf(false) }
    var selectedPaymentMethodType by remember { mutableStateOf(billToEdit?.paymentMethodType ?: "Bank Account") }
    var paymentMethodValue by remember { mutableStateOf(billToEdit?.paymentMethodValue ?: "") }
    var notes by remember { mutableStateOf(billToEdit?.notes ?: "") }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var currencyExpanded by remember { mutableStateOf(false) }
    var customCurrencyEnabled by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(billToEdit?.currency ?: currencySymbol) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        title = {
            Text(
                text = if (billToEdit == null) Loc.t("add_bill", appLanguage) else Loc.t("edit_bill", appLanguage),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showError) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Bill Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Loc.t("name", appLanguage)) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    placeholder = { Text("e.g. Electric Bill, Rent") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount
                val amountLabel = if (isVariable) {
                    if (appLanguage == "fa") "مبلغ تخمینی (اختیاری)" else "Estimated Amount (Optional)"
                } else {
                    Loc.t("amount", appLanguage)
                }
                val amountPlaceholder = if (isVariable) {
                    if (appLanguage == "fa") "مثال: ۵۰,۰۰۰ (یا خالی بگذارید)" else "e.g. 50.00 (or leave empty)"
                } else {
                    "e.g. 150.00"
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(amountLabel) },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text(amountPlaceholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Currency Dropdown
                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = !currencyExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCurrency,
                        onValueChange = {
                            selectedCurrency = it
                        },
                        readOnly = !customCurrencyEnabled,
                        label = { Text(Loc.t("multi_currency", appLanguage)) },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        trailingIcon = { 
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false }
                    ) {
                        val currencyOptions = listOf("$", "€", "£", "¥", "﷼", "IRR", "Custom")
                        currencyOptions.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text(curr) },
                                onClick = {
                                    if (curr == "Custom") {
                                        customCurrencyEnabled = true
                                        selectedCurrency = ""
                                    } else {
                                        customCurrencyEnabled = false
                                        selectedCurrency = curr
                                    }
                                    currencyExpanded = false
                                }
                            )
                        }
                    }
                }

                // Due Date Day (1-31)
                OutlinedTextField(
                    value = dueDateDayStr,
                    onValueChange = { dueDateDayStr = it },
                    label = { Text(Loc.t("due_day", appLanguage)) },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("e.g. 15") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Loc.t("category", appLanguage)) },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Start Month and Year Section
                val monthsList = remember(calendarMode, appLanguage) {
                    if (calendarMode == "Jalali") {
                        if (appLanguage == "fa") Loc.faMonths else Loc.enMonths
                    } else {
                        Loc.gregMonths
                    }
                }

                Text(
                    text = "${Loc.t("start_month", appLanguage)} & ${Loc.t("start_year", appLanguage)}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Month Dropdown
                    var startMonthExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = startMonthExpanded,
                        onExpandedChange = { startMonthExpanded = !startMonthExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = monthsList.getOrNull(startMonth - 1) ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Loc.t("start_month", appLanguage)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startMonthExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = startMonthExpanded,
                            onDismissRequest = { startMonthExpanded = false }
                        ) {
                            monthsList.forEachIndexed { index, monthName ->
                                DropdownMenuItem(
                                    text = { Text(monthName) },
                                    onClick = {
                                        startMonth = index + 1
                                        startMonthExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Year Dropdown
                    var startYearExpanded by remember { mutableStateOf(false) }
                    val yearsList = remember(defaultStartYear) {
                        (defaultStartYear - 2..defaultStartYear + 10).toList()
                    }
                    ExposedDropdownMenuBox(
                        expanded = startYearExpanded,
                        onExpandedChange = { startYearExpanded = !startYearExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = startYear.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Loc.t("start_year", appLanguage)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startYearExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = startYearExpanded,
                            onDismissRequest = { startYearExpanded = false }
                        ) {
                            yearsList.forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(y.toString()) },
                                    onClick = {
                                        startYear = y
                                        startYearExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Mutually exclusive type options (Radio Buttons)
                Text(
                    text = "Payment Type",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Regular Bill / Expense
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedType = "regular"
                                isRecurring = false
                                isLoan = false
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (selectedType == "regular"),
                            onClick = {
                                selectedType = "regular"
                                isRecurring = false
                                isLoan = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Regular Bill / One-time Expense",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Standard bills (utilities, shopping, etc.)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // 2. Constant Monthly Expense
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedType = "recurring"
                                isRecurring = true
                                isLoan = false
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (selectedType == "recurring"),
                            onClick = {
                                selectedType = "recurring"
                                isRecurring = true
                                isLoan = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Constant Monthly Expense",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Keeps tracking every month automatically (rent, subscription)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // 3. Loan / Installment Plan
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedType = "loan"
                                isRecurring = true
                                isLoan = true
                                selectedCategory = "Loan"
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (selectedType == "loan"),
                            onClick = {
                                selectedType = "loan"
                                isRecurring = true
                                isLoan = true
                                selectedCategory = "Loan"
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Loan / Installment Plan",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Track payments remaining and total duration (car loan, etc.)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // If Loan, show installment inputs
                if (isLoan) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = totalInstallmentsStr,
                            onValueChange = { totalInstallmentsStr = it },
                            label = { Text("Total Payments") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = remainingInstallmentsStr,
                            onValueChange = { remainingInstallmentsStr = it },
                            label = { Text("Remaining") },
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Payment Method Details",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Payment Method Type
                ExposedDropdownMenuBox(
                    expanded = paymentMethodTypeExpanded,
                    onExpandedChange = { paymentMethodTypeExpanded = !paymentMethodTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedPaymentMethodType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Source Type") },
                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMethodTypeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = paymentMethodTypeExpanded,
                        onDismissRequest = { paymentMethodTypeExpanded = false }
                    ) {
                        paymentMethodTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedPaymentMethodType = type
                                    paymentMethodTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Payment Method Value (Bank Account No, URL, etc.)
                val valueLabel = when (selectedPaymentMethodType) {
                    "Bank Account" -> "Bank Account Number / Details"
                    "Mobile App" -> "App Name (PayPal, Venmo, Revolut, etc.)"
                    "Website URL" -> "Payment Link / Website Address"
                    else -> "Details / Reference Info"
                }

                val valuePlaceholder = when (selectedPaymentMethodType) {
                    "Bank Account" -> "e.g. US12 3456 7890 1234"
                    "Mobile App" -> "e.g. PayPal, Venmo, Apple Pay"
                    "Website URL" -> "e.g. www.utilitycompany.com/pay"
                    else -> "Enter payment reference"
                }

                OutlinedTextField(
                    value = paymentMethodValue,
                    onValueChange = { paymentMethodValue = it },
                    label = { Text(valueLabel) },
                    leadingIcon = {
                        val icon = when (selectedPaymentMethodType) {
                            "Bank Account" -> Icons.Default.AccountBalance
                            "Mobile App" -> Icons.Default.PhoneAndroid
                            "Website URL" -> Icons.Default.Language
                            else -> Icons.Default.Info
                        }
                        Icon(icon, contentDescription = null)
                    },
                    placeholder = { Text(valuePlaceholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(Loc.t("notes", appLanguage)) },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    placeholder = { Text("e.g. Autopay active, shared card") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Variable Amount Switch
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isVariable = !isVariable }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = if (isVariable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = Loc.t("variable_amount_title", appLanguage),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = Loc.t("variable_amount_desc", appLanguage),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Switch(
                        checked = isVariable,
                        onCheckedChange = { isVariable = it }
                    )
                }

                // Reminder Switch
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { remindersEnabled = !remindersEnabled }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (remindersEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (remindersEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = Loc.t("enable_due_reminders", appLanguage),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = Loc.t("reminders_desc", appLanguage),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = { remindersEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: if (isVariable) 0.0 else null
                    val dueDateDay = dueDateDayStr.toIntOrNull()
                    val totalInstallments = totalInstallmentsStr.toIntOrNull() ?: 0
                    val remainingInstallments = remainingInstallmentsStr.toIntOrNull() ?: 0

                    if (name.isBlank()) {
                        errorMessage = Loc.t("enter_name", appLanguage)
                        showError = true
                    } else if (amount == null || amount < 0.0 || (!isVariable && amount <= 0.0)) {
                        errorMessage = Loc.t("enter_amount", appLanguage)
                        showError = true
                    } else if (dueDateDay == null || dueDateDay < 1 || dueDateDay > 31) {
                        errorMessage = Loc.t("invalid_fields", appLanguage)
                        showError = true
                    } else if (isLoan && (totalInstallments < 1 || remainingInstallments < 0 || remainingInstallments > totalInstallments)) {
                        errorMessage = Loc.t("invalid_fields", appLanguage)
                        showError = true
                    } else {
                        onSave(
                            name.trim(),
                            amount,
                            isRecurring,
                            isLoan,
                            totalInstallments,
                            remainingInstallments,
                            dueDateDay,
                            selectedCategory,
                            selectedPaymentMethodType,
                            paymentMethodValue.trim(),
                            notes.trim(),
                            remindersEnabled,
                            selectedCurrency,
                            startYear,
                            startMonth,
                            isVariable
                        )
                    }
                }
            ) {
                Text(Loc.t("save", appLanguage))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Loc.t("cancel", appLanguage))
            }
        }
    )
}

@Composable
fun BillDetailsDialog(
    bill: Bill,
    isPaid: Boolean,
    paidAmount: Double? = null,
    calendarMode: String = "Gregorian",
    currencySymbol: String = "$",
    appLanguage: String = "en",
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePayment: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = bill.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                // Payment toggle inside title
                IconButton(onClick = onTogglePayment) {
                    Icon(
                        imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Mark status",
                        tint = if (isPaid) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Amount card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val amountToDisplay = if (isPaid) {
                            paidAmount ?: bill.amount
                        } else {
                            bill.amount
                        }
                        
                        val amountText = if (!isPaid && bill.isVariable && amountToDisplay == 0.0) {
                            Loc.t("variable_label", appLanguage)
                        } else {
                            val formatted = String.format("%,.2f", amountToDisplay).replace(".00", "")
                            "${bill.currency}$formatted"
                        }

                        Text(
                            text = amountText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        if (!isPaid && bill.isVariable) {
                            Text(
                                text = if (bill.amount > 0.0) Loc.t("estimated_amount", appLanguage) else Loc.t("variable_label", appLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        } else {
                            Text(
                                text = if (isPaid) Loc.t("paid_bills", appLanguage) else Loc.t("unpaid", appLanguage),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (isPaid) Color(0xFF10B981) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // General info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = Loc.t("due_day", appLanguage),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Day ${bill.dueDateDay} of Month",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = Loc.t("category", appLanguage),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = bill.category,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CategoryColors[bill.category] ?: MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Payment Start Period Detail
                if (bill.startYear != null && bill.startMonth != null) {
                    val startMonthsList = if (calendarMode == "Jalali") {
                        if (appLanguage == "fa") Loc.faMonths else Loc.enMonths
                    } else {
                        Loc.gregMonths
                    }
                    val startMonthName = startMonthsList.getOrNull(bill.startMonth - 1) ?: bill.startMonth.toString()
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (appLanguage == "fa") "دوره شروع پرداخت" else "Payment Start Period",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "$startMonthName ${bill.startYear}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Loan specific details
                if (bill.isLoan) {
                    HorizontalDivider()
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = Loc.t("installments", appLanguage),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${bill.remainingInstallments} of ${bill.totalInstallments} remaining",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            val progress = if (bill.totalInstallments > 0) {
                                (bill.totalInstallments - bill.remainingInstallments).toFloat() / bill.totalInstallments
                            } else 1f
                            Text(
                                text = String.format("%.0f%% completed", progress * 100),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val progressValue = if (bill.totalInstallments > 0) {
                             (bill.totalInstallments - bill.remainingInstallments).toFloat() / bill.totalInstallments
                        } else 1f
                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Payment Method Card
                HorizontalDivider()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = Loc.t("payment_method", appLanguage),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val icon = when (bill.paymentMethodType) {
                                "Bank Account" -> Icons.Default.AccountBalance
                                "Mobile App" -> Icons.Default.PhoneAndroid
                                "Website URL" -> Icons.Default.Language
                                else -> Icons.Default.AccountBalanceWallet
                            }
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = bill.paymentMethodType,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        if (bill.paymentMethodValue.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = bill.paymentMethodValue,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                if (bill.paymentMethodType == "Bank Account") {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(bill.paymentMethodValue))
                                            isCopied = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                            contentDescription = "Copy info",
                                            tint = if (isCopied) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else if (bill.paymentMethodType == "Website URL") {
                                    IconButton(
                                        onClick = {
                                            var url = bill.paymentMethodValue
                                            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                                url = "https://$url"
                                            }
                                            try {
                                                uriHandler.openUri(url)
                                            } catch (e: Exception) {
                                                // Handle malformed URI or lack of browser
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Visit website",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Reminders Status
                HorizontalDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (bill.remindersEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = if (bill.remindersEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = Loc.t("due_reminders", appLanguage),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (bill.remindersEnabled) Loc.t("enabled", appLanguage) else Loc.t("disabled", appLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (bill.remindersEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Notes Card
                if (bill.notes.isNotBlank()) {
                    Column {
                        Text(
                            text = Loc.t("notes", appLanguage),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = bill.notes,
                            fontSize = 13.sp,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit button
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Bill",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Bill",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDismiss) {
                    Text(Loc.t("cancel", appLanguage).replace("Cancel", "Close").replace("انصراف", "بستن"))
                }
            }
        }
    )
}

@Composable
fun PayVariableAmountDialog(
    bill: Bill,
    appLanguage: String,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amountStr by remember { mutableStateOf(if (bill.amount > 0.0) bill.amount.toString() else "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Loc.t("enter_paid_amount", appLanguage),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = Loc.t("payment_prompt_variable", appLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        showError = false
                    },
                    label = { Text(Loc.t("amount", appLanguage)) },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("e.g. 50.00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError
                )

                if (showError) {
                    Text(
                        text = Loc.t("enter_amount", appLanguage),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountStr.toDoubleOrNull()
                    if (parsed != null && parsed >= 0.0) {
                        onSave(parsed)
                    } else {
                        showError = true
                    }
                }
            ) {
                Text(Loc.t("record_payment", appLanguage))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Loc.t("cancel", appLanguage))
            }
        }
    )
}

