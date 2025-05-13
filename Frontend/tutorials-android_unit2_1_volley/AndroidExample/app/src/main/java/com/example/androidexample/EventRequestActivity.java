package com.example.androidexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;

import org.json.*;

import java.util.HashMap;
import java.util.Map;

public class EventRequestActivity extends AppCompatActivity {

    EditText titleInput, dateInput, timeInput, groupIdInput, descriptionInput, locationInput, capacityInput, durationInput;
    Button submitProposalButton;
    TextView resultMessage;
    long groupID;
    String userID, username, time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_requests);

        SharedPreferences sharedPreferences3 = getSharedPreferences("groupInfo", MODE_PRIVATE);
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        SharedPreferences sharedPreferences2 = getSharedPreferences("requestInfo", MODE_PRIVATE);
        userID = sharedPreferences.getString("userID", "0");
        username = sharedPreferences.getString("username", "0");
        groupID = sharedPreferences3.getLong("groupID", 0);
        time = sharedPreferences2.getString("startTime", "0");

        titleInput = findViewById(R.id.titleInput);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        groupIdInput = findViewById(R.id.groupIdInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        locationInput = findViewById(R.id.locationInput);
        capacityInput = findViewById(R.id.capacityInput);
        durationInput = findViewById(R.id.durationInput);
        submitProposalButton = findViewById(R.id.submitProposalButton);
        resultMessage = findViewById(R.id.resultMessage);

        submitProposalButton.setOnClickListener(v -> proposeEvent());
    }

    private void proposeEvent() {
        String url = "http://coms-3090-009.class.las.iastate.edu:8080/event/request/" + userID;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("title", titleInput.getText().toString());
            requestBody.put("eventTime", time);
            requestBody.put("eventGroupId", Long.parseLong(groupIdInput.getText().toString()));
            requestBody.put("description", descriptionInput.getText().toString());
            requestBody.put("location", locationInput.getText().toString());
            requestBody.put("capacity", Integer.parseInt(capacityInput.getText().toString()));
            requestBody.put("durationInMinutes", Integer.parseInt(durationInput.getText().toString()));
        } catch (JSONException e) {
            resultMessage.setText("Error forming request.");
            return;
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        resultMessage.setText(response.getString("message"));
                    } catch (JSONException e) {
                        resultMessage.setText("Response parse error.");
                    }
                },
                error -> resultMessage.setText("Request failed: " + error.toString())
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonRequest);
    }
}
