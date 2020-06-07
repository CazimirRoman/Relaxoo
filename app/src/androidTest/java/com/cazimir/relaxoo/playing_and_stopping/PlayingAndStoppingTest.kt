package com.cazimir.relaxoo

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.util.TestUtil
import com.cazimir.relaxoo.util.TestUtil.Companion.checkIfVolumeSliderIsDisplayed
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnPlayStopButton
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnSounds
import org.hamcrest.CoreMatchers
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

    // this is used to start the activity (no need to call startActivity)
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

        onView(withId(R.id.sounds_recycler_view))
                .check(matches(TestUtil.withViewAtPosition(0, hasDescendant(allOf(withId(R.id.sound_volume), isDisplayed())))))
        onView(withId(R.id.sounds_recycler_view))
                .check(matches(TestUtil.withViewAtPosition(1, hasDescendant(allOf(withId(R.id.sound_volume), isDisplayed())))));

        clickOnPlayStopButton()
        onView(withId(R.id.sounds_recycler_view))
                .check(matches(TestUtil.withViewAtPosition(0, hasDescendant(allOf(withId(R.id.sound_volume), not(isDisplayed()))))))
        onView(withId(R.id.sounds_recycler_view))
                .check(matches(TestUtil.withViewAtPosition(1, hasDescendant(allOf(withId(R.id.sound_volume), not(isDisplayed()))))));
    }

    @Test
    fun stop_single_sound() {
        clickOnSounds(1)
        onView(withId(R.id.sounds_recycler_view))
                .check(matches(TestUtil.withViewAtPosition(0, hasDescendant(allOf(withId(R.id.sound_volume), isDisplayed())))))
        //check if global play/stop icon changed
        onView(withId(R.id.play_button)).check(matches(withTagValue(CoreMatchers.equalTo("stop_button"))))

        clickOnSounds(1)
        onView(withId(R.id.sounds_recycler_view))
                .check(matches(TestUtil.withViewAtPosition(0, hasDescendant(allOf(withId(R.id.sound_volume), not(isDisplayed()))))))

        //check if global play/stop icon changed
        onView(withId(R.id.play_button)).check(matches(withTagValue(CoreMatchers.equalTo("play_button"))))


    }

    @Test
    fun volume_change_on_single_sound() {

    }
}