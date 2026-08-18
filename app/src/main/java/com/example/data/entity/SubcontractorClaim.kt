package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subcontractor_claims")
data class SubcontractorClaim(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "package_id") val packageId: String,
    @ColumnInfo(name = "claim_number") val claimNumber: Int,
    @ColumnInfo(name = "claim_date") val claimDate: String,
    @ColumnInfo(name = "claim_amount") val claimAmount: Double, // Incremental claim amount for this payment app
    @ColumnInfo(name = "previously_claimed_amount") val previouslyClaimedAmount: Double = 0.0, // Double-counting protection
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "created_at_timestamp") val createdAtTimestamp: Long = System.currentTimeMillis()
)
