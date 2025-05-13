package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;


/**
 * This StudyTrackChatActivity class is a websocket that allows users to track their event timer
 * and chat with other members
 *
 */
public class EventChatActivity extends AppCompatActivity implements WebSocketListener{
    /** Button for send and start timer */
    private Button sendBtn, startTimerBtn;
    private Button reportBtn;
    private Button backBtn;
    /** EditText for sending message */
    private EditText msgEtx;
    /** TextView for message and timer */
    private TextView msgTv, timerTv;
    /** Handler for timer */
    private Handler timerHandler = new Handler();
    /** event's duration in minutes */
    private long eventDurationMinutes;
    /** remaining time in seconds */
    private int remainingSeconds;
    /** boolean to check if the timer is running */
    private boolean isTimerRunning = false;
    /** the event's title */
    private String eventTitle;
    /** the event's title to be displayed */
    private String displayTitle;
    /** the current user's ID number */
    private String userID;
    /** the current event's group ID */
    private long groupID;
    private long eventID;
    private long userRole;

    /**
     * Initiates the activity and sets up the UI and event listeners.
     * Retrieves event and user information from SharedPreferences and sets up the WebSocket
     * @param savedInstanceState a Bundle containing saved state from Login activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_chat);


        SharedPreferences sharedPreferences1 = getSharedPreferences("eventInfo", MODE_PRIVATE);
        eventDurationMinutes = sharedPreferences1.getLong("eventDuration", 0);
        remainingSeconds = (int) (eventDurationMinutes * 60);
        eventTitle = sharedPreferences1.getString("eventTitle", "Study");
        displayTitle = eventTitle + " Group Chat";
        groupID = sharedPreferences1.getLong("groupID", 0);
        eventID = sharedPreferences1.getLong("eventID", 0);

        SharedPreferences sharedPreferences2 = getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sharedPreferences2.getString("userID","0");
        userRole = sharedPreferences2.getLong("userRole",0);
        Log.d("UserLevel", "Parsed user level: " + userRole);

        fetchChatHistory();

        String serverUrl = "ws://coms-3090-009.class.las.iastate.edu:8080/ws/" + groupID + "?userId=" + userID;
       // String serverUrl2 = "ws://coms-3090-009.class.las.iastate.edu:8080/ws/chat/" + groupID + "?userId=" + userID;
        String serverUrl2 = "ws://10.0.2.2:8080/ws/chat/" + groupID + "?userId=" + userID+ "&eventId=" + eventID;

        Log.d("WebSocket", "Connecting to WebSocket at: " + serverUrl);
        // Establish WebSocket connection and set listener
        WebSocketManager.getInstance().connectWebSocket(serverUrl);



        /* initialize UI elements */
        sendBtn = (Button) findViewById(R.id.sendBtn);
        msgEtx = (EditText) findViewById(R.id.msgEdt);
        msgTv = (TextView) findViewById(R.id.tx1);
        timerTv = findViewById(R.id.timerTv);
        startTimerBtn = findViewById(R.id.startTimerBtn);
        msgTv.setText(displayTitle);
        reportBtn = findViewById(R.id.reportBtn);
        backBtn = findViewById(R.id.goBackBtn3);

        if (userRole == 0){
            reportBtn.setVisibility(View.GONE);
        }

        /* connect this activity to the websocket instance */
        WebSocketManager.getInstance().setWebSocketListener(EventChatActivity.this);

        /* send button listener */
        sendBtn.setOnClickListener(v -> {
            try {
                String message_text = msgEtx.getText().toString();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("message", message_text);
                jsonObject.put("userId", userID);
                jsonObject.put("groupId", groupID);
                jsonObject.put("eventId", eventID);
                // send JSON message
                WebSocketManager.getInstance().sendMessage(jsonObject.toString());

                //clear input
                msgEtx.setText("");
            } catch (Exception e) {
                Log.d("ExceptionSendMessage:", e.getMessage().toString());
            }


        });

        reportBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EventChatActivity.this, ReportActivity.class);
            startActivity(intent);
        });
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EventChatActivity.this, EventsActivity.class);
            startActivity(intent);
        });

        /* start timer button listener */
        startTimerBtn.setOnClickListener(v -> startTimer());
        updateTimerUI();
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


    public void fetchChatHistory() {
        SharedPreferences sharedPreferences1 = getSharedPreferences("eventInfo", MODE_PRIVATE);
        groupID = sharedPreferences1.getLong("groupID", 0);
        Log.d("groupID", "Parsed user grupID: " + groupID);

        SharedPreferences sharedPreferences2 = getSharedPreferences("userInfo", MODE_PRIVATE);
        userRole = sharedPreferences2.getLong("userRole", 0);
        Log.d("UserLevel", "Parsed user level2: " + userRole);

        String url = "http://coms-3090-009.class.las.iastate.edu:8080/chat/" + groupID+ "/" +eventID+"/" +userID + "/all";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject messageObj = response.getJSONObject(i);
                            String username = messageObj.getString("username");
                            String text = messageObj.getString("message");
                            String msgTime = messageObj.getString("msgTime");
                            String msgID = messageObj.getString("messageId");

                            // Format time
                            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                            Date date;
                            try {
                                date = originalFormat.parse(msgTime);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                date = new Date();
                            }
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            String formattedTime = timeFormat.format(date);

                            if (userRole == 0) {
                                // Compose display message
                                String displayMessage = "[" + formattedTime + "] " + username + ": " + text;

                                // Append to TextView
                                String currentText = msgTv.getText().toString();
                                msgTv.setText(currentText + "\n" + displayMessage);
                            }
                            else{
                                    // Compose display message
                                    String displayMessage = "[" + formattedTime + "] [id: "+msgID+"]" + username + ": " + text;

                                    // Append to TextView
                                    String currentText = msgTv.getText().toString();
                                    msgTv.setText(currentText + "\n" + displayMessage);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> Log.e("Volley", "Failed to fetch chat history", error)
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }




    /**
     * Starts the timer if it is not already running.
     * The timer updates every second, calling the udpateTimeRunnable method.
     */
    public void startTimer(){
        if (!isTimerRunning){
            isTimerRunning = true;
            timerHandler.postDelayed(updateTimerRunnable, 1000); //update every 1 second

        }
    }

    /**
     * Runnable that updates the timer
     */
    public Runnable updateTimerRunnable = new Runnable(){
        @Override
        public void run(){
            if (remainingSeconds > 0){
                remainingSeconds--;
                updateTimerUI();
                timerHandler.postDelayed(this,1000);
            } else{
                //timer reaches 0, stop updating
                isTimerRunning = false;

            }

        }
    };

    /**
     * updates the timer UI to display the remaining time in minutes and seconds.
     */
    public void updateTimerUI(){
        //convert remaining seconds to minutes and seconds for display
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String timeText = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
        timerTv.setText("Time Left: " + timeText);
    }

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
//            String s = msgTv.getText().toString();
//            msgTv.setText(s + "\n"+message);
            try{
                JSONObject jsonMessage = new JSONObject(message);
                String username = jsonMessage.getString("username");
                String text = jsonMessage.getString("message");
                String msgTime = jsonMessage.getString("msgTime");
                String msgID = jsonMessage.getString("messageId");

                // Use SimpleDateFormat to parse the time
                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
                Date date = null;
                try{
                    date = originalFormat.parse(msgTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = new Date();
                }

                // Format the date to get the "HH:mm" portion
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String formattedTime = timeFormat.format(date);

                //new display of message
                String displayMessage;
                if (userRole == 0) {
                    displayMessage = "[" + formattedTime + "] " + username + ": " + text;
                } else {
                    displayMessage = "[" + formattedTime + "] [id: " + msgID + "]" + username + ": " + text;
                }
                //append the formatted message to the existing messages.
                String currentText = msgTv.getText().toString();
                msgTv.setText(currentText + "\n" + displayMessage);
            } catch(JSONException e){
                String currentText = msgTv.getText().toString();
                msgTv.setText(currentText + "\n" + message);
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
            String s = msgTv.getText().toString();
            msgTv.setText(s + "---\nconnection closed by " + closedBy + "\nreason: " + reason);
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