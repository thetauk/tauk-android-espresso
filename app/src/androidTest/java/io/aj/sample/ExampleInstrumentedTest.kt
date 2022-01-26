package io.aj.sample

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.AndroidJUnitRunner
import com.tauk.android.espresso.rules.TaukWatcher

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val watcher = TaukWatcher()

    @Test
    fun verifyHomeScreen() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        (InstrumentationRegistry.getInstrumentation() as AndroidJUnitRunner).startPerformanceSnapshot()
        assertEquals("io.aj.sample", appContext.packageName)

        onView(withText("Hello World!")).check(matches(isDisplayed()))
    }

    @Test
    fun failingTest() {
        onView(withText("Not Hello World!")).check(matches(isDisplayed()))

    }
}