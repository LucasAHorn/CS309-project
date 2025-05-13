package com.example.androidexample;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jakewharton.threetenabp.AndroidThreeTen;


import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * This eventsActivity class is for displaying, creating, and managing events in the application.
 * It interacts with the backend to fetch event data, RSVP status, and user level.
 * The activity allows users to view events, select dates, and add new events.
 *
 * @author Lauren Kwon
 *
 */
public class EventsActivity extends AppCompatActivity{
    /** Textview to click the date of the event. */
    private TextView tvSelectedDate;
    /** Recyclerview for connecting events. */
    private RecyclerView recyclerView;
    private Button backBtn3;
    /** FloatingActionButton for creating events. */
    private Button fabAddEvent;
    private Button pollBtn;
    /** postman URL for backend mock server. */
    private static final String BACKEND_BASE_URL2 = "https://b165d33f-a05a-481d-9b91-a13100e43086.mock.pstmn.io";
    /** Actual backend URL for the server. */
    private static final String BACKEND_BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080";

    /** events URL that extends the backend server */
    private static final String EVENTS_URL = BACKEND_BASE_URL+"/event";
    /** EventAdapter that connects events. */
    private EventAdapter eventAdapter;
    /** An array of future events. */
    private List<Event> eventList = new ArrayList<>();
    /** An array of RSVPd events. */
    private Set<Long> rsvpdEventIDs = new HashSet<>();

    //    private static final DateTimeFormatter MOCK_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy HH:mm");
    /** the DateTimeFormatter in yyyy-MM-dd'T'HH:mm:ss.SS format */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS");
    /** Current user's ID number */
    public long currentUserID;
    /** Current user's group ID number */
        public long currentUserGroupID;
//    public long currentUserGroupID = 2;
    /** Event ID number of the selected one */
    public long currentEventGroupID;
    public long currentEventDuration;
    /** Current user's role in the specific group. Either user, moderator, admin */
    public long currentUserRole;
    /** Request queue for volley requests */
    private RequestQueue requestQueue;

    /**
     * Initiates UI components, sets RecyclerView and fetches events from the server.
     * Sets up click listeners for the date picker and event creation button
     *
     * @param savedInstanceState if the activity is re-activated after being shut down,
     *                           this Bundle will contain the data that is most updated.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        SharedPreferences sharedPreferences3 = getSharedPreferences("groupInfo", MODE_PRIVATE);
        currentUserGroupID = sharedPreferences3.getLong("groupID", 0);

        SharedPreferences sharedPreferences = getSharedPreferences("eventInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("groupID", currentUserGroupID);
        editor.apply();

        SharedPreferences sharedPreferences2 = getSharedPreferences("userInfo", MODE_PRIVATE);
        String currentUserIDstr = sharedPreferences2.getString("userID", "0");
        currentUserID = Long.parseLong(currentUserIDstr);

        //fetchUserGroups(currentUserID);

        //initialize the ThreeTenABP library for using date/time
        AndroidThreeTen.init(this);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        recyclerView = findViewById(R.id.recyclerViewEvents);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        backBtn3 = findViewById(R.id.goBackBtn3);


        //initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);


        //set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(eventList, BACKEND_BASE_URL, currentUserID, currentUserGroupID, currentUserRole, rsvpdEventIDs);
        recyclerView.setAdapter(eventAdapter);
        fetchUserLevel();
        fetchEvents();

        //Fetch events from the backend
        fetchRSVPdEvents();

        //Set up Click listener for selecting a date
        tvSelectedDate.setOnClickListener(v -> showDatePicker());

//        //Set up Click listener for adding a new event
//        fabAddEvent.setOnClickListener(v -> addEvent());


        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(EventsActivity.this, AddEventActivity.class);
            startActivity(intent);
        });

        backBtn3.setOnClickListener(v -> {

            Intent intent = new Intent(EventsActivity.this, GroupActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEvents();  // This should re-fetch all events from the server
    }


    /**
     * Fetches the user's role in the group and updates the visibility of certain UI elements
     * based on the user's permission(e.g. date selection and event creation)
     */
    public void fetchUserLevel(){
        String url = BACKEND_BASE_URL +"/group/" + currentUserGroupID +"/userLevel/"+currentUserID;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try{
                        //iterate through the JSON array and get RSVPd eventIDs.
                        currentUserRole =Integer.parseInt(response.trim());
                        if(currentUserRole == 0){
                            tvSelectedDate.setVisibility(View.GONE);
                            fabAddEvent.setVisibility(View.GONE);
                        }
                        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong("userRole", currentUserRole);
                        editor.apply();
                        eventAdapter = new EventAdapter(eventList, BACKEND_BASE_URL, currentUserID, currentUserGroupID, currentUserRole, rsvpdEventIDs);
                        recyclerView.setAdapter(eventAdapter);
                        Log.d("UserLevel", "Parsed user level: " + currentUserRole);
                    } catch(NumberFormatException e){
                        e.printStackTrace();
                        Toast.makeText(EventsActivity.this, "Error parsing user level", Toast.LENGTH_SHORT).show();
                    }
                },
                error ->{
                    Toast.makeText(EventsActivity.this, "Failed to load user level", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);
    }

