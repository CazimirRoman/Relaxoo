import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.EspressoIdlingResource
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.util.TestUtil
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class TimerTest {

    companion object {
        private const val TAG = "TimerTest"
    }

    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun setup() {
        Log.d(TAG, "setup: called")
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        device.setOrientationNatural()
        TestUtil.clearSharedPreferences()
    }

    @After
    fun teardown() {
        Log.d(TAG, "teardown: called")
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun start_and_stop_timer() {
        TestUtil.startActivity()
        TestUtil.clickOnSounds(1)
        TestUtil.clickOnTimerButton()
        Espresso.onView(ViewMatchers.withId(R.id.timer_dialog)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onData(CoreMatchers.allOf()).inAdapterView(ViewMatchers.withId(R.id.timer_list)).atPosition(0).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.timerText)).check(ViewAssertions.matches(CoreMatchers.allOf(ViewMatchers.isDisplayed(), CoreMatchers.not(ViewMatchers.withText("Sounds will stop in 00:00:00")))))
        TestUtil.clickOnPlayStopButton()
        Espresso.onData(CoreMatchers.allOf()).inAdapterView(ViewMatchers.withId(R.id.gridView)).atPosition(0).onChildView(ViewMatchers.withId(R.id.sound_volume)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun stop_timer_with_timer_button() {
        TestUtil.startActivity()
        TestUtil.clickOnSounds(1)
        TestUtil.clickOnTimerButton()
        Espresso.onView(ViewMatchers.withId(R.id.timer_dialog)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onData(CoreMatchers.allOf()).inAdapterView(ViewMatchers.withId(R.id.timer_list)).atPosition(0).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.timerText)).check(ViewAssertions.matches(CoreMatchers.allOf(ViewMatchers.isDisplayed(), CoreMatchers.not(ViewMatchers.withText("Sounds will stop in 00:00:00")))))
        TestUtil.clickOnTimerButton()
        Espresso.onView(ViewMatchers.withId(R.id.timerText)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
        Espresso.onData(CoreMatchers.allOf()).inAdapterView(ViewMatchers.withId(R.id.gridView)).atPosition(0).onChildView(ViewMatchers.withId(R.id.sound_volume)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }
}