package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {
    private List<String> memberUsernames = new ArrayList<>();
    private List<String> modUsernames = new ArrayList<>();
    private int currentMemPage = 0;
    private int currentModPage = 0;
    private final int MEMS_PER_PAGE = 10;
    private final int MODS_PER_PAGE = 5;

    private Button editBtn, eventBtn, startLPBtn, joinLPBtn, prevPage, nextPage, groupAvailBtn, eventReqBtn;
    private EditText groupNameTxt;
    private TextView groupDescriptionTxt, adminNameTxt, modNameTxt1, modNameTxt2, modNameTxt3, modNameTxt4, modNameTxt5, memNameTxt1, memNameTxt2, memNameTxt3, memNameTxt4, memNameTxt5, memNameTxt6, memNameTxt7, memNameTxt8, memNameTxt9, memNameTxt10;
    private int memCount, modCount;
    private String userID, username;
    private static final String BASE_URL2 = "http://10.0.2.2:8080/";
    private static final String BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080/";
    private RequestQueue requestQueue;
    private long userLvl, otherUserLvl, groupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_page);

        memCount = 0;
        modCount = 0;

        requestQueue = Volley.newRequestQueue(this);

        editBtn = findViewById(R.id.editbtn);
        eventBtn = findViewById(R.id.groupEventBtn);
        eventReqBtn = findViewById(R.id.groupEventRequestBtn);
        nextPage = findViewById(R.id.btnNext);
        prevPage = findViewById(R.id.previousbtn);
        startLPBtn = findViewById(R.id.groupStartBtn);
        joinLPBtn = findViewById(R.id.groupJoinPollBtn);
        groupAvailBtn = findViewById(R.id.btnGroupAvail);
        groupDescriptionTxt = findViewById(R.id.groupDescText);
        groupNameTxt = findViewById(R.id.groupTitleText);
        adminNameTxt = findViewById(R.id.adminNameTxt);
        modNameTxt1 = findViewById(R.id.modNameTxt1);
        modNameTxt2 = findViewById(R.id.modNameTxt2);
        modNameTxt3 = findViewById(R.id.modNameTxt3);
        modNameTxt4 = findViewById(R.id.modNameTxt4);
        modNameTxt5 = findViewById(R.id.modNameTxt5);
        memNameTxt1 = findViewById(R.id.memNameTxt1);
        memNameTxt2 = findViewById(R.id.memNameTxt2);
        memNameTxt3 = findViewById(R.id.memNameTxt3);
        memNameTxt4 = findViewById(R.id.memNameTxt4);
        memNameTxt5 = findViewById(R.id.memNameTxt5);
        memNameTxt6 = findViewById(R.id.memNameTxt6);
        memNameTxt7 = findViewById(R.id.memNameTxt7);
        memNameTxt8 = findViewById(R.id.memNameTxt8);
        memNameTxt9 = findViewById(R.id.memNameTxt9);
        memNameTxt10 = findViewById(R.id.memNameTxt10);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this, GroupEditorActivity.class);
                startActivity(intent);
            }
        });
        eventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this, EventsActivity.class);
                startActivity(intent);
            }
        });
        groupAvailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this, StudyPlannerPossibleTimesActivity.class);
                startActivity(intent);
            }
        });
        joinLPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(GroupActivity.this, PollResultsActivity.class);
