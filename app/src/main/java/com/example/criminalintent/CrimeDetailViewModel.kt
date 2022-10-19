package com.example.criminalintent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CrimeDetailViewModel(crimeId: UUID) : ViewModel() {

    private val crimeRepository = CrimeRepository.get()

    private val _crime: MutableStateFlow<Crime?> =
        MutableStateFlow(null)
    val crime: StateFlow<Crime?> = _crime.asStateFlow()

    init {
        viewModelScope.launch {
            _crime.value =
                crimeRepository.getCrime(crimeId)
        }
    }

    // Update this flow with the new Crime changed by the user input
    fun updateCrime(onUpdate: (Crime) -> Crime) {
        _crime.update { oldCrime ->
            oldCrime?.let { onUpdate(it) }
        }
    }

    fun removeCrime(crime: Crime) {
        viewModelScope.launch {
            crimeRepository.removeCrime(crime)
        }
    }

    // Update database with the new user input when pressing "BACK" from the DetailFragment
    override fun onCleared() {
        super.onCleared()

        crime.value?.let {
            crimeRepository.updateCrime(it)
        }
    }
}

// Creating Factory to transfer the ID to the ViewModel via viewModels() lambda
class CrimeDetailViewModelFactory(
    private val crimeId: UUID
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CrimeDetailViewModel(crimeId) as T
    }
}