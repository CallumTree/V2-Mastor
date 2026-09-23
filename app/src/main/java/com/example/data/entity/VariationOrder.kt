package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * VariationOrder Entity.
 *
 * CRITICAL RULE: Markup amounts are NEVER stored columns.
 */
@Entity(
    tableName = "variation_orders",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
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
        Index(value = ["project_id"]),
        Index(value = ["current_valuation_id"]),
        Index(value = ["vo_number"])
    ]
)
data class VariationOrder(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "project_id")
    val projectId: String,
    @ColumnInfo(name = "vo_number")
    val voNumber: String, // Shared across line/property block belonging to one ticket
    @ColumnInfo(name = "external_vo_number")
    val externalVoNumber: String,
    val status: String, // VO Identified / VO Received / VO Completed / VO Invoiced / VO Paid
    val property: String,
    @ColumnInfo(name = "location_room")
    val locationRoom: String,
    val code: String,
    val description: String,
    val qty: Double,
    val units: String,
    val rate: Double, // Base rate
    val notes: String = "",
    @ColumnInfo(name = "date_raised")
    val dateRaised: String,
    val tick: Boolean = false,
    @ColumnInfo(name = "claim_percent")
    val claimPercent: Double = 0.0,
    @ColumnInfo(name = "previously_certified_percent")
    val previouslyCertifiedPercent: Double = 0.0,
    @ColumnInfo(name = "current_valuation_id")
    val currentValuationId: String? = null,
    /** Photo evidence: JSON array of content URIs. Added in DB v10. */
    @ColumnInfo(name = "photo_uris", defaultValue = "'[]'")
    val photoUris: String = "[]"
) {
    fun photoList(): List<String> = try {
        val arr = org.json.JSONArray(photoUris)
        (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
    } catch (e: Exception) { emptyList() }
}