    /**
     * Fetches the list of events that the user has RSVPd to an updates the event adapter.
     */
    public void fetchRSVPdEvents(){
        String url = EVENTS_URL +"/RSVPdEvents/" + currentUserID;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try{
                        //iterate through the JSON array and get RSVPd eventIDs.
                        for (int i = 0; i < response.length(); i++){
                            JSONObject event = response.getJSONObject(i);
                            long eventID = event.getLong("eventId");
                            rsvpdEventIDs.add(eventID);
                        }
                        Log.d("RSVP", "RSVPd event IDs: " + rsvpdEventIDs.toString());
                        //update the adapter with the RSVPd events
                        eventAdapter.updateRSVPdEvents(rsvpdEventIDs);
                    } catch(JSONException e){
                        e.printStackTrace();
                    }
                },
                error ->{
                    Toast.makeText(EventsActivity.this, "Failed to load RSVPd events", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(request);
    }


    /**
     * GET Events
     * Fetches a list of future events from the server and updates the eventList
     * Notifies the adapter to refresh the displayed events.
     */
    public void fetchEvents(){
        String getURL = EVENTS_URL+"/future/groupEvents/"+currentUserGroupID;

        //Create a request to fetch the events
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getURL, null,
                response -> {
                    eventList.clear();
                    for (int i = 0; i < response.length(); i++){
                        try{
                            //extracts individual event data from the JSON response.
                            JSONObject jsonEvent = response.getJSONObject(i);

                            //gets the event details from the JSON object
                            long eventID = jsonEvent.getLong("eventId");
                            String title = jsonEvent.getString("title");
                            String location = jsonEvent.getString("location");
                            String description = jsonEvent.getString("description");
                            int capacity = jsonEvent.getInt("capacity");
                            int durationInMinutes = jsonEvent.getInt("durationInMinutes");
                            JSONObject groupObject = jsonEvent.getJSONObject("eventGroup");
                            long groupID = groupObject.getLong("groupId");

                            // get and parse the eventDate string to LocalDateTime object
                            String eventDateStr = jsonEvent.getString("eventTime");
                            LocalDateTime eventDate;
                            try {
                                // Remove milliseconds
                                if (eventDateStr.contains(".")) {
                                    eventDateStr = eventDateStr.substring(0, eventDateStr.indexOf("."));
                                }
                                //Parse the event date string into a LocalDateTime object using specific format
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]");
                                eventDate = LocalDateTime.parse(eventDateStr, formatter);
                            } catch (Exception e) {
                                eventDate = LocalDateTime.now(); // Fallback for invalid dates
                                Log.e("DateParseError", "Failed to parse event date: " + eventDateStr, e);
                            }
                            //create an Event object from the parsed data and add it to the event list
                            eventList.add(new Event(eventID, title, eventDate, location, description, capacity, durationInMinutes, groupID));
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                    //notify the event adapter that the event list has been updated so the RecyclerView can be updated.
                    eventAdapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("VolleyError", "Error fetching events", error);
                    Toast.makeText(this, "failed to GET events", Toast.LENGTH_SHORT).show();
                }
        );
        //add the request to the request queue to be executed.
        requestQueue.add(request);
    }

    /**
     * Displays a DatePickerDialog for the user to select a date for the event.
     * Once the date is selected, it calls ShowTimePicker method to allow suer to pick a time.
     */
    //Shows a DatePickerDialog for the user to select a date
    public void showDatePicker(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    //Format the selected date into a string in "MM/dd/yyyy" format
                    String selectedDate = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                    //Call showTimePicker to allow user to select the time
                    showTimePicker(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Displays a TimePickerDialog for the user to select a  time for the event.
     * The selected time is then combined with the selected date and displayed in the TextView.
     * @param selectedDate The date selected by the user, to which the selected time will be combined.
     */
    //Shows a TimePickerDialog for the user to select a time after selecting the date
    public void showTimePicker(String selectedDate){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    //Create a LocalTime object from the selected hour and minute
                    LocalTime selectedTime = LocalTime.of(hourOfDay, minute1);
                    // Format the time to "HH:mm" format
                    String formattedTime = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                    //combine the selected Date and Time into a single string
                    String dateTime = selectedDate + " " + formattedTime;
                    //Display the combined Date and Time in the TextView
                    tvSelectedDate.setText(dateTime);
                    SharedPreferences prefs = getSharedPreferences("EventInfo", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("selectedDateTime", dateTime);
                    editor.apply();
                    Log.d("DateTimeSaved", "Saved Date and Time: " + dateTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    public void setTestUserContext(int id, int groupID, int eventGroupID, int role) {
        this.currentUserID = id;
        this.currentUserGroupID = groupID;
        this.currentEventGroupID = eventGroupID;
        this.currentUserRole = role;
    }

}
