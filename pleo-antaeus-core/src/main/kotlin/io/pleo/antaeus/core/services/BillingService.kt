package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging
import java.time.LocalDate

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {

    private val logger = KotlinLogging.logger {}

    fun chargeAll(dueDate: LocalDate) {
        invoiceService.fetchPendingInvoices(dueDate)
            .forEach { processInvoice(it) }
    }

    // TODO Ideally async executed with retry policy on NetworkException
    private fun processInvoice(invoice: Invoice) {
        try {
            val successfullyCharged = paymentProvider.charge(invoice);
            if (successfullyCharged) {
                invoiceService.processSuccessfully(invoice.id)
            } else {
                invoiceService.processLowBalance(invoice.id)
            }
            // TODO notify user about the payment status
        } catch (ex: Exception) {
            logger.error { ex }
            when (ex) {
                is CustomerNotFoundException -> invoiceService.processFailed(invoice.id, "Customer not found")
                is CurrencyMismatchException -> invoiceService.processFailed(invoice.id, "Currency mismatch")
                else -> {
                    invoiceService.processFailed(invoice.id, ex.message.toString())
                    throw ex
                }
            }
        }
    }
}
