package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val isRecurring: Boolean, // True for constant monthly expenses/recurring bills
    val isLoan: Boolean, // True if this is a loan with installments
    val totalInstallments: Int = 0, // e.g. 36 for a 3-year car loan
    val remainingInstallments: Int = 0, // e.g. 24 payments left
    val dueDateDay: Int = 1, // Day of the month (1 to 31)
    val category: String = "Other", // e.g., "Housing", "Utilities", "Subscription", "Loan", "Insurance", "Other"
    val paymentMethodType: String = "Other", // e.g., "Bank Account", "App", "Website", "Other"
    val paymentMethodValue: String = "", // e.g., bank account number, PayPal app name, or URL
    val notes: String = "",
    val isArchived: Boolean = false,
    val archiveYear: Int? = null,
    val archiveMonth: Int? = null,
    val remindersEnabled: Boolean = true,
    val currency: String = "$",
    val startYear: Int? = null,
    val startMonth: Int? = null,
    val isVariable: Boolean = false
)

@Entity(tableName = "bill_payment_history")
data class BillPaymentHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val billId: Int,
    val year: Int,
    val month: Int, // 1 for Jan, 12 for Dec
    val paidAmount: Double,
    val paymentDate: Long = System.currentTimeMillis()
)
