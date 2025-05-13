package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Objects;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;



public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private Button submitBtn, backBtn;
    private EditText userNameInput, userEmailInput, userPasswordInput, userPasswordConfirmInput, userSQ1Input, userSQ2Input;
    String userName, testResult;
    private String userEmail;
    private String userPassword;
    private String userPasswordConfirm;
    private String userSQ1;
    private String userSQ2;

    private static final String CREATE_USERS_URL2 = "http://10.0.2.2:8080/user";
    private static final String CREATE_USERS_URL = "http://coms-3090-009.class.las.iastate.edu:8080/user";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        submitBtn = findViewById(R.id.submitButton);
        backBtn = findViewById(R.id.signupBackBtn);

        userNameInput = findViewById(R.id.userNameInput);
        userEmailInput = findViewById(R.id.userEmailInput);
        userPasswordInput = findViewById(R.id.userPasswordInput);
        userPasswordConfirmInput = findViewById(R.id.userPasswordConfirmInput);
        userSQ1Input = findViewById(R.id.userSQ1Input);
        userSQ2Input = findViewById(R.id.userSQ2Input);

        submitBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        String[] methods = new String[]{"DELETE", "POST"};
        testResult = "";
    }
    private void signUpRequest() {
        userName = userNameInput.getText().toString();
        userPassword = userPasswordInput.getText().toString();
        userEmail = userEmailInput.getText().toString();
        userSQ1 = userSQ1Input.getText().toString();
        userSQ2 = userSQ2Input.getText().toString();

        RequestQueue queue = Volley.newRequestQueue(this);

        if (userName.isEmpty() || userPassword.isEmpty() || userEmail.isEmpty() || userSQ1.isEmpty() || userSQ2.isEmpty()) {
            Toast.makeText(SignupActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            testResult = "Please enter username and password";
            return;
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", userName);
            requestBody.put("password", userPassword);
            requestBody.put("email", userEmail);
            requestBody.put("birthCity", userSQ1);
            requestBody.put("firstPetName", userSQ2);
            Log.d("Sign up Request", requestBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("Pre Object Request", "Before request creation");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                CREATE_USERS_URL + "/create",
                requestBody,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){
                        try {
                            if (response.has("success") && response.getInt("success") == 1) {
                                Toast.makeText(SignupActivity.this, "Sign up Successful", Toast.LENGTH_SHORT).show();
                                testResult = "Sign up Successful";
                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SignupActivity.this, "Sign up Failed. Try again.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SignupActivity.this, "Unexpected response format", Toast.LENGTH_SHORT).show();
                        }
                    }

                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String errorData = new String(error.networkResponse.data);
                            Log.e("Sign up Error", "Code: " + statusCode + ", Response: " + errorData);
                        }
                        Log.d("errorListener","inErrorListener");
                    }


                }

        );


//        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);


    }


    @Override
    public void onClick(View v){
        int id = v.getId();
        if (id == R.id.signupBackBtn) {
            startActivity(new Intent(SignupActivity.this, MainActivity2.class));
        } else if (id == R.id.submitButton) {
            userName = userNameInput.getText().toString();
            userEmail = userEmailInput.getText().toString();
            userPassword = userPasswordInput.getText().toString();
            userPasswordConfirm = userPasswordConfirmInput.getText().toString();
            userSQ1 = userSQ1Input.getText().toString();
            userSQ2 = userSQ2Input.getText().toString();
            if (Objects.equals(userPassword, userPasswordConfirm)) {
                signUpRequest();
//                Intent intent = new Intent(SignupActivity.this, HomeScreenActivity.class);
//                startActivity(intent);
            } else {
                Toast.makeText(SignupActivity.this, "Password and password confirmation must match", Toast.LENGTH_SHORT).show();
                testResult = "Password and password confirmation must match";
            }
        }
    }
}
