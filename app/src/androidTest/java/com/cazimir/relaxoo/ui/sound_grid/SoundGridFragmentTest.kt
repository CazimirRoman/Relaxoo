package com.cazimir.relaxoo.ui.sound_grid

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.cazimir.relaxoo.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SoundGridFragmentTest {

    @Test
    fun testGridViewIsDisplayed() {
        val scenario = launchFragmentInContainer<SoundGridFragment>()
        onView(withId(R.id.sounds_recycler_view)).check(matches(isDisplayed()))
    }
}