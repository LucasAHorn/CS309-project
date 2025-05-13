package com.example.androidexample;

import org.json.JSONObject;
import org.json.JSONException;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StudyPlannerPossibleTimesActivity extends AppCompatActivity {

    EditText time1Txt, time2Txt, time3Txt;
    Button time1Btn, time2Btn, time3Btn;
    EditText spptStartDateTxt, spptStartTimeTxt, spptEndDateTxt, spptEndTimeTxt, spptDurationTxt;
    Button searchBtn;
    String userID;
    private static final String URL = "http://coms-3090-009.class.las.iastate.edu:8080/";

    long groupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_planner_possible_times);

        // Get group ID and user ID from SharedPreferences
        SharedPreferences sharedPreferences1 = getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sharedPreferences1.getString("userID", "0");
        SharedPreferences sharedPreferences = getSharedPreferences("groupInfo", MODE_PRIVATE);
        groupID = sharedPreferences.getLong("groupID", 0);

        // Bind input fields
        spptStartDateTxt = findViewById(R.id.spptStartDateTxt);
        spptStartTimeTxt = findViewById(R.id.spptStartTimeTxt);
        spptEndDateTxt = findViewById(R.id.spptEndDateTxt);
        spptEndTimeTxt = findViewById(R.id.spptEndTimeTxt);
        spptDurationTxt = findViewById(R.id.spptDurationTxt);
        searchBtn = findViewById(R.id.searchBtn);

        // Bind time slots and buttons
        time1Txt = findViewById(R.id.time1Txt);
        time2Txt = findViewById(R.id.time2Txt);
        time3Txt = findViewById(R.id.time3Txt);

        time1Btn = findViewById(R.id.time1Btn);
        time2Btn = findViewById(R.id.time2Btn);
        time3Btn = findViewById(R.id.time3Btn);

        // Search button click handler
        searchBtn.setOnClickListener(view -> {
            String startDate = spptStartDateTxt.getText().toString();
            String startTime = spptStartTimeTxt.getText().toString();
            String endDate = spptEndDateTxt.getText().toString();
            String endTime = spptEndTimeTxt.getText().toString();
            String duration = spptDurationTxt.getText().toString();

            // Format start and end times to match backend expectations
            String startFormatted = formatToBackendFormat(startDate, startTime);
            String endFormatted = formatToBackendFormat(endDate, endTime);

            // Create the URL
            String url = URL + "TimeToMeet/findPotentialEvents/" + duration + "/" + groupID
                    + "?startTime=" + startFormatted + "&endTime=" + endFormatted;

            // Send GET request
            RequestQueue queue = Volley.newRequestQueue(this);
            JsonArrayRequest request = new JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    response -> {
                        try {


                            int n = Math.min(3, response.length());
                            if (n > 0) {
                                JSONObject eventObj = response.getJSONObject(0);
                                String eventTime = eventObj.getString("eventStartTime");  // returns "2025-06-02T11:00:00"
                                showTime(0, eventTime);
                            }
                            if (n > 1){
                                JSONObject eventObj = response.getJSONObject(1);
                                String eventTime = eventObj.getString("eventStartTime");  // returns "2025-06-02T11:00:00"
                                showTime(1, eventTime);
                            }
                            if (n > 2) {
                                JSONObject eventObj = response.getJSONObject(2);
                                String eventTime = eventObj.getString("eventStartTime");  // returns "2025-06-02T11:00:00"
                                showTime(2, eventTime);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> error.printStackTrace()
            );

            queue.add(request);
        });

        // Button click handlers to schedule event
        time1Btn.setOnClickListener(view -> schedule(time1Txt.getText().toString()));
        time2Btn.setOnClickListener(view -> schedule(time2Txt.getText().toString()));
        time3Btn.setOnClickListener(view -> schedule(time3Txt.getText().toString()));
    }

    private void showTime(int index, String time) {
        switch (index) {
            case 0:
                time1Txt.setText(time);
                time1Txt.setVisibility(View.VISIBLE);
                time1Btn.setVisibility(View.VISIBLE);
                break;
            case 1:
                time2Txt.setText(time);
                time2Txt.setVisibility(View.VISIBLE);
                time2Btn.setVisibility(View.VISIBLE);
                break;
            case 2:
                time3Txt.setText(time);
                time3Txt.setVisibility(View.VISIBLE);
                time3Btn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void schedule(String time) {
        String url = URL + "event/request/" + userID;

        JSONObject eventRequestJson = new JSONObject();
        try {
            eventRequestJson.put("eventGroupId", groupID);
            eventRequestJson.put("title", "Study Group Session");
            eventRequestJson.put("eventTime", time);
            eventRequestJson.put("creatorName", userID);
            eventRequestJson.put("description", "A session for group study");
            eventRequestJson.put("location", "Room 101");
            eventRequestJson.put("capacity", 20);
            eventRequestJson.put("durationInMinutes", 30);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                eventRequestJson,
                response -> {
                    Intent intent = new Intent(StudyPlannerPossibleTimesActivity.this, GroupActivity.class);
                    startActivity(intent);
                },
                error -> {
                    error.printStackTrace();
                }
        );

        queue.add(request);
    }

    // Format datetime as "yyyy-MM-dd_HH:mm" to match backend expectations
    private String formatToBackendFormat(String date, String time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            LocalDateTime dateTime = LocalDateTime.parse(date + " " + time, inputFormatter);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            return dateTime.format(outputFormatter);
        }
        return date.replace("/", "-") + "T" + time; // fallback
    }

}