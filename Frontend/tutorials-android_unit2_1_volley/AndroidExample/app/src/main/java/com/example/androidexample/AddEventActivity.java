package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class AddEventActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etLocation, etCapacity, etDuration;
    //private Spinner spinnerGroupID;
    private Button btnAddEvent, btnCancel;
    private static final String BACKEND_BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080";
    /**
     * URL for backend local server.
     */
    private static final String BACKEND_BASE_URL2 = "http://10.0.2.2:8080";
    /**
     * events URL that extends the backend server
     */
    private static final String EVENTS_URL = BACKEND_BASE_URL + "/event";
    public String currentUserID;
    private long selectedGroupUserRole;
    private long currentUserGroupID;
    private RequestQueue requestQueue;
    /**
     * An array of group names that the user is in.
     */
    List<String> groupNames = new ArrayList<>();
    /**
     * An array of group IDs that the user is in.
     */
    List<Long> groupIDs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        SharedPreferences prefs2 = getSharedPreferences("userInfo", MODE_PRIVATE);
        currentUserID = prefs2.getString("userID", "0");

//        Long userID = Long.parseLong(currentUserID);
//        fetchUserGroups(userID);

        requestQueue = Volley.newRequestQueue(this);


        // Bind UI elements
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etCapacity = findViewById(R.id.etCapacity);
        etDuration = findViewById(R.id.etDuration);
//        spinnerGroupID = findViewById(R.id.spinnerGroupID);

        btnAddEvent = findViewById(R.id.btnAddEvent);
        btnCancel = findViewById(R.id.btnCancel);

