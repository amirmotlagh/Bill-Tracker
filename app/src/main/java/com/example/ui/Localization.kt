package com.example.ui

import java.util.Calendar

object Loc {
    private val en = mapOf(
        "app_title" to "Bill Tracker",
        "dashboard" to "Dashboard",
        "trends" to "Trends",
        "settings_language" to "App Language",
        "settings_calendar" to "Calendar Mode",
        "add_bill" to "Add Bill",
        "edit_bill" to "Edit Bill",
        "save" to "Save",
        "cancel" to "Cancel",
        "name" to "Name",
        "amount" to "Amount",
        "due_day" to "Due Day of Month",
        "category" to "Category",
        "payment_method" to "Payment Method",
        "payment_value" to "Account/Card Details",
        "notes" to "Notes (Optional)",
        "reminders_enabled" to "Enable Due Reminders",
        "reminders_desc" to "Get notified on this device when this bill is due",
        "settings" to "Settings",
        "notification_settings" to "Notification Settings",
        "enable_due_reminders" to "Enable Due Reminders",
        "enable_due_reminders_desc" to "Get notified on this device for upcoming bills",
        "days_left_payment" to "Days left to payment due:",
        "time_of_day" to "Time of day to notify:",
        "same_day" to "Same day (Due day)",
        "one_day_before" to "1 day before",
        "days_before" to "%d days before",
        "hour_label" to "Hour: %02d",
        "minute_label" to "Minute: %02d",
        "calendar_mode" to "Calendar Mode",
        "app_language" to "App Language",
        "gregorian" to "Gregorian Calendar",
        "jalali" to "Jalali (Hijri Shamsi)",
        "english" to "English",
        "persian" to "Persian (فارسی)",
        "total_bills" to "Total Bills",
        "paid_bills" to "Paid",
        "pending_bills" to "Pending",
        "constant_expenses" to "Constant Expenses",
        "upcoming_alerts" to "Upcoming & Overdue Alerts",
        "all_paid_cheer" to "All bills for this month are paid! 🎉",
        "overdue_by" to "Overdue by %d days",
        "due_today" to "Due Today",
        "due_in_days" to "Due in %d days",
        "delete_bill" to "Delete Bill",
        "delete_confirm" to "Are you sure you want to delete this bill?",
        "recurring" to "Recurring Monthly",
        "installments" to "Installment Plan",
        "total_installments" to "Total Installments",
        "remaining_installments" to "Remaining Installments",
        "mark_paid" to "Mark as Paid",
        "mark_unpaid" to "Mark as Unpaid",
        "view_details" to "View Details",
        "no_bills" to "No bills added yet. Tap the button below to add your first bill!",
        "currency_selector" to "Currency selector",
        "custom_currency" to "Custom Currency Symbol",
        "custom_currency_desc" to "Enter a custom currency symbol:",
        "search_bills" to "Search bills...",
        "filter_category" to "Category: All",
        "export_pdf" to "Export PDF Summary",
        "due_reminders" to "Due Reminders",
        "enabled" to "Enabled",
        "disabled" to "Disabled",
        "no_alerts" to "No upcoming alerts",
        "history_title" to "Payment History",
        "paid_on" to "Paid on",
        "unpaid" to "Unpaid",
        "partially_paid" to "Partially Paid",
        "loan_installments_status" to "Installments: %d / %d remaining",
        "add_payment_record" to "Record Payment",
        "select_month" to "Select Month",
        "select_year" to "Select Year",
        "prev_month" to "Prev Month",
        "next_month" to "Next Month",
        "stats_and_insights" to "Stats & Insights",
        "category_distribution" to "Category Distribution",
        "payment_methods_used" to "Payment Methods",
        "monthly_spending_trend" to "Monthly Spending Trend",
        "average_monthly_bill" to "Average Monthly Bill",
        "highest_bill_category" to "Highest Category",
        "total_amount_saved" to "Total Amount Saved",
        "bills_list" to "Bills List",
        "active_bills" to "Active Bills",
        "archived_bills" to "Archived Bills",
        "archive_bill" to "Archive Bill",
        "unarchive_bill" to "Unarchive Bill",
        "delete_this_and_future" to "Delete this and future instances",
        "delete_all" to "Delete all occurrences",
        "save_changes" to "Save Changes",
        "enter_amount" to "Enter amount",
        "enter_name" to "Enter name",
        "invalid_fields" to "Please fill all required fields correctly",
        "multi_currency" to "Bill Currency",
        "start_month" to "Start Month",
        "start_year" to "Start Year",
        "variable_amount_title" to "Credit / Variable Amount",
        "variable_amount_desc" to "For bills with flexible amounts (e.g. app credits, credit card spends). Specify an estimated amount or leave as 0.",
        "estimated_amount" to "Estimated Amount",
        "variable_label" to "Variable Amount",
        "variable_est" to "Est. %s%s",
        "enter_paid_amount" to "Enter Paid Amount",
        "record_payment" to "Record Payment",
        "payment_prompt_variable" to "This bill is a variable credit/expense. Please enter the exact amount paid for this period:"
    )

