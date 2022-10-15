package com.example.criminalintent

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class CrimeListViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val crimes = mutableListOf<Crime>()

    init {
        for (i in 0 until 100) {
            val crime = Crime(
                id = UUID.randomUUID(),
                title = "Crime #$i",
                date = FormattedDate,
                isSolved = i % 2 == 0
            )

            crimes += crime
        }
    }
}

val FormattedDate = object : Date() {
    override fun toString(): String {
        return SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.US).format(Date())
    }
}