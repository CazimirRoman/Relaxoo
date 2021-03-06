package com.cazimir.relaxoo.muting

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.EspressoIdlingResource
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.util.TestUtil.Companion.checkIfVolumeSliderIsDisplayed
import com.cazimir.relaxoo.util.TestUtil.Companion.checkVisibilityOfView
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnMuteButton
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnPlayStopButton
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnSounds
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class MutingTest {

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Rule
    @JvmField
    val grantPermissionRule2: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE)


    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    companion object {
        private const val TAG = "MutingTest"
    }

    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun setup() {
//        clearSharedPreferences()
//        startActivity()
        Log.d(TAG, "setup: called")
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
    fun mute_all_sounds() {
        val numberOfSoundsClicked = 2
        // cannot actually test this UI behaviour as there is no other UI indication that the sound is playing
        clickOnSounds(numberOfSoundsClicked)
        checkIfVolumeSliderIsDisplayed(numberOfSoundsClicked)
        clickOnMuteButton()
        // TODO: 08-Apr-20 Unit test for this behaviour
        onView(withId(R.id.mute_button)).check(matches(withTagValue(equalTo("mute_on"))))
        checkVisibilityOfView(R.id.sound_list_fragment, ViewMatchers.Visibility.VISIBLE)
        clickOnMuteButton()
    }
}