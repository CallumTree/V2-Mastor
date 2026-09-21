package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.MastorDao
import com.example.data.entity.CachedCloudFile
import com.example.data.entity.LinkedDocument
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.SiteDiaryEntry
import com.example.data.entity.Valuation
import com.example.data.entity.VariationOrder
import com.example.data.entity.WorkOrder

@Database(
    entities = [
        Project::class,
        WorkOrder::class,
        ScopeElement::class,
        VariationOrder::class,
        Valuation::class,
        SiteDiaryEntry::class,
        LinkedDocument::class,
        CachedCloudFile::class,
        com.example.data.entity.Subcontractor::class,
        com.example.data.entity.ProcurementPackage::class,
        com.example.data.entity.SubcontractorQuote::class,
        com.example.data.entity.SubcontractorClaim::class
    ],
    version = 9,
    exportSchema = false
)
abstract class MastorDatabase : RoomDatabase() {

    abstract fun mastorDao(): MastorDao

    companion object {
        @Volatile
        private var INSTANCE: MastorDatabase? = null

        fun getDatabase(context: Context): MastorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MastorDatabase::class.java,
                    "mastor_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
