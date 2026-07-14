package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BillAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPrefs = context.getSharedPreferences("BillTrackerPrefs", Context.MODE_PRIVATE)
        val enabled = sharedPrefs.getBoolean("notifications_enabled", true)
        val calendarMode = sharedPrefs.getString("calendar_mode", "Gregorian") ?: "Gregorian"
        val appLang = sharedPrefs.getString("app_language", "en") ?: "en"
        
        // If it's a boot completed event, reschedule alarms and exit
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            BillAlarmScheduler.scheduleAlarms(context)
            return
        }

        if (!enabled) return

        val daysBefore = sharedPrefs.getInt("days_before_notify", 1)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val dao = database.billDao()
                
                val bills = dao.getAllBillsList()
                
                val calendar = Calendar.getInstance()
                val gregYear = calendar.get(Calendar.YEAR)
                val gregMonth = calendar.get(Calendar.MONTH) + 1 // 1-indexed
                val gregDay = calendar.get(Calendar.DAY_OF_MONTH)
                
                val currentYear: Int
                val currentMonth: Int
                val currentDay: Int
                
                if (calendarMode == "Jalali") {
                    val jalali = JalaliHelper.gregorianToJalali(gregYear, gregMonth, gregDay)
                    currentYear = jalali.year
                    currentMonth = jalali.month
                    currentDay = jalali.day
                } else {
                    currentYear = gregYear
                    currentMonth = gregMonth
                    currentDay = gregDay
                }
                
                for (bill in bills) {
                    if (!bill.remindersEnabled) continue
                    
                    // Skip if archived in this or prior month
                    if (bill.isArchived) {
                        val archYear = bill.archiveYear
                        val archMonth = bill.archiveMonth
                        if (archYear != null && archMonth != null) {
                            if ((currentYear > archYear) || (currentYear == archYear && currentMonth >= archMonth)) {
                                continue
                            }
                        }
                    }
                    
                    // Skip if current date is before start month/year
                    val startYear = bill.startYear
                    val startMonth = bill.startMonth
                    if (startYear != null && startMonth != null) {
                        if ((currentYear < startYear) || (currentYear == startYear && currentMonth < startMonth)) {
                            continue
                        }
                    }
                    
                    // Match bill due day with currentDay + daysBefore
                    if (bill.dueDateDay == currentDay + daysBefore) {
                        val isPaid = dao.isBillPaidInMonth(bill.id, currentYear, currentMonth)
                        if (!isPaid) {
                            showNotification(context, bill.id, bill.name, bill.amount, bill.currency, daysBefore, appLang)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
                // Schedule for the next day
                BillAlarmScheduler.scheduleAlarms(context)
            }
        }
    }

    private fun showNotification(
        context: Context, 
        billId: Int, 
        name: String, 
        amount: Double, 
        currency: String,
        daysBefore: Int, 
        appLang: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bill_due_reminders"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Bill Due Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming and overdue bills."
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            billId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val formattedAmount = String.format("%,.2f", amount).replace(".00", "")
        
        val titleText: String
        val contentText: String
        
        if (appLang == "fa") {
            titleText = "یادآور سررسید قبض"
            contentText = when (daysBefore) {
                0 -> "قبض \"$name\" امروز سررسید می‌شود! مبلغ: $currency$formattedAmount"
                1 -> "قبض \"$name\" فردا سررسید می‌شود! مبلغ: $currency$formattedAmount"
                else -> "قبض \"$name\" در $daysBefore روز آینده سررسید می‌شود! مبلغ: $currency$formattedAmount"
            }
        } else {
            titleText = "Upcoming Bill Reminder"
            contentText = when (daysBefore) {
                0 -> "Your bill \"$name\" is due TODAY! Amount: $currency$formattedAmount"
                1 -> "Your bill \"$name\" is due TOMORROW! Amount: $currency$formattedAmount"
                else -> "Your bill \"$name\" is due in $daysBefore days! Amount: $currency$formattedAmount"
            }
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(titleText)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(billId, notification)
    }
}
