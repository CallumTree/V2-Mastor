package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "procurement_packages")
data class ProcurementPackage(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "package_ref") val packageRef: String,
    @ColumnInfo(name = "project_id") val projectId: String,
    @ColumnInfo(name = "trade") val trade: String, // Scaffolding, Roofing, Painting, Fencing, Electrical, Plumbing, Joinery, Other
    @ColumnInfo(name = "scope_element_ids") val scopeElementIds: String, // Comma-separated ScopeElement IDs
    @ColumnInfo(name = "subcontractor_id") val subcontractorId: String? = null,
    @ColumnInfo(name = "status") val status: String = "Scope Sent", // Scope Sent, Quote Received, Awarded, In Progress, Complete, Declined
    @ColumnInfo(name = "review_status") val reviewStatus: String = "Not Reviewed", // Not Reviewed, Reviewed, Approved for Issue
    @ColumnInfo(name = "date_sent") val dateSent: String,
    @ColumnInfo(name = "quote_amount") val quoteAmount: Double? = null,
    @ColumnInfo(name = "quote_date") val quoteDate: String? = null,
    @ColumnInfo(name = "quote_notes") val quoteNotes: String? = null
)
