package com.example.androidexample;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Root;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;


@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class LaurenSystemTest {

    private static final int SIMULATED_DELAY_MS = 5000;

    @Rule
    public ActivityScenarioRule<LoginActivity> loginRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    //successfully works.
    @Test
    public void testA1_LoginSuccess() {
        onView(withId(R.id.login_username_edt))
                .perform(typeText("testuser1"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edt))
                .perform(typeText("thisisright"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.homeText))
                .check(matches(isDisplayed()));
    }
    //when trying to test with button, it successfully works
    //when trying to test with toast message, it fails.
    @Test
    public void testA2_LoginFailure() {
        onView(withId(R.id.login_username_edt)).perform(typeText("wrongUser"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edt)).perform(typeText("wrongPass"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.login_username_edt)).check(matches(isDisplayed()));

        //this toaster matcher doesn't work.
//        onView(withText("Login Failed."))
//                .inRoot(withDecorView(not(is(getActivityInstance().getWindow().getDecorView())))) // Toast is not part of the activity's view hierarchy
//                .check(matches(isDisplayed()));
    }


    //successfully works
    @Test
    public void testB1_FetchEvents() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();

        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();

        SharedPreferences groupPrefs = context.getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
        groupPrefs.edit().putLong("groupID", 2).apply();

        ActivityScenario<EventsActivity> scenario = ActivityScenario.launch(EventsActivity.class);
        scenario.onActivity(activity -> {
            activity.setTestUserContext(2, 2, 2, 2);
            activity.fetchEvents();
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        onView(withId(R.id.recyclerViewEvents)).check(matches(hasDescendant(withText("Capacity: 52"))));
        onView(withId(R.id.recyclerViewEvents)).check(matches(hasDescendant(withText("Halloween Party"))));
    }

    @Test
    public void testB2_PostEvent(){
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();

        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();
        eventPrefs.edit().putString("selectedDateTime", "4/30/2028 20:33").apply();


        SharedPreferences groupPrefs = context.getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
        groupPrefs.edit().putLong("groupID", 2).apply();


        ActivityScenario<AddEventActivity> scenario = ActivityScenario.launch(AddEventActivity.class);
        scenario.onActivity(activity -> {

            activity.currentUserID = "2";
            activity.fetchUserLevel(2);
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.etTitle)).perform(typeText("Test Event"), closeSoftKeyboard());
            onView(withId(R.id.etDescription)).perform(typeText("Test Description"), closeSoftKeyboard());
            onView(withId(R.id.etLocation)).perform(typeText("Library"), closeSoftKeyboard());
            onView(withId(R.id.etCapacity)).perform(typeText("20"), closeSoftKeyboard());
            onView(withId(R.id.etDuration)).perform(typeText("90"), closeSoftKeyboard());


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.btnAddEvent)).perform(click());
    }



    //passes the test
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }

    //passes the test
    @Test
    public void testB3ModifyEvent() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();

        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();

        SharedPreferences groupPrefs = context.getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
        groupPrefs.edit().putLong("groupID", 2).apply();

        ActivityScenario<EventsActivity> scenario = ActivityScenario.launch(EventsActivity.class);
        scenario.onActivity(activity -> {
            activity.setTestUserContext(2, 2, 2, 2);
            activity.fetchEvents();
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(4, clickChildViewWithId(R.id.btnModify)));

        onView(withId(R.id.etTitle)).perform(clearText(), typeText("New Title"));
        onView(withId(R.id.etCapacity)).perform(clearText(), typeText("20"));
        closeSoftKeyboard();
        onView(withText("Save")).perform(click());


//        // Confirm toast or backend response
//        onView(withText("Event updated successfully"))
//                .inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView()))))
//                .check(matches(isDisplayed()));
    }
    //passes the test
    @Test
    public void testB4_ModifyDateTimePicker() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();

        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();

        SharedPreferences groupPrefs = context.getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
        groupPrefs.edit().putLong("groupID", 2).apply();


        ActivityScenario<EventsActivity> scenario = ActivityScenario.launch(EventsActivity.class);
        scenario.onActivity(activity -> {
            activity.setTestUserContext(2, 2, 2, 2);
            activity.fetchEvents();
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(4, clickChildViewWithId(R.id.btnModify)));

        onView(withId(R.id.etEventTime)).perform(click());

        onView(withClassName(Matchers.equalTo("android.widget.DatePicker")))
                .inRoot(isDialog()) // Important: Tell Espresso it's in a dialog
                .perform(PickerActions.setDate(2030, 5, 10));

        onView(withId(android.R.id.button1)).perform(click()); // OK button

        onView(withClassName(Matchers.equalTo("android.widget.TimePicker")))
                .inRoot(isDialog())
                .perform(PickerActions.setTime(14, 30));

        onView(withId(android.R.id.button1)).perform(click()); // OK button



        onView(withId(R.id.etEventTime)).check(matches(withText("5/10/2030 14:30")));


        closeSoftKeyboard();

        onView(withText("Save")).perform(click());


    }


