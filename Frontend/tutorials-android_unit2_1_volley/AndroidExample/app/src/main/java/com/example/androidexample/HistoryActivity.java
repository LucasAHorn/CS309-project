package com.example.androidexample;

import static androidx.core.util.TimeUtils.formatDuration;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
/**
 * This HistoryActivity class displays previously attended events.
 * It tracks the user's attended event counts and time spent.
 *
 * @author Lauren Kwon
 *
 */

public class HistoryActivity extends AppCompatActivity {
    /** TextView of past event counts, sum of durationInMinutes of the events */
    private TextView tvPastCount, tvPastDuration;
    /** RecyclerView of the past attended events */
    public RecyclerView rvSessions;
    /** HistoryAdapter that connects the previously attended events */
    private HistoryAdapter adapter;
    /** Spinner that allows filter for groups */
    private Spinner spinnerGroupID;
    /** list of events the user previously attended */
    private List<Event> historyList = new ArrayList<>();
    /** list of group names the user is in */
    private List<String> groupNames = new ArrayList<>();
    /** list of groupIDs the user is in */
    private List<Long> groupIDs = new ArrayList<>();
    /** current user's ID number*/
    public String currentUserID;
    /** current user's group ID number */
    public long currentUserGroupID = 2;
//    private long currentEventGroupID;
    /** RequestQueue for fetching previously attended events */
    private RequestQueue requestQueue;
    /** the actual server backend URL */
    private static final String BACKEND_BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080";
    private static final String BACKEND_BASE_URL2 = "http://10.0.2.2:8080";

    /**
     * called when the activity is created. Initializes the UI components and
     * fetches the user's group information and past events
     * @param savedInstanceState if the activity is re-activated after being shut down,
     *                           this Bundle will contain the data that is most updated.
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        requestQueue = Volley.newRequestQueue(this);
        tvPastCount = findViewById(R.id.tvPastCount);
        tvPastDuration = findViewById(R.id.tvPastDuration);
        rvSessions = findViewById(R.id.rvSession);
        spinnerGroupID = findViewById(R.id.spinnerGroupID);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        currentUserID = sharedPreferences.getString("userID", "0");
        fetchUserGroups(currentUserID);

        adapter = new HistoryAdapter(historyList);
        rvSessions.setAdapter(adapter);
        rvSessions.setLayoutManager(new LinearLayoutManager(this));

        fetchPastEvents();
    }

    /**
     * Fetches past events attended by the user from the backend server.
     * THe events are filtered by the current user's group and displayed in the RecyclerView.
     */
    public void fetchPastEvents(){
        String url = BACKEND_BASE_URL + "/event/past/RSVPd/" +currentUserID+"/"+currentUserGroupID;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    historyList.clear();
                    for (int i = 0; i < response.length(); i++){
                        try{
                            //extracts individual event data from the JSON response.
                            JSONObject jsonEvent = response.getJSONObject(i);

                            //gets the event details from the JSON object
                            long eventID = jsonEvent.getLong("id");
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
                            historyList.add(new Event(eventID, title, eventDate, location, description, capacity, durationInMinutes, groupID));
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                    updatePastStats();
                    //notify the event adapter that the event list has been updated so the RecyclerView can be updated.
                    adapter.notifyDataSetChanged();
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
     * Updates the statistics for the user's past events
     * It calculates the number of events attended and the total time spent in these events.
     */
    public void updatePastStats(){
        int count = historyList.size();
        int totalMin = 0;
        if(count >= 0) {
            for (Event evt : historyList) {
                totalMin += evt.getDurationInMinutes();
            }
        }

            String eventLabel = count == 1 ? " event" : " events";
        tvPastCount.setText("Attended: " + count + eventLabel + ".");
        tvPastDuration.setText("Total Time: " + formatDuration(totalMin));
    }

    /**
     * Formats the total duration of time spent in events.
     * @param minutes The total number of minutes to be formatted.
     * @return A formatted string representing the time in hours and minutes.
     */
    public String formatDuration(int minutes){
        int h = minutes / 60;
        int m = minutes % 60;
        if (h>0){
            //example: 1h 10m
            return String.format("%dh %02dm", h,m);
        }else{
            return String.format("%dm", m);
        }
    }

    /**
     * Fetches the list of groups associated with the current user from the backend server.
     * The data is used to populate a spinner for group selection
     *
     * @param currentUserID the current user's ID
     */
    public void fetchUserGroups(String currentUserID){
        String url = BACKEND_BASE_URL + "/group/allGroups/" + currentUserID;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null, response ->{
            try{
                groupNames.clear();
                groupIDs.clear();

                for (int i = 0; i < response.length(); i++){
                    JSONObject group = response.getJSONObject(i);
                    long groupID = group.getLong("groupId");
                    String groupName = group.getString("title");

                    groupIDs.add(groupID);
                    groupNames.add(groupName);
                }
                Log.d("groupIDs", "groupID list: " + groupIDs);
                Log.d("groupNames", "groupName list: " + groupNames);

                if(groupNames.isEmpty()){
                    groupNames.add("no groups found");
                }

                ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupNames);
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGroupID.setAdapter(adapterSpinner);

                spinnerGroupID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id){
                        if (groupIDs.size() > position) {
                            currentUserGroupID = groupIDs.get(position);
                            if (currentUserGroupID != -1L) { // Only fetch events if it's a valid group
                                fetchPastEvents();
                            }
                        } else {
                            Log.w("HistoryActivity", "groupIDs is empty or invalid position selected");
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parentView){

                    }
                });

            } catch(JSONException e){
                e.printStackTrace();
                Toast.makeText(this, "error parsing group data", Toast.LENGTH_SHORT).show();
            }
        },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "failed to load groups", Toast.LENGTH_SHORT).show();

                }
        );
        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }
}


