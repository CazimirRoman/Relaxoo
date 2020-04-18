package com.cazimir.relaxoo.util

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


class TestUtil {
    companion object {

        private const val TAG = "TestUtil"

        fun startActivity() {
            ActivityScenario.launch(MainActivity::class.java)
        }

        fun clearSharedPreferences() {
            Log.d(TAG, "clearSharedPreferences: called")
            val sharedPreferences = getInstrumentation().targetContext.getSharedPreferences("PREFERENCES_FILE_NAME", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.commit()
        }

        fun clickOnSounds(howMany: Int) {
            when (howMany) {
                1 -> onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).perform(click())
                2 -> {
                    onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).perform(click())
                    onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).perform(click())
                }
            }
        }

        fun checkIfVolumeSliderIsDisplayed(howMany: Int) {
            when (howMany) {
                1 -> onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0)
                        .onChildView(
                                withId(R.id.sound_volume)
                        ).check(matches(ViewMatchers.isDisplayed()))
                2 -> {
                    onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0)
                            .onChildView(
                                    withId(R.id.sound_volume)
                            ).check(matches(ViewMatchers.isDisplayed()))
                    onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2)
                            .onChildView(
                                    withId(R.id.sound_volume)
                            ).check(matches(ViewMatchers.isDisplayed()))
                }
            }
        }

        fun clickOnTimerButton() {
            onView(withId(R.id.set_timer_button)).perform(click())
        }

        fun clickOnPlayStopButton() {
            onView(withId(R.id.play_button)).perform(click())
        }

        fun clickOnRandomButton() {
            onView(withId(R.id.random_button)).perform(click())
        }

        fun clickOnSaveComboButton() {
            onView(withId(R.id.save_fav_button)).perform(click())
        }

        fun clickOnMuteButton() {
            onView(withId(R.id.mute_button)).perform(click())
        }

        fun checkVisibilityOfView(viewId: Int, visibility: ViewMatchers.Visibility) {
            onView(withId(viewId)).check(matches(withEffectiveVisibility(visibility)))
        }

        fun swipeViewPagerLeft(numberOfSwipes: Int) {
            for (i in 0 until numberOfSwipes) {
                onView(withId(R.id.pager)).perform(ViewActions.swipeLeft())
            }
        }


        fun swipeViewPagerRight(numberOfSwipes: Int) {
            for (i in 0 until numberOfSwipes) {
                onView(withId(R.id.pager)).perform(ViewActions.swipeRight())
            }
        }

        // used to click on a specific view child in recyclerview list
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

        fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher? {
            return RecyclerViewMatcher(recyclerViewId)
        }
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
