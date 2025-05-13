package com.example.androidexample;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This EventAdapter class is a Recyclerview adapter for displaying and managing
 * a list of events in the application.It combines events into a list and provides
 * features like modifying, deleting, RSVPing the events.
 *
 * @author Lauren Kwon
 *
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    /** The List of events that is connected by the event adapter */
    private List<Event> eventList;
    /** The backend server URL */
    private String backendURL;
    /** The Set of RSVPd event IDs */
    private Set<Long> rsvpdEventIDs;
    /** current user's ID number */
    private long currentUserID;
    /** current User's Group ID number */
    private long currentUserGroupID;
    /** current User's Role in the specific group. Specifies whether it is user, moderator, or admin */
    private long currentUserRole;
    private long pollID;

    /**
     * Constructor that initiates the adapter with a list of events, backendURL,
     * userID, and a set of RSVPd eventIDs.
     * @param eventList the list of events that will be connected by this adapter.
     * @param backendURL the URL of backend server.
     * @param currentUserID the user's ID that will distinguish which events to be connected.
     * @param currentUserGroupID the user's groupID
     * @param currentUserRole the user's role in the groupID (user/moderator/admin)
     * @param rsvpdEventIDs the user's rsvped events
     */
    public EventAdapter(List<Event> eventList, String backendURL, long currentUserID, long currentUserGroupID, long currentUserRole, Set<Long> rsvpdEventIDs) {
        this.eventList = eventList;
        this.backendURL = backendURL;
        this.currentUserID = currentUserID;
        this.currentUserGroupID = currentUserGroupID;
        this.currentUserRole = currentUserRole;
        this.rsvpdEventIDs = rsvpdEventIDs;
    }

    /**
     * Updates the set of RSVPd eventIDs and refreshes the RecyclerView
     * @param rsvpdEventIDs the user's rsvpd events
     */
    public void updateRSVPdEvents(Set<Long> rsvpdEventIDs){
        this.rsvpdEventIDs = rsvpdEventIDs;
        notifyDataSetChanged(); //notify the adapter that data has changed so the UI can be updated.
    }

    /**
     * ViewHolder class to hold references to the UI elements of each event in the RecyclerView.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvEventDate, tvLocation, tvDescription, tvCapacity, tvRSVPList;
        Button btnRSVP;
        Button btnExpand;
        Button btnModify;
        Button btnDelete;
        Button btnChatEnter;
        Button btnParticipants;
        Button btnEnterPoll;
        Button btnCreatePoll;
        LinearLayout expandableLayout;

        /**
         * Constructor for initiating the ViewHolder with the event item view.
         * @param itemView refers to single item in RecyclerView which is a single event.
         */
        public EventViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            btnRSVP = itemView.findViewById(R.id.btnRSVP);
            btnExpand = itemView.findViewById(R.id.btnExpand);
            btnModify = itemView.findViewById(R.id.btnModify);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnChatEnter = itemView.findViewById(R.id.btnChatEnter);
            btnParticipants = itemView.findViewById(R.id.btnParticipants);
            btnEnterPoll = itemView.findViewById(R.id.btnEnterPoll);
            btnCreatePoll = itemView.findViewById(R.id.btnCreatePoll);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
        }
    }

    /**
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.(not used because we have only one view type here)
     *
     * @return A new EventViewHolder by inflating the item layout.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate the layout for a single event item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
       //return new EventViewHolder by inflating the item layout
        return new EventViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder at the specific position in the RecyclerView
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     *
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        if (eventList == null || position < 0 || position >= eventList.size()) {
            Log.e("EventAdapter", "Invalid eventList or position: " + position);
            return;
        }

        //Get the event at the given position
        Event event = eventList.get(position);
        //Set event details to the TextViews.
        holder.tvTitle.setText(event.getTitle());
        holder.tvEventDate.setText("Time: " + event.getEventTime() +"-" + event.getEndTime());
        holder.tvLocation.setText("Location: " +event.getLocation());
        holder.tvCapacity.setText("Capacity: " +event.getCapacity());
        holder.tvDescription.setText("Details \n" +event.getDescription());

        //Initially hide the event description
        holder.expandableLayout.setVisibility(View.GONE);

        //Shows the event details when the expand button is clicked.
        holder.btnExpand.setOnClickListener(v -> {
            if (holder.expandableLayout.getVisibility() == View.GONE){
                holder.expandableLayout.setVisibility(View.VISIBLE);
                holder.btnExpand.setText("Hide Details");
            }else{
                holder.expandableLayout.setVisibility(View.GONE);
                holder.btnExpand.setText("Show Details");
            }
        });

        if(currentUserRole == 0){
            holder.btnModify.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnParticipants.setVisibility(View.GONE);
            holder.btnCreatePoll.setVisibility(View.GONE);
        }


        //Set up event modification and deletion button click listeners.
        holder.btnModify.setOnClickListener(v -> showModifyDialog(holder.itemView, event));
        holder.btnDelete.setOnClickListener(v -> showDeleteDialog(holder.itemView, event.getEventID(), position));
        holder.btnParticipants.setOnClickListener(v -> {
            fetchEventGroupID(v.getContext(), event.getEventID());
            Intent intent = new Intent(v.getContext(), ParticipantsActivity.class);
            v.getContext().startActivity(intent);
        });
        holder.btnEnterPoll.setOnClickListener(v -> {
            fetchEventGroupID(v.getContext(), event.getEventID());
            long eventID = event.getEventID();
            Context context = v.getContext();

            fetchEventGroupID(context, eventID);

            fetchPollID(context, eventID, new PollIDCallback() {
                @Override
                public void onPollIDFetched(long pollID) {SharedPreferences sharedPreferences1 = v.getContext().getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
            SharedPreferences sharedPreferences2 = v.getContext().getSharedPreferences("pollInfo", Context.MODE_PRIVATE);
            SharedPreferences sharedPreferences3 = v.getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            SharedPreferences sharedPreferences4 = v.getContext().getSharedPreferences("userVoteInfo", Context.MODE_PRIVATE);
            Intent intent = new Intent(context, PollResultsActivity.class);
            context.startActivity(intent);
                }
            });
        });
        holder.btnCreatePoll.setOnClickListener(v -> {
            fetchEventGroupID(v.getContext(), event.getEventID());

            SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
            long groupID = sharedPreferences.getLong("groupID",0);
            long eventID = sharedPreferences.getLong("eventID",0);
            Intent intent;
            intent = new Intent(v.getContext(), AdminPollActivity.class);
            intent.putExtra("groupID",groupID);
            intent.putExtra("eventID",eventID);
            v.getContext().startActivity(intent);
        });

        //Set up the RSVP button onClickListener
        //check if the user has already RSVPd to the event
        if (rsvpdEventIDs.contains(event.getEventID())){
            //if already RSVPd, show the "Undo RSVP" dialog
            holder.btnRSVP.setText("Registered");
            holder.btnRSVP.setBackgroundColor(Color.RED);
            holder.btnChatEnter.setOnClickListener(v ->{
                fetchEventGroupID(v.getContext(), event.getEventID());
                SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
                long groupID = sharedPreferences.getLong("groupID",0);
                Intent intent = new Intent(v.getContext(), EventChatActivity.class);
                intent.putExtra("groupID",groupID);
                v.getContext().startActivity(intent);
            });
        }else{
            holder.btnRSVP.setText("RSVP");
            holder.btnRSVP.setBackgroundColor(Color.BLUE);
            holder.btnChatEnter.setOnClickListener(v ->{
                Toast.makeText(v.getContext(), "You need to RSVP first to enter groupChat.", Toast.LENGTH_SHORT).show();
            });
        }
        holder.btnRSVP.setOnClickListener(v ->{
            Log.d("EventAdapter", "RSVP Button Clicked for Event: " + event.getEventID());
            if (rsvpdEventIDs.contains(event.getEventID())){
                //if already RSVPd, show the "Undo RSVP" dialog
                Log.d("EventAdapter", "Event already RSVPed. Showing Undo RSVP Dialog");
                showUndoRSVPDialog(holder.itemView, event, holder.btnRSVP);
            }else{
                Log.d("EventAdapter", "Event not RSVPed. Showing RSVP Dialog");
                showRSVPDialog(holder.itemView, event, holder.btnRSVP);
            }
        });

    }

    /**
     * Shows a dialog to confirm the user's interaction to RSVP to an event.
     * @param itemView The parent view of the dialog
     * @param event The event the user is RSVPing to
     * @param btnRSVP The RSVP button to update.
     */
    public void showRSVPDialog(View itemView, Event event, Button btnRSVP) {
        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
        builder.setTitle("Join Event")
                .setMessage("Do you want to join this event?")
                .setPositiveButton("Yes", (dialog, which) -> sendRSVPRequest(itemView, event, true, btnRSVP))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Shows a dialog to confirm the user's interaction to undo RSVP to an event
     * The user can either confirm or cancel the action.
     *
     * @param itemView The parent view of the dialog.
     * @param event The event the user is undo RSVP for.
     * @param btnRSVP The RSVP button will be updated based on the user's action.
     */
    public void showUndoRSVPDialog(View itemView, Event event, Button btnRSVP) {
        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
        builder.setTitle("Leave Event")
                .setMessage("Do you want to undo your RSVP?")
                .setPositiveButton("Yes", (dialog, which) -> sendRSVPRequest(itemView, event, false, btnRSVP))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Sends an RSVP request to the backend server to either join or leave an event.
     * The request is made via PUT method if joining, or Delete if undoing the RSVP.
     * @param itemView The parent view for context, used for Toast messages.
     * @param event The event to which the RSVP is being made or undone.
     * @param isJoining A boolean indicating whether the user is joining(true) or leaving(false) the event.
     * @param btnRSVP The RSVP button that will be updated with the current state(RSVP/Registered)
     */
    //RSVP and Undo-RSVP Methods
    public void sendRSVPRequest(View itemView, Event event, boolean isJoining, Button btnRSVP) {;
        long userID = currentUserID;
        String url = isJoining
                ? backendURL + "/event/RSVP/" + event.getEventID() + "/" + userID
                : backendURL + "/event/removeRSVP/" + event.getEventID() + "/" + userID;

        int method = isJoining ? Request.Method.PUT : Request.Method.DELETE;

        StringRequest request = new StringRequest(method, url,
                response -> {
                    String message = isJoining ? "Joined event" : "RSVP undone";
                    Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                    if (isJoining){
                        btnRSVP.setText("Registered");
                        btnRSVP.setBackgroundColor(Color.RED);
                        rsvpdEventIDs.add(event.getEventID());
                    } else{
                            btnRSVP.setText("RSVP");
                            btnRSVP.setBackgroundColor(Color.BLUE);
                            rsvpdEventIDs.remove(event.getEventID());
                    }

                },
                error -> {
                    Toast.makeText(itemView.getContext(), "Failed to update RSVP", Toast.LENGTH_SHORT).show();
                    Log.e("RSVP", "Error: " + error.getMessage());
                });
        VolleySingleton.getInstance(itemView.getContext()).getRequestQueue().add(request);
    }


    /**
     * Shows a dialog to modify the details of an existing event
     * updates the selected event by letting the user change event's info
     *
     * @param itemView The view associated with the event item that is selected.
     * @param event The event object containing the details to be edited.
     */
    public void showModifyDialog(View itemView, Event event){
        //Initializing an instance of AlertDialog.Builder to use pop-up dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
        //Inflate the layout for the update event dialog.
        LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
        View dialogView = inflater.inflate(R.layout.dialog_update_event, null);
        builder.setView(dialogView);

        //Find the EditText fields in the dialog layout
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etLocation = dialogView.findViewById(R.id.etLocation);
        EditText etCapacity = dialogView.findViewById(R.id.etCapacity);
        EditText etEventTime = dialogView.findViewById(R.id.etEventTime);
        EditText etDuration = dialogView.findViewById(R.id.etDuration);

        //Set current event details in the EditText fields
        etTitle.setText(event.getTitle());
        etDescription.setText(event.getDescription());
        etLocation.setText(event.getLocation());
        etCapacity.setText(String.valueOf(event.getCapacity()));
        etEventTime.setText(event.getEventDateStr());  // Format:
        etDuration.setText(String.valueOf(event.getDurationInMinutes()));

        // SetOnclickListener to open DateTime picker when event date is clicked.
        etEventTime.setOnClickListener(v -> {
            showDateTimePickerDialog(etEventTime);
        });

        // Set up the "Save" button behavior
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Get the modified values from the EditText fields
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String capacityStr = etCapacity.getText().toString().trim();
            String eventDate = etEventTime.getText().toString().trim();
            String durationInMinutesStr = etDuration.getText().toString().trim();

            //Checks if all fields are filled out
            if (title.isEmpty() || description.isEmpty() || location.isEmpty() || capacityStr.isEmpty() || eventDate.isEmpty()) {
                //show a toast if any field is empty
                Toast.makeText(itemView.getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                //parse capacity into integer and update event details
                try{
                    int capacity = Integer.parseInt(capacityStr);
                    event.setCapacity(capacity);
                    int duration = Integer.parseInt(durationInMinutesStr);
                    event.setDurationInMinutes(duration);
                } catch(NumberFormatException e){
                    Toast.makeText(itemView.getContext(), "Capacity must be a number", Toast.LENGTH_SHORT).show();
                    return;
                }
                event.setTitle(title);
                event.setDescription(description);
                event.setLocation(location);
                event.setEventTime(eventDate);


                //update the event on the backend.
                updateEvent(itemView.getContext(), event);
            }
        });
        //set up the "Cancel" button behavior
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        //show the dialog
        builder.show();
    }


    /**
     * This method makes PUT request to update an event on the backend using VOLLEY
     * The event details are sent in a JSON object, and if the update is successful,
     * the event list is updated, and the RecyclerView is refreshed.
     * @param context The context of the activity or application , used to show Toast messages.
     * @param event The event object containing the updated details to be sent to the server.
     */
    public void updateEvent(Context context, Event event) {
//        String url = backendURL + "/event/edit/" + event.getEventID();  // Update the URL to the specific event ID
        String url = backendURL + "/event/edit/" + currentUserID;  // Update the URL to the specific event ID
//        String url =  "http://10.0.2.2:8080/event/edit/" + currentUserID;  // Update the URL to the specific event ID
        //create JSON object with key-value pairs that match the expected request body for the API
        //each property from the event object is added to the JSON request.
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("eventId", event.getEventID());
            jsonBody.put("title", event.getTitle());
            jsonBody.put("eventTime", event.getEventTime());
            jsonBody.put("location", event.getLocation());
            jsonBody.put("description", event.getDescription());
            jsonBody.put("capacity", event.getCapacity());
            jsonBody.put("durationInMinutes", event.getDurationInMinutes());
            jsonBody.put("eventGroupId", event.getGroupID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //sends the PUT request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                response -> {
                    Toast.makeText(context, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    // update the event list and refresh the RecyclerView
                    int position = eventList.indexOf(event);
                    if (position != -1) {
                        eventList.set(position, event);
                        notifyItemChanged(position);
                    }
                },
                error -> {
                    Toast.makeText(context, "Failed to update event", Toast.LENGTH_SHORT).show();
                    Log.e("UpdateEvent", "Error: " + error.getMessage());
                });
        //adds the request to the VOLLEY queue
        Volley.newRequestQueue(context).add(request);
    }

    /**
     * Displays a confirmation dialog before deleting an event
     * @param itemView the view from which the dialog is used
     * @param eventID the ID of the event to be deleted.
     * @param position the position of the event in the list.
     */
    public void showDeleteDialog(View itemView, long eventID, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
        builder.setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", (dialog, which) -> deleteEvent(itemView, eventID, position))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Deletes the selected event from the backend and updates the event list in RecyclerView
     * @param itemView the view from which the deletion is happening.
     * @param eventID the ID of the event to be deleted.
     * @param position the position of the event in the list.
     */
    public void deleteEvent(View itemView, long eventID, int position) {
        // API endpoint for deleting the event
        String url = backendURL + "/event/" + eventID + "/" + currentUserID;

        //Create a DELETE request using Volley
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    // If successful, remove the event from the list and update RecyclerView
                    eventList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(itemView.getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    // If deletion fails, show an error message
                    Toast.makeText(itemView.getContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
                    Log.e("DeleteEvent", "Error: " + error.getMessage());
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Add any headers if required, like authorization tokens or content type
                Map<String, String> headers = new HashMap<>();
                return headers;
            }
        };


        // Add the request to the Volley request queue
        Volley.newRequestQueue(itemView.getContext()).add(request);
    }

    /**
     * Returns the total number of events in the list
     *
     * @return the size of the event list
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Displays a DatePickerDialog followed by a TimePickerDialog for the eventDate.
     * The selected date and time are formatted and set in the given EditText field.
     *
     * @param etEventDate the EditText where the selected date and time will be displayed.
     */
    public void showDateTimePickerDialog(EditText etEventDate) {
        // Get the current date and time
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Show DatePickerDialog to select a date
        DatePickerDialog datePickerDialog = new DatePickerDialog(etEventDate.getContext(), (view, yearSelected, monthOfYear, dayOfMonth) -> {
            String selectedDate = (monthOfYear + 1) + "/" + dayOfMonth + "/" + yearSelected;
            // After selecting date, show TimePickerDialog to select time
            TimePickerDialog timePickerDialog = new TimePickerDialog(etEventDate.getContext(), (view1, hourOfDay, minuteSelected) -> {
                LocalTime selectedTime = LocalTime.of(hourOfDay, minuteSelected);
                String formattedTime = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                // Combine the selected date and time into a single string in "MM/dd/yyyy HH:mm" format
                String dateTime = selectedDate + " " + formattedTime;
                //Set the formatted date-time in the EditText
                etEventDate.setText(dateTime); // Set the event date with time
            }, hour, minute, true);

            timePickerDialog.show(); // Show time picker after selecting date
        }, year, month, day);

        datePickerDialog.show(); // Show date picker dialog
    }

    /**
     * Fetches event details such as groupID from the Backend
     * @param context The context from which the method is called.
     * @param eventID The ID of the event to fetch details for.
     */
    private void fetchEventGroupID(Context context, long eventID){
        String url = backendURL + "/event/" + eventID;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try{
                        String eventTitle = response.getString("title");
                        long eventDuration = response.getLong("durationInMinutes");
                        JSONObject eventGroup = response.getJSONObject("eventGroup");
                        long groupID = eventGroup.getLong("groupId");
                        Log.d("FetchEventGroup", "Group ID: " + groupID);
                        SharedPreferences sharedPreferences = context.getSharedPreferences("eventInfo", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("eventTitle", eventTitle);
                        editor.putLong("eventDuration", eventDuration);
                        editor.putLong("eventID", eventID);
                        editor.apply();
                        Log.d("FetchEventID", "event ID: " + eventID);


                    } catch(JSONException e){
                        e.printStackTrace();
                        Toast.makeText(context, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("FetchEventInfo", "Error fetching event: " +error.getMessage());
                    Toast.makeText(context, "Failed to load event info", Toast.LENGTH_SHORT).show();
                });
        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    private void fetchPollID(Context context, long eventID, PollIDCallback callback){
        String url = backendURL + "/event/" + eventID;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject poll = response.getJSONObject("poll");
                        long fetchedPollID = 0;

                        if (poll != null) {
                            fetchedPollID = poll.getLong("pollId");
                            String pollTitle = poll.getString("prompt");
                            JSONArray pollOptionsArray = poll.getJSONArray("pollOptions");
                            String multiChoice = poll.getString("multiChoice");

                            SharedPreferences sharedPref = context.getSharedPreferences("pollInfo", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            String pollOptionsString = pollOptionsArray.toString();
                            editor.putLong("pollID", fetchedPollID);
                            editor.putString("pollTitle", pollTitle);
                            editor.putString("pollOptions", pollOptionsString);
                            editor.putString("multiChoice", multiChoice);
                            editor.apply();

                            Log.d("PollInfo", "pollID = " + fetchedPollID);
                        }

                        // Notify callback
                        callback.onPollIDFetched(fetchedPollID);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Poll not created yet.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("FetchPollInfo", "Error fetching poll: " + error.getMessage());
                    Toast.makeText(context, "Failed to load poll info", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    public interface PollIDCallback {
        void onPollIDFetched(long pollID);
    }




}


