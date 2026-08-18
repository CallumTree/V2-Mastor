package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Valuation Entity.
 *
 * CRITICAL RULE: Section totals, uplift amounts, and Grand Invoice Total are NEVER stored columns — always calculated live.
 */
@Entity(
    tableName = "valuations",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["project_id"])
    ]
)
data class Valuation(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "valuation_number")
    val valuationNumber: String,
    @ColumnInfo(name = "project_id")
    val projectId: String,
    val date: String,
    @ColumnInfo(name = "prepared_by")
    val preparedBy: String,
    val status: String // Draft / Invoiced / Locked
)
