package com.example.criminalintent

import android.app.Application

/*
The Application is not re-created on configuration changes. It is created
when the app launches and destroyed when the app process is destroyed.
*/

// Initializing CrimeRepository singleton:
class CriminalIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}