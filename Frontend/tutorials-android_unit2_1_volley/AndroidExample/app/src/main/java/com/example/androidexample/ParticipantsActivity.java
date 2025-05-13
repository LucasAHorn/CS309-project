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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONException;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParticipantsActivity extends AppCompatActivity {
    /** Textview to click the date of the event. */
    private TextView tvEventTitle, tvParticipants;
    private Button backBtn4;

    /** Actual backend URL for the server. */
    private static final String BACKEND_BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080";
    /** URL for backend local server. */
    private static final String BACKEND_BASE_URL2 = "http://10.0.2.2:8080";
    /** events URL that extends the backend server */

    /** Current user's ID number */
    private long currentUserID;
    /** Current user's group ID number */
    private long currentEventID;
    /** Event ID number of the selected one */
    private long currentEventGroupID;
    /** Current user's role in the specific group. Either user, moderator, admin */
    private long currentUserRole;
    private String currentEventTitle;
    /** Request queue for volley requests */
    private RequestQueue requestQueue;

    /**
     * Initiates UI components, sets RecyclerView and fetches events from the server.
     * Sets up click listeners for the date picker and event creation button
     *
     * @param savedInstanceState if the activity is re-activated after being shut down,
     *                           this Bundle will contain the data that is most updated.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        SharedPreferences sharedPreferences1 = getSharedPreferences("eventInfo", MODE_PRIVATE);
        currentEventGroupID = sharedPreferences1.getLong("groupID", 0);
        currentEventID = sharedPreferences1.getLong("eventID", 0);
        currentEventTitle = sharedPreferences1.getString("eventTitle","");

        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventTitle.setText(currentEventTitle);
        tvParticipants = findViewById(R.id.tvParticipants);
        backBtn4 = findViewById(R.id.backBtn4);

        backBtn4.setOnClickListener(v -> {
            Intent intent = new Intent(ParticipantsActivity.this, EventsActivity.class);
            startActivity(intent);
        });


        //initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        fetchParticipants();

    }
    private void fetchParticipants() {
        String url = BACKEND_BASE_URL + "/event/participants/" + currentEventID;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            sb.append(response.getString(i)).append("\n");
                        } catch (JSONException e) {
                            Log.e("Participants", "JSON parsing error", e);
                        }
                    }
                    tvParticipants.setText(sb.toString().trim());
                },
                error -> {
                    Toast.makeText(this, "Failed to load participants", Toast.LENGTH_SHORT).show();
                    Log.e("Participants", "Volley error", error);
                }
        );

        requestQueue.add(jsonArrayRequest);
    }


}

