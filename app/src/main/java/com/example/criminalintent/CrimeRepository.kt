package com.example.criminalintent

import android.content.Context
import androidx.room.Room
import com.example.criminalintent.database.CrimeDatabase
import java.util.UUID

/*
Creating a singleton class with help of private constructor and companion object.
Companion object has the function to initialize the repository once
and the function to get this instance or throw an exception of the instance wasn't initialized.
*/

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) {

    // Setting up repository properties:
    private val database: CrimeDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            CrimeDatabase::class.java,
            DATABASE_NAME
        ).build()

    // Adding repository functions:
    suspend fun getCrimes(): List<Crime> =
        database.crimeDao().getCrimes()

    suspend fun getCrime(id: UUID): Crime =
        database.crimeDao().getCrime(id)

    // Creating singleton instance of CrimeRepository:
    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}