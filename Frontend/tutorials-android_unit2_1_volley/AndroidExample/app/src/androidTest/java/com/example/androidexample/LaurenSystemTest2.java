//package com.example.androidexample;
//
//import static androidx.test.espresso.Espresso.onData;
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.Espresso.pressBack;
//import static androidx.test.espresso.action.ViewActions.clearText;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
//import static androidx.test.espresso.action.ViewActions.typeText;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.intent.Intents.intended;
//import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
//import static androidx.test.espresso.matcher.RootMatchers.isDialog;
//import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
//import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
//import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
//import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.withText;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Handler;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.DatePicker;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.TimePicker;
//
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.test.core.app.ActivityScenario;
//import androidx.test.core.app.ApplicationProvider;
//import androidx.test.espresso.Root;
//import androidx.test.espresso.UiController;
//import androidx.test.espresso.ViewAction;
//import androidx.test.espresso.assertion.ViewAssertions;
//import androidx.test.espresso.contrib.PickerActions;
//import androidx.test.espresso.contrib.RecyclerViewActions;
//import androidx.test.espresso.intent.Intents;
//import androidx.test.espresso.matcher.RootMatchers;
//import androidx.test.espresso.matcher.ViewMatchers;
//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//import androidx.test.filters.LargeTest;
//import androidx.test.runner.AndroidJUnit4;
//
//import static org.hamcrest.Matchers.allOf;
//import static org.hamcrest.Matchers.anything;
//import static org.hamcrest.Matchers.containsString;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.not;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotEquals;
//import static org.junit.Assert.assertTrue;
//
//
//import org.hamcrest.Description;
//import org.hamcrest.Matcher;
//import org.hamcrest.Matchers;
//import org.hamcrest.TypeSafeMatcher;
//import org.junit.Before;
//import org.junit.FixMethodOrder;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.MethodSorters;
//
//
//@RunWith(AndroidJUnit4.class)
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@LargeTest
//public class LaurenSystemTest2 {
//
//    private static final int SIMULATED_DELAY_MS = 5000;
//
//    @Rule
//    public ActivityScenarioRule<LoginActivity> loginRule =
//            new ActivityScenarioRule<>(LoginActivity.class);
//
//    //successfully works.
//    @Test
//    public void testA1_LoginSuccess() {
//        onView(withId(R.id.login_username_edt))
//                .perform(typeText("testuser1"), closeSoftKeyboard());
//        onView(withId(R.id.login_password_edt))
//                .perform(typeText("thisisright"), closeSoftKeyboard());
//        onView(withId(R.id.login_login_btn)).perform(click());
//
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        onView(withId(R.id.homeText))
//                .check(matches(isDisplayed()));
//    }
//    @Test
//    public void testNavigationButtons() {
//        Intents.init();  // Start capturing intents
//
//        ActivityScenario.launch(HomeScreenActivity.class);
//
//        // Test groupbtn → ClassPageActivity
//        onView(withId(R.id.groupbtn)).perform(click());
//        intended(hasComponent(ClassPageActivity.class.getName()));
//        pressBack(); // return to HomeScreen
//
//        // Test historyBtn → HistoryActivity
//        onView(withId(R.id.historyBtn)).perform(click());
//        intended(hasComponent(HistoryActivity.class.getName()));
//        pressBack();
//
//        // Test goBackbtn1 → LoginActivity
//        onView(withId(R.id.goBackbtn1)).perform(click());
//        intended(hasComponent(LoginActivity.class.getName()));
//
//        Intents.release(); // Stop capturing intents
//    }
//
////    //    @Test
////    public void testB8_RSVPEvent() {
////        Context context = ApplicationProvider.getApplicationContext();
////
////        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
////        userPrefs.edit().putString("userID", "2").apply();
////
////        SharedPreferences groupPrefs = context.getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
////        groupPrefs.edit().putLong("groupID", 2).apply();
////
////        ActivityScenario<EventsActivity> scenario = ActivityScenario.launch(EventsActivity.class);
////        scenario.onActivity(activity -> {
////            activity.setTestUserContext(2, 2, 2, 2);
////            activity.fetchEvents();
////        });
////
////        try {
////            Thread.sleep(3000); // Wait for events to load
////        } catch (InterruptedException e) {}
////
////        onView(withId(R.id.recyclerViewEvents))
////                .perform(RecyclerViewActions.actionOnItemAtPosition(4, clickChildViewWithId(R.id.btnRSVP)));
////        onView(withText("Yes")).inRoot(isDialog()).perform(click());
////        try {
////            Thread.sleep(3000); // Wait for events to load
////        } catch (InterruptedException e) {}
////        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
////        long groupID = eventPrefs.getLong("groupID", -1);
////
////        assertNotEquals("Group ID should be updated in SharedPreferences", -1, groupID);
////    }
////    public static ViewAction checkChildViewWithIdText(final int id, String expectedText) {
////        return new ViewAction() {
////            @Override
////            public Matcher<View> getConstraints() {
////                return isAssignableFrom(View.class);
////            }
////
////            @Override
////            public String getDescription() {
////                return "Check text of a child view with specified id.";
////            }
////
////            @Override
////            public void perform(UiController uiController, View view) {
////                View v = view.findViewById(id);
////                if (v instanceof TextView) {
////                    TextView textView = (TextView) v;
////                    assertEquals(expectedText, textView.getText().toString());
////                }
////            }
////        };
////    }
////        //passes the test
////    public static ViewAction clickChildViewWithId(final int id) {
////        return new ViewAction() {
////            @Override
////            public Matcher<View> getConstraints() {
////                return isAssignableFrom(View.class);
////            }
////
////            @Override
////            public String getDescription() {
////                return "Click on a child view with specified id.";
////            }
////
////            @Override
////            public void perform(UiController uiController, View view) {
////                View v = view.findViewById(id);
////                if (v != null) {
////                    v.performClick();
////                }
////            }
////        };
////    }
////
////
////    @Test
////    public void testE1_OnCreate() {
////        // Set up the shared preferences for user and event data
////        Context context = ApplicationProvider.getApplicationContext();
////        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
////        userPrefs.edit().putString("userID", "2").apply();
////        userPrefs.edit().putLong("userRole", 2).apply();
////
////        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
////        eventPrefs.edit().putLong("groupID", 2).apply();
////
////        // Launch ReportActivity
////        ActivityScenario<ReportActivity> scenario = ActivityScenario.launch(ReportActivity.class);
////
////        // Step 1: Check if the UI components are initialized correctly
////        onView(withId(R.id.tvInstruction1)).check(matches(isDisplayed()));
////        onView(withId(R.id.tvInstruction2)).check(matches(isDisplayed()));
////        onView(withId(R.id.tvInstruction3)).check(matches(isDisplayed()));
////        onView(withId(R.id.tvResult)).check(matches(isDisplayed()));
////
////        onView(withId(R.id.deleteMessageBtn)).check(matches(isDisplayed()));
////        onView(withId(R.id.banUserBtn)).check(matches(isDisplayed()));
////        onView(withId(R.id.unbanUserBtn)).check(matches(isDisplayed()));
////
////        // Step 2: Check if the SharedPreferences values are applied correctly
////        // For example, you could check if `tvResult` displays a value after a specific action
////        // or check if other UI elements reflect the data from SharedPreferences.
////
////        // You can test if the buttons behave as expected (for example, onClick actions).
////        onView(withId(R.id.deleteMessageBtn)).perform(click());
////        onView(withId(R.id.tvResult)).check(matches(withText("emptyChatID")));
////    }
////
////    @Test
////    public void testE1_deleteChat() {
////        Context context = ApplicationProvider.getApplicationContext();
////        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
////        userPrefs.edit().putString("userID", "2").apply();
////        userPrefs.edit().putLong("userRole", 2).apply();
////        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
////        eventPrefs.edit().putLong("groupID", 2).apply();
////
//////        Intents.init();
////
////        ActivityScenario<ReportActivity> scenario = ActivityScenario.launch(ReportActivity.class);
////        onView(withId(R.id.etChatID)).perform(typeText("5"), closeSoftKeyboard());
////        onView(withId(R.id.deleteMessageBtn)).perform(click());
////        onView(withId(R.id.tvResult)).check(matches(not(withText("Delete Message"))));
////    }
////
////    @Test
////    public void testE2_banUser() {
////        Context context = ApplicationProvider.getApplicationContext();
////        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
////        userPrefs.edit().putString("userID", "2").apply();
////        userPrefs.edit().putLong("userRole", 2).apply();
////        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
////        eventPrefs.edit().putLong("groupID", 2).apply();
////        ActivityScenario<ReportActivity> scenario = ActivityScenario.launch(ReportActivity.class);
////        onView(withId(R.id.etBanUsername)).perform(typeText("testUser2"), closeSoftKeyboard());
////        onView(withId(R.id.etBanReason)).perform(typeText("test ban reason"), closeSoftKeyboard());
////        onView(withId(R.id.banUserBtn)).perform(click());
////        try {
////            Thread.sleep(3000); // Wait for events to load
////        } catch (InterruptedException e) {}
////        onView(withId(R.id.tvResult))
////                .check(matches(withText("Ban User")));
////
////    }
////
////    @Test
////    public void testE3_unbanUser() {
////        Context context = ApplicationProvider.getApplicationContext();
////        SharedPreferences userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
////        userPrefs.edit().putString("userID", "2").apply();
////        userPrefs.edit().putLong("userRole", 2).apply();
////        SharedPreferences eventPrefs = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
////        eventPrefs.edit().putLong("groupID", 2).apply();
////        ActivityScenario<ReportActivity> scenario = ActivityScenario.launch(ReportActivity.class);
////        onView(withId(R.id.etUnbanUsername)).perform(typeText("testUser2"), closeSoftKeyboard());
////        onView(withId(R.id.unbanUserBtn)).perform(click());
////        try {
////            Thread.sleep(3000); // Wait for events to load
////        } catch (InterruptedException e) {}
////        onView(withId(R.id.tvResult))
////                .check(matches(withText("Unban User")));
////
////    }
////    @Test
////    public void testF1_resetPassword(){
////        Context context = ApplicationProvider.getApplicationContext();
////        SharedPreferences sharedPreferences = context.getSharedPreferences("userSecurity", Context.MODE_PRIVATE);
////        sharedPreferences.edit()
////                .putString("username", "testuser2")
////                .putString("city", "CA")
////                .putString("pet", "pet")
////                .apply();
////        ActivityScenario<ResetActivity> scenario = ActivityScenario.launch(ResetActivity.class);
////        onView(withId(R.id.reset_password_edt)).perform(typeText("thisisnew"), closeSoftKeyboard());
////        onView(withId(R.id.reset_confirm_edt)).perform(typeText("thisisnew"), closeSoftKeyboard());
////        onView(withId(R.id.reset_password_btn)).perform(click());
////
//////        onView(withText("Enter New Password"))
//////                .inRoot(withDecorView(not(is(getActivityDecorView()))))
//////                .check(matches(isDisplayed()));
////    }
//
//
//
//}
