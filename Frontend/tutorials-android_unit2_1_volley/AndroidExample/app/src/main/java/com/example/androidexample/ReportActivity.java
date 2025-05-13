package com.example.androidexample;

import android.annotation.SuppressLint;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportActivity extends AppCompatActivity {
    /** Textview to click the date of the event. */
    private TextView tvInstruction1, tvInstruction2, tvInstruction3, tvResult;
    private Button deleteMessageBtn;
    private Button banUserBtn;
    private Button unbanUserBtn;
    private Button backBtn;
    EditText etChatID, etBanUsername, etBanReason, etUnbanUsername;
    /** Actual backend URL for the server. */
    private static final String BACKEND_BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080";
    /** URL for backend local server. */
    private static final String BACKEND_BASE_URL2 = "http://10.0.2.2:8080";
    /** events URL that extends the backend server */
    private static final String CHAT_URL = BACKEND_BASE_URL+"/chat/";

    /** Current user's ID number */
    private long currentUserID;
    /** Current user's group ID number */
    private long currentUserGroupID;
    /** Event ID number of the selected one */
    private long currentEventGroupID;
    /** Current user's role in the specific group. Either user, moderator, admin */
    private long currentUserRole;
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
        setContentView(R.layout.activity_report);

        SharedPreferences sharedPreferences1 = getSharedPreferences("eventInfo", MODE_PRIVATE);
        currentEventGroupID = sharedPreferences1.getLong("groupID", 0);

        SharedPreferences sharedPreferences2 = getSharedPreferences("userInfo", MODE_PRIVATE);
        currentUserRole = sharedPreferences2.getLong("userRole", 0);
        String currentUserIDstr = sharedPreferences2.getString("userID", "0");
        currentUserID = Long.parseLong(currentUserIDstr);

        tvInstruction1 = findViewById(R.id.tvInstruction1);
        tvInstruction2 = findViewById(R.id.tvInstruction2);
        tvInstruction3 = findViewById(R.id.tvInstruction3);
        tvResult = findViewById(R.id.tvResult);

        deleteMessageBtn = findViewById(R.id.deleteMessageBtn);
        banUserBtn = findViewById(R.id.banUserBtn);
        unbanUserBtn = findViewById(R.id.unbanUserBtn);

        etChatID = findViewById(R.id.etChatID);
        etBanUsername = findViewById(R.id.etBanUsername);
        etUnbanUsername = findViewById(R.id.etUnbanUsername);
        etBanReason = findViewById(R.id.etBanReason);
        backBtn = findViewById(R.id.goBackBtn2);


        //initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        deleteMessageBtn.setOnClickListener(v ->{
            String chatIdInput = etChatID.getText().toString().trim();
            if (chatIdInput.isEmpty()){
                tvResult.setText("emptyChatID");
                Toast.makeText(this, "Please enter a chatID", Toast.LENGTH_SHORT).show();
                return;
            }
            //tvResult.setText("deleteMessage");
            long chatID = Long.parseLong(chatIdInput);
            deleteMessage(currentEventGroupID, currentUserID, chatID);
        });
        banUserBtn.setOnClickListener(v -> {
            String banUsernameInput = etBanUsername.getText().toString().trim();
            String banReasonInput = etBanReason.getText().toString();
            if (banUsernameInput.isEmpty()){
                tvResult.setText("emptyUsername");
                Toast.makeText(this, "Please enter a username of the user to ban", Toast.LENGTH_SHORT).show();
                return;
            }
            if (banReasonInput.isEmpty()){
                tvResult.setText("emptyBanReason");
                Toast.makeText(this, "Please enter a reason for banning the user", Toast.LENGTH_SHORT).show();
                return;
            }
            //tvResult.setText("banUser");
            ban(currentEventGroupID, currentUserID, banUsernameInput, banReasonInput);
        });
        unbanUserBtn.setOnClickListener(v -> {
            String unbanUsernameInput = etUnbanUsername.getText().toString().trim();
            if (unbanUsernameInput.isEmpty()){
                tvResult.setText("emptyUsername");
                Toast.makeText(this, "Please enter a username of the user to ban", Toast.LENGTH_SHORT).show();
                return;
            }
            tvResult.setText("unbanUser");
            unban(currentEventGroupID, currentUserID, unbanUsernameInput);
        });
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ReportActivity.this, EventChatActivity.class);
            startActivity(intent);
        });


    }

    private void deleteMessage(long groupID, long managerID, long chatID) {
        String url = CHAT_URL  + groupID + "/" + managerID + "/" + chatID;

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
            Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show();
                    tvResult.setText("deleteMessage");
            },
                error -> {
                   // Toast.makeText(this, "Failed to delete message", Toast.LENGTH_SHORT).show();
                    Log.e("DeleteMessage", "Error: " + error.toString());
                });

        requestQueue.add(request);
    }

    private void ban(long groupID, long managerID, String username, String reason) {
        String url = CHAT_URL + groupID + "/" + managerID + "/" + username + "/"+ reason;

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
            Toast.makeText(this, "User Banned", Toast.LENGTH_SHORT).show();
                    tvResult.setText("Ban User");
            },
                error -> {
                   // Toast.makeText(this, "Failed to ban user", Toast.LENGTH_SHORT).show();
                    Log.e("Ban User", "Error: " + error.toString());
                });

        requestQueue.add(request);
    }

    private void unban(long groupID, long managerID, String username) {
        String url = CHAT_URL + "unban/"+groupID + "/" + managerID + "/" + username;

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
            Toast.makeText(this, "User Unbanned", Toast.LENGTH_SHORT).show();
                    tvResult.setText("Unban User");},
                error -> {
                    Toast.makeText(this, "Failed to unban user", Toast.LENGTH_SHORT).show();
                    Log.e("Unban User", "Error: " + error.toString());
                });

        requestQueue.add(request);
    }


}

