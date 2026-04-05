package com.example.fiverr.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fiverr.R;
import com.example.fiverr.models.Gig;

import java.util.ArrayList;
import java.util.List;

public class GigAdapter extends RecyclerView.Adapter<GigAdapter.GigViewHolder> {

    private List<Gig> gigs = new ArrayList<>();
    private final OnGigClickListener listener;

    public interface OnGigClickListener {
        void onGigClick(Gig gig);
    }

    public GigAdapter(OnGigClickListener listener) {
        this.listener = listener;
    }

    public void setGigs(List<Gig> gigs) {
        this.gigs = gigs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gig, parent, false);
        return new GigViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GigViewHolder holder, int position) {
        Gig gig = gigs.get(position);
        holder.bind(gig);
    }

    @Override
    public int getItemCount() {
        return gigs.size();
    }

    class GigViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvCategory, tvPrice, tvPostedBy, tvStatus;

        GigViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvGigTitle);
            tvCategory = itemView.findViewById(R.id.tvGigCategory);
            tvPrice = itemView.findViewById(R.id.tvGigPrice);
            tvPostedBy = itemView.findViewById(R.id.tvGigPostedBy);
            tvStatus = itemView.findViewById(R.id.tvGigStatus);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onGigClick(gigs.get(pos));
                }
            });
        }

        void bind(Gig gig) {
            tvTitle.setText(gig.getTitle());
            tvCategory.setText(gig.getCategory());
            tvPrice.setText(String.format("$%.0f", gig.getPrice()));
            tvPostedBy.setText(gig.getPostedByName());
            tvStatus.setText(gig.getStatus());

            if ("accepted".equals(gig.getStatus())) {
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.colorStatusPending));
            } else {
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.colorStatusActive));
            }
        }
    }
}
