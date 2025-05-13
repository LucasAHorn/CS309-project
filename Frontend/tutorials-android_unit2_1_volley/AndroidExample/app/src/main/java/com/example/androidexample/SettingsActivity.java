package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String DELETE_USERS_URL = "http://coms-3090-009.class.las.iastate.edu:8080/";
    private Button btnBack, btnDeleteAcc, btnDeleteAccYes, btnDeleteAccNo;
    private TextView deleteConfirmText;
    private EditText usernameInput, emailInput;
    private String username, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBack = findViewById(R.id.btnBack);
        btnDeleteAcc = findViewById(R.id.btnDeleteAcc);
        btnDeleteAccNo = findViewById(R.id.btnDeleteAccNo);
        btnDeleteAccYes = findViewById(R.id.btnDeleteAccYes);
        deleteConfirmText = findViewById(R.id.deleteConfirmText);
        usernameInput = findViewById((R.id.usernameInput));
        emailInput = findViewById((R.id.emailInput));

        btnBack.setOnClickListener(this);
        btnDeleteAcc.setOnClickListener(this);
        btnDeleteAccNo.setOnClickListener(this);
        btnDeleteAccYes.setOnClickListener(this);

        deleteConfirmText.setVisibility(View.INVISIBLE);
        btnDeleteAccNo.setVisibility(View.INVISIBLE);
        btnDeleteAccYes.setVisibility(View.INVISIBLE);
        usernameInput.setVisibility(View.INVISIBLE);
        emailInput.setVisibility(View.INVISIBLE);

    }
    private void deleteRequest() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject requestBody = new JSONObject();
            try {
                requestBody.put("name", username);
                requestBody.put("email", email);
            Log.d("Delete Request", requestBody.toString());
        } catch (
        JSONException e) {
            e.printStackTrace();
        }

        Log.d("Pre Object Request", "Before request creation");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.DELETE,
            DELETE_USERS_URL + "user/delete",
            requestBody,
            new Response.Listener<JSONObject>(){
                @Override
                public void onResponse(JSONObject response){
                    try {
                        requestBody.put("name", username);
                        requestBody.put("email", email);
                        if (response.has("success") && response.getBoolean("success")) {
                            Toast.makeText(SettingsActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Delete Failed. Incorrect credentials.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SettingsActivity.this, "Unexpected response format", Toast.LENGTH_SHORT).show();
                    }
                    Log.d("Server Response", response.toString());
                }
            },
            new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String errorData = new String(error.networkResponse.data);
                        Log.e("Delete Error", "Code: " + statusCode + ", Response: " + errorData);
                    }
                    Log.d("errorListener","inErrorListener");
                }


            }

    );

    }

    @Override
    public void onClick(View v){
        int id = v.getId();
        if (id == R.id.btnBack) {
            startActivity(new Intent(SettingsActivity.this, HomeScreenActivity.class));
        } else if (id == R.id.btnDeleteAcc) {
            deleteConfirmText.setVisibility(View.VISIBLE);
            btnDeleteAccNo.setVisibility(View.VISIBLE);
            btnDeleteAccYes.setVisibility(View.VISIBLE);
            btnDeleteAcc.setVisibility(View.INVISIBLE);
            btnBack.setVisibility(View.INVISIBLE);
            usernameInput.setVisibility(View.VISIBLE);
            emailInput.setVisibility(View.VISIBLE);
        } else if (id == R.id.btnDeleteAccYes){
            username = usernameInput.toString();
            email = emailInput.toString();
            deleteRequest();

            startActivity(new Intent(SettingsActivity.this, MainActivity2.class));
            startActivity(new Intent(SettingsActivity.this, SignupActivity.class));

        } else if (id == R.id.btnDeleteAccNo) {
            deleteConfirmText.setVisibility(View.INVISIBLE);
            btnDeleteAccNo.setVisibility(View.INVISIBLE);
            btnDeleteAccYes.setVisibility(View.INVISIBLE);
            btnDeleteAcc.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.VISIBLE);
            usernameInput.setVisibility(View.INVISIBLE);
            emailInput.setVisibility(View.INVISIBLE);
        }

    }
}
