package com.kanj.apps.callercontact;

import android.content.SharedPreferences;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.widget.ListView;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SettingsTest {
    private int numberOfSwitches;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void getSwitchCount() {
        Log.v("Kanj", "Running the before crap");
        numberOfSwitches = ((ListView) mActivityRule.getActivity().findViewById(R.id.list))
                .getAdapter().getCount();
    }

    @Test
    public void aTestEnableDisableFunction() {
        // All switches should be enabled because main setting is enabled
        for (int i = 0; i<numberOfSwitches; i++) {
            onData(anything()).inAdapterView(withId(R.id.list))
                    .atPosition(i)
                    .check(matches(isEnabled()));
        }

        // Disable the main setting now
        onView(withId(R.id.enable_switch)).check(matches(isChecked())).perform(click());

        // All switches should be hidden because main setting is disabled
        onView(withId(R.id.list)).check(matches(not(isDisplayed())));

        mActivityRule.getActivity().finish();
    }

    @Test
    public void bTestEnableMainAndDisableAllSettings() {
        // Enable the main setting back first
        onView(withId(R.id.enable_switch)).check(matches(not(isChecked()))).perform(click());

        for (int i = 0; i<numberOfSwitches; i++) {
            // All switches are on by default. Turn off all
            onData(anything()).inAdapterView(withId(R.id.list))
                    .atPosition(i)
                    .onChildView(withId(R.id.sw))
                    .check(matches(isChecked()))
                    .perform(click());
        }

        mActivityRule.getActivity().finish();
    }

    @Test
    public void cTestIfAllSettingsAreDisabled() {
        for (int i = 0; i<numberOfSwitches; i++) {
            // All switches should be disabled now
            onData(anything()).inAdapterView(withId(R.id.list))
                    .atPosition(i)
                    .onChildView(withId(R.id.sw))
                    .check(matches(not(isChecked())));
        }
    }
}