package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This StudyTrackChatActivity class is a websocket that allows users to track their event timer
 * and chat with other members
 *
 */
public class PollResultsActivity extends AppCompatActivity implements WebSocketListener{
    private Button removeBtn, backBtn, option1Btn, option2Btn, option3Btn, option4Btn;
    private TextView pollTitle, multipleChoiceAvailability, topAnswer;
    private String userID, multiChoice;
    private long eventID, pollID, userRole, groupID;
    private String opt1, opt2, opt3, opt4;
    private Long result1, result2, result3, result4;
    private boolean hasVoted;


    /**
     * Initiates the activity and sets up the UI and event listeners.
     * Retrieves event and user information from SharedPreferences and sets up the WebSocket
     * @param savedInstanceState a Bundle containing saved state from Login activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_poll_results);

        /* initialize UI elements */
        option1Btn = findViewById(R.id.option1Btn);
        option2Btn = findViewById(R.id.option2Btn);
        option3Btn = findViewById(R.id.option3Btn);
        option4Btn = findViewById(R.id.option4Btn);
        backBtn = findViewById(R.id.pollBackBtn);

        pollTitle = findViewById(R.id.pollTitle);
        multipleChoiceAvailability = findViewById(R.id.multiChoiceAvailability);
        topAnswer = findViewById(R.id.topAnswer);
        removeBtn = findViewById(R.id.removeBtn);

        SharedPreferences sharedPreferences1 = getSharedPreferences("eventInfo", MODE_PRIVATE);
        eventID = sharedPreferences1.getLong("eventID", 0);
        groupID = sharedPreferences1.getLong("groupID", 0);

        SharedPreferences sharedPreferences2 = getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sharedPreferences2.getString("userID","0");
        userRole = sharedPreferences2.getLong("userRole",0);

        SharedPreferences sharedPreferences3 = getSharedPreferences("pollInfo", MODE_PRIVATE);
        pollID = sharedPreferences3.getLong("pollID", 0);
        String optionsJson = sharedPreferences3.getString("pollOptions", new JSONArray(Arrays.asList("A", "B", "C", "D")).toString());
        try {
            JSONArray optionsArray = new JSONArray(optionsJson);

            option1Btn.setText(optionsArray.getString(0));
            opt1 = optionsArray.getString(0);
            option2Btn.setText(optionsArray.getString(1));
            opt2 = optionsArray.getString(1);
            option3Btn.setText(optionsArray.getString(2));
            opt3 = optionsArray.getString(2);
            option4Btn.setText(optionsArray.getString(3));
            opt4 = optionsArray.getString(3);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load poll options", Toast.LENGTH_SHORT).show();
        }
        SharedPreferences sharedPreferences = getSharedPreferences("userVoteInfo", MODE_PRIVATE);
        hasVoted = sharedPreferences.getBoolean("hasVoted_" + pollID, false);

        String title = sharedPreferences3.getString("pollTitle", "Poll");
        pollTitle.setText(title);
        multiChoice = sharedPreferences3.getString("multiChoice", "false");

        if (multiChoice.equals("false")){
            multipleChoiceAvailability.setText("Choose Only One Option");
        }

        String serverUrl = "ws://coms-3090-009.class.las.iastate.edu:8080/ws/poll/" + pollID + "?userId=" + userID;

        Log.d("WebSocket", "Connecting to WebSocket at: " + serverUrl);
        // Establish WebSocket connection and set listener
        WebSocketManager.getInstance().connectWebSocket(serverUrl);

        /* connect this activity to the websocket instance */
        WebSocketManager.getInstance().setWebSocketListener(PollResultsActivity.this);

