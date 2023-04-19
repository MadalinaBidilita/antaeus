/*
    Defines mappings between database rows and Kotlin objects.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.*
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toInvoice(): Invoice = Invoice(
    id = this[InvoiceTable.id].value,
    amount = Money(
        value = this[InvoiceTable.value],
        currency = Currency.valueOf(this[InvoiceTable.currency])
    ),
    status = InvoiceStatus.valueOf(this[InvoiceTable.status]),
    customerId = this[InvoiceTable.customerId].value,
    dueDate = this[InvoiceTable.dueDate]
)

fun ResultRow.toCustomer(): Customer = Customer(
    id = this[CustomerTable.id].value,
    currency = Currency.valueOf(this[CustomerTable.currency])
)

fun ResultRow.toTransaction(): Transaction = Transaction(
    id = this[TransactionHistoryTable.id].value,
    invoiceId = this[TransactionHistoryTable.invoiceId].value,
    status = TransactionStatus.valueOf(this[TransactionHistoryTable.status]),
    reason = this[TransactionHistoryTable.reason],
    timestamp = this[TransactionHistoryTable.timestamp]
)