    private val fa = mapOf(
        "app_title" to "دفترچه قبوض",
        "dashboard" to "پیشخوان",
        "trends" to "روندها",
        "settings_language" to "زبان برنامه",
        "settings_calendar" to "نوع تقویم",
        "add_bill" to "افزودن قبض",
        "edit_bill" to "ویرایش قبض",
        "save" to "ذخیره",
        "cancel" to "انصراف",
        "name" to "نام قبض",
        "amount" to "مبلغ",
        "due_day" to "روز سررسید در ماه",
        "category" to "دسته‌بندی",
        "payment_method" to "روش پرداخت",
        "payment_value" to "جزئیات حساب/کارت",
        "notes" to "یادداشت‌ها (اختیاری)",
        "reminders_enabled" to "فعالسازی یادآور سررسید",
        "reminders_desc" to "دریافت نوتیفیکیشن در این دستگاه هنگام سررسید",
        "settings" to "تنظیمات",
        "notification_settings" to "تنظیمات یادآورها",
        "enable_due_reminders" to "فعالسازی یادآورهای سررسید",
        "enable_due_reminders_desc" to "دریافت نوتیفیکیشن برای قبوض در پیش رو",
        "days_left_payment" to "تعداد روز مانده به سررسید:",
        "time_of_day" to "زمان نوتیفیکیشن در روز:",
        "same_day" to "همان روز (روز سررسید)",
        "one_day_before" to "۱ روز قبل",
        "days_before" to "%d روز قبل",
        "hour_label" to "ساعت: %02d",
        "minute_label" to "دقیقه: %02d",
        "calendar_mode" to "نوع تقویم",
        "app_language" to "زبان برنامه",
        "gregorian" to "تقویم میلادی",
        "jalali" to "تقویم جلالی (خورشیدی)",
        "english" to "English",
        "persian" to "فارسی",
        "total_bills" to "کل قبوض",
        "paid_bills" to "پرداخت شده",
        "pending_bills" to "معوقه",
        "constant_expenses" to "هزینه‌های ثابت",
        "upcoming_alerts" to "هشدارهای سررسید و معوقه",
        "all_paid_cheer" to "همه قبوض این ماه پرداخت شده‌اند! 🎉",
        "overdue_by" to "%d روز به تاخیر افتاده",
        "due_today" to "سررسید امروز",
        "due_in_days" to "%d روز مانده به سررسید",
        "delete_bill" to "حذف قبض",
        "delete_confirm" to "آیا از حذف این قبض اطمینان دارید؟",
        "recurring" to "تکرارشونده ماهانه",
        "installments" to "طرح اقساطی",
        "total_installments" to "کل اقساط",
        "remaining_installments" to "اقساط باقی‌مانده",
        "mark_paid" to "علامت‌گذاری به عنوان پرداخت شده",
        "mark_unpaid" to "علامت‌گذاری به عنوان پرداخت نشده",
        "view_details" to "مشاهده جزئیات",
        "no_bills" to "هنوز قبضی اضافه نشده است. برای ثبت اولین قبض روی دکمه زیر ضربه بزنید!",
        "currency_selector" to "انتخاب واحد پول",
        "custom_currency" to "نماد واحد پول سفارشی",
        "custom_currency_desc" to "نماد سفارشی را وارد کنید:",
        "search_bills" to "جستجوی قبوض...",
        "filter_category" to "دسته‌بندی: همه",
        "export_pdf" to "خروجی PDF خلاصه",
        "due_reminders" to "یادآور سررسید",
        "enabled" to "فعال",
        "disabled" to "غیرفعال",
        "no_alerts" to "بدون هشدار در پیش رو",
        "history_title" to "تاریخچه پرداخت‌ها",
        "paid_on" to "پرداخت شده در",
        "unpaid" to "پرداخت نشده",
        "partially_paid" to "پرداخت جزئی",
        "loan_installments_status" to "اقساط: %d از %d باقی‌مانده",
        "add_payment_record" to "ثبت پرداخت",
        "select_month" to "انتخاب ماه",
        "select_year" to "انتخاب سال",
        "prev_month" to "ماه قبل",
        "next_month" to "ماه بعد",
        "stats_and_insights" to "آمار و اطلاعات",
        "category_distribution" to "توزیع دسته‌بندی‌ها",
        "payment_methods_used" to "روش‌های پرداخت",
        "monthly_spending_trend" to "روند هزینه‌های ماهانه",
        "average_monthly_bill" to "میانگین قبوض ماهانه",
        "highest_bill_category" to "بیشترین دسته‌بندی",
        "total_amount_saved" to "کل مبلغ ذخیره شده",
        "bills_list" to "لیست قبوض",
        "active_bills" to "قبوض فعال",
        "archived_bills" to "قبوض بایگانی شده",
        "archive_bill" to "بایگانی کردن",
        "unarchive_bill" to "خروج از بایگانی",
        "delete_this_and_future" to "حذف این مورد و موارد آینده",
        "delete_all" to "حذف تمامی موارد",
        "save_changes" to "ذخیره تغییرات",
        "enter_amount" to "مبلغ را وارد کنید",
        "enter_name" to "نام را وارد کنید",
        "invalid_fields" to "لطفاً تمامی فیلدهای الزامی را به درستی پر کنید",
        "multi_currency" to "واحد پول قبض",
        "start_month" to "ماه شروع",
        "start_year" to "سال شروع",
        "variable_amount_title" to "اعتباری / مبلغ متغیر",
        "variable_amount_desc" to "برای قبوضی که مبلغ آن‌ها متغیر است (مانند اعتبار اپلیکیشن، مصرف کارت اعتباری). می‌توانید مبلغ تخمینی وارد کنید یا مقدار را ۰ بگذارید.",
        "estimated_amount" to "مبلغ تخمینی",
        "variable_label" to "مبلغ متغیر",
        "variable_est" to "تخمینی %s%s",
        "enter_paid_amount" to "ورود مبلغ پرداخت شده",
        "record_payment" to "ثبت پرداخت",
        "payment_prompt_variable" to "این مورد دارای مبلغ متغیر/اعتباری است. لطفا مبلغ دقیق پرداخت شده برای این دوره را وارد کنید:"
    )

