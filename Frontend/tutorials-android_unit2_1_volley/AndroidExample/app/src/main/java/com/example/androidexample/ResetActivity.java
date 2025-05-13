package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

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
/**
 * This ResetActivityActivity class allows users to reset their password.
 * They need to input username, security questions.
 *
 * @author Lauren Kwon
 *
 */
public class ResetActivity extends AppCompatActivity {
    /** EditText for reset password and confirm */
    private EditText resetPasswordEdt, resetConfirmEdt;
    /** Button for reset and login */
    private Button resetResetBtn, resetBackBtn;
    /** current username, city, petName for security questions check */
    private String savedUsername, savedCity, savedPet;
    /** backend base URL for mock server */
    private static final String MOCK_RESET_URL = "https://ba467358-30be-429e-97db-5838a34a70cc.mock.pstmn.io/reset";
    /** backend base URL for actual server */
    private static final String BACKEND_BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080";
    /** URL for reset activity */
    private static final String RESET_URL = BACKEND_BASE_URL+"/user/reset-password";
    /**
     * initiates the activity and sets up the UI and event listeners for the reset password process.
     *  @param savedInstanceState if the activity is re-activated after being shut down,
     *                                      this Bundle will contain the data that is most updated.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        SharedPreferences sharedPreferences = getSharedPreferences("userSecurity", MODE_PRIVATE);
        savedUsername = sharedPreferences.getString("username","");
        savedCity = sharedPreferences.getString("city","");
        savedPet = sharedPreferences.getString("pet","");

        resetPasswordEdt = findViewById(R.id.reset_password_edt);
        resetConfirmEdt = findViewById(R.id.reset_confirm_edt);

        resetResetBtn = findViewById(R.id.reset_password_btn);
        resetBackBtn = findViewById(R.id.reset_back_btn);

        resetResetBtn.setOnClickListener(v -> resetPassword());
        resetBackBtn.setOnClickListener(
                v -> startActivity(new Intent(ResetActivity.this, VerificationActivity.class)));
    }

    /**
     * Resets the user's password by sending a request to the backend server.
     * The request includes the username, security question answers, amd new password.
     */
    private void resetPassword() {
        String newPassword = resetPasswordEdt.getText().toString().trim();
        String confirmPassword = resetConfirmEdt.getText().toString().trim();

        Log.d("ResetPassword", "Attempting password reset");
        Log.d("ResetPassword", "Username: " + savedUsername);
        Log.d("ResetPassword", "City: " + savedCity);
        Log.d("ResetPassword", "Pet: " + savedPet);
        Log.d("ResetPassword", "New Password: " + newPassword);
        Log.d("ResetPassword", "Confirm Password: " + confirmPassword);

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter New Password", Toast.LENGTH_LONG).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getApplicationContext(), "Password don't match", Toast.LENGTH_LONG).show();
            return;
        }

        //JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", savedUsername);
            jsonBody.put("birthCity", savedCity);
            jsonBody.put("firstPetName", savedPet);
            jsonBody.put("newPassword", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        Log.d("ResetPassword", "Request Body: " + jsonBody.toString());

        //send the POST request using Volley
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                RESET_URL,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("ResetPassword", "Response from backend: " + response.toString());
                            int success = response.getInt("success");
                            String message = response.getString("message");
                            if (success==1) {
                                Log.d("ResetPassword", "Password reset successful: " + message);
                                Toast.makeText(ResetActivity.this, "Password Changed Successfully", Toast.LENGTH_SHORT).show();
                                //navigate to Login Activity
                                Intent intent = new Intent(ResetActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Log.d("ResetPassword", "Password reset failed: " + message);
                                Toast.makeText(ResetActivity.this, "Reset Failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("ResetPassword", "JSON parsing error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
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

        RequestQueue queue = Volley.newRequestQueue(ResetActivity.this);
        queue.add(request);
        //add request to Volley queue
        //VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);

    }
}
//};
//        //add request to Volley queue
//        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
