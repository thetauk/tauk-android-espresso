package io.aj.sample

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tauk.android.espresso.rules.TaukWatcher
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class StocksTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val watcher = TaukWatcher()
    /*
    val watcher = TaukWatcher("BG2Ov-9ggzHM10RzCjIwfI8KLc8w", "q9OSPWkdQ")
    */

    @Before
    fun setup() {
        IdlingRegistry.getInstance()
            .register(CountingIdlingResourceSingleton.countingIdlingResource)
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance()
            .unregister(CountingIdlingResourceSingleton.countingIdlingResource)
    }

    @Test
    fun verifyStockValue() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("io.aj.sample", appContext.packageName)

        onView(withId(R.id.stocksButton)).perform(click())
        onView(withId(R.id.symbolSpinner)).perform(click())
        onView(withText("Tesla (TSLA)")).perform(click())
        onView(withId(R.id.fetchResultsButton)).perform(click())

        onView(withText("Open price of the day: ")).check(matches(isDisplayed()))
        onView(withId(R.id.openText)).check(matches(not(withText(""))))
        onView(withText("Low price of the day: ")).check(matches(isDisplayed()))
        onView(withId(R.id.lowText)).check(matches(not(withText(""))))
        onView(withText("High price of the day: ")).check(matches(isDisplayed()))
        onView(withId(R.id.highText)).check(matches(not(withText(""))))
        onView(withText("Current price: ")).check(matches(isDisplayed()))
        onView(withId(R.id.currentText)).check(matches(not(withText(""))))
    }

    @Test
    fun checkMetaInDropDownList() {
        onView(withId(R.id.stocksButton)).perform(click())
        onView(withId(R.id.symbolSpinner)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Meta (FB)"))).check(
            matches(
                isDisplayed()
            )
        )
    }
}