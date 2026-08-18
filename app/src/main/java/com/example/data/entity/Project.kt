package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Project Entity: Master entity representing a UK construction project.
 *
 * NOTE: [uplift1Percent] and [uplift2Percent] are the ONLY two places markup percentages
 * are ever stored anywhere in the application database.
 */
@Entity(tableName = "projects")
data class Project(
    @PrimaryKey
    val id: String,
    val name: String,
    val client: String,
    val address: String,
    @ColumnInfo(name = "site_manager")
    val siteManager: String,
    val surveyor: String,
    val status: String = "Active", // e.g. Active, Planning, Completed
    @ColumnInfo(name = "contract_ref")
    val contractRef: String,
    @ColumnInfo(name = "work_type")
    val workType: String, // PPR or Internal Works
    @ColumnInfo(name = "start_date")
    val startDate: String,
    @ColumnInfo(name = "end_date")
    val endDate: String,
    @ColumnInfo(name = "project_number")
    val projectNumber: String,
    @ColumnInfo(name = "contract_value")
    val contractValue: Double,
    @ColumnInfo(name = "uplift_1_percent")
    val uplift1Percent: Double, // Central uplift 1 (e.g. 15.0%)
    @ColumnInfo(name = "uplift_2_percent")
    val uplift2Percent: Double,  // Central uplift 2 (e.g. 5.0%)
    @ColumnInfo(name = "image_url")
    val imageUrl: String = ""
)
