package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.util.Log;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class UserPollActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textView;
    private static final String MOCK_API_URL = "http://coms-3090-009.class.las.iastate.edu:8080/";

    private Button loginBtn, signupBtn, endBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        textView = findViewById(R.id.textView);
        loginBtn = findViewById(R.id.btn_login_main);
        signupBtn = findViewById(R.id.btn_signup_main);
        endBtn = findViewById(R.id.btn_end_main);

        /* button click listeners */
        loginBtn.setOnClickListener(this);
        signupBtn.setOnClickListener(this);
        endBtn.setOnClickListener(this);

        /* click listener on login button pressed */
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(UserPollActivity.this, EventsActivity.class);
                startActivity(intent);
            }
        });

//        closebtn.setOnClickListener(v -> {
//            String message = answer1.getText().toString().trim();
//            // broadcast this message to the WebSocketService
//            // tag it with the key - to specify which WebSocketClient (connection) to send
//            // in this case: "chat1"
//            if (message.isEmpty()) {
//                Log.w(TAG, "sendBtn clicked but message was empty.");
//                return;
//            }
//            Intent intent = new Intent("SendWebSocketMessage");
//            intent.putExtra("key", "chat1");
//            intent.putExtra("message", answer1.getText().toString());
//            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//            Log.d(TAG, "Broadcast sent for message: " + message);
//            answer1.setText("");
//            Log.d(TAG, "Message input cleared.");
//        });

        /* click listener on signup button pressed */
        //signupBtn.setOnClickListener(this);
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(UserPollActivity.this, AdminPollActivity.class);
                startActivity(intent);
            }
        });

        /* click listener on signup button pressed */
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity(); //this ends the application
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketManager.getInstance().disconnectWebSocket();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_login_main) {
            startActivity(new Intent(UserPollActivity.this, LoginActivity.class));
        } else if (id == R.id.btn_signup_main) {
            startActivity(new Intent(UserPollActivity.this, AdminPollActivity.class));
        } else if (id == R.id.btn_end_main) {
            finishAffinity();
        }
    }
}