        option1Btn.setOnClickListener(v -> {
            vote(pollID, opt1);
        });
        option2Btn.setOnClickListener(v -> {
            vote(pollID, opt2);
        });
        option3Btn.setOnClickListener(v -> {
            vote(pollID, opt3);
        });
        option4Btn.setOnClickListener(v -> {
            vote(pollID, opt4);
        });
        removeBtn.setOnClickListener(v -> {
//            vote(pollID, re);
        });
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PollResultsActivity.this, EventsActivity.class);
            startActivity(intent);
        });

    }
    @Override
    protected void onPause() {
        super.onPause();

        // Disconnect the WebSocket when the activity is paused
        WebSocketManager.getInstance().disconnectWebSocket();
        Log.d("WebSocket", "WebSocket disconnected onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Optionally disconnect WebSocket when the activity is stopped
        WebSocketManager.getInstance().disconnectWebSocket();
        Log.d("WebSocket", "WebSocket disconnected onStop");
    }

    private void vote(Long n, String voteString){
        SharedPreferences sharedPreferences = getSharedPreferences("userVoteInfo", MODE_PRIVATE);
        if (multiChoice.equals("false")) {
            boolean hasVoted = sharedPreferences.getBoolean("hasVoted_" + pollID, false);

            if (hasVoted) {
                // If already voted, show a message and return
                Toast.makeText(this, "You have already voted", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", userID);
            jsonObject.put("groupId", groupID);
            jsonObject.put("pollId", pollID);
            jsonObject.put("voteString", voteString);
            String message = jsonObject.toString();
            Log.d("VotePayload", message);

            WebSocketManager.getInstance().sendMessage(message);

            // After voting, mark the user as voted if multipleChoice is false
            if (multiChoice.equals("false")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("hasVoted_" + pollID, true);  // FIXED
                editor.apply();
            }


            Toast.makeText(this, "Your vote has been submitted", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            Log.e("ExceptionSendMessage", "Error sending vote: " + e.getMessage());
            }
    }

//    private void removeVote(Long n, String voteString){
//        SharedPreferences sharedPreferences = getSharedPreferences("userVoteInfo", MODE_PRIVATE);
//        if (multiChoice.equals("false")) {
//            sharedPreferences.getBoolean("hasVoted_" + pollID, false);
//
//
//
//            if (hasVoted) {
//                // If already voted, show a message and return
//                Toast.makeText(this, "You have already voted", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }
//
//        try {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("userId", userID);
//            jsonObject.put("groupId", groupID);
//            jsonObject.put("pollId", pollID);
//            jsonObject.put("removeVote", voteString);
//            String message = jsonObject.toString();
//            Log.d("VotePayload", message);
//
//            WebSocketManager.getInstance().sendMessage(message);
//
//            // After voting, mark the user as voted if multipleChoice is false
//            if (multiChoice.equals("false")) {
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putBoolean("hasVoted", true);
//                editor.apply(); // Commit the change
//            }
//
//            Toast.makeText(this, "Your vote has been submitted", Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            Log.e("ExceptionSendMessage", "Error sending vote: " + e.getMessage());
//        }
//    }



    /**
     * Called when a message is received from the WebSocket.
     * This method ensures that UI updates happen on the main thread.
     */
    @Override
    public void onWebSocketMessage(String message) {
        /**
         * In Android, all UI-related operations must be performed on the main UI thread
         * to ensure smooth and responsive user interfaces. The 'runOnUiThread' method
         * is used to post a runnable to the UI thread's message queue, allowing UI updates
         * to occur safely from a background or non-UI thread.
         */
        runOnUiThread(() -> {
            try {
                JSONObject jsonMessage = new JSONObject(message);

                // Extract the message or response from the server (assuming "responseMessage" is the key)
               result1 = jsonMessage.optLong(opt1, -1);
               result2 = jsonMessage.optLong(opt2, -1);
               result3 = jsonMessage.optLong(opt3, -1);
               result4 = jsonMessage.optLong(opt4, -1);

                long max = Math.max(Math.max(result1, result2), Math.max(result3, result4));
                List<String> topAnswers = new ArrayList<>();
                if (result1 == max) topAnswers.add(opt1);
                if (result2 == max) topAnswers.add(opt2);
                if (result3 == max) topAnswers.add(opt3);
                if (result4 == max) topAnswers.add(opt4);

                topAnswer.setText("Top: " + String.join(", ", topAnswers));

                //noMultiVote = jsonMessage.optLong(opt4, -1);

//
//
//                // Show the response message in a Toast
//                Toast.makeText(PollResultsActivity.this, response1, Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
            }
        });
    }


    /**
     * Called when the WebSocket connection is closed.
     * Displays the closure reason in the TextView.
     *
     * @param code   The status code of the closure
     * @param reason The reason provided for closure
     */
    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
        });
    }

    /**
     * Called when the WebSocket connection is successfully opened.
     * @param handshakedata Information about the server handshake.
     */
    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        Log.d("WebSocket", "Connection opened: " + handshakedata.getHttpStatusMessage());
    }


    /**
     * Called when an error occurs with the WebSocket connection.
     * @param ex The exception that describes the error.
     */
    @Override
    public void onWebSocketError(Exception ex) {
        Log.e("WebSocket", "Error: ", ex);
    }
}