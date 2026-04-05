package com.example.fiverr.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fiverr.R;
import com.example.fiverr.adapters.AdminGigAdapter;
import com.example.fiverr.models.Gig;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageGigsFragment extends Fragment implements AdminGigAdapter.OnAdminGigActionListener {

    private AdminGigAdapter adapter;
    private DatabaseReference gigsRef;
    private List<Gig> gigs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_gigs, container, false);

        gigsRef = FirebaseDatabase.getInstance().getReference("gigs");
        RecyclerView rv = view.findViewById(R.id.rvManageGigs);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminGigAdapter(this);
        rv.setAdapter(adapter);

        loadGigs();
        return view;
    }

    private void loadGigs() {
        gigsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gigs.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Gig gig = ds.getValue(Gig.class);
                    if (gig != null) {
                        gig.setGigId(ds.getKey());
                        gigs.add(gig);
                    }
                }
                adapter.setGigs(gigs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onEditGig(Gig gig) {
        // Show edit dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Gig");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 8);

        EditText etTitle = new EditText(requireContext());
        etTitle.setHint("Title");
        etTitle.setText(gig.getTitle());
        layout.addView(etTitle);

        EditText etPrice = new EditText(requireContext());
        etPrice.setHint("Price");
        etPrice.setText(String.valueOf(gig.getPrice()));
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etPrice);

        EditText etDesc = new EditText(requireContext());
        etDesc.setHint("Description");
        etDesc.setText(gig.getDescription());
        layout.addView(etDesc);

        builder.setView(layout);

        builder.setPositiveButton(getString(R.string.btn_save), (d, w) -> {
            String newTitle = etTitle.getText().toString().trim();
            String newPriceStr = etPrice.getText().toString().trim();
            String newDesc = etDesc.getText().toString().trim();

            if (!newTitle.isEmpty() && gig.getGigId() != null) {
                gigsRef.child(gig.getGigId()).child("title").setValue(newTitle);
            }
            if (!newPriceStr.isEmpty() && gig.getGigId() != null) {
                try {
                    double newPrice = Double.parseDouble(newPriceStr);
                    gigsRef.child(gig.getGigId()).child("price").setValue(newPrice);
                } catch (NumberFormatException ignored) {}
            }
            if (!newDesc.isEmpty() && gig.getGigId() != null) {
                gigsRef.child(gig.getGigId()).child("description").setValue(newDesc);
            }

            if (getView() != null) {
                Snackbar.make(getView(), "Gig updated", Snackbar.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(getString(R.string.btn_cancel), null);
        builder.show();
    }

    @Override
    public void onDeleteGig(Gig gig) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_delete))
                .setPositiveButton(getString(R.string.btn_delete), (d, w) -> {
                    if (gig.getGigId() != null) {
                        gigsRef.child(gig.getGigId()).removeValue();
                        if (getView() != null) {
                            Snackbar.make(getView(), getString(R.string.item_deleted),
                                            Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }
}
