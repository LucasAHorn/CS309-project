package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeScreenActivity extends AppCompatActivity implements View.OnClickListener{


    private Button historyBtn, btnGroups, btnBack, btnStudyBuddy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        historyBtn = findViewById(R.id.historyBtn);
        //studyTrackBtn = findViewById(R.id.studyTrackBtn);
        btnGroups = findViewById(R.id.groupbtn);
        btnBack = findViewById(R.id.goBackbtn1);
        btnStudyBuddy = findViewById(R.id.studyBuddyBtn);

        historyBtn.setOnClickListener(this);
        //studyTrackBtn.setOnClickListener(this);
        btnGroups.setOnClickListener(this);
        btnStudyBuddy.setOnClickListener(this);
        btnBack.setOnClickListener(this);


    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.groupbtn) {
            startActivity(new Intent(HomeScreenActivity.this, ClassPageActivity.class));
        } else if (id == R.id.historyBtn) {
            startActivity(new Intent(HomeScreenActivity.this, HistoryActivity.class));
        }
        else if (id == R.id.goBackbtn1) {
            startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
        }
        else if (id == R.id.studyBuddyBtn) {
            startActivity(new Intent(HomeScreenActivity.this, StudyBuddyActivity.class));
        }
    }

}
