package com.cazimir.relaxoo

import android.util.Log
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.util.TestUtil.Companion.checkVisibilityOfView
import com.cazimir.relaxoo.util.TestUtil.Companion.clearSharedPreferences
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnRandomButton
import com.cazimir.relaxoo.util.TestUtil.Companion.startActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class RandomTest {

    companion object {
        private const val TAG = "RandomTest"
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
    fun random_sounds() {
        // cannot actually test this UI behaviour as there is no sound playing
        startActivity()
        clickOnRandomButton()
        // just check it din not crash the application - need a unit test for this
        // TODO: 08-Apr-20 Unit Test for this behaviour
        checkVisibilityOfView(R.id.sound_list_fragment, Visibility.VISIBLE)
    }
}