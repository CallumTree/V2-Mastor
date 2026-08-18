package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subcontractors")
data class Subcontractor(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "company_name") val companyName: String,
    @ColumnInfo(name = "contact_name") val contactName: String,
    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "trade_specialism") val tradeSpecialism: String, // Scaffolding, Roofing, Painting, Fencing, Electrical, Plumbing, Joinery, Other
    @ColumnInfo(name = "notes") val notes: String = ""
)
