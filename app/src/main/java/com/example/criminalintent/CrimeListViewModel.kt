package com.example.criminalintent

import android.text.format.DateFormat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
        var formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val date = LocalDate.parse( formatter).toString()

        return date
    }
}