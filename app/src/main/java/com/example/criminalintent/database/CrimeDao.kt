package com.example.criminalintent.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.criminalintent.Crime
import kotlinx.coroutines.flow.Flow
import java.util.*

/*
When you hook CrimeDao up to your database class, Room will generate
implementations of the functions you add to this interface.
*/

@Dao
interface CrimeDao {
    @Query("SELECT * FROM crime")
    fun getCrimes(): Flow<List<Crime>>

    @Query("SELECT * FROM crime WHERE id=(:id)")
    suspend fun getCrime(id: UUID): Crime

    @Update
    suspend fun updateCrime(crime: Crime)

    @Insert
    suspend fun addCrime(crime: Crime)

    @Delete
    suspend fun removeCrime(crime: Crime)
}