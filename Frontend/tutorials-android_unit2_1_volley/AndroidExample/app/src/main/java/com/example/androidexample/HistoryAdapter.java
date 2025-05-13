package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * This HistoryAdapter class is a Recyclerview adapter for displaying and managing
 * a list of previously attended events in the application.It combines events into a list.
 *
 * @author Lauren Kwon
 *
 */
    public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
        /** list of events the user previously attended */
        private List<Event> historyList;

    /**
     * Constructor for the HistoryEvent class
     * @param pastEventList A list of Event objects representing the previously attended events.
     */
    public HistoryAdapter(List<Event> pastEventList){
        this.historyList = pastEventList;
    }

    /**
     * The ViewHolder class holds the view references for a single item in the Recyclerview.
     */
    public static class HistoryViewHolder extends RecyclerView.ViewHolder{
        /** TextView of title and eventDate of the previously attended event */
        TextView tvTitle, tvEventDate;

        /**
         * Constructor for the HistoryViewHolder
         * @param itemView The view for a single item in the RecyclerView
         */
        public HistoryViewHolder(View itemView){
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
        }
    }

    /**
     * called when the RecycleView needs a new ViewHolder to be created
     * It inflates the layout for each individual event item.
     * @param parent The parent view group into which the new view will be added.
     * @param viewType The view type of the new View.
     *
     * @return A new instance of HistoryViewHolder
     */
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_event, parent, false);
        return new HistoryViewHolder(view);
    }

    /**
     * called to bind data to the view holder for a specific event item in the RecyclerView.
     * It updates the views with the even's title and date.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position){
        Event event = historyList.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvEventDate.setText("Time: " + event.getEventTime() +"-" + event.getEndTime());
    }

    /**
     * Returns the total number of events in the history list.
     * @return the number of items in the history list.
     */
    @Override
    public int getItemCount(){
        return historyList.size();
    }

}
