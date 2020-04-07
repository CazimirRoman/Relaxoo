package com.cazimir.relaxoo

import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    val device = UiDevice.getInstance(getInstrumentation())

    @Before
    fun registerIdlingResource() {
        Log.d(TAG, "registerIdlingResource: called")
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        device.setOrientationNatural()
    }

    @After
    fun unregisterIdlingResource() {
        Log.d(TAG, "unregisterIdlingResource: called")
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

//    @Test
//    fun show_splash_screen() {
//        ActivityScenario.launch(MainActivity::class.java)
//        //not needed for splash
//        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
//        onView(withId(R.id.splash)).check(matches(isDisplayed()))
//    }

    @Test
    fun shouldShowViewPagerAfterLoadingSoundsToSoundPoolComplete() {
        ActivityScenario.launch(MainActivity::class.java)
        checkVisibilityOfView(R.id.pager, Visibility.VISIBLE)
    }

    @Test
    fun rotating_favorites_fragment() {
        ActivityScenario.launch(MainActivity::class.java)
        swipeViewPagerLeft(1)
        checkVisibilityOfView(R.id.favorites_fragment, Visibility.VISIBLE)
        device.setOrientationLeft()
        checkVisibilityOfView(R.id.favorites_fragment, Visibility.VISIBLE)
    }

    @Test
    fun shouldInstantiateAllFragments() {
        ActivityScenario.launch(MainActivity::class.java)
        checkVisibilityOfView(R.id.sound_list_fragment, Visibility.VISIBLE)
        checkVisibilityOfView(R.id.favorites_fragment, Visibility.VISIBLE)
        swipeViewPagerLeft(3)
        checkVisibilityOfView(R.id.create_sound_fragment, Visibility.VISIBLE)
        checkVisibilityOfView(R.id.about_fragment, Visibility.VISIBLE)
    }

    @Test
    fun rotating_sound_grid_fragment_maintains_state() {
        ActivityScenario.launch(MainActivity::class.java)
        checkVisibilityOfView(R.id.sound_list_fragment, Visibility.VISIBLE)
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).perform(click())
        device.setOrientationLeft()
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))
        onView(withId(R.id.play_button)).perform(click())
    }

    @Test
    fun rotating_create_sound_fragment() {
        ActivityScenario.launch(MainActivity::class.java)
        swipeViewPagerLeft(2)
        checkVisibilityOfView(R.id.create_sound_fragment, Visibility.VISIBLE)
        device.setOrientationLeft()
        checkVisibilityOfView(R.id.create_sound_fragment, Visibility.VISIBLE)
    }

    @Test
    fun rotating_about_fragment() {
        ActivityScenario.launch(MainActivity::class.java)
        swipeViewPagerLeft(3)
        checkVisibilityOfView(R.id.about_fragment, Visibility.VISIBLE)
        device.setOrientationLeft()
        checkVisibilityOfView(R.id.about_fragment, Visibility.VISIBLE)
    }

    @Test
    fun show_volume_slider_when_clicking_on_sound() {
        ActivityScenario.launch(MainActivity::class.java)
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).perform(click())
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))
        onView(withId(R.id.play_button)).perform(click())
    }

    @Test
    fun stop_all_sounds() {
        ActivityScenario.launch(MainActivity::class.java)
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).perform(click())
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).perform(click())
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))

        onView(withId(R.id.play_button)).perform(click())
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).onChildView(withId(R.id.sound_volume)).check(matches(not(isDisplayed())))
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(not(isDisplayed())))

    }

    private fun checkVisibilityOfView(view: Int, visibility: Visibility) {
        onView(withId(view)).check(matches(withEffectiveVisibility(visibility)))
    }

    private fun swipeViewPagerLeft(numberOfSwipes: Int) {
        for (i in 0 until numberOfSwipes) {
            onView(withId(R.id.pager)).perform(ViewActions.swipeLeft())
        }
    }

    companion object {
        private const val TAG = "MainActivityTest"
    }
}