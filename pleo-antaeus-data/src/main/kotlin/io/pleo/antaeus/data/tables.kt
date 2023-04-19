/*
    Defines database tables and their schemas.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object InvoiceTable : IntIdTable() {
    val currency = varchar("currency", 3)
    val value = decimal("value", 1000, 2)
    val customerId = reference("customer_id", CustomerTable.id)
    val status = text("status")
    val dueDate = date("due_date")
}

object CustomerTable : IntIdTable() {
    val currency = varchar("currency", 3)
}

object TransactionHistoryTable : IntIdTable() {
    val invoiceId = reference("customer_id", InvoiceTable.id)
    val status = text("status")
    val reason = text("reason")
    val timestamp = timestamp("due_date")
}
