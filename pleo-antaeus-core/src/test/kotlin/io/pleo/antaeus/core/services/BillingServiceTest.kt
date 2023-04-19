package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency.EUR
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus.PENDING
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class BillingServiceTest {
    private val invoiceService = mockk<InvoiceService>(relaxed = true)
    private val paymentProvider = mockk<PaymentProvider>()
    private val billingService = BillingService(paymentProvider, invoiceService)
    private val dueDate = LocalDate.of(2023, 5, 1)

    @Test
    fun `should process invoice successfully`() {
        every { paymentProvider.charge(any()) } returns true
        every { invoiceService.fetchPendingInvoices(dueDate) } returns listOf(
            Invoice(1, 1, Money(BigDecimal(10), EUR), PENDING, dueDate),
        )
        billingService.chargeAll(dueDate)
        verify(exactly = 1) {
            invoiceService.processSuccessfully(eq(1))
        }
    }

    @Test
    fun `should process invoice when not enough balance`() {
        every { paymentProvider.charge(any()) } returns false
        every { invoiceService.fetchPendingInvoices(dueDate) } returns listOf(
            Invoice(1, 1, Money(BigDecimal(10), EUR), PENDING, dueDate),
        )
        billingService.chargeAll(dueDate)
        verify(exactly = 1) {
            invoiceService.processLowBalance(eq(1))
        }
    }

    @Test
    fun `should process invoice as failed when CustomerNotFoundException is thrown`() {
        every { paymentProvider.charge(any()) } throws CustomerNotFoundException(1)
        every { invoiceService.fetchPendingInvoices(dueDate) } returns listOf(
            Invoice(1, 1, Money(BigDecimal(10), EUR), PENDING, dueDate),
        )
        billingService.chargeAll(dueDate)
        verify(exactly = 1) {
            invoiceService.processFailed(eq(1), "Customer not found")
        }
    }

    @Test
    fun `should process invoice as failed when CurrencyMismatchException is thrown`() {
        every { paymentProvider.charge(any()) } throws CurrencyMismatchException(1, 1)
        every { invoiceService.fetchPendingInvoices(dueDate) } returns listOf(
            Invoice(1, 1, Money(BigDecimal(10), EUR), PENDING, dueDate),
        )
        billingService.chargeAll(dueDate)
        verify(exactly = 1) {
            invoiceService.processFailed(eq(1), "Currency mismatch")
        }
    }
}