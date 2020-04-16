package com.cazimir.relaxoo

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
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.adapter.SavedComboAdapter
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

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

//    @Test
//    fun show_splash_screen() {
//        ActivityScenario.launch(MainActivity::class.java)
//        //not needed for splash
//        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
//        onView(withId(R.id.splash)).check(matches(isDisplayed()))
//    }

    @Test
    fun shouldShowViewPagerAfterLoadingSoundsToSoundPoolComplete() {
        startActivity()
        checkVisibilityOfView(R.id.pager, Visibility.VISIBLE)
    }

    @Test
    fun rotating_favorites_fragment() {
        startActivity()
        swipeViewPagerLeft(1)
        checkVisibilityOfView(R.id.favorites_fragment, Visibility.VISIBLE)
        device.setOrientationLeft()
        checkVisibilityOfView(R.id.favorites_fragment, Visibility.VISIBLE)
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

    @Test
    fun rotating_sound_grid_fragment_maintains_state() {
        startActivity()
        clickOnPlayStopButton()
        checkVisibilityOfView(R.id.sound_list_fragment, Visibility.VISIBLE)
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).perform(click())
        device.setOrientationLeft()
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))
        clickOnPlayStopButton()
    }

    @Test
    fun rotating_create_sound_fragment() {
        startActivity()
        swipeViewPagerLeft(2)
        checkVisibilityOfView(R.id.create_sound_fragment, Visibility.VISIBLE)
        device.setOrientationLeft()
        checkVisibilityOfView(R.id.create_sound_fragment, Visibility.VISIBLE)
    }

    @Test
    fun rotating_about_fragment() {
        startActivity()
        swipeViewPagerLeft(3)
        checkVisibilityOfView(R.id.about_fragment, Visibility.VISIBLE)
        device.setOrientationLeft()
        checkVisibilityOfView(R.id.about_fragment, Visibility.VISIBLE)
    }

    @Test
    fun show_volume_slider_when_clicking_on_sound() {
        startActivity()
        clickOnPlayStopButton()
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).perform(click())
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))
        clickOnPlayStopButton()
    }

    @Test
    fun stop_single_sound() {
        TODO("Not yet implemented")
    }

    @Test
    fun volume_change_on_single_sound() {
        TODO("Not yet implemented")
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

    private fun clickOnPlayStopButton() {
        onView(withId(R.id.play_button)).perform(click())
    }

    @Test
    fun random_sounds() {
        // cannot actually test this UI behaviour as there is no sound playing
        startActivity()
        clickOnRandomButton()
        // just check it din not crash the application - need a unit test for this
        // TODO: 08-Apr-20 Unit Test for this behaviour
        checkVisibilityOfView(R.id.sound_list_fragment, Visibility.VISIBLE)
    }

    private fun clickOnRandomButton() {
        onView(withId(R.id.random_button)).perform(click())
    }

    private fun clickOnSaveComboButton() {
        onView(withId(R.id.save_fav_button)).perform(click())
    }

    @Test
    fun add_combo() {
        startActivity()
        clickOnSounds(2)
        clickOnSaveComboButton()
        checkVisibilityOfView(R.id.save_favorites_dialog, Visibility.VISIBLE)
        onView(withId(R.id.comboName)).perform(typeText("Saved combo"))
        onView(withText("OK")).perform(click())
        swipeViewPagerLeft(1)
        onView(withRecyclerView(R.id.favoritesList)!!.atPosition(0))
                .check(matches(hasDescendant(withText("Saved combo"))))
        onView(withId(R.id.favoritesList)).perform(
                RecyclerViewActions.actionOnItemAtPosition<SavedComboAdapter.ViewHolder>(0, MyViewAction.clickChildViewWithId(R.id.deleteCombo)))

        onView(withText("OK")).perform(click())
    }

    @Test
    fun delete_combo() {

        startActivity()
        clickOnSounds(2)
        clickOnSaveComboButton()

        checkVisibilityOfView(R.id.save_favorites_dialog, Visibility.VISIBLE)
        onView(withId(R.id.comboName))
                .perform(typeText("Saved combo"))
        onView(withText("OK"))
                .perform(click())

        swipeViewPagerLeft(1)

        onView(withRecyclerView(R.id.favoritesList)!!.atPosition(0))
                .check(matches(hasDescendant(withText("Saved combo"))))

        onView(withId(R.id.favoritesList))
                .perform(scrollToPosition<SavedComboAdapter.ViewHolder>(0))

        onView(withId(R.id.favoritesList))
                .perform(
                        RecyclerViewActions.actionOnItemAtPosition<SavedComboAdapter.ViewHolder>(0, MyViewAction.clickChildViewWithId(R.id.deleteCombo)))

        onView(withText("OK")).perform(click())
        onView(withId(R.id.no_favorites_text)).check(matches(isDisplayed()))
    }

    @Test
    fun trigger_combo() {
        startActivity()
        clickOnSounds(2)
        clickOnSaveComboButton()
        checkVisibilityOfView(R.id.save_favorites_dialog, Visibility.VISIBLE)
        onView(withId(R.id.comboName)).perform(typeText("Saved combo"))
        onView(withText("OK")).perform(click())

        clickOnPlayStopButton()

        swipeViewPagerLeft(1)

        onView(withId(R.id.favoritesList))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<SavedComboAdapter.ViewHolder>(
                    0,
                    click()
                )
            )

        swipeViewPagerRight(1)

        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2).onChildView(withId(R.id.sound_volume)).check(matches(isDisplayed()))

        swipeViewPagerLeft(1)

        onView(withId(R.id.favoritesList)).perform(
                RecyclerViewActions.actionOnItemAtPosition<SavedComboAdapter.ViewHolder>(0, MyViewAction.clickChildViewWithId(R.id.deleteCombo)))

        onView(withText("OK")).perform(click())

        swipeViewPagerRight(1)
    }

    private fun startActivity() {
        ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun start_and_stop_timer() {
        startActivity()
        clickOnSounds(1)
        clickOnTimerButton()
        onView(withId(R.id.timer_dialog)).check(matches(isDisplayed()))
        onData(allOf()).inAdapterView(withId(R.id.timer_list)).atPosition(0).perform(click())
        onView(withId(R.id.timerText)).check(matches(allOf(isDisplayed(), not(withText("Sounds will stop in 00:00:00")))))
        clickOnPlayStopButton()
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).onChildView(withId(R.id.sound_volume)).check(matches(not(isDisplayed())))
    }

    @Test
    fun stop_timer_with_timer_button() {
        startActivity()
        clickOnSounds(1)
        clickOnTimerButton()
        onView(withId(R.id.timer_dialog)).check(matches(isDisplayed()))
        onData(allOf()).inAdapterView(withId(R.id.timer_list)).atPosition(0).perform(click())
        onView(withId(R.id.timerText)).check(matches(allOf(isDisplayed(), not(withText("Sounds will stop in 00:00:00")))))
        clickOnTimerButton()
        onView(withId(R.id.timerText)).check(matches(not(isDisplayed())))
        onData(allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0).onChildView(withId(R.id.sound_volume)).check(matches(not(isDisplayed())))
    }

    @Test
    fun rotate_timer() {
        startActivity()
        clickOnSounds(1)
        clickOnTimerButton()
        onView(withId(R.id.timer_dialog)).check(matches(isDisplayed()))
        onData(allOf()).inAdapterView(withId(R.id.timer_list)).atPosition(0).perform(click())
        onView(withId(R.id.timerText)).check(matches(allOf(isDisplayed(), not(withText("Sounds will stop in 00:00:00")))))
        device.setOrientationLeft()
        onView(withId(R.id.timerText)).check(matches(allOf(isDisplayed(), not(withText("Sounds will stop in 00:00:00")))))
    }


    //region helper methods

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