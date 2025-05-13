package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;

/**
 * This LoginActivity class allows users to input their username, password to login.
 *
 * @author Lauren Kwon
 *
 */

public class LoginActivity extends AppCompatActivity{
    /** EditText for username */
    public EditText usernameInput;  // define username edittext variable
    /** EditText for password */
    public EditText passwordInput;  // define password edittext variable
    /** Button for login */
    public Button loginButton;         // define login button variable
    /** Button for signup */
    private Button backButton;        // define signup button variable
    /** Button for reset */
    private Button resetButton;
    /** current user's username */
    private static String currUser;

    /** postman URL for backend mock server. */
    private static final String LOGIN_URL_MOCK = "https://ba467358-30be-429e-97db-5838a34a70cc.mock.pstmn.io/login";
    /** Actual backend URL for the server. */
    private static final String BACKEND_BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080";
    /** URL for backend local server. */
    private static final String LOCALHOST_BASE_URL = "http://10.0.2.2:8080";
    /** login URL that extends the backend server */
    private static final String LOGIN_URL = BACKEND_BASE_URL+"/user/login";
    private static final String LOGIN_URL2 = LOCALHOST_BASE_URL +"/user/login";

    /**
     * Initializes the login activity and sets up the UI elements and event listeners.
     * @param savedInstanceState if the activity is re-activated after being shut down,
     *                                 this Bundle will contain the data that is most updated.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);            // link to Login activity XML

        /* initialize UI elements */
        usernameInput = findViewById(R.id.login_username_edt);
        passwordInput = findViewById(R.id.login_password_edt);

        loginButton = findViewById(R.id.login_login_btn);    // link to login button in the Login activity XML
        backButton = findViewById(R.id.login_back_btn);  // link to signup button in the Login activity XML
        resetButton = findViewById(R.id.login_reset_btn);

        /* click listener on login button pressed */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();

            }
        });
        /* click listener on signup button pressed */
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when signup button is pressed, use intent to switch to Signup Activity */
                //has to change this part as well
                Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
                startActivity(intent);  // go to SignupActivity
            }
        });


        /* click listener on reset button pressed */
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, VerificationActivity.class);
                startActivity(intent);  // go to VerificationActivity
            }
        });
    }

    /**
     * Logs in the user by sending POST request to the backend server.
     */
    private void loginUser(){
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
//        String username = "testuser1";
//        String password = "thisisright";
            currUser = username;

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject requestBody = new JSONObject();
            try{
                requestBody.put("name", username);
                requestBody.put("password", password);

                Log.d("Login Request", requestBody.toString());
            } catch(JSONException e){
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    LOGIN_URL,
                    requestBody,
                    new Response.Listener<JSONObject>(){
                        @Override
                        public void onResponse(JSONObject response){
                            try {
                                if (response.has("success") && response.getInt("success") == 1) {
                                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                    String userID = response.getString("userId");

                                    //Saving username and userID to utilize in the future activities.
                                    SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("username", username);
                                    editor.putString("userID",userID);
                                    editor.apply();
                                    Log.d("SharedPreferences", "Saved username: " + sharedPreferences.getString("username", "Not found"));
                                    Log.d("SharedPreferences", "Saved userID: " + sharedPreferences.getString("userID", "Not found"));
                                    Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(LoginActivity.this,"Login Failed.", Toast.LENGTH_SHORT).show();
                                    Log.d("LoginDebug", "Toast should show: Login Failed");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(LoginActivity.this, "Unexpected response format", Toast.LENGTH_SHORT).show();
                            }
                            Log.d("Server Response", response.toString());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                int statusCode = error.networkResponse.statusCode;
                                String errorData = new String(error.networkResponse.data);
                                Log.e("Login Error", "Code: " + statusCode + ", Response: " + errorData);

                                if (statusCode == 401) {
                                    Toast.makeText(LoginActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();
                                } else if (statusCode == 400) {
                                    Toast.makeText(LoginActivity.this, "Username and password are required.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Network error. No response.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Authorization", "Bearer YOUR_ACCESS_TOKEN");
                headers.put("Content-Type", "application/json");
                    return headers;
                }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
//                params.put("param1", "value1");
//                params.put("param2", "value2");
                    return params;
                }

            };

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(jsonObjectRequest);

        }

    /**
     * retrieves the current logged-in user.
     *
     * @return The current logged-in user's username
     */
    public static String getCurUser(){
            return currUser;
        }
    }