//        // Confirm toast or backend response
//        onView(withText("Event updated successfully"))
//                .inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView()))))
//                .check(matches(isDisplayed()));


    //passes the test
    @Test
    public void testB5_FetchUserLevel() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();

        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();

        SharedPreferences groupPrefs = context.getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
        groupPrefs.edit().putLong("groupID", 2).apply();


        ActivityScenario<EventsActivity> scenario = ActivityScenario.launch(EventsActivity.class);
        scenario.onActivity(activity -> {
            View tvSelectedDate = activity.findViewById(R.id.tvSelectedDate);
            View fabAddEvent = activity.findViewById(R.id.fabAddEvent);

            SharedPreferences prefs = activity.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            long role = prefs.getLong("userRole", -1);

            // Adjust expected value based on what you know the backend returns
            if (role == 0) {
                assertEquals(View.GONE, tvSelectedDate.getVisibility());
                assertEquals(View.GONE, fabAddEvent.getVisibility());
            } else {
                assertEquals(View.VISIBLE, tvSelectedDate.getVisibility());
                assertEquals(View.VISIBLE, fabAddEvent.getVisibility());
            }
        });



//        // Confirm toast or backend response
//        onView(withText("Event updated successfully"))
//                .inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView()))))
//                .check(matches(isDisplayed()));
    }
    //passes the test
    @Test
    public void testB6_RSVPdEventsAndFetchEvents() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();

        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();

        SharedPreferences groupPrefs = context.getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
        groupPrefs.edit().putLong("groupID", 2).apply();

        ActivityScenario<EventsActivity> scenario = ActivityScenario.launch(EventsActivity.class);

        scenario.onActivity(activity -> {
            activity.fetchRSVPdEvents();
            activity.fetchEvents();
        });

        // Wait for async operations (only if necessary — better with IdlingResource or Espresso)
        try {
            Thread.sleep(3000); // Not ideal, but usable for quick test
        } catch (InterruptedException e) {}

        // Check if RecyclerView is populated
        onView(withId(R.id.recyclerViewEvents))
                .check(matches(hasMinimumChildCount(1)));
    }
    //passes the test
    @Test
    public void testB7_DateTimePicker() {
        ActivityScenario<EventsActivity> scenario = ActivityScenario.launch(EventsActivity.class);

        onView(withId(R.id.tvSelectedDate)).perform(click());

        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2028, 5, 15));
        onView(withId(android.R.id.button1)).perform(click());

        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(PickerActions.setTime(14, 30));
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.tvSelectedDate))
                .check(matches(withText("5/15/2028 14:30")));
    }
    @Test
    public void testB8_RSVPEvent() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();

        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();

        SharedPreferences groupPrefs = context.getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
        groupPrefs.edit().putLong("groupID", 2).apply();

        ActivityScenario<EventsActivity> scenario = ActivityScenario.launch(EventsActivity.class);
        scenario.onActivity(activity -> {
            activity.setTestUserContext(2, 2, 2, 2);
            activity.fetchEvents();
        });

        try {
            Thread.sleep(3000); // Wait for events to load
        } catch (InterruptedException e) {}

        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(4, clickChildViewWithId(R.id.btnRSVP)));
        onView(withText("Yes")).inRoot(isDialog()).perform(click());
        try {
            Thread.sleep(3000); // Wait for events to load
        } catch (InterruptedException e) {}
        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(4,
                        checkChildViewWithIdText(R.id.btnRSVP, "Registered")));

    }

    @Test
    public void testB91_UndoRSVPEvent() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();

        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();

        SharedPreferences groupPrefs = context.getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
        groupPrefs.edit().putLong("groupID", 2).apply();

        ActivityScenario<EventsActivity> scenario = ActivityScenario.launch(EventsActivity.class);
        scenario.onActivity(activity -> {
            activity.setTestUserContext(2, 2, 2, 2);
            activity.fetchEvents();
        });

        try {
            Thread.sleep(6000); // Wait for events to load
        } catch (InterruptedException e) {}

        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(4, clickChildViewWithId(R.id.btnRSVP)));
        onView(withText("Yes")).inRoot(isDialog()).perform(click());
        try {
            Thread.sleep(3000); // Wait for events to load
        } catch (InterruptedException e) {}
        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(4,
                        checkChildViewWithIdText(R.id.btnRSVP, "RSVP")));

    }

    public static ViewAction checkChildViewWithIdText(final int id, String expectedText) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "Check text of a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v instanceof TextView) {
                    TextView textView = (TextView) v;
                    assertEquals(expectedText, textView.getText().toString());
                }
            }
        };
    }

    @Test
    public void testB92_DeleteEvent() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();

        SharedPreferences groupPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        groupPrefs.edit().putLong("groupID", 2).apply();

        ActivityScenario<EventsActivity> scenario = ActivityScenario.launch(EventsActivity.class);
        scenario.onActivity(activity -> {
            activity.setTestUserContext(2, 2, 2, 2);
            activity.fetchEvents();
        });

        try {
            Thread.sleep(3000); // Wait for events to load
        } catch (InterruptedException e) {}

        onView(withId(R.id.recyclerViewEvents))
                .perform(RecyclerViewActions.actionOnItemAtPosition(4, clickChildViewWithId(R.id.btnDelete)));

        onView(withText("Yes")).inRoot(isDialog()).perform(click());

        // Optional: Verify Toast or that item is gone
        // onView(withText("Event deleted"))
        //     .inRoot(withDecorView(not(is(getActivityInstance().getWindow().getDecorView()))))
        //     .check(matches(isDisplayed()));


    }
    @Test
    public void testC1_updateTimer() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();

        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();
        eventPrefs.edit().putLong("eventID", 6).apply();
        eventPrefs.edit().putLong("eventDuration", 1).apply();
        eventPrefs.edit().putString("eventTitle", "Demo4").apply();

        ActivityScenario<EventChatActivity> scenario = ActivityScenario.launch(EventChatActivity.class);
        scenario.onActivity(activity -> {
            TextView timerTv = activity.findViewById(R.id.timerTv);

            String initialText = timerTv.getText().toString();
            assertTrue(initialText.contains("01:00"));

            activity.startTimer();

            new Handler().postDelayed(() -> {
                String updatedText = timerTv.getText().toString();
                // Should now show something like "00:58" or "00:59"
                assertTrue(updatedText.contains("00:5") || updatedText.contains("00:4"));
            }, 2000);


    });
    }

    @Test
    public void testC2_sendMessage() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();

        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();
        eventPrefs.edit().putLong("eventID", 6).apply();
        eventPrefs.edit().putLong("eventDuration", 123).apply();
        eventPrefs.edit().putString("eventTitle", "Demo4").apply();

        ActivityScenario<EventChatActivity> scenario = ActivityScenario.launch(EventChatActivity.class);
        scenario.onActivity(activity -> {
            EditText msgEdt = activity.findViewById(R.id.msgEdt);
            TextView tx1 = activity.findViewById(R.id.tx1);
    });
        String testMessage = "test message2";
        onView(withId(R.id.msgEdt)).perform(typeText(testMessage), closeSoftKeyboard());
        onView(withId(R.id.sendBtn)).perform(click());
        onView(withId(R.id.tx1))
                .check(matches(withText(containsString("test message2"))));
    }

    @Test
    public void testD1_FetchPastEvents() {
        Context context1 = ApplicationProvider.getApplicationContext();
        SharedPreferences userPrefs1 = context1.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs1.edit().putString("userID", "2").apply();

//        SharedPreferences userPrefs2 = context1.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
//        userPrefs2.edit().putLong("groupID", 2).apply();

        ActivityScenario<HistoryActivity> scenario = ActivityScenario.launch(HistoryActivity.class);
        scenario.onActivity(activity -> {
            activity.currentUserGroupID = 2;
            activity.fetchPastEvents();
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        onView(withId(R.id.rvSession)).check(matches(hasDescendant(withText("pastRSVP1"))));
    }

    @Test
    public void testD2_SpinnerSelectGroup() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply(); // or any valid user ID

        ActivityScenario<HistoryActivity> scenario = ActivityScenario.launch(HistoryActivity.class);

        onView(withId(R.id.spinnerGroupID)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        onView(withId(R.id.tvPastCount)).check(matches(not(withText("Attended: 1 events."))));
    }

//    @Test
//    public void testE1_OnCreate() {
//        // Set up the shared preferences for user and event data
//        Context context = ApplicationProvider.getApplicationContext();
//        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
//        userPrefs.edit().putString("userID", "2").apply();
//        userPrefs.edit().putLong("userRole", 2).apply();
//
//        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
//        eventPrefs.edit().putLong("groupID", 2).apply();
//
//        // Launch ReportActivity
//        ActivityScenario<ReportActivity> scenario = ActivityScenario.launch(ReportActivity.class);
//
//        // Step 1: Check if the UI components are initialized correctly
//        onView(withId(R.id.tvInstruction1)).check(matches(isDisplayed()));
//        onView(withId(R.id.tvInstruction2)).check(matches(isDisplayed()));
//        onView(withId(R.id.tvInstruction3)).check(matches(isDisplayed()));
//        onView(withId(R.id.tvResult)).check(matches(isDisplayed()));
//
//        onView(withId(R.id.deleteMessageBtn)).check(matches(isDisplayed()));
//        onView(withId(R.id.banUserBtn)).check(matches(isDisplayed()));
//        onView(withId(R.id.unbanUserBtn)).check(matches(isDisplayed()));
//
//        // Step 2: Check if the SharedPreferences values are applied correctly
//        // For example, you could check if `tvResult` displays a value after a specific action
//        // or check if other UI elements reflect the data from SharedPreferences.
//
//        // You can test if the buttons behave as expected (for example, onClick actions).
//        onView(withId(R.id.deleteMessageBtn)).perform(click());
//        onView(withId(R.id.tvResult)).check(matches(withText("emptyChatID")));
//    }
//
//    @Test
//    public void testE1_deleteChat() {
//        Context context = ApplicationProvider.getApplicationContext();
//        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
//        userPrefs.edit().putString("userID", "2").apply();
//        userPrefs.edit().putLong("userRole", 2).apply();
//        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
//        eventPrefs.edit().putLong("groupID", 2).apply();
//
//        Intents.init();
//
//        ActivityScenario<ReportActivity> scenario = ActivityScenario.launch(ReportActivity.class);
//       // onView(withId(R.id.etChatID)).perform(typeText("5"), closeSoftKeyboard());
//        onView(withId(R.id.goBackBtn2)).perform(click());
//
//        intended(hasComponent(EventChatActivity.class.getName()));
//        Intents.release();
////        onView(withId(R.id.tvResult)).check(matches(not(withText("Delete Message"))));
//    }
//
//    @Test
//    public void testE2_banUser() {
//        Context context = ApplicationProvider.getApplicationContext();
//        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
//        userPrefs.edit().putString("userID", "2").apply();
//        userPrefs.edit().putLong("userRole", 2).apply();
//        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
//        eventPrefs.edit().putLong("groupID", 2).apply();
//        ActivityScenario<ReportActivity> scenario = ActivityScenario.launch(ReportActivity.class);
//        onView(withId(R.id.etBanUsername)).perform(typeText("testUser2"), closeSoftKeyboard());
//        onView(withId(R.id.etBanReason)).perform(typeText("test ban reason"), closeSoftKeyboard());
//        onView(withId(R.id.banUserBtn)).perform(click());
//        try {
//            Thread.sleep(3000); // Wait for events to load
//        } catch (InterruptedException e) {}
//        onView(withId(R.id.tvResult))
//                .check(matches(withText("Ban User")));
//
//    }
//
//    @Test
//    public void testE3_unbanUser() {
//        Context context = ApplicationProvider.getApplicationContext();
//        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
//        userPrefs.edit().putString("userID", "2").apply();
//        userPrefs.edit().putLong("userRole", 2).apply();
//        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
//        eventPrefs.edit().putLong("groupID", 2).apply();
//        ActivityScenario<ReportActivity> scenario = ActivityScenario.launch(ReportActivity.class);
//        onView(withId(R.id.etUnbanUsername)).perform(typeText("testUser2"), closeSoftKeyboard());
//        onView(withId(R.id.unbanUserBtn)).perform(click());
//        try {
//            Thread.sleep(3000); // Wait for events to load
//        } catch (InterruptedException e) {}
//        onView(withId(R.id.tvResult))
//                .check(matches(withText("Unban User")));
//
//    }



    @Test
    public void testF1_resetPassword(){
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("userSecurity", Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString("username", "testuser2")
                .putString("city", "CA")
                .putString("pet", "pet")
                .apply();
        ActivityScenario<ResetActivity> scenario = ActivityScenario.launch(ResetActivity.class);
        onView(withId(R.id.reset_password_edt)).perform(typeText("thisisnew"), closeSoftKeyboard());
        onView(withId(R.id.reset_confirm_edt)).perform(typeText("thisisnew"), closeSoftKeyboard());
        onView(withId(R.id.reset_password_btn)).perform(click());

//        onView(withText("Enter New Password"))
//                .inRoot(withDecorView(not(is(getActivityDecorView()))))
//                .check(matches(isDisplayed()));
    }

    //successfully works.
    @Test
    public void testF2_LoginSuccess() {
        onView(withId(R.id.login_username_edt))
                .perform(typeText("testuser2"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edt))
                .perform(typeText("thisisnew"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.homeText))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testG1_VerificationInput() {
        ActivityScenario<VerificationActivity> scenario = ActivityScenario.launch(VerificationActivity.class);
        onView(withId(R.id.verification_username_edt)).perform(typeText("testuser1"), closeSoftKeyboard());
        onView(withId(R.id.verification_city_edt)).perform(typeText("CA"), closeSoftKeyboard());
        onView(withId(R.id.verification_pet_edt)).perform(typeText("pet"), closeSoftKeyboard());

        // Click next
        onView(withId(R.id.verification_next_btn)).perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.reset_password_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testH1_EventTitle(){
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();
        userPrefs.edit().putLong("userRole", 2).apply();
        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();
        eventPrefs.edit().putLong("eventID", 6).apply();
        eventPrefs.edit().putString("eventTitle", "Halloween Party").apply();
        ActivityScenario<ParticipantsActivity> scenario = ActivityScenario.launch(ParticipantsActivity.class);
        onView(withId(R.id.tvEventTitle)).check(matches(withText("Halloween Party")));
    }

    @Test
    public void testH2_BackBtn() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();
        userPrefs.edit().putLong("userRole", 2).apply();
        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();
        eventPrefs.edit().putLong("eventID", 6).apply();
        Intents.init();
        ActivityScenario<ParticipantsActivity> scenario = ActivityScenario.launch(ParticipantsActivity.class);
        onView(withId(R.id.backBtn4)).perform(click());
        intended(hasComponent(EventsActivity.class.getName()));
        Intents.release();
    }
    @Test
    public void testH3_fetchParticipants(){
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userPrefs.edit().putString("userID", "2").apply();
        userPrefs.edit().putLong("userRole", 2).apply();
        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
        eventPrefs.edit().putLong("groupID", 2).apply();
        eventPrefs.edit().putLong("eventID", 2).apply();
        eventPrefs.edit().putString("eventTitle", "Halloween Party").apply();
        ActivityScenario<ParticipantsActivity> scenario = ActivityScenario.launch(ParticipantsActivity.class);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.tvParticipants)).check(matches(withText(containsString("testuser1"))));
    }
    @Test
    public void testI1_NavigationButtons() {
        Intents.init();  // Start capturing intents

        ActivityScenario.launch(HomeScreenActivity.class);

        // Test groupbtn → ClassPageActivity
        onView(withId(R.id.groupbtn)).perform(click());
        intended(hasComponent(ClassPageActivity.class.getName()));
        pressBack(); // return to HomeScreen

        // Test historyBtn → HistoryActivity
        onView(withId(R.id.historyBtn)).perform(click());
        intended(hasComponent(HistoryActivity.class.getName()));
        pressBack();

        // Test historyBtn → HistoryActivity
        onView(withId(R.id.studyBuddyBtn)).perform(click());
        intended(hasComponent(StudyBuddyActivity.class.getName()));
        pressBack();

        // Test goBackbtn1 → LoginActivity
        onView(withId(R.id.goBackbtn1)).perform(click());
        intended(hasComponent(LoginActivity.class.getName()));

        Intents.release(); // Stop capturing intents
    } @Test
    public void testI2_NavigationButtons() {
        Intents.init();  // Start capturing intents

        ActivityScenario.launch(MainActivity2.class);

        // Test groupbtn → ClassPageActivity
        onView(withId(R.id.btn_login_main)).perform(click());
        intended(hasComponent(LoginActivity.class.getName()));
        pressBack(); // return to HomeScreen

        // Test historyBtn → HistoryActivity
        onView(withId(R.id.btn_signup_main)).perform(click());
        intended(hasComponent(SignupActivity.class.getName()));
        pressBack();

        Intents.release(); // Stop capturing intents
    }

}
