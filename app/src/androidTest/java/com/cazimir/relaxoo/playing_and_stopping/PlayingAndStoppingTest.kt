package com.cazimir.relaxoo

import android.util.Log
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.util.TestUtil.Companion.checkIfVolumeSliderIsDisplayed
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnPlayStopButton
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnSounds
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class PlayingAndStoppingTest {

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Rule
    @JvmField
    val grantPermissionRule2: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    companion object {
        private const val TAG = "PlayingAndStoppingTest"
    }

    val device: UiDevice = UiDevice.getInstance(getInstrumentation())

    @Before
    fun setup() {
//        clearSharedPreferences()
//        startActivity()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        device.setOrientationNatural()
    }

    @After
    fun teardown() {
        Log.d(TAG, "teardown: called")
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        clickOnPlayStopButton()
    }

    @Test
    fun play_single_sound() {
        clickOnSounds(1)
        checkIfVolumeSliderIsDisplayed(1)
    }

    @Test
    fun stop_all_sounds() {
        clickOnSounds(2)
        onData(allOf()).inAdapterView(withId(R.id.sounds_recycler_view)).atPosition(0).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))
        onData(allOf()).inAdapterView(withId(R.id.sounds_recycler_view)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))

        clickOnPlayStopButton()
        onData(allOf()).inAdapterView(withId(R.id.sounds_recycler_view)).atPosition(0).onChildView(withId(R.id.sound_volume)).check(matches(not(isDisplayed())))
        onData(allOf()).inAdapterView(withId(R.id.sounds_recycler_view)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(not(isDisplayed())))

    }

//    @Test
//    fun stop_single_sound() {
//        TODO("Not yet implemented")
//    }
//
//    @Test
//    fun volume_change_on_single_sound() {
//        TODO("Not yet implemented")
//    }
}