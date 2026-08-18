package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * WorkOrder Entity.
 *
 * CRITICAL RULE: Revenue, cost, and % complete are NEVER stored columns in this table.
 * They are always calculated live on read from the associated [ScopeElement] list.
 */
@Entity(
    tableName = "work_orders",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["project_id"]),
        Index(value = ["wo_ref"], unique = true)
    ]
)
data class WorkOrder(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "project_id")
    val projectId: String,
    @ColumnInfo(name = "wo_ref")
    val woRef: String, // Unique per project, format WO-001...
    val description: String, // Property address for PPR jobs, room/area for Internal Works
    @ColumnInfo(name = "work_type")
    val workType: String,
    val customer: String,
    val status: String,
    @ColumnInfo(name = "responsible_party")
    val responsibleParty: String,
    val notes: String = ""
)
