package com.example.data

import kotlinx.coroutines.flow.Flow

class BillRepository(private val billDao: BillDao) {
    val allBills: Flow<List<Bill>> = billDao.getAllBills()

    suspend fun getBillById(id: Int): Bill? {
        return billDao.getBillById(id)
    }

    suspend fun saveBill(bill: Bill): Long {
        return billDao.insertBill(bill)
    }

    suspend fun deleteBillById(id: Int) {
        billDao.deleteBillById(id)
    }

    fun getPaymentsForMonth(year: Int, month: Int): Flow<List<BillPaymentHistory>> {
        return billDao.getPaymentsByMonth(year, month)
    }

    fun getPaymentsForBill(billId: Int): Flow<List<BillPaymentHistory>> {
        return billDao.getPaymentsForBill(billId)
    }

    suspend fun markBillAsPaid(billId: Int, year: Int, month: Int, amount: Double) {
        // 1. Record the payment history
        val history = BillPaymentHistory(
            billId = billId,
            year = year,
            month = month,
            paidAmount = amount
        )
        billDao.insertPaymentHistory(history)

        // 2. If it is a loan, decrement its remaining installments
        val bill = billDao.getBillById(billId)
        if (bill != null && bill.isLoan) {
            if (bill.remainingInstallments > 0) {
                val updatedBill = bill.copy(
                    remainingInstallments = bill.remainingInstallments - 1
                )
                billDao.insertBill(updatedBill)
            }
        }
    }

    suspend fun markBillAsUnpaid(billId: Int, year: Int, month: Int) {
        // 1. Delete the payment history record
        billDao.deletePaymentHistory(billId, year, month)

        // 2. If it is a loan, increment its remaining installments (up to totalInstallments)
        val bill = billDao.getBillById(billId)
        if (bill != null && bill.isLoan) {
            if (bill.remainingInstallments < bill.totalInstallments) {
                val updatedBill = bill.copy(
                    remainingInstallments = bill.remainingInstallments + 1
                )
                billDao.insertBill(updatedBill)
            }
        }
    }
}
