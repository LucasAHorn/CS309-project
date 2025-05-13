package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class GroupAvailabilityActivity extends AppCompatActivity {
    private EditText sunTxt, monTxt, tueTxt, wedTxt, thuTxt, friTxt, satTxt;
    private Button eventBtn, availabilityBtn, backBtn;
    private EventsActivity event;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_planner_result);

        sunTxt = findViewById(R.id.gpSunTimeTxt);
        monTxt = findViewById(R.id.gpMonTimeTxt);
        tueTxt = findViewById(R.id.gpTuesTimeTxt);
        wedTxt = findViewById(R.id.gpWedTimeTxt);
        thuTxt = findViewById(R.id.gpThurTimeTxt);
        friTxt = findViewById(R.id.gpFriTimeTxt);
        satTxt = findViewById(R.id.gpSatTimeTxt);

        availabilityBtn = findViewById(R.id.gpAvailableBtn);
        eventBtn = findViewById(R.id.gpEventBtn);
        backBtn = findViewById(R.id.gpBackBtn);

        event = new EventsActivity();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupAvailabilityActivity.this, DebugGroupActivity.class);
                startActivity(intent);
            }
        });
        availabilityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupAvailabilityActivity.this, StudyPlannerPossibleTimesActivity.class);
                startActivity(intent);
            }
        });
        eventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupAvailabilityActivity.this, StudyPlannerPossibleTimesActivity.class);
                startActivity(intent);
            }
        });
    }
    private int getAvailability(){
        return 9;
    }
}