//                startActivity(intent);
            }
        });
        startLPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this, AdminPollActivity.class);
                startActivity(intent);
            }
        });
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentMemPage++;
                renderMembers();
            }
        });

        prevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentMemPage--;
                renderMembers();
            }
        });
        eventReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this, EventProposalsActivity.class);
                startActivity(intent);
            }
        });


        //groupID = getIntent().getLongExtra("groupID", -1);
        //userID = getIntent().getLongExtra("userID", -1);

        SharedPreferences sharedPreferences3 = getSharedPreferences("groupInfo", MODE_PRIVATE);
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sharedPreferences.getString("userID", "0");
        username = sharedPreferences.getString("username", "0");
        groupNameTxt.setText(sharedPreferences3.getString("groupTitle", "0"));
        groupDescriptionTxt.setText(sharedPreferences3.getString("groupDescription", "0"));
        groupID = sharedPreferences3.getLong("groupID", 0);

        fetchUserLevel(groupID, userID);
        fetchGroupMembers(groupID);
    }
    private void fetchGroupMembers(long groupID) {
        String url = BASE_URL + "group/" + groupID + "/allEnrollments";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject user = response.getJSONObject(i);
                            Long userId = user.getLong("userId");
                            String uname = user.getString("username");

                            String userLvlUrl = BASE_URL + "group/" + groupID + "/userLevel/" + userId;

                            StringRequest levelRequest = new StringRequest(Request.Method.GET, userLvlUrl,
                                    userLvlResponse -> {
                                        long level = Long.parseLong(userLvlResponse.trim());
                                        if (level == 2) {
                                            runOnUiThread(() -> adminNameTxt.setText(uname));
                                            //eventReqBtn.setVisibility(View.VISIBLE);
                                        } else if (level == 1) {
                                            modUsernames.add(uname);
                                            //eventReqBtn.setVisibility(View.VISIBLE);
                                            renderModerators();
                                        } else if (level == 0) {
                                            memberUsernames.add(uname);
                                            //eventReqBtn.setVisibility(View.GONE);
                                            renderMembers();
                                        }
                                    },
                                    error -> Log.e("GroupActivity", "Failed to fetch user level")
                            );

                            requestQueue.add(levelRequest);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void fetchUserLevel(long groupID, String userID){
        String url = BASE_URL +"group/" + groupID +"/userLevel/"+userID;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try{
                        //iterate through the JSON array and get RSVPd eventIDs.
                        userLvl =Integer.parseInt(response.trim());
                        if(userLvl == 0){
                            editBtn.setVisibility(View.GONE);
                            joinLPBtn.setVisibility(View.VISIBLE);
                            startLPBtn.setVisibility(View.GONE);
                            groupAvailBtn.setVisibility(View.GONE);
                            memNameTxt1.setVisibility(View.VISIBLE);
                            eventReqBtn.setVisibility(View.GONE);
                            ++memCount;
                        }
                        else if(userLvl == 1){
                            modNameTxt1.setVisibility(View.VISIBLE);
                            eventReqBtn.setVisibility(View.VISIBLE);
                            modNameTxt1.setText(username);
                            ++modCount;
                        }
                        else if(userLvl == 2){
                            adminNameTxt.setText(username);
                            eventReqBtn.setVisibility(View.VISIBLE);
                        }
                        Log.d("UserLevel", "Parsed user level: " + userLvl);
                    } catch(NumberFormatException e){
                        e.printStackTrace();
                        Toast.makeText(GroupActivity.this, "Error parsing user level", Toast.LENGTH_SHORT).show();
                    }
                },
                error ->{
                    Toast.makeText(GroupActivity.this, "Failed to load user level", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);
    }
    private void fetchOtherLevel(long groupID, String userID){
        String url = BASE_URL +"group/" + groupID +"/userLevel/"+userID;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try{
                        //iterate through the JSON array and get RSVPd eventIDs.
                        otherUserLvl =Integer.parseInt(response.trim());
                        Log.d("otherUserLevel", "Parsed user level: " + otherUserLvl);
                    } catch(NumberFormatException e){
                        e.printStackTrace();
                        Toast.makeText(GroupActivity.this, "Error parsing other user level", Toast.LENGTH_SHORT).show();
                    }
                },
                error ->{
                    Toast.makeText(GroupActivity.this, "Failed to load other user level", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);
    }
    private void renderMembers() {
        TextView[] memberViews = {
                memNameTxt1, memNameTxt2, memNameTxt3, memNameTxt4, memNameTxt5,
                memNameTxt6, memNameTxt7, memNameTxt8, memNameTxt9, memNameTxt10
        };
        int start = currentMemPage * MEMS_PER_PAGE;
        int end = Math.min(start + MEMS_PER_PAGE, memberUsernames.size());

        for (int i = 0; i < MEMS_PER_PAGE; i++) {
            if (start + i < end) {
                memberViews[i].setText(memberUsernames.get(start + i));
                memberViews[i].setVisibility(View.VISIBLE);
            } else {
                memberViews[i].setVisibility(View.GONE);
            }
        }

        nextPage.setVisibility(memberUsernames.size() > (currentMemPage + 1) * MEMS_PER_PAGE ? View.VISIBLE : View.GONE);
        prevPage.setVisibility(currentMemPage > 0 ? View.VISIBLE : View.GONE);
    }

    private void renderModerators() {
        TextView[] modViews = {
                modNameTxt1, modNameTxt2, modNameTxt3, modNameTxt4, modNameTxt5
        };
        int start = currentModPage * MODS_PER_PAGE;
        int end = Math.min(start + MODS_PER_PAGE, modUsernames.size());

        for (int i = 0; i < MODS_PER_PAGE; i++) {
            if (start + i < end) {
                modViews[i].setText(modUsernames.get(start + i));
                modViews[i].setVisibility(View.VISIBLE);
            } else {
                modViews[i].setVisibility(View.GONE);
            }
        }
    }

}