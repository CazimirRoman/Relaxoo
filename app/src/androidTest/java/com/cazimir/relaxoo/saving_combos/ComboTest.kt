package com.cazimir.relaxoo

import android.util.Log
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.adapter.SavedComboAdapter
import com.cazimir.relaxoo.util.TestUtil.Companion.checkVisibilityOfView
import com.cazimir.relaxoo.util.TestUtil.Companion.clickChildViewWithId
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnPlayStopButton
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnSaveComboButton
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnSounds
import com.cazimir.relaxoo.util.TestUtil.Companion.swipeViewPagerLeft
import com.cazimir.relaxoo.util.TestUtil.Companion.swipeViewPagerRight
import com.cazimir.relaxoo.util.TestUtil.Companion.withRecyclerView
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class ComboTest {

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Rule
    @JvmField
    val grantPermissionRule2: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    companion object {
        private const val TAG = "ComboTest"
    }

    val device = UiDevice.getInstance(getInstrumentation())

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
    fun add_combo() {
        clickOnSounds(2)
        clickOnSaveComboButton()
        checkVisibilityOfView(R.id.save_favorites_dialog, Visibility.VISIBLE)
        onView(withId(R.id.comboName)).perform(typeText("Saved combo"))
        onView(withText("OK")).perform(click())
        swipeViewPagerLeft(1)
        onView(withRecyclerView(R.id.favoritesList)!!.atPosition(0))
                .check(matches(hasDescendant(withText("Saved combo"))))
        onView(withId(R.id.favoritesList)).perform(
                RecyclerViewActions.actionOnItemAtPosition<SavedComboAdapter.ViewHolder>(0, clickChildViewWithId(R.id.deleteCombo)))

        onView(withText("OK")).perform(click())
    }

    @Test
    fun delete_combo() {
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
                        RecyclerViewActions.actionOnItemAtPosition<SavedComboAdapter.ViewHolder>(0, clickChildViewWithId(R.id.deleteCombo)))

        onView(withText("OK")).perform(click())
        onView(withId(R.id.no_favorites_text)).check(matches(isDisplayed()))
    }

    @Test
    fun trigger_combo() {
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
                RecyclerViewActions.actionOnItemAtPosition<SavedComboAdapter.ViewHolder>(0, clickChildViewWithId(R.id.deleteCombo)))

        onView(withText("OK")).perform(click())

        swipeViewPagerRight(1)
    }
}