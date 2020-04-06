package com.cazimir.relaxoo.ui.favorites

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.ui.RecyclerViewMatcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class FavoritesFragmentTest {

    @Test
    fun fragmentIsDisplayed() {
        val scenario = launchFragmentInContainer<FavoritesFragment>()
        onView(withId(R.id.favoritesList)).check(matches(isDisplayed()))
//        //check specific recyclerview item has a specific test
//        onView(withRecyclerView(R.id.favoritesList)?.atPosition(0)).check(matches(hasDescendant(withText("sfsfsd"))))
    }


    fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher? {
        return RecyclerViewMatcher(recyclerViewId)
    }
}