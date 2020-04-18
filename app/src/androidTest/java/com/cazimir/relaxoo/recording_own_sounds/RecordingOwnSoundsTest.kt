package com.cazimir.relaxoo.recording_own_sounds

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.EspressoIdlingResource
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.adapter.RecordingAdapter
import com.cazimir.relaxoo.util.TestUtil.Companion.clearSharedPreferences
import com.cazimir.relaxoo.util.TestUtil.Companion.clickChildViewWithId
import com.cazimir.relaxoo.util.TestUtil.Companion.startActivity
import com.cazimir.relaxoo.util.TestUtil.Companion.swipeViewPagerLeft
import com.cazimir.relaxoo.util.TestUtil.Companion.withRecyclerView
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class RecordingOwnSoundsTest {

    companion object {
        private const val TAG = "RecordingOwnSoundsTest"
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

    @Test
    fun pin_to_dashboard() {
        startActivity()
        swipeViewPagerLeft(2)
        onView(withId(R.id.recording_list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecordingAdapter.ViewHolder>(0, clickChildViewWithId(R.id.options_recording)))
        onView(withId(R.id.bottom_recording)).check(matches(isDisplayed()))
        onView(withId(R.id.pin_to_dashboard)).perform(click())
        onView(withId(R.id.sound_list_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun edit() {
        val NEWNAME = "new name for recording"
        startActivity()
        swipeViewPagerLeft(2)
        onView(withId(R.id.recording_list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecordingAdapter.ViewHolder>(0, clickChildViewWithId(R.id.options_recording)))
        onView(withId(R.id.bottom_recording)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recording_name)).perform(click())
        onView(withId(R.id.edit_recording_dialog)).check(matches(isDisplayed()))
        onView(withId(R.id.new_recording_name)).perform(replaceText(NEWNAME))
        onView(withText("OK")).perform(click())
        onView(withRecyclerView(R.id.recording_list)!!.atPosition(0))
                .check(matches(hasDescendant(withText(NEWNAME))))
    }

    @Test
    fun empty_edit_should_show_error() {
        startActivity()
        swipeViewPagerLeft(2)
        onView(withId(R.id.recording_list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecordingAdapter.ViewHolder>(0, clickChildViewWithId(R.id.options_recording)))
        onView(withId(R.id.bottom_recording)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recording_name)).perform(click())
        onView(withId(R.id.edit_recording_dialog)).check(matches(isDisplayed()))
        onView(withId(R.id.new_recording_name)).perform(replaceText(""))
        onView(withId(R.id.new_recording_name)).check(matches(hasErrorText("Please enter a new name")))
    }

    @Test
    fun delete() {
        startActivity()
        swipeViewPagerLeft(2)
        onView(withId(R.id.recording_list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecordingAdapter.ViewHolder>(0, clickChildViewWithId(R.id.options_recording)))
        onView(withId(R.id.bottom_recording)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_recording)).perform(click())
        onView(withId(R.id.delete_confirmation_dialog)).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())
        onView(withId(R.id.no_recordings_text)).check(matches(isDisplayed()))
    }

    @Test
    fun external_recording_library() {
        TODO("Not yet implemented")
    }
}