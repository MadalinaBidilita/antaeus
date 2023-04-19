/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.InvoiceStatus.OVERDUE
import io.pleo.antaeus.models.InvoiceStatus.PAID
import io.pleo.antaeus.models.TransactionStatus.*
import java.time.LocalDate

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetchPendingInvoices(dueDate: LocalDate): List<Invoice> {
        return dal.fetchInvoicesByStatusAndDueDate(InvoiceStatus.PENDING, dueDate)
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun processSuccessfully(id: Int) {
        //TODO Should be executed in the same transaction
        dal.updateInvoiceStatus(id, PAID)
        dal.saveTransaction(id, SUCCESSFUL)
    }

    fun processLowBalance(id: Int) {
        //TODO Should be executed in the same transaction
        dal.saveTransaction(id, LOW_BALANCE)
        val failedAttempts = dal.countTransactionsByIdAndStatus(id, LOW_BALANCE)
        if (failedAttempts >= 3) {
            dal.updateInvoiceStatus(id, OVERDUE)
        }
    }

    fun processFailed(id: Int, reason: String) {
        dal.saveTransaction(id, ERROR, reason)
    }
}
