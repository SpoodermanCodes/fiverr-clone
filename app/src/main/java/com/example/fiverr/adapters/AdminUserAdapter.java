package com.example.fiverr.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fiverr.R;
import com.example.fiverr.models.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.AdminUserViewHolder> {

    private List<User> users = new ArrayList<>();
    private final OnAdminUserActionListener listener;

    public interface OnAdminUserActionListener {
        void onToggleStatus(User user);
        void onDeleteUser(User user);
    }

    public AdminUserAdapter(OnAdminUserActionListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new AdminUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class AdminUserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUsername, tvEmail, tvStatus, tvId;
        private final Button btnToggle, btnDelete;

        AdminUserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvAdminUserName);
            tvEmail = itemView.findViewById(R.id.tvAdminUserEmail);
            tvStatus = itemView.findViewById(R.id.tvAdminUserStatus);
            tvId = itemView.findViewById(R.id.tvAdminUserId);
            btnToggle = itemView.findViewById(R.id.btnToggleStatus);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }

        void bind(User user) {
            tvUsername.setText(user.getUsername());
            tvEmail.setText(user.getEmail());
            tvId.setText("ID: " + user.getId());

            boolean isActive = "active".equals(user.getStatus());
            tvStatus.setText(isActive ? "Active" : "Dormant");
            tvStatus.setTextColor(itemView.getContext().getColor(
                    isActive ? R.color.colorStatusActive : R.color.colorStatusDormant));

            btnToggle.setText(isActive ? "Deactivate" : "Activate");
            btnToggle.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            itemView.getContext().getColor(
                                    isActive ? R.color.colorStatusDormant : R.color.colorStatusActive)));
            btnToggle.setTextColor(itemView.getContext().getColor(R.color.white));
            btnToggle.setOnClickListener(v -> {
                if (listener != null) listener.onToggleStatus(user);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteUser(user);
            });
        }
    }
}
