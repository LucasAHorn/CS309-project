package com.example.androidexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventProposalsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProposalAdapter adapter;
    private List<EventRequest> proposals = new ArrayList<>();
    private EditText enterUserTxt;
    private Button epSearch;
    private String userId, username;
    private long groupId;
    private RequestQueue requestQueue;

    private static final String BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_proposals);

        recyclerView = findViewById(R.id.proposalsRecyclerView);

        epSearch = findViewById(R.id.epSearch);

        requestQueue = Volley.newRequestQueue(this);


        SharedPreferences sharedPreferences3 = getSharedPreferences("groupInfo", MODE_PRIVATE);
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        userId = sharedPreferences.getString("userID", "0");
        //username = sharedPreferences.getString("username", "0");
        groupId = sharedPreferences3.getLong("groupID", 0);

        adapter = new ProposalAdapter(proposals, userId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        epSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchProposals();

            }
        });
    }

    private void fetchProposals() {
        String url = "http://coms-3090-009.class.las.iastate.edu:8080/event/request/" + groupId + "/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    proposals.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            EventRequest event = new EventRequest(obj); // Your constructor to parse JSON
                            proposals.add(event);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Failed to load proposals", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }
}