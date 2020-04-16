package com.cazimir.relaxoo.muting

import android.content.Context
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.EspressoIdlingResource
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class MutingTest {

    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

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
    fun mute_all_sounds() {
        // cannot actually test this UI behaviour as there is no other UI indication that the sound is playing
        startActivity()
        clickOnSounds(2)
        Espresso.onData(CoreMatchers.allOf()).inAdapterView(withId(R.id.gridView)).atPosition(0)
            .onChildView(
                withId(R.id.sound_volume)
            ).check(matches(ViewMatchers.isDisplayed()))
        Espresso.onData(CoreMatchers.allOf()).inAdapterView(withId(R.id.gridView)).atPosition(2)
            .onChildView(
                withId(R.id.sound_volume)
            ).check(matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.mute_button)).perform(ViewActions.click())
        // TODO: 08-Apr-20 Unit test for this behaviour
        onView(withId(R.id.mute_button)).check(matches(withTagValue(equalTo("mute_on"))))

        checkVisibilityOfView(R.id.sound_list_fragment, ViewMatchers.Visibility.VISIBLE)
    }


    //region helper methods

    private fun clickOnPlayStopButton() {
        Espresso.onView(withId(R.id.play_button)).perform(ViewActions.click())
    }

    private fun clickOnRandomButton() {
        Espresso.onView(withId(R.id.random_button)).perform(ViewActions.click())
    }

    private fun clickOnSaveComboButton() {
        Espresso.onView(withId(R.id.save_fav_button)).perform(ViewActions.click())
    }

    private fun clearSharedPreferences() {
        Log.d(TAG, "clearSharedPreferences: called")
        val sharedPreferences = InstrumentationRegistry.getInstrumentation()
            .targetContext.getSharedPreferences("PREFERENCES_FILE_NAME", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.commit()
    }

    private fun startActivity() {
        ActivityScenario.launch(MainActivity::class.java)
    }

    private fun clickOnSounds(howMany: Int) {
        when (howMany) {
            1 -> Espresso.onData(CoreMatchers.allOf()).inAdapterView(withId(R.id.gridView)).atPosition(
                0
            ).perform(
                ViewActions.click()
            )
            2 -> {
                Espresso.onData(CoreMatchers.allOf())
                    .inAdapterView(withId(R.id.gridView)).atPosition(0).perform(
                        ViewActions.click()
                    )
                Espresso.onData(CoreMatchers.allOf())
                    .inAdapterView(withId(R.id.gridView)).atPosition(2).perform(
                        ViewActions.click()
                    )
            }
        }
    }

    private fun checkVisibilityOfView(viewId: Int, visibility: ViewMatchers.Visibility) {
        Espresso.onView(withId(viewId))
            .check(matches(ViewMatchers.withEffectiveVisibility(visibility)))
    }

    // endregion

    companion object {
        private const val TAG = "MainActivityTest"
    }
}