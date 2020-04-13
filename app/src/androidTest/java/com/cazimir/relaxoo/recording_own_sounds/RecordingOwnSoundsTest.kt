package com.cazimir.relaxoo.recording_own_sounds

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.EspressoIdlingResource
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.MainActivityTest
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.adapter.RecordingAdapter
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class RecordingOwnSoundsTest {

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
                RecyclerViewActions.actionOnItemAtPosition<RecordingAdapter.ViewHolder>(0, MainActivityTest.Companion.MyViewAction.clickChildViewWithId(R.id.options_recording)))
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
                RecyclerViewActions.actionOnItemAtPosition<RecordingAdapter.ViewHolder>(0, MainActivityTest.Companion.MyViewAction.clickChildViewWithId(R.id.options_recording)))
        onView(withId(R.id.bottom_recording)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recording_name)).perform(click())
        onView(withId(R.id.edit_recording_dialog)).check(matches(isDisplayed()))
        onView(withId(R.id.new_recording_name)).perform(replaceText(NEWNAME))
        onView(withText("OK")).perform(click())
        onView(MainActivityTest.withRecyclerView(R.id.recording_list)!!.atPosition(0))
                .check(matches(hasDescendant(withText(NEWNAME))))
    }

    @Test
    fun delete() {
        startActivity()
        swipeViewPagerLeft(2)
        onView(withId(R.id.recording_list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecordingAdapter.ViewHolder>(0, MainActivityTest.Companion.MyViewAction.clickChildViewWithId(R.id.options_recording)))
        onView(withId(R.id.bottom_recording)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_recording)).perform(click())
        onView(withId(R.id.delete_confirmation_dialog)).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())
        onView(withId(R.id.no_recordings_text)).check(matches(isDisplayed()))
    }

//region helper methods

    private fun clickOnPlayStopButton() {
        onView(withId(R.id.play_button)).perform(click())
    }

    private fun clickOnRandomButton() {
        onView(withId(R.id.random_button)).perform(click())
    }

    private fun clickOnSaveComboButton() {
        onView(withId(R.id.save_fav_button)).perform(click())
    }

    private fun clearSharedPreferences() {
        Log.d(TAG, "clearSharedPreferences: called")
        val sharedPreferences = getInstrumentation().targetContext.getSharedPreferences("PREFERENCES_FILE_NAME", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.commit()
    }

    private fun startActivity() {
        ActivityScenario.launch(MainActivity::class.java)
    }

    private fun clickOnSounds(howMany: Int) {
        when (howMany) {
            1 -> onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).perform(click())
            2 -> {
                onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).perform(click())
                onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).perform(click())
            }
        }


    }

    private fun clickOnTimerButton() {
        onView(withId(R.id.set_timer_button)).perform(click())
    }

    private fun checkVisibilityOfView(viewId: Int, visibility: Visibility) {
        onView(withId(viewId)).check(matches(withEffectiveVisibility(visibility)))
    }

    private fun swipeViewPagerLeft(numberOfSwipes: Int) {
        for (i in 0 until numberOfSwipes) {
            onView(withId(R.id.pager)).perform(ViewActions.swipeLeft())
        }
    }


    private fun swipeViewPagerRight(numberOfSwipes: Int) {
        for (i in 0 until numberOfSwipes) {
            onView(withId(R.id.pager)).perform(ViewActions.swipeRight())
        }
    }

    // endregion

    companion object {
        private const val TAG = "MainActivityTest"

        // used to click on a specific view child in recyclerview list
        object MyViewAction {
            fun clickChildViewWithId(id: Int): ViewAction {
                return object : ViewAction {
                    override fun getConstraints(): Matcher<View>? {
                        return null
                    }

                    override fun getDescription(): String {
                        return "Click on a child view with specified id."
                    }

                    override fun perform(uiController: UiController, view: View) {
                        val v = view.findViewById<View>(id)
                        v.performClick()
                    }
                }
            }
        }


        fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher? {
            return RecyclerViewMatcher(recyclerViewId)
        }

        class RecyclerViewMatcher(private val recyclerViewId: Int) {
            fun atPosition(position: Int): Matcher<View> {
                return atPositionOnView(position, -1)
            }

            fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {
                return object : TypeSafeMatcher<View>() {
                    var resources: Resources? = null
                    var childView: View? = null
                    override fun describeTo(description: Description) {
                        var idDescription = Integer.toString(recyclerViewId)
                        if (resources != null) {
                            idDescription = try {
                                resources!!.getResourceName(recyclerViewId)
                            } catch (var4: Resources.NotFoundException) {
                                String.format("%s (resource name not found)",
                                        *arrayOf<Any>(Integer.valueOf(recyclerViewId)))
                            }
                        }
                        description.appendText("with id: $idDescription")
                    }

                    override fun matchesSafely(view: View): Boolean {
                        resources = view.resources
                        if (childView == null) {
                            val recyclerView = view.rootView.findViewById<View>(recyclerViewId) as RecyclerView
                            childView = if (recyclerView.id == recyclerViewId) {
                                recyclerView.findViewHolderForAdapterPosition(position)!!.itemView
                            } else {
                                return false
                            }
                        }
                        return if (targetViewId == -1) {
                            view === childView
                        } else {
                            val targetView = childView!!.findViewById<View>(targetViewId)
                            view === targetView
                        }
                    }
                }
            }
        }


    }
}