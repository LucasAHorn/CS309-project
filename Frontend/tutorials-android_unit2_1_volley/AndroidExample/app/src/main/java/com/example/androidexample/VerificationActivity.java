package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This VerificationActivity class is connected screen from ResetActivity where it checks
 * security questions to reset password.
 *
 * @author Lauren Kwon
 *
 */
public class VerificationActivity extends AppCompatActivity {
    /** EditText for username, city, petName for security questions */
    private EditText usernameInput, cityInput, petInput;
    /** Button for going back and next */
    private Button verificationBackBtn, verificationNextBtn;
    /** mock server backend URL */
    private static final String VERIFICATION_URL_MOCK = "https://ba467358-30be-429e-97db-5838a34a70cc.mock.pstmn.io/users/security/{user_id}";
    /** real server backend URL */
    private static final String BACKEND_BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080";
    /** URL for verification */
    private static final String VERIFICATION_URL = BACKEND_BASE_URL + "/user/security-questions";

    /**
     * Intializes the activity, sets up the UI components and the button click listeners.
     * @param savedInstanceState a bundle containing the activity's previously saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        usernameInput = findViewById(R.id.verification_username_edt);
        cityInput = findViewById(R.id.verification_city_edt);
        petInput = findViewById(R.id.verification_pet_edt);

        verificationBackBtn = findViewById(R.id.verification_back_btn);
        verificationNextBtn = findViewById(R.id.verification_next_btn);

        verificationNextBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String city = cityInput.getText().toString();
                String pet = petInput.getText().toString();

                SharedPreferences sharedPreferences = getSharedPreferences("userSecurity", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putString("city", city);
                editor.putString("pet", pet);
                editor.apply();
                Intent intent = new Intent(VerificationActivity.this, ResetActivity.class);
                startActivity(intent);  // go to SignupActivity

//                verifyUser(email,city,pet);
            }
        });
        verificationBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(VerificationActivity.this, LoginActivity.class);
                startActivity(intent);  // go to SignupActivity
            }
        });
    }

    /**
     * Verifies the user (But this method is not being used for now)
     * @param username the current username
     * @param city the current user's born city
     * @param petName the current user's first pet name
     */
    private void verifyUser2(String username, String city, String petName) {

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("city", city);
            requestBody.put("petName", petName);
            Log.d("Verification Request", requestBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                VERIFICATION_URL,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("success") && response.getInt("success") == 1) {
                                Toast.makeText(VerificationActivity.this, "Verification Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(VerificationActivity.this, ResetActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(VerificationActivity.this, "Verification Failed. Incorrect credentials.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(VerificationActivity.this, "Unexpected response format", Toast.LENGTH_SHORT).show();
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
                            Log.e("Verification Error", "Code: " + statusCode + ", Response: " + errorData);

                            if (statusCode == 401) {
                                Toast.makeText(VerificationActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();
                            } else if (statusCode == 400) {
                                Toast.makeText(VerificationActivity.this, "Username and password are required.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VerificationActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(VerificationActivity.this, "Network error. No response.", Toast.LENGTH_SHORT).show();
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
}
