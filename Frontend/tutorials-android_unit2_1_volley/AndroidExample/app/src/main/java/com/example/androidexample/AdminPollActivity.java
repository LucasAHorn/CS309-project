package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdminPollActivity extends AppCompatActivity {

    private Button startbtn, backBtn;
    private EditText pollDesc, pollTitle, option1Txt, option2Txt, option3Txt, option4Txt;
    private Switch multiVoteSwitch;
    private String descTxt, titleTxt, option1, option2, option3, option4, userID, username;
    private long eventID, pollID, groupID;
    private boolean multiChoice;
    private String options;
    private RequestQueue requestQueue;
    private static final String BASE_URL2 = "http://10.0.2.2:8080/";
    private static final String BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_poll_admin);
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences sharedPreferences1 = getSharedPreferences("groupInfo", MODE_PRIVATE);
        groupID = sharedPreferences1.getLong("groupID", 0);
        SharedPreferences sharedPreferences2 = getSharedPreferences("eventInfo", MODE_PRIVATE);
        eventID = sharedPreferences2.getLong("eventID", 0);
        SharedPreferences sharedPreferences3 = getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sharedPreferences3.getString("userID", "0");
        username = sharedPreferences3.getString("username","");

        startbtn = findViewById(R.id.startbtn);
        multiVoteSwitch = findViewById(R.id.multiSwitch);
        pollDesc = findViewById(R.id.pollDescTxt);
        pollTitle = findViewById(R.id.pollTitleTxt);
        option1Txt = findViewById(R.id.option1Txt);
        option2Txt = findViewById(R.id.option2Txt);
        option3Txt = findViewById(R.id.option3Txt);
        option4Txt = findViewById(R.id.option4Txt);
        backBtn = findViewById(R.id.adminbackbtn);



        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminPollActivity.this, EventsActivity.class);
                startActivity(intent);
            }
        });

        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                multiChoice = multiVoteSwitch.isChecked();
                descTxt = pollDesc.getText().toString();
                titleTxt = pollTitle.getText().toString();
                option1 = option1Txt.getText().toString();
                option2 = option2Txt.getText().toString();
                option3 = option3Txt.getText().toString();
                option4 = option4Txt.getText().toString();

                postPoll(option1, option2, option3, option4);

            }
        });
    }

    private void postPoll(String s1, String s2, String s3, String s4){
        String postURL = BASE_URL + "poll/create/" + groupID + "/" + eventID + "/" + userID;

        JSONArray pollOptions = new JSONArray();
        pollOptions.put(s1);
        pollOptions.put(s2);
        pollOptions.put(s3);
        pollOptions.put(s4);

        JSONObject jsonBody = new JSONObject();
        try{
            //Add the event details to the JSON object
            jsonBody.put("pollId", pollID);
            jsonBody.put("prompt", titleTxt);
            jsonBody.put("pollOptions", pollOptions);
            jsonBody.put("multiChoice", multiChoice);
            Log.d("Request", jsonBody.toString());
        } catch(JSONException e){
            e.printStackTrace();
        }
        //Create a JsonObjectRequest to send the POST request with the event data
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postURL, jsonBody,
                response -> {
                    Log.d("Server Response", response.toString());
                },
                //if it fails, shows error message.
                error -> Log.e("Volley Error", error.toString())
        );
        //Add the request to the Volley request queue to be executed
        requestQueue.add(request);

    }

}