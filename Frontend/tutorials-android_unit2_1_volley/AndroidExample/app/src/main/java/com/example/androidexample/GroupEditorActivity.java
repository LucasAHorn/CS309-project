package com.example.androidexample;

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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class GroupEditorActivity extends AppCompatActivity {
    private TextView groupTitleTxt;
    private EditText memNameTxt;
    private Button addMemBtn, deleteMemBtn, promoteModBtn, demoteModBtn, promoteAdminBtn, deleteGroupBtn;
    private RequestQueue requestQueue;
    private long userLvl, groupID;
    private String memName, groupName, userID;
    private static final String BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_editor);

        SharedPreferences sharedPreferences3 = getSharedPreferences("groupInfo", MODE_PRIVATE);
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sharedPreferences.getString("userID", "0");
        groupID = sharedPreferences3.getLong("groupID", 0);
        groupName = sharedPreferences3.getString("groupTitle", "0");

        groupTitleTxt = findViewById(R.id.groupEditTitleText);
        memNameTxt = findViewById(R.id.memNameChange);
        addMemBtn = findViewById(R.id.addMemberbtn);
        deleteMemBtn = findViewById(R.id.deleteMemberbtn);
        promoteModBtn = findViewById(R.id.promoteModeratorbtn);
        demoteModBtn = findViewById(R.id.demoteModeratorBtn);
        promoteAdminBtn = findViewById(R.id.promoteAdminbtn);
        deleteGroupBtn = findViewById(R.id.deleteGroupBtn);

        addMemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memName = memNameTxt.getText().toString();
                addMember(groupID, memName);
            }
        });
        deleteMemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memName = memNameTxt.getText().toString();
                deleteMember(groupID, memName);
            }
        });
        promoteModBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memName = memNameTxt.getText().toString();
                promoteModerator(groupID, memName);
            }
        });
        promoteAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memName = memNameTxt.getText().toString();
                promoteAdmin(groupID, memName);
            }
        });
        demoteModBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memName = memNameTxt.getText().toString();
                demoteMember(groupID, memName);
            }
        });
        deleteGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            deleteGroup(groupID, userID);
            }
        });
        groupTitleTxt.setText(groupName);
    }
    private void fetchUserLevel(long groupID, String userID) {
        String url = BASE_URL + "group/" + groupID + "/userLevel/" + userID;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        //iterate through the JSON array and get RSVPd eventIDs.
                        userLvl = Integer.parseInt(response.trim());
                        if (userLvl == 1) {
                            demoteModBtn.setVisibility(View.GONE);
                            promoteAdminBtn.setVisibility(View.GONE);
                            deleteGroupBtn.setVisibility(View.GONE);
                        }
                        Log.d("UserLevel", "Parsed user level: " + userLvl);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Toast.makeText(GroupEditorActivity.this, "Error parsing user level", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(GroupEditorActivity.this, "Failed to load user level", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);
    }

    private void addMember(long groupID, String username) {
        String url = BASE_URL + "group/" + groupID + "/" + userID + "/addMember/" + username;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    try {
                        if(response.has("success") && response.getInt("success") == 1){
                            Log.d("GroupEditor", "added successfully");
                            Toast.makeText(GroupEditorActivity.this, "Add Successful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GroupEditorActivity", "JSON parse error", e);
                        Toast.makeText(GroupEditorActivity.this, "Add Failed", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(GroupEditorActivity.this, "Add Failed", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void deleteMember(long groupID, String username) {
        String url = BASE_URL + "group/" + groupID + "/" + userID + "/deleteMember/" + username;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    try {
                        if(response.has("success") && response.getInt("success") == 1){
                            Log.d("GroupEditor", "delete successful");
                            Toast.makeText(GroupEditorActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GroupEditorActivity", "JSON parse error", e);
                        Toast.makeText(GroupEditorActivity.this, "Delete Failed", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(GroupEditorActivity.this, "Delete Failed", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void promoteModerator(long groupID, String username) {
        String url = BASE_URL + "group/" + groupID + "/" + userID + "/makeModerator/" + username;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    try {
                        if(response.has("success") && response.getInt("success") == 1){
                            Log.d("GroupEditor", "promoted successfully");
                            Toast.makeText(GroupEditorActivity.this, "member promoted", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GroupEditorActivity", "JSON parse error", e);
                        Toast.makeText(GroupEditorActivity.this, "Failed to promote member", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(GroupEditorActivity.this, "Failed to promote member", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void promoteAdmin(long groupID, String username) {
        String url = BASE_URL + "group/" + groupID + "/" + userID + "/makeModerator/" + username;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    try {
                        if(response.has("success") && response.getInt("success") == 1){
                            Log.d("GroupEditor", "promoted successfully");
                            Toast.makeText(GroupEditorActivity.this, "member promoted", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GroupEditorActivity", "JSON parse error", e);
                        Toast.makeText(GroupEditorActivity.this, "Failed to promote member", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(GroupEditorActivity.this, "Failed to promote member", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void deleteGroup(long groupID, String userID) {
        String url = BASE_URL + "group/" + groupID + "/" + userID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    try {
                        if(response.has("success") && response.getInt("success") == 1){
                            Log.d("GroupEditor", "group deleted successfully");
                            Toast.makeText(GroupEditorActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GroupEditorActivity", "JSON parse error", e);
                        Toast.makeText(GroupEditorActivity.this, "Delete Failed", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(GroupEditorActivity.this, "Failed to delete group", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void demoteMember(long groupID, String username) {
        String url = BASE_URL + "group/" + groupID + "/" + userID + "/makeMember/" + username;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    try {
                        if(response.has("success") && response.getInt("success") == 1){
                            Log.d("GroupEditor", "demoted successfully");
                            Toast.makeText(GroupEditorActivity.this, "member demoted", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GroupEditorActivity", "JSON parse error", e);
                        Toast.makeText(GroupEditorActivity.this, "Failed to demote member", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(GroupEditorActivity.this, "Failed to demote member", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
}
