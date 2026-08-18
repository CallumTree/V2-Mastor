package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_records")
data class InvoiceRecord(
    @PrimaryKey val id: String,
    val invoiceNumber: String,
    val valuationId: String,
    val valuationNumber: String,
    val dateIssued: String,
    val dueDate: String,
    val clientName: String,
    val netAmount: Double,
    val vatAmount: Double,
    val grossAmount: Double,
    val status: String
)
