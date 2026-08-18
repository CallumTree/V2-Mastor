package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subcontractor_quotes")
data class SubcontractorQuote(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "package_id") val packageId: String,
    @ColumnInfo(name = "subcontractor_id") val subcontractorId: String,
    @ColumnInfo(name = "quote_amount") val quoteAmount: Double,
    @ColumnInfo(name = "quote_date") val quoteDate: String,
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "is_lump_sum") val isLumpSum: Boolean = true
)
