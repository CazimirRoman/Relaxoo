import android.util.Log
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.cazimir.relaxoo.EspressoIdlingResource
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.util.TestUtil
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnPlayStopButton
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnSounds
import com.cazimir.relaxoo.util.TestUtil.Companion.clickOnTimerButton
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class TimerTest {

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Rule
    @JvmField
    val grantPermissionRule2: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    companion object {
        private const val TAG = "TimerTest"
    }

    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

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
    fun start_and_stop_timer() {
        clickOnSounds(1)
        clickOnTimerButton()
        onView(ViewMatchers.withId(R.id.timer_dialog)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onData(CoreMatchers.allOf()).inAdapterView(ViewMatchers.withId(R.id.timer_list)).atPosition(0).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.timerText)).check(ViewAssertions.matches(CoreMatchers.allOf(ViewMatchers.isDisplayed(), CoreMatchers.not(ViewMatchers.withText("Sounds will stop in 00:00:00")))))
        TestUtil.clickOnPlayStopButton()
        onData(CoreMatchers.allOf()).inAdapterView(ViewMatchers.withId(R.id.gridView)).atPosition(0).onChildView(ViewMatchers.withId(R.id.sound_volume)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun stop_timer_with_timer_button() {
        clickOnSounds(1)
        clickOnTimerButton()
        onView(ViewMatchers.withId(R.id.timer_dialog)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onData(CoreMatchers.allOf()).inAdapterView(ViewMatchers.withId(R.id.timer_list)).atPosition(0).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.timerText)).check(ViewAssertions.matches(CoreMatchers.allOf(ViewMatchers.isDisplayed(), CoreMatchers.not(ViewMatchers.withText("Sounds will stop in 00:00:00")))))
        clickOnTimerButton()
        onView(ViewMatchers.withId(R.id.timerText)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
        onData(CoreMatchers.allOf()).inAdapterView(ViewMatchers.withId(R.id.gridView)).atPosition(0).onChildView(ViewMatchers.withId(R.id.sound_volume)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }
}