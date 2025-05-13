package com.example.androidexample;

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

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProposalAdapter extends RecyclerView.Adapter<ProposalAdapter.ViewHolder> {
    private List<EventRequest> proposals;
    private String userId;

    public ProposalAdapter(List<EventRequest> proposals, String userId) {
        this.proposals = proposals;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ProposalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.proposal_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProposalAdapter.ViewHolder holder, int position) {
        EventRequest proposal = proposals.get(position);
        holder.title.setText(proposal.getTitle());
        holder.time.setText(proposal.getEventTime());
        holder.description.setText(proposal.getDescription());

        holder.approve.setOnClickListener(v -> {
            String url = "http://coms-3090-009.class.las.iastate.edu:8080/event/request/" + proposal.getRequestId() + "/" + userId;
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> Toast.makeText(v.getContext(), "Approved", Toast.LENGTH_SHORT).show(),
                    error -> Toast.makeText(v.getContext(), "Failed to approve", Toast.LENGTH_SHORT).show());
            Volley.newRequestQueue(v.getContext()).add(request);
        });

        holder.delete.setOnClickListener(v -> {
            String url = "http://coms-3090-009.class.las.iastate.edu:8080/event/request/" + proposal.getRequestId() + "/" + userId;
            StringRequest request = new StringRequest(Request.Method.DELETE, url,
                    response -> Toast.makeText(v.getContext(), "Deleted", Toast.LENGTH_SHORT).show(),
                    error -> Toast.makeText(v.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());
            Volley.newRequestQueue(v.getContext()).add(request);
        });
    }

    @Override
    public int getItemCount() {
        return proposals.size();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, time, description;
        Button approve, edit, delete;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.proposalTitle);
            time = itemView.findViewById(R.id.proposalTime);
            description = itemView.findViewById(R.id.proposalDescription);
            approve = itemView.findViewById(R.id.btnApprove);
            delete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
