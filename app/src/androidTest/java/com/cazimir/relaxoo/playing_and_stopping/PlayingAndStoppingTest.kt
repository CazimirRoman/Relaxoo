package com.cazimir.relaxoo

import android.content.Context
import android.util.Log
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.util.TestUtil.Companion.checkIfVolumeSliderIsDisplayed
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnPlayStopButton
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnSounds
import com.cazimir.relaxoo.util.TestUtil.Companion.startActivity
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class PlayingAndStoppingTest {

    companion object {
        private const val TAG = "PlayingAndStoppingTest"
    }

    val device = UiDevice.getInstance(getInstrumentation())

    @Before
    fun setup() {
        Log.d(TAG, "setup: called")
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        device.setOrientationNatural()
        clearSharedPreferences()
    }

    private fun clearSharedPreferences() {
        Log.d(TAG, "clearSharedPreferences: called")
        val sharedPreferences = getInstrumentation().targetContext.getSharedPreferences("PREFERENCES_FILE_NAME", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.commit()
    }

    @After
    fun teardown() {
        Log.d(TAG, "teardown: called")
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun play_single_sound() {
        startActivity()
        clickOnPlayStopButton()
        clickOnSounds(1)
        checkIfVolumeSliderIsDisplayed(1)
        clickOnPlayStopButton()
    }

    @Test
    fun stop_all_sounds() {
        startActivity()
        clickOnPlayStopButton()
        clickOnSounds(2)
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))

        clickOnPlayStopButton()
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).onChildView(withId(R.id.sound_volume)).check(matches(not(isDisplayed())))
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(not(isDisplayed())))

    }

    @Test
    fun stop_single_sound() {
        TODO("Not yet implemented")
    }

    @Test
    fun volume_change_on_single_sound() {
        TODO("Not yet implemented")
    }
}