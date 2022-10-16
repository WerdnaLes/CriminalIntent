package com.example.criminalintent

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG2 = "CrimeListViewModel"

class CrimeListViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val crimes = mutableListOf<Crime>()

    init {
        Log.d(TAG2, "init starting")
        viewModelScope.launch {
            Log.d(TAG2, "coroutine launched")
            crimes += loadCrimes()
            Log.d(TAG2, "Loading crimes finished")
        }
    }

    suspend fun loadCrimes(): List<Crime> {
        val result = mutableListOf<Crime>()
        delay(5000)
        for (i in 0 until 100) {
            val crime = Crime(
                id = UUID.randomUUID(),
                title = "Crime #$i",
                date = FormattedDate,
                isSolved = i % 2 == 0
            )

            result += crime
        }
        return result
    }
}

val FormattedDate = object : Date() {
    override fun toString(): String {
        return SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.US).format(Date())
    }
}