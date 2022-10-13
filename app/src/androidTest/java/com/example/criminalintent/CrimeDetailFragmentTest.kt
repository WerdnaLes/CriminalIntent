package com.example.criminalintent

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CrimeDetailFragmentTest {

    private lateinit var scenario: FragmentScenario<CrimeDetailFragment>

    @Before
    fun setUp() {
        scenario = launchFragmentInContainer()
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun isEditTextHooked() {
        val testText = "Stole my apple"
        onView(withId(R.id.crime_title)).perform(typeText(testText))
        scenario.onFragment {
            assertEquals(testText, it.crime.title)
        }
    }

    @Test
    fun isCheckBoxHooked() {
        onView(withId(R.id.crime_solved)).perform(click())
        scenario.onFragment {
            assertTrue(it.crime.isSolved)
        }
    }
}