/*
    Implements the data access layer (DAL).
    The data access layer generates and executes requests to the database.

    See the `mappings` module for the conversions between database rows and Kotlin objects.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate

class AntaeusDal(private val db: Database) {
    fun fetchInvoice(id: Int): Invoice? {
        // transaction(db) runs the internal query as a new database transaction.
        return transaction(db) {
            // Returns the first invoice with matching id.
            InvoiceTable
                .select { InvoiceTable.id.eq(id) }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    fun fetchInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    fun fetchInvoicesByStatusAndDueDate(status: InvoiceStatus, dueDate: LocalDate): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .select {
                    InvoiceTable.status.eq(status.toString())
                        .and(InvoiceTable.dueDate.lessEq(dueDate))
                }
                .map { it.toInvoice() }
        }
    }

    fun createInvoice(
        amount: Money,
        customer: Customer,
        status: InvoiceStatus = InvoiceStatus.PENDING,
        dueDate: LocalDate
    ): Invoice? {
        val id = transaction(db) {
            // Insert the invoice and returns its new id.
            InvoiceTable
                .insert {
                    it[this.value] = amount.value
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                    it[this.dueDate] = dueDate
                } get InvoiceTable.id
        }

        return fetchInvoice(id.value)
    }

    fun updateInvoiceStatus(id: Int, status: InvoiceStatus): Invoice? {
        val id = transaction(db) {
            InvoiceTable.update({ InvoiceTable.id eq id }) {
                it[this.status] = status.toString()
            }
        }
        return fetchInvoice(id)
    }

    fun saveTransaction(invoiceId: Int, status: TransactionStatus, reason: String = "") {
        transaction(db) {
            TransactionHistoryTable.insert {
                it[this.invoiceId] = invoiceId
                it[this.status] = status.toString()
                it[this.timestamp] = Instant.now()
                it[this.reason] = reason
            }
        }
    }

    fun countTransactionsByIdAndStatus(invoiceId: Int, status: TransactionStatus): Long {
        return transaction(db) {
            TransactionHistoryTable
                .select {
                    TransactionHistoryTable.invoiceId.eq(invoiceId)
                        .and(TransactionHistoryTable.status.eq(status.toString()))
                }
                .count()
        }
    }

    fun fetchCustomer(id: Int): Customer? {
        return transaction(db) {
            CustomerTable
                .select { CustomerTable.id.eq(id) }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return transaction(db) {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    fun createCustomer(currency: Currency): Customer? {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.currency] = currency.toString()
            } get CustomerTable.id
        }

        return fetchCustomer(id.value)
    }
}
