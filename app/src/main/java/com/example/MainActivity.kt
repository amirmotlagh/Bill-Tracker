package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BillViewModel
import com.example.ui.BillViewModelFactory
import com.example.ui.Loc
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.TrendsScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize VM using the application context factory
                val appViewModel: BillViewModel = viewModel(
                    factory = BillViewModelFactory(application)
                )

                var selectedTab by remember { mutableIntStateOf(0) }

                val selectedCurrency by appViewModel.selectedCurrency.collectAsState()
                val appLanguage by appViewModel.appLanguage.collectAsState()
                val calendarMode by appViewModel.calendarMode.collectAsState()
                var currencyMenuExpanded by remember { mutableStateOf(false) }
                var showCustomCurrencyDialog by remember { mutableStateOf(false) }
                var customCurrencyInput by remember { mutableStateOf("") }

                val notificationsEnabled by appViewModel.notificationsEnabled.collectAsState()
                val daysBeforeNotify by appViewModel.daysBeforeNotify.collectAsState()
                val notifyHour by appViewModel.notifyHour.collectAsState()
                val notifyMinute by appViewModel.notifyMinute.collectAsState()

                var showSettingsDialog by remember { mutableStateOf(false) }

                val hasNotificationPermission = remember {
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else {
                            true
                        }
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        hasNotificationPermission.value = isGranted
                        if (!isGranted) {
                            appViewModel.setNotificationSettings(false, daysBeforeNotify, notifyHour, notifyMinute)
                        }
                    }
                )

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            navigationIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .size(36.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_app_logo),
                                        contentDescription = "App Logo",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            title = {
                                Text(
                                    text = if (selectedTab == 0) Loc.t("app_title", appLanguage) else Loc.t("stats_and_insights", appLanguage),
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            actions = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Box {
                                        FilledTonalButton(
                                            onClick = { currencyMenuExpanded = true },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = "Currency selector",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = selectedCurrency, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                        }

                                        DropdownMenu(
                                            expanded = currencyMenuExpanded,
                                            onDismissRequest = { currencyMenuExpanded = false }
                                        ) {
                                            val currencies = listOf("$", "€", "£", "¥", "₹", "C$", "A$", "IRR", "₪", "₩")
                                            currencies.forEach { curr ->
                                                DropdownMenuItem(
                                                    text = { Text(curr) },
                                                    onClick = {
                                                        appViewModel.setCurrency(curr)
                                                        currencyMenuExpanded = false
                                                    }
                                                )
                                            }
                                            HorizontalDivider()
                                            DropdownMenuItem(
                                                text = { Text("Custom...") },
                                                onClick = {
                                                    customCurrencyInput = selectedCurrency
                                                    showCustomCurrencyDialog = true
                                                    currencyMenuExpanded = false
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { showSettingsDialog = true },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Notification Settings",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                label = { Text(Loc.t("dashboard", appLanguage), fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Dashboard,
                                        contentDescription = "Dashboard"
                                    )
                                }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                label = { Text(Loc.t("trends", appLanguage), fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Spending Trends"
                                    )
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (selectedTab == 0) {
                            DashboardScreen(viewModel = appViewModel)
                        } else {
                            TrendsScreen(viewModel = appViewModel)
                        }

                        if (showCustomCurrencyDialog) {
                            AlertDialog(
                                onDismissRequest = { showCustomCurrencyDialog = false },
                                title = { Text("Custom Currency", fontWeight = FontWeight.Bold) },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Type custom symbol or letters (e.g. IRR, USD, CHF, TL, CA$):", fontSize = 14.sp)
                                        OutlinedTextField(
                                            value = customCurrencyInput,
                                            onValueChange = { customCurrencyInput = it },
                                            placeholder = { Text("e.g. IRR") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            if (customCurrencyInput.isNotBlank()) {
                                                appViewModel.setCurrency(customCurrencyInput.trim())
                                            }
                                            showCustomCurrencyDialog = false
                                        }
                                    ) {
                                        Text("Set")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showCustomCurrencyDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                        if (showSettingsDialog) {
                            var tempEnabled by remember { mutableStateOf(notificationsEnabled) }
                            var tempDaysBefore by remember { mutableStateOf(daysBeforeNotify) }
                            var tempHour by remember { mutableStateOf(notifyHour) }
                            var tempMinute by remember { mutableStateOf(notifyMinute) }

                            var daysExpanded by remember { mutableStateOf(false) }
                            var hourExpanded by remember { mutableStateOf(false) }
                            var minuteExpanded by remember { mutableStateOf(false) }

                            AlertDialog(
                                onDismissRequest = { showSettingsDialog = false },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(Loc.t("settings", appLanguage), fontWeight = FontWeight.Bold)
                                    }
                                },
                                text = {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // 1. Language Selection dropdown
                                        var langExpanded by remember { mutableStateOf(false) }
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = Loc.t("settings_language", appLanguage),
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedButton(
                                                    onClick = { langExpanded = true },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(if (appLanguage == "fa") "فارسی (Persian)" else "English", fontWeight = FontWeight.Medium)
                                                }
                                                DropdownMenu(
                                                    expanded = langExpanded,
                                                    onDismissRequest = { langExpanded = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("English") },
                                                        onClick = {
                                                            appViewModel.setAppLanguage("en")
                                                            langExpanded = false
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("فارسی (Persian)") },
                                                        onClick = {
                                                            appViewModel.setAppLanguage("fa")
                                                            langExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                                        // 2. Calendar Mode Picker dropdown
                                        var calExpanded by remember { mutableStateOf(false) }
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = Loc.t("settings_calendar", appLanguage),
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedButton(
                                                    onClick = { calExpanded = true },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(if (calendarMode == "Jalali") Loc.t("jalali", appLanguage) else Loc.t("gregorian", appLanguage), fontWeight = FontWeight.Medium)
                                                }
                                                DropdownMenu(
                                                    expanded = calExpanded,
                                                    onDismissRequest = { calExpanded = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text(Loc.t("gregorian", appLanguage)) },
                                                        onClick = {
                                                            appViewModel.setCalendarMode("Gregorian")
                                                            calExpanded = false
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text(Loc.t("jalali", appLanguage)) },
                                                        onClick = {
                                                            appViewModel.setCalendarMode("Jalali")
                                                            calExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                                        // 3. Enable Due Reminders Row
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = Loc.t("enable_due_reminders", appLanguage),
                                                    fontWeight = FontWeight.SemiBold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = Loc.t("enable_due_reminders_desc", appLanguage),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                            Switch(
                                                checked = tempEnabled,
                                                onCheckedChange = { isChecked ->
                                                    if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                        val isGranted = ContextCompat.checkSelfPermission(
                                                            this@MainActivity,
                                                            Manifest.permission.POST_NOTIFICATIONS
                                                        ) == PackageManager.PERMISSION_GRANTED
                                                        if (!isGranted) {
                                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                        } else {
                                                            tempEnabled = true
                                                        }
                                                    } else {
                                                        tempEnabled = isChecked
                                                    }
                                                }
                                            )
                                        }

                                        if (tempEnabled) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = Loc.t("days_left_payment", appLanguage),
                                                    fontWeight = FontWeight.SemiBold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                    OutlinedButton(
                                                        onClick = { daysExpanded = true },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        val text = when (tempDaysBefore) {
                                                            0 -> Loc.t("same_day", appLanguage)
                                                            1 -> Loc.t("one_day_before", appLanguage)
                                                            else -> String.format(Loc.t("days_before", appLanguage), tempDaysBefore)
                                                        }
                                                        Text(text, fontWeight = FontWeight.Medium)
                                                    }
                                                    DropdownMenu(
                                                        expanded = daysExpanded,
                                                        onDismissRequest = { daysExpanded = false }
                                                    ) {
                                                        listOf(0, 1, 2, 3, 5, 7).forEach { days ->
                                                            DropdownMenuItem(
                                                                text = {
                                                                    val t = when (days) {
                                                                        0 -> Loc.t("same_day", appLanguage)
                                                                        1 -> Loc.t("one_day_before", appLanguage)
                                                                        else -> String.format(Loc.t("days_before", appLanguage), days)
                                                                    }
                                                                    Text(t)
                                                                },
                                                                onClick = {
                                                                    tempDaysBefore = days
                                                                    daysExpanded = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = Loc.t("time_of_day", appLanguage),
                                                    fontWeight = FontWeight.SemiBold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        OutlinedButton(
                                                            onClick = { hourExpanded = true },
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(String.format(Loc.t("hour_label", appLanguage), tempHour), fontWeight = FontWeight.Medium)
                                                        }
                                                        DropdownMenu(
                                                            expanded = hourExpanded,
                                                            onDismissRequest = { hourExpanded = false }
                                                        ) {
                                                            (0..23).forEach { h ->
                                                                DropdownMenuItem(
                                                                    text = { Text(String.format("%02d:00", h)) },
                                                                    onClick = {
                                                                        tempHour = h
                                                                        hourExpanded = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Box(modifier = Modifier.weight(1f)) {
                                                        OutlinedButton(
                                                            onClick = { minuteExpanded = true },
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(String.format(Loc.t("minute_label", appLanguage), tempMinute), fontWeight = FontWeight.Medium)
                                                        }
                                                        DropdownMenu(
                                                            expanded = minuteExpanded,
                                                            onDismissRequest = { minuteExpanded = false }
                                                        ) {
                                                            listOf(0, 15, 30, 45).forEach { m ->
                                                                DropdownMenuItem(
                                                                    text = { Text(String.format(":%02d", m)) },
                                                                    onClick = {
                                                                        tempMinute = m
                                                                        minuteExpanded = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            appViewModel.setNotificationSettings(tempEnabled, tempDaysBefore, tempHour, tempMinute)
                                            showSettingsDialog = false
                                        }
                                    ) {
                                        Text(Loc.t("save", appLanguage))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showSettingsDialog = false }) {
                                        Text(Loc.t("cancel", appLanguage))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
