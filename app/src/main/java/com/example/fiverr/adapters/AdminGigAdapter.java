package com.example.fiverr.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fiverr.R;
import com.example.fiverr.models.Gig;

import java.util.ArrayList;
import java.util.List;

public class AdminGigAdapter extends RecyclerView.Adapter<AdminGigAdapter.AdminGigViewHolder> {

    private List<Gig> gigs = new ArrayList<>();
    private final OnAdminGigActionListener listener;

    public interface OnAdminGigActionListener {
        void onEditGig(Gig gig);
        void onDeleteGig(Gig gig);
    }

    public AdminGigAdapter(OnAdminGigActionListener listener) {
        this.listener = listener;
    }

    public void setGigs(List<Gig> gigs) {
        this.gigs = gigs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminGigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_gig, parent, false);
        return new AdminGigViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminGigViewHolder holder, int position) {
        holder.bind(gigs.get(position));
    }

    @Override
    public int getItemCount() {
        return gigs.size();
    }

    class AdminGigViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvCategory, tvPrice, tvPostedBy, tvStatus;
        private final Button btnEdit, btnDelete;

        AdminGigViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAdminGigTitle);
            tvCategory = itemView.findViewById(R.id.tvAdminGigCategory);
            tvPrice = itemView.findViewById(R.id.tvAdminGigPrice);
            tvPostedBy = itemView.findViewById(R.id.tvAdminGigPostedBy);
            tvStatus = itemView.findViewById(R.id.tvAdminGigStatus);
            btnEdit = itemView.findViewById(R.id.btnEditGig);
            btnDelete = itemView.findViewById(R.id.btnDeleteGig);
        }

        void bind(Gig gig) {
            tvTitle.setText(gig.getTitle());
            tvCategory.setText(gig.getCategory());
            tvPrice.setText(String.format("$%.0f", gig.getPrice()));
            tvPostedBy.setText(gig.getPostedByName());
            tvStatus.setText(gig.getStatus());

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditGig(gig);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteGig(gig);
            });
        }
    }
}
