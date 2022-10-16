package com.example.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.criminalintent.Crime

/*
Adding @TypeConverters tells the database to use the functions
in that class when converting types
*/

@Database(entities = [Crime::class], version = 1) // class that represents the database
@TypeConverters(CrimeTypeConverters::class) // add Type Converters
abstract class CrimeDatabase : RoomDatabase() {
    abstract fun crimeDao(): CrimeDao
}