package com.example.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema migrations.
 *
 * RULE: every change to an @Entity (new column, new table, rename) MUST bump the version in
 * MastorDatabase and add a Migration here. There is no destructive fallback any more — a
 * missing migration crashes on launch in testing instead of silently wiping a live job's
 * site diary, valuations and variations.
 *
 * New NOT NULL columns need a DEFAULT, and the entity's @ColumnInfo(defaultValue = ...) must
 * match it exactly or Room's schema check fails.
 */

/** v10: photo evidence on variation orders (JSON array of content URIs). */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE variation_orders ADD COLUMN photo_uris TEXT NOT NULL DEFAULT '[]'")
    }
}

val ALL_MIGRATIONS: Array<Migration> = arrayOf(
    MIGRATION_9_10
)
