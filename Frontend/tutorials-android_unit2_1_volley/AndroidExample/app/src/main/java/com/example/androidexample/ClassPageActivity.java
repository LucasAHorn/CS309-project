package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
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
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassPageActivity extends AppCompatActivity implements View.OnClickListener {
//    private static final String BACKEND_BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080";
//    /** URL for backend local server. */
    private static final String LOCALHOST_BASE_URL2 = "http://10.0.2.2:8080";
    private static final String BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080";

    private Button btnGroup1, btnGroup2, btnGroup3, btnGroup4, btnGroup5, btnGroup6;
    private Button backBtn, leftArrow, rightArrow;
    private Button newGroupBtn;
    private TextView noGroupsText;
    private ArrayList<Group> groupList = new ArrayList<>();
    private int groupIndex = 0;
    private int currentPage = 0;
    private int totalPages = 0;
    private RequestQueue requestQueueUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_page);

        btnGroup1 = findViewById(R.id.groupbtn1);
        btnGroup2 = findViewById(R.id.groupbtn2);
        btnGroup3 = findViewById(R.id.groupbtn3);
        btnGroup4 = findViewById(R.id.groupbtn4);
        btnGroup5 = findViewById(R.id.groupbtn5);
        btnGroup6 = findViewById(R.id.groupbtn6);
        backBtn = findViewById(R.id.back_btn);
        newGroupBtn = findViewById(R.id.newGroup_btn);
        leftArrow = findViewById(R.id.prevbtn);
        rightArrow = findViewById(R.id.nextbtn);
        noGroupsText = findViewById(R.id.NAGroupText);

        backBtn.setOnClickListener(this);
        newGroupBtn.setOnClickListener(this);
        leftArrow.setOnClickListener(v -> changePage(-1));
        rightArrow.setOnClickListener(v -> changePage(1));

        btnGroup1.setOnClickListener(v -> openGroupAt(currentPage * 6));
        btnGroup2.setOnClickListener(v -> openGroupAt(currentPage * 6 + 1));
        btnGroup3.setOnClickListener(v -> openGroupAt(currentPage * 6 + 2));
        btnGroup4.setOnClickListener(v -> openGroupAt(currentPage * 6 + 3));
        btnGroup5.setOnClickListener(v -> openGroupAt(currentPage * 6 + 4));
        btnGroup6.setOnClickListener(v -> openGroupAt(currentPage * 6 + 5));

        requestQueueUser = Volley.newRequestQueue(this);
        getGroups();
    }

    private void getGroups() {
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userID", "0");
        String getURL = BASE_URL + "/group/allGroups/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getURL, null,
                response -> {
                    try {
                        groupList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonGroup = response.getJSONObject(i);
                            long groupId = jsonGroup.getLong("groupId");
                            String title = jsonGroup.getString("title");
                            String description = jsonGroup.getString("description");

                            groupList.add(new Group(title, description, groupId));
                        }
                        groupIndex = groupList.size();
                        if(groupIndex != 0){
                            noGroupsText.setVisibility(View.INVISIBLE);
                        }
                        totalPages = (int) Math.ceil(groupIndex / 6.0);
                        currentPage = 0;
                        updateGroupButtons();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error fetching groups", error);
                    Toast.makeText(this, "Failed to get groups", Toast.LENGTH_SHORT).show();
                });

        requestQueueUser.add(request);
    }

    private void updateGroupButtons() {
        List<Button> buttons = Arrays.asList(btnGroup1, btnGroup2, btnGroup3, btnGroup4, btnGroup5, btnGroup6);
        int start = currentPage * 6;

        for (int i = 0; i < 6; i++) {
            int groupPosition = start + i;
            if (groupPosition < groupList.size()) {
                Group g = groupList.get(groupPosition);
                buttons.get(i).setText(g.getTitle());
                buttons.get(i).setVisibility(View.VISIBLE);
            } else {
                buttons.get(i).setVisibility(View.INVISIBLE);
            }
        }

        leftArrow.setVisibility(currentPage > 0 ? View.VISIBLE : View.INVISIBLE);
        rightArrow.setVisibility(currentPage < totalPages - 1 ? View.VISIBLE : View.INVISIBLE);
    }
    private void changePage(int delta) {
        currentPage += delta;
        if (currentPage < 0) currentPage = 0;
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        updateGroupButtons();
    }
    private void openGroupAt(int index) {
        if (index >= groupList.size()) return;
        Group selectedGroup = groupList.get(index);

        Intent intent = new Intent(ClassPageActivity.this, GroupActivity.class);
        intent.putExtra("groupID", selectedGroup.getGroupID());
        SharedPreferences sharedPreferences3 = getSharedPreferences("groupInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences3.edit();
        editor.putLong("groupID", selectedGroup.getGroupID());
        editor.putString("groupTitle", selectedGroup.getTitle());
        editor.putString("groupDescription", selectedGroup.getDescription());
        editor.apply();
        startActivity(intent);
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_btn) {
            startActivity(new Intent(ClassPageActivity.this, HomeScreenActivity.class));
        } else if (id == R.id.newGroup_btn) {
            startActivity(new Intent(ClassPageActivity.this, ClassCreationActivity.class));
        }
    }
}

