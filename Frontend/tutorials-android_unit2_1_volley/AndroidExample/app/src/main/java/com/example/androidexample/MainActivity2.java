package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

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


public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {

    private TextView textView;
    private static final String BACKEND_BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080";

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
                Intent intent = new Intent(MainActivity2.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        /* click listener on signup button pressed */
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(MainActivity2.this, SignupActivity.class);
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
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_login_main) {
            startActivity(new Intent(MainActivity2.this, LoginActivity.class));
        } else if (id == R.id.btn_signup_main) {
            startActivity(new Intent(MainActivity2.this, SignupActivity.class));
        } else if (id == R.id.btn_end_main) {
            finishAffinity();
        }
    }
}