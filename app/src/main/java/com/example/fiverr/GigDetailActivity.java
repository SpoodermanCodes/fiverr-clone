package com.example.fiverr;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fiverr.models.Gig;
import com.example.fiverr.utils.SessionManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GigDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvCategory, tvPrice, tvPostedBy, tvDescription, tvStatus, tvYourGig;
    private Button btnAccept;
    private DatabaseReference gigsRef;
    private SessionManager session;
    private String gigId;
    private Gig currentGig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gig_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);
        gigsRef = FirebaseDatabase.getInstance("https://fiverr-d61c9-default-rtdb.firebaseio.com").getReference("gigs");
        gigId = getIntent().getStringExtra("gigId");

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvPostedBy = findViewById(R.id.tvDetailPostedBy);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvYourGig = findViewById(R.id.tvYourGig);
        btnAccept = findViewById(R.id.btnAcceptGig);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnAccept.setOnClickListener(v -> acceptGig());

        if (gigId != null) {
            loadGigDetails();
        }
    }

    private void loadGigDetails() {
        gigsRef.child(gigId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentGig = snapshot.getValue(Gig.class);
                if (currentGig != null) {
                    currentGig.setGigId(snapshot.getKey());
                    displayGig(currentGig);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayGig(Gig gig) {
        tvTitle.setText(gig.getTitle());
        tvCategory.setText(gig.getCategory());
        tvPrice.setText(String.format("$%.0f", gig.getPrice()));
        tvPostedBy.setText(gig.getPostedByName());
        tvDescription.setText(gig.getDescription());
        tvStatus.setText(gig.getStatus());

        String userId = String.valueOf(session.getUserId());
        boolean isOwnGig = userId.equals(gig.getPostedByUserId());

        if (isOwnGig) {
            // User's own gig - show label, hide accept
            tvYourGig.setVisibility(View.VISIBLE);
            btnAccept.setVisibility(View.GONE);
        } else if ("accepted".equals(gig.getStatus())) {
            btnAccept.setEnabled(false);
            btnAccept.setText(getString(R.string.gig_already_accepted));
            btnAccept.setAlpha(0.5f);
        } else {
            tvYourGig.setVisibility(View.GONE);
            btnAccept.setVisibility(View.VISIBLE);
            btnAccept.setEnabled(true);
            btnAccept.setAlpha(1f);
        }

        // Status color
        if ("accepted".equals(gig.getStatus())) {
            tvStatus.setTextColor(getColor(R.color.colorStatusPending));
        } else {
            tvStatus.setTextColor(getColor(R.color.colorStatusActive));
        }
    }

    private void acceptGig() {
        if (currentGig == null || gigId == null) return;

        String userId = String.valueOf(session.getUserId());
        String username = session.getUsername();

        gigsRef.child(gigId).child("status").setValue("accepted");
        gigsRef.child(gigId).child("acceptedByUserId").setValue(userId);
        gigsRef.child(gigId).child("acceptedByName").setValue(username);

        Snackbar.make(findViewById(R.id.main), getString(R.string.gig_accepted),
                        Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.colorPrimary))
                .setTextColor(getColor(R.color.colorOnPrimary))
                .show();
    }
}
