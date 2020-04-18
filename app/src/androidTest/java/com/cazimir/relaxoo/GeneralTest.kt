package com.cazimir.relaxoo

import android.util.Log
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.util.TestUtil.Companion.checkVisibilityOfView
import com.cazimir.relaxoo.util.TestUtil.Companion.clearSharedPreferences
import com.cazimir.relaxoo.util.TestUtil.Companion.startActivity
import com.cazimir.relaxoo.util.TestUtil.Companion.swipeViewPagerLeft
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class GeneralTest {

    companion object {
        private const val TAG = "GeneralTest"
    }

    val device = UiDevice.getInstance(getInstrumentation())

    @Before
    fun setup() {
        Log.d(TAG, "setup: called")
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        device.setOrientationNatural()
        clearSharedPreferences()
    }

    @After
    fun teardown() {
        Log.d(TAG, "teardown: called")
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

//    @Test
//    fun show_splash_screen() {
//        startActivity()
//        //not needed for splash
//        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
//        onView(withId(R.id.splash)).check(matches(isDisplayed()))
//    }

    @Test
    fun show_pager_with_starting_fragment() {
        startActivity()
        checkVisibilityOfView(R.id.pager, Visibility.VISIBLE)
    }

    @Test
    fun shouldInstantiateAllFragments() {
        startActivity()
        checkVisibilityOfView(R.id.sound_list_fragment, Visibility.VISIBLE)
        checkVisibilityOfView(R.id.favorites_fragment, Visibility.VISIBLE)
        swipeViewPagerLeft(3)
        checkVisibilityOfView(R.id.create_sound_fragment, Visibility.VISIBLE)
        checkVisibilityOfView(R.id.about_fragment, Visibility.VISIBLE)
    }
}