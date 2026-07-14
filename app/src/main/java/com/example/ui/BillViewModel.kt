package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Bill
import com.example.data.BillPaymentHistory
import com.example.data.BillRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class BillViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BillRepository
    private val sharedPreferences = application.getSharedPreferences("BillTrackerPrefs", Application.MODE_PRIVATE)

    // Selected Month & Year (for the Calendar View)
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1) // 1-indexed (1 = Jan, 12 = Dec)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Calendar & Language selection StateFlows
    private val _calendarMode = MutableStateFlow(sharedPreferences.getString("calendar_mode", "Gregorian") ?: "Gregorian")
    val calendarMode: StateFlow<String> = _calendarMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(sharedPreferences.getString("app_language", "en") ?: "en")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // Currency selection StateFlow
    private val _selectedCurrency = MutableStateFlow(sharedPreferences.getString("currency", "$") ?: "$")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    // Notification preferences
    private val _notificationsEnabled = MutableStateFlow(sharedPreferences.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _daysBeforeNotify = MutableStateFlow(sharedPreferences.getInt("days_before_notify", 1))
    val daysBeforeNotify: StateFlow<Int> = _daysBeforeNotify.asStateFlow()

    private val _notifyHour = MutableStateFlow(sharedPreferences.getInt("notify_hour", 9))
    val notifyHour: StateFlow<Int> = _notifyHour.asStateFlow()

    private val _notifyMinute = MutableStateFlow(sharedPreferences.getInt("notify_minute", 0))
    val notifyMinute: StateFlow<Int> = _notifyMinute.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BillRepository(database.billDao())
        
        // Initialize date based on calendar mode
        val mode = sharedPreferences.getString("calendar_mode", "Gregorian") ?: "Gregorian"
        val today = Calendar.getInstance()
        if (mode == "Jalali") {
            val jalali = JalaliHelper.gregorianToJalali(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH)
            )
            _selectedYear.value = jalali.year
            _selectedMonth.value = jalali.month
        } else {
            _selectedYear.value = today.get(Calendar.YEAR)
            _selectedMonth.value = today.get(Calendar.MONTH) + 1
        }
        
        BillAlarmScheduler.scheduleAlarms(application)
    }

    fun setCalendarMode(mode: String) {
        viewModelScope.launch {
            _calendarMode.value = mode
            sharedPreferences.edit().putString("calendar_mode", mode).apply()
            
            // Re-initialize month & year to the current date of the newly selected calendar!
            val today = Calendar.getInstance()
            if (mode == "Jalali") {
                val jalali = JalaliHelper.gregorianToJalali(
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH) + 1,
                    today.get(Calendar.DAY_OF_MONTH)
                )
                _selectedYear.value = jalali.year
                _selectedMonth.value = jalali.month
            } else {
                _selectedYear.value = today.get(Calendar.YEAR)
                _selectedMonth.value = today.get(Calendar.MONTH) + 1
            }
        }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch {
            _appLanguage.value = lang
            sharedPreferences.edit().putString("app_language", lang).apply()
        }
    }

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            _selectedCurrency.value = currency
            sharedPreferences.edit().putString("currency", currency).apply()
        }
    }

    fun setNotificationSettings(enabled: Boolean, daysBefore: Int, hour: Int, minute: Int) {
        viewModelScope.launch {
            _notificationsEnabled.value = enabled
            _daysBeforeNotify.value = daysBefore
            _notifyHour.value = hour
            _notifyMinute.value = minute
            sharedPreferences.edit()
                .putBoolean("notifications_enabled", enabled)
                .putInt("days_before_notify", daysBefore)
                .putInt("notify_hour", hour)
                .putInt("notify_minute", minute)
                .apply()
            
            BillAlarmScheduler.scheduleAlarms(getApplication())
        }
    }

    // All registered bill templates
    val allBills: StateFlow<List<Bill>> = repository.allBills
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Payments recorded in the selected month & year
    val selectedMonthPayments: StateFlow<List<BillPaymentHistory>> = combine(
        _selectedYear, _selectedMonth
    ) { year, month ->
        Pair(year, month)
    }.flatMapLatest { (year, month) ->
        repository.getPaymentsForMonth(year, month)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Sum of monthly constant/recurring expenses (recalculated dynamically for selected month)
    val constantExpensesSum: StateFlow<Double> = combine(
        allBills, _selectedYear, _selectedMonth
    ) { bills, year, month ->
        bills.filter { bill ->
            val isActive = if (!bill.isArchived) {
                true
            } else {
                val archYear = bill.archiveYear
                val archMonth = bill.archiveMonth
                if (archYear != null && archMonth != null) {
                    (year < archYear) || (year == archYear && month < archMonth)
                } else {
                    true
                }
            }
            val hasStarted = if (bill.startYear != null && bill.startMonth != null) {
                (year > bill.startYear) || (year == bill.startYear && month >= bill.startMonth)
            } else {
                true
            }
            isActive && hasStarted && (bill.isRecurring || bill.isLoan)
        }.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // List of payments for all months to calculate trend charts
    val allPaymentsHistory: StateFlow<List<BillPaymentHistory>> = combine(
        _selectedYear, _calendarMode
    ) { year, mode ->
        Pair(year, mode)
    }.flatMapLatest { (year, mode) ->
        combine((1..12).map { m -> repository.getPaymentsForMonth(year, m) }) { arrays ->
            arrays.flatMap { it.toList() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun changeMonth(offset: Int) {
        viewModelScope.launch {
            var newMonth = _selectedMonth.value + offset
            var newYear = _selectedYear.value
            if (newMonth > 12) {
                newMonth = 1
                newYear++
            } else if (newMonth < 1) {
                newMonth = 12
                newYear--
            }
            _selectedMonth.value = newMonth
            _selectedYear.value = newYear
        }
    }

    fun setMonthAndYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun addBill(
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
        remindersEnabled: Boolean = true,
        currency: String = "$",
        startYear: Int? = null,
        startMonth: Int? = null,
        isVariable: Boolean = false
    ) {
        viewModelScope.launch {
            val bill = Bill(
                name = name,
                amount = amount,
                isRecurring = isRecurring,
                isLoan = isLoan,
                totalInstallments = if (isLoan) totalInstallments else 0,
                remainingInstallments = if (isLoan) remainingInstallments else 0,
                dueDateDay = dueDateDay,
                category = category,
                paymentMethodType = paymentMethodType,
                paymentMethodValue = paymentMethodValue,
                notes = notes,
                remindersEnabled = remindersEnabled,
                currency = currency,
                startYear = startYear,
                startMonth = startMonth,
                isVariable = isVariable
            )
            repository.saveBill(bill)
        }
    }

    fun updateBill(bill: Bill) {
        viewModelScope.launch {
            repository.saveBill(bill)
        }
    }

    fun deleteBill(billId: Int) {
        viewModelScope.launch {
            repository.deleteBillById(billId)
        }
    }

    fun archiveBill(billId: Int, year: Int, month: Int) {
        viewModelScope.launch {
            val bill = repository.getBillById(billId)
            if (bill != null) {
                val updatedBill = bill.copy(
                    isArchived = true,
                    archiveYear = year,
                    archiveMonth = month
                )
                repository.saveBill(updatedBill)
            }
        }
    }

    fun togglePayment(bill: Bill, year: Int, month: Int, isPaid: Boolean, customAmount: Double? = null) {
        viewModelScope.launch {
            if (isPaid) {
                repository.markBillAsPaid(bill.id, year, month, customAmount ?: bill.amount)
            } else {
                repository.markBillAsUnpaid(bill.id, year, month)
            }
        }
    }
}

class BillViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
