package com.example.fiverr.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fiverr.R;
import com.example.fiverr.adapters.AdminUserAdapter;
import com.example.fiverr.db.DatabaseHelper;
import com.example.fiverr.models.User;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ManageUsersFragment extends Fragment implements AdminUserAdapter.OnAdminUserActionListener {

    private AdminUserAdapter adapter;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_users, container, false);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        RecyclerView rv = view.findViewById(R.id.rvManageUsers);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminUserAdapter(this);
        rv.setAdapter(adapter);

        loadUsers();
        return view;
    }

    private void loadUsers() {
        List<User> users = dbHelper.getAllUsers();
        adapter.setUsers(users);
    }

    @Override
    public void onToggleStatus(User user) {
        String newStatus = "active".equals(user.getStatus()) ? "dormant" : "active";
        dbHelper.updateUserStatus(user.getId(), newStatus);
        loadUsers();

        String msg = "active".equals(newStatus)
                ? getString(R.string.user_unbanned)
                : getString(R.string.user_banned);

        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(requireContext().getColor(R.color.colorPrimary))
                    .setTextColor(requireContext().getColor(R.color.colorOnPrimary))
                    .show();
        }
    }

    @Override
    public void onDeleteUser(User user) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_delete))
                .setPositiveButton(getString(R.string.btn_delete), (d, w) -> {
                    dbHelper.deleteUser(user.getId());
                    loadUsers();
                    if (getView() != null) {
                        Snackbar.make(getView(), getString(R.string.item_deleted),
                                        Snackbar.LENGTH_SHORT)
                                .show();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }
}
