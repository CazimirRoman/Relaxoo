package com.cazimir.relaxoo

import android.util.Log
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.util.TestUtil.Companion.checkVisibilityOfView
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnPlayStopButton
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnSounds
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnTimerButton
import com.cazimir.relaxoo.util.TestUtil.Companion.swipeViewPagerLeft
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class RotationTest {

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Rule
    @JvmField
    val grantPermissionRule2: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    companion object {
        private const val TAG = "ViewRotationTest"
    }

    private val device = UiDevice.getInstance(getInstrumentation())

    @Before
    fun setup() {
//        startActivity()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        device.setOrientationNatural()
//        clearSharedPreferences()
    }

    @After
    fun teardown() {
        Log.d(TAG, "teardown: called")
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        clickOnPlayStopButton()
    }

    @Test
    fun rotate_favorites_fragment() {
        swipeViewPagerLeft(1)
        checkVisibilityOfView(R.id.favorites_fragment, Visibility.VISIBLE)
        device.setOrientationLeft()
        checkVisibilityOfView(R.id.favorites_fragment, Visibility.VISIBLE)
    }

    @Test
    fun rotate_sound_grid_fragment() {
        clickOnPlayStopButton()
        checkVisibilityOfView(R.id.sound_list_fragment, Visibility.VISIBLE)
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).perform(click())
        device.setOrientationLeft()
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))
        clickOnPlayStopButton()
    }

    @Test
    fun rotate_create_sound_fragment() {
        swipeViewPagerLeft(2)
        checkVisibilityOfView(R.id.create_sound_fragment, Visibility.VISIBLE)
        device.setOrientationLeft()
        checkVisibilityOfView(R.id.create_sound_fragment, Visibility.VISIBLE)
    }

    @Test
    fun rotate_about_fragment() {
        swipeViewPagerLeft(3)
        checkVisibilityOfView(R.id.about_fragment, Visibility.VISIBLE)
        device.setOrientationLeft()
        checkVisibilityOfView(R.id.about_fragment, Visibility.VISIBLE)
    }

    @Test
    fun rotate_timer() {
        clickOnSounds(1)
        clickOnTimerButton()
        onView(withId(R.id.timer_dialog)).check(matches(isDisplayed()))
        onData(allOf()).inAdapterView(withId(R.id.timer_list)).atPosition(0).perform(click())
        onView(withId(R.id.timerText)).check(matches(allOf(isDisplayed(), not(withText("Sounds will stop in 00:00:00")))))
        device.setOrientationLeft()
        onView(withId(R.id.timerText)).check(matches(allOf(isDisplayed(), not(withText("Sounds will stop in 00:00:00")))))
    }
}