//        // Populate spinner
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupNames);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerGroupID.setAdapter(adapter);

        btnAddEvent.setOnClickListener(v -> {

            SharedPreferences prefs1 = getSharedPreferences("EventInfo", MODE_PRIVATE);
            String theDateTime = prefs1.getString("selectedDateTime", "4/30/2028 20:33");
            Log.d("DateTimeRetrieve", "Retrieved selectedDateTime: " + theDateTime);

            SharedPreferences sharedPreferences1 = getSharedPreferences("eventInfo", MODE_PRIVATE);
            currentUserGroupID = sharedPreferences1.getLong("groupID", 0);
            Log.d("currentUserGroupID retrieved", "groupID: " + currentUserGroupID);

            String title = etTitle.getText().toString();
            String selectedEventDateTime = theDateTime;
            String description = etDescription.getText().toString();
            String location = etLocation.getText().toString();
            String capacityText = etCapacity.getText().toString();
            String durationText = etDuration.getText().toString();

            if (selectedEventDateTime.equals("Click to Enter Date and Time")) {
                Toast.makeText(this, "Please select a date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                if (title.isEmpty() || description.isEmpty() || location.isEmpty() || capacityText.isEmpty() || durationText.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int capacity = Integer.parseInt(capacityText);
                int durationInMinutes = Integer.parseInt(durationText);

                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy HH:mm");
                DateTimeFormatter backendFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS");

                LocalDateTime selectedEventDateTimeLDT = LocalDateTime.parse(selectedEventDateTime, inputFormatter);
                String formattedEventDateTime = selectedEventDateTimeLDT.format(backendFormatter);

//                long groupID = groupIDs.get(spinnerGroupID.getSelectedItemPosition());

//                fetchUserLevel(groupID);
                long groupID = currentUserGroupID;

                Event newEvent = new Event(title, selectedEventDateTimeLDT, location, description, capacity, durationInMinutes, groupID);

                // Post back to server
                postEvent(newEvent);

                // Go back to EventsActivity
                Intent intent = new Intent(AddEventActivity.this, EventsActivity.class);
                startActivity(intent);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * POST event
     * Sends a POST request to create a new event on the server and adds it
     * to the event List if it is successful
     *
     * @param newEvent the event to be added
     */
    private void postEvent(Event newEvent){
//        String postURL = BASE_URL+"/events";
        //the URL for the POST request to create a new event
//        String postURL = BASE_URL + "/event/create";
        String postURL = EVENTS_URL+"/create/" + currentUserID;

        //Create a new JSONObject to hole the event data to be sent to the server
        //However, it doesn't contain eventID because the server assigns it automatically.
        JSONObject jsonBody = new JSONObject();
        try{
            DateTimeFormatter backendFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
            String formattedEventDateTime = newEvent.getEventTime().format(backendFormatter);

            //Add the event details to the JSON object
            jsonBody.put("title", newEvent.getTitle());
            jsonBody.put("eventTime", formattedEventDateTime);
            jsonBody.put("location", newEvent.getLocation());
            jsonBody.put("description", newEvent.getDescription());
            jsonBody.put("capacity", newEvent.getCapacity());
            jsonBody.put("eventGroupId", newEvent.getGroupID());
            jsonBody.put("durationInMinutes", newEvent.getDurationInMinutes());
            Log.d("Request", jsonBody.toString());
        } catch(JSONException e){
            e.printStackTrace();
        }
        //Create a JsonObjectRequest to send the POST request with the event data
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postURL, jsonBody,
                response -> {
                    Log.d("Response", "Response: " + response.toString());
                    Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show();
                },
                //if it fails, shows error message.
                error -> Toast.makeText(this, "Event not added", Toast.LENGTH_SHORT).show()
        );
        //Add the request to the Volley request queue to be executed
        requestQueue.add(request);

    }
//        private void fetchUserGroups(long userID){
//            String url = BACKEND_BASE_URL + "/group/allGroups/" + userID;
//
//            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
//                    Request.Method.GET, url, null, response -> {
//                try {
//                    groupNames.clear();
//                    groupIDs.clear();
//
//                    for (int i = 0; i < response.length(); i++) {
//                        JSONObject group = response.getJSONObject(i);
//                        long groupID = group.getLong("groupId");
//                        String groupName = group.getString("title");
//
//                        groupIDs.add(groupID);
//                        groupNames.add(groupName);
//                    }
//                    Log.d("groupIDs", "groupID list: " + groupIDs);
//                    Log.d("groupNames", "groupName list: " + groupNames);
//
//                    if (groupNames.isEmpty()) {
//                        groupNames.add("no groups found");
//                    }
//
//                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupNames);
//                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    spinnerGroupID.setAdapter(adapter);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Toast.makeText(this, "error parsing group data", Toast.LENGTH_SHORT).show();
//                }
//            },
//                    error -> {
//                        error.printStackTrace();
//                        Toast.makeText(this, "failed to load groups", Toast.LENGTH_SHORT).show();
//
//                    }
//            );
//            Volley.newRequestQueue(this).add(jsonArrayRequest);
//        }

    /**
     * Fetches the user's role in the group and updates the visibility of certain UI elements
     * based on the user's permission(e.g. date selection and event creation)
     */
    public void fetchUserLevel(long selectedGroupID){
        String url = BACKEND_BASE_URL +"/group/" + selectedGroupID +"/userLevel/"+currentUserID;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try{
                        //iterate through the JSON array and get RSVPd eventIDs.
                        selectedGroupUserRole =Integer.parseInt(response.trim());
                        if(selectedGroupUserRole == 0){
                            Toast.makeText(AddEventActivity.this, "You aren't allowed to add event to the selected group.", Toast.LENGTH_SHORT).show();
                        }
                        Log.d("UserLevel", "Parsed user level to the selected group: " + selectedGroupUserRole);
                    } catch(NumberFormatException e){
                        e.printStackTrace();
                        Toast.makeText(AddEventActivity.this, "Error parsing user level", Toast.LENGTH_SHORT).show();
                    }
                },
                error ->{
                    Toast.makeText(AddEventActivity.this, "Failed to load user level", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);
    }


    }

