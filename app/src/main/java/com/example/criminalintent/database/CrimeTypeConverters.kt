package com.example.criminalintent.database

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
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

// Custom Date for Buttons date representing:
class FormattedDate(
    private val millis: Long = System.currentTimeMillis()
) : Date() {
    override fun toString(): String {
        return SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.US).format(Date(millis))
    }

    fun timeString():String{
        return SimpleDateFormat("h:mm a", Locale.US).format(Date(millis))
    }
}