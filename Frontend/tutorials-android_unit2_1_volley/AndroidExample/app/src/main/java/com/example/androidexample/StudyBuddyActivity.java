package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StudyBuddyActivity extends AppCompatActivity {
    private RequestQueue requestQueue;

    private String URL = "http://coms-3090-009.class.las.iastate.edu:8080";
    private String userID, groupName, username, groupTitle, groupDesc, name1, name2, name3;
    private EditText groupNameTxt, groupDescTxt;
    private Button createGroup1Btn, createGroup2Btn, createGroup3Btn, searchBtn, createBtn;
    private TextView group1Txt, group2Txt, group3Txt;
    private long groupID = -1, newGroupID = -1;
    private int success = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_buddy);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sharedPreferences.getString("userID", "0");
        groupDesc = " ";
        groupTitle = " ";
        name1 = "";
        name2 = "";
        name3 = "";

        requestQueue = Volley.newRequestQueue(this);

        groupNameTxt = findViewById(R.id.sbGroupNameTxt);
        groupDescTxt = findViewById(R.id.sbGroupDescriptionTxt);
        createGroup1Btn = findViewById(R.id.startGroup1Btn);
        createGroup2Btn = findViewById(R.id.startGroup2Btn);
        createGroup3Btn = findViewById(R.id.startGroup3Btn);
        createBtn = findViewById(R.id.sbCreateBtn);
        searchBtn = findViewById(R.id.sbSearchBtn);
        group1Txt = findViewById(R.id.sbUser1Txt);
        group2Txt = findViewById(R.id.sbUser2Txt);
        group3Txt = findViewById(R.id.sbUser3Txt);

        createGroup1Btn.setVisibility(View.INVISIBLE);
        createGroup2Btn.setVisibility(View.INVISIBLE);
        createGroup3Btn.setVisibility(View.INVISIBLE);
        group1Txt.setVisibility(View.INVISIBLE);
        group2Txt.setVisibility(View.INVISIBLE);
        group3Txt.setVisibility(View.INVISIBLE);

        groupDescTxt.setVisibility(View.INVISIBLE);
        createBtn.setVisibility(View.INVISIBLE);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupName = groupNameTxt.getText().toString();
                findUsers();
            }
        });

        createGroup1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = name1;
                getGroupInfo();
            }
        });
        createGroup2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = name2;
                getGroupInfo();
            }
        });
        createGroup3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = name3;
                getGroupInfo();
            }
        });
    }
    private void search(String groupName) {
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userID", "0");
        String getURL = URL + "/group/allGroups/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getURL, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonGroup = response.getJSONObject(i);
                            String title = jsonGroup.getString("title");
                            if(title.equals(groupName)){
                                groupID = jsonGroup.getLong("groupID");
                            }
                        }
                        if(groupID == -1){
                            Log.d("SB Finding Group", "Group does not exist");
                            Toast.makeText(StudyBuddyActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            groupNameTxt.setText("");
                            findUsers();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error fetching groups", error);
                    Toast.makeText(this, "Failed to get groups", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);

    }

    private void findUsers(){
        //GET request to backend to find users with the most groups in common
        //provide groupID and userID

            String url = URL + "/studyBuddy/find/" + userID;

            // Instantiate the RequestQueue
            RequestQueue queue = Volley.newRequestQueue(this);

            // Create a request for the data
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            // List to store the study buddies' information
                            List<String> studyBuddiesList = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    // Parse the response for each study buddy
                                    JSONObject buddyJson = response.getJSONObject(i);

                                    // Extract User Info
                                    JSONObject userJson = buddyJson.getJSONObject("user");
                                    String name = userJson.getString("username");

                                    // Extract Shared Groups
                                    List<String> sharedGroups = new ArrayList<>();
                                    JSONArray groupsArray = buddyJson.getJSONArray("sharedEventGroups");
                                    for (int j = 0; j < groupsArray.length(); j++) {
                                        sharedGroups.add(groupsArray.getString(j));
                                    }

                                    // Prepare the string representation of this study buddy
                                    String studyBuddyInfo = "Name: " + name + "\nShared Groups: " + String.join(", ", sharedGroups);
                                    if(i == 0){
                                        group1Txt.setText(studyBuddyInfo);
                                        group1Txt.setVisibility(View.VISIBLE);
                                        createGroup1Btn.setVisibility(View.VISIBLE);
                                        name1 = name;
                                    }
                                    else if(i == 1){
                                        group2Txt.setText(studyBuddyInfo);
                                        group2Txt.setVisibility(View.VISIBLE);
                                        createGroup2Btn.setVisibility(View.VISIBLE);
                                        name2 = name;
                                    }
                                    else if(i == 2) {
                                        group3Txt.setText(studyBuddyInfo);
                                        group3Txt.setVisibility(View.VISIBLE);
                                        createGroup3Btn.setVisibility(View.VISIBLE);
                                        name3 = name;
                                    }

                                    studyBuddiesList.add(studyBuddyInfo);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            // Display the study buddies list in the UI
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Handle error
                            Toast.makeText(StudyBuddyActivity.this, "Error fetching study buddies", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Add the request to the RequestQueue
            queue.add(jsonArrayRequest);





        //set text with usernames
//        group1Txt.setVisibility(View.VISIBLE);
//        group2Txt.setVisibility(View.VISIBLE);
//        group3Txt.setVisibility(View.VISIBLE);
//        createGroup1Btn.setVisibility(View.VISIBLE);
//        createGroup2Btn.setVisibility(View.VISIBLE);
//        createGroup3Btn.setVisibility(View.VISIBLE);
    }
    private void getGroupInfo(){
        searchBtn.setVisibility(View.GONE);
        createGroup1Btn.setVisibility(View.GONE);
        createGroup2Btn.setVisibility(View.GONE);
        createGroup3Btn.setVisibility(View.GONE);
        group1Txt.setVisibility(View.GONE);
        group2Txt.setVisibility(View.GONE);
        group3Txt.setVisibility(View.GONE);
        groupDescTxt.setVisibility(View.VISIBLE);
        groupNameTxt.setVisibility(View.VISIBLE);
        createBtn.setVisibility(View.VISIBLE);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupDesc = groupDescTxt.getText().toString();
                groupTitle = groupNameTxt.getText().toString();
                postGroup(groupTitle, groupDesc);
            }
        });
    }
    private void postGroup(String title, String description){
        String postURL = URL+"/group/create/" + userID;
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("title", title);
            jsonBody.put("description", description);
        } catch(JSONException e){
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postURL, jsonBody,
                response -> {
                    //Toast.makeText(this, "Group created", Toast.LENGTH_SHORT).show();
                    try {
                        if (response.has("success") && response.getInt("success") == 1) {
                            Toast.makeText(StudyBuddyActivity.this, "Sign up Successful", Toast.LENGTH_SHORT).show();
                            success = 1;
                            if(success == 1){
                                fetchID(groupTitle);
                                Intent intent = new Intent(StudyBuddyActivity.this, HomeScreenActivity.class);
                                SharedPreferences sharedPreferences3 = getSharedPreferences("groupInfo", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences3.edit();
                                editor.putLong("groupID", newGroupID);
                                editor.putString("groupTitle", groupTitle);
                                editor.putString("groupDescription", groupDesc);
                                editor.apply();
                                startActivity(intent);
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> Toast.makeText(this, "Group not created", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(request);
    }

    private void addMember(long newGroupID, String username) {
        String url = URL + "/group/" + newGroupID + "/" + userID + "/addMember/" + username;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    try {
                        if(response.has("success") && response.getInt("success") == 1){
                            Log.d("GroupEditor", "added successfully");
                            Toast.makeText(StudyBuddyActivity.this, "Add Successful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GroupEditorActivity", "JSON parse error", e);
                        Toast.makeText(StudyBuddyActivity.this, "Add Failed", Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent(StudyBuddyActivity.this, ClassPageActivity.class);
                    startActivity(intent);
                },
                error -> Toast.makeText(StudyBuddyActivity.this, "Add Failed", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void fetchID(String groupTitle) {
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userID", "0");
        String getURL = URL + "/group/allGroups/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getURL, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonGroup = response.getJSONObject(i);
                            String title = jsonGroup.getString("title");
                            if(title.equals(groupTitle)){
                                newGroupID = jsonGroup.getLong("groupId");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    addMember(newGroupID, username);
                    if(newGroupID == -1){
                        Log.d("SB Finding Group", "Group does not exist");
                        Toast.makeText(StudyBuddyActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error fetching groups", error);
                    Toast.makeText(this, "Failed to get groups", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);

    }
}