    fun t(key: String, lang: String): String {
        val map = if (lang == "fa") fa else en
        return map[key] ?: key
    }

    val faMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val enMonths = listOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
    )

    val gregMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun formatCurrencyMap(sums: Map<String, Double>): String {
        if (sums.isEmpty()) return "0"
        return sums.entries.joinToString(", ") { (curr, amount) ->
            val formattedAmount = String.format("%,.2f", amount).replace(".00", "")
            "$curr$formattedAmount"
        }
    }
}

object JalaliHelper {
    data class JalaliDate(val year: Int, val month: Int, val day: Int)

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(0, 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gDayNo = 0
        for (i in 1 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        gDayNo += gd

        // Adjust for Gregorian leap year
        if (gm > 2 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }

        val gy2 = gy - 1600
        gDayNo += gy2 * 365 + gy2 / 4 - gy2 / 100 + gy2 / 400

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        for (i in 1..12) {
            val days = if (i == 12 && isJalaliLeap(jy)) 30 else jDaysInMonth[i]
            if (jDayNo < days) {
                jm = i
                jd = jDayNo + 1
                break
            }
            jDayNo -= days
        }

        return JalaliDate(jy, jm, jd)
    }

    private fun isJalaliLeap(jy: Int): Boolean {
        val r = jy % 33
        return r == 1 || r == 5 || r == 9 || r == 13 || r == 17 || r == 22 || r == 26 || r == 30
    }
}
