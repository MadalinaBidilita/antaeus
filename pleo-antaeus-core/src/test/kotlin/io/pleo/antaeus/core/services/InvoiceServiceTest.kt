package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.TransactionStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvoiceServiceTest {
    private val dal = mockk<AntaeusDal>(relaxed = true) {
        every { fetchInvoice(404) } returns null
    }

    private val invoiceService = InvoiceService(dal = dal)

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }
    @Test
    fun `will process invoice successfully`() {
        invoiceService.processSuccessfully(1)
        verify(exactly = 1) {
            dal.updateInvoiceStatus(eq(1), eq(InvoiceStatus.PAID))
            dal.saveTransaction(eq(1), eq(TransactionStatus.SUCCESSFUL))
        }
    }

    @Test
    fun `should not mark invoice as overdue after first failed attempt `() {
        every { dal.countTransactionsByIdAndStatus(eq(1), TransactionStatus.LOW_BALANCE) } returns 0
        invoiceService.processLowBalance(1)
        verify(exactly = 0) {
            dal.updateInvoiceStatus(eq(1), eq(InvoiceStatus.OVERDUE))
        }
        verify(exactly = 1) {
            dal.saveTransaction(eq(1), eq(TransactionStatus.LOW_BALANCE))
        }
    }

    @Test
    fun `should mark invoice as overdue after 3 failed attempts because of insufficient balance`() {
        every { dal.countTransactionsByIdAndStatus(eq(1), TransactionStatus.LOW_BALANCE) } returns 3
        invoiceService.processLowBalance(1)
        verify(exactly = 1) {
            dal.updateInvoiceStatus(eq(1), eq(InvoiceStatus.OVERDUE))
            dal.saveTransaction(eq(1), eq(TransactionStatus.LOW_BALANCE))
        }
    }

    @Test
    fun `should not update the invoice status in case of errors`() {
        invoiceService.processFailed(1, "")
        verify(exactly = 0) {
            dal.updateInvoiceStatus(eq(1), any())
        }
        verify(exactly = 1) {
            dal.saveTransaction(eq(1), eq(TransactionStatus.ERROR))
        }
    }
}
