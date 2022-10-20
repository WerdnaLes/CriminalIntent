package com.example.criminalintent.database

import androidx.room.TypeConverter
import java.util.*

/*
Room can store primitive types, enum classes and the UUID type, but can't store Date.
That's the reason of creating Type Converters
*/

//Create Type Converters to tell how Database should store specific Data
class CrimeTypeConverters {
    @TypeConverter // Store Date as Long
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter // Return Long as Date object
    fun toDate(millisSinceEpoch: Long): Date {
        return Date(millisSinceEpoch)
    }
}