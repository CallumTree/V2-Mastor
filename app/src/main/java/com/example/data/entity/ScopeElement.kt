package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ScopeElement Entity.
 *
 * CRITICAL RULE: Cost is NEVER a stored column. Calculated as (qty * rate) on every read.
 */
@Entity(
    tableName = "scope_elements",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrder::class,
            parentColumns = ["wo_ref"],
            childColumns = ["wo_ref"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Valuation::class,
            parentColumns = ["id"],
            childColumns = ["current_valuation_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["wo_ref"]),
        Index(value = ["current_valuation_id"])
    ]
)
data class ScopeElement(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "wo_ref")
    val woRef: String, // Hard FK to WorkOrder.woRef
    @ColumnInfo(name = "location_room")
    val locationRoom: String,
    val code: String,
    val description: String,
    val qty: Double,
    val units: String,
    val rate: Double, // Base rate only
    val notes: String = "",
    val tick: Boolean = false,
    @ColumnInfo(name = "claim_percent")
    val claimPercent: Double = 0.0, // Numeric, default 0
    @ColumnInfo(name = "previously_certified_percent")
    val previouslyCertifiedPercent: Double = 0.0, // Frozen snapshot from last invoiced valuation
    @ColumnInfo(name = "current_valuation_id")
    val currentValuationId: String? = null // Nullable FK to Valuation
)
