package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class DebugGroupActivity extends AppCompatActivity {
    private Button startPoll, joinPoll, groupAvail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_page);

        startPoll = findViewById(R.id.groupStartBtn);
        joinPoll = findViewById(R.id.groupJoinPollBtn);
        groupAvail = findViewById(R.id.btnGroupAvail);

        joinPoll.setVisibility(View.VISIBLE);
        startPoll.setVisibility(View.VISIBLE);
        groupAvail.setVisibility(View.VISIBLE);

        joinPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(DebugGroupActivity.this, PollResultsActivity.class);
//                startActivity(intent);
            }
        });
        startPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DebugGroupActivity.this, AdminPollActivity.class);
                startActivity(intent);
            }
        });
        groupAvail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DebugGroupActivity.this, StudyPlannerPossibleTimesActivity.class);
                startActivity(intent);
            }
        });
    }
}