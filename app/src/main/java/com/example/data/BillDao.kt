package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY dueDateDay ASC")
    fun getAllBills(): Flow<List<Bill>>

    @Query("SELECT * FROM bills ORDER BY dueDateDay ASC")
    suspend fun getAllBillsList(): List<Bill>

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: Int): Bill?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Delete
    suspend fun deleteBill(bill: Bill)

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteBillById(id: Int)

    @Query("SELECT * FROM bill_payment_history WHERE year = :year AND month = :month")
    fun getPaymentsByMonth(year: Int, month: Int): Flow<List<BillPaymentHistory>>

    @Query("SELECT * FROM bill_payment_history WHERE billId = :billId")
    fun getPaymentsForBill(billId: Int): Flow<List<BillPaymentHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentHistory(history: BillPaymentHistory): Long

    @Query("DELETE FROM bill_payment_history WHERE billId = :billId AND year = :year AND month = :month")
    suspend fun deletePaymentHistory(billId: Int, year: Int, month: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bill_payment_history WHERE billId = :billId AND year = :year AND month = :month)")
    suspend fun isBillPaidInMonth(billId: Int, year: Int, month: Int): Boolean
}
