package com.example.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.criminalintent.Crime

/*
Adding @TypeConverters tells the database to use the functions
in that class when converting types.

~Add exportSchema = false parameter if you want to disable the export
*/

@Database(entities = [Crime::class], version = 2) // class that represents the database
@TypeConverters(CrimeTypeConverters::class) // add Type Converters
abstract class CrimeDatabase : RoomDatabase() {
    abstract fun crimeDao(): CrimeDao
}

// Create new Migration object that shows that version 2 needs to have a new column:
val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''"
        )
    }
}