package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class ClassCreationActivity extends AppCompatActivity{
    private Button btnBack, btnSubmit;
    private EditText txtTitle, txtDescription;
    private LoginActivity login;
    private String title, description, user, currentUserIDstr;
    private Long currentUserID;
    private int prefilled;
    private static final String BASE_URL = "http://coms-3090-009.class.las.iastate.edu:8080/";
    //private static final String BASE_URL = "http://10.0.2.2:8080/";
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_creation);

        requestQueue = Volley.newRequestQueue(this);

        sharedPreferences2 = getSharedPreferences("userInfo", MODE_PRIVATE);
        SharedPreferences sharedPreferences4 = getSharedPreferences("prefillInfo", MODE_PRIVATE);
        prefilled = sharedPreferences4.getInt("prefill", 0);
        currentUserIDstr = sharedPreferences2.getString("userID", "0");
        currentUserID = Long.parseLong(currentUserIDstr);

        btnBack = findViewById(R.id.backbtn);
        btnSubmit = findViewById(R.id.submitbtn);
        txtTitle = findViewById(R.id.groupNameInput);
        txtDescription = findViewById(R.id.groupDescriptionInput);

        title = "";
        description = "";
        login = new LoginActivity();
        user = login.getCurUser();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClassCreationActivity.this, ClassPageActivity.class);
                startActivity(intent);
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = txtTitle.getText().toString();
                description = txtDescription.getText().toString();
                if(description.equalsIgnoreCase("") || title.equalsIgnoreCase("")){
                    Toast.makeText(ClassCreationActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
                postGroup();
                startActivity(new Intent(ClassCreationActivity.this, ClassPageActivity.class));
            }
        });
    }

    private void postGroup(){
        String postURL = BASE_URL+"group/create/" + currentUserIDstr;
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("title", title);
            jsonBody.put("description", description);
        } catch(JSONException e){
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postURL, jsonBody,
                response -> {
                    //Toast.makeText(this, "Group created", Toast.LENGTH_SHORT).show();
                    try {
                        if (response.has("success") && response.getInt("success") == 1) {
                            Toast.makeText(ClassCreationActivity.this, "Group created", Toast.LENGTH_SHORT).show();
                            //testResult = "Sign up Successful";
                            if(prefilled == 0){
                                Intent intent = new Intent(ClassCreationActivity.this, HomeScreenActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Intent intent = new Intent(ClassCreationActivity.this, GroupEditorActivity.class);
                                startActivity(intent);
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> Toast.makeText(this, "Group not created", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(request);
    }
}
