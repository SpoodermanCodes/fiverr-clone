package com.example.fiverr;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fiverr.adapters.GigAdapter;
import com.example.fiverr.models.Gig;
import com.example.fiverr.services.BackgroundMusicService;
import com.example.fiverr.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements GigAdapter.OnGigClickListener {

    private RecyclerView rvGigs;
    private TextView tvNoGigs;
    private GigAdapter adapter;
    private List<Gig> allGigs = new ArrayList<>();
    private List<Gig> filteredGigs = new ArrayList<>();
    private DatabaseReference gigsRef;
    private SessionManager session;
    private boolean isMusicPlaying = false;
    private boolean showingMyGigs = false;
    private String currentCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);
        gigsRef = FirebaseDatabase.getInstance().getReference("gigs");

        rvGigs = findViewById(R.id.rvGigs);
        tvNoGigs = findViewById(R.id.tvNoGigs);
        EditText etSearch = findViewById(R.id.etSearch);
        ImageButton btnMusic = findViewById(R.id.btnMusic);
        ImageButton btnProfile = findViewById(R.id.btnProfile);

        // Setup RecyclerView
        adapter = new GigAdapter(this);
        rvGigs.setLayoutManager(new LinearLayoutManager(this));
        rvGigs.setAdapter(adapter);

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGigs(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Music toggle
        btnMusic.setOnClickListener(v -> toggleMusic(btnMusic));

        // Profile
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Category chips
        setupCategoryChips();

        // Bottom nav
        findViewById(R.id.navExplore).setOnClickListener(v -> {
            showingMyGigs = false;
            applyFilters("");
        });

        findViewById(R.id.navMyGigs).setOnClickListener(v -> {
            showingMyGigs = true;
            applyFilters("");
        });

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        findViewById(R.id.fabCreateGig).setOnClickListener(v ->
                startActivity(new Intent(this, CreateGigActivity.class)));

        // Load gigs from Firebase
        loadGigs();
    }

    private void setupCategoryChips() {
        View.OnClickListener chipListener = v -> {
            TextView chip = (TextView) v;
            currentCategory = chip.getText().toString();
            applyFilters(((EditText) findViewById(R.id.etSearch)).getText().toString());
        };

        findViewById(R.id.chipAll).setOnClickListener(chipListener);
        findViewById(R.id.chipDesign).setOnClickListener(chipListener);
        findViewById(R.id.chipDev).setOnClickListener(chipListener);
        findViewById(R.id.chipMarketing).setOnClickListener(chipListener);
        findViewById(R.id.chipWriting).setOnClickListener(chipListener);
        findViewById(R.id.chipData).setOnClickListener(chipListener);
    }

    private void loadGigs() {
        gigsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allGigs.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Gig gig = ds.getValue(Gig.class);
                    if (gig != null) {
                        gig.setGigId(ds.getKey());
                        allGigs.add(gig);
                    }
                }
                applyFilters("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterGigs(String query) {
        applyFilters(query);
    }

    private void applyFilters(String query) {
        filteredGigs.clear();
        String userId = String.valueOf(session.getUserId());
        String lowerQuery = query.toLowerCase();

        for (Gig gig : allGigs) {
            // My Gigs filter
            if (showingMyGigs) {
                if (!userId.equals(gig.getPostedByUserId())) continue;
            }

            // Category filter
            if (!"All".equals(currentCategory)) {
                if (gig.getCategory() == null || !gig.getCategory().equals(currentCategory)) continue;
            }

            // Search filter
            if (!lowerQuery.isEmpty()) {
                boolean matches = (gig.getTitle() != null && gig.getTitle().toLowerCase().contains(lowerQuery))
                        || (gig.getDescription() != null && gig.getDescription().toLowerCase().contains(lowerQuery))
                        || (gig.getCategory() != null && gig.getCategory().toLowerCase().contains(lowerQuery));
                if (!matches) continue;
            }

            filteredGigs.add(gig);
        }

        adapter.setGigs(filteredGigs);
        tvNoGigs.setVisibility(filteredGigs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void toggleMusic(ImageButton btn) {
        Intent serviceIntent = new Intent(this, BackgroundMusicService.class);
        if (isMusicPlaying) {
            serviceIntent.setAction(BackgroundMusicService.ACTION_PAUSE);
            btn.setImageResource(android.R.drawable.ic_media_play);
        } else {
            serviceIntent.setAction(BackgroundMusicService.ACTION_PLAY);
            btn.setImageResource(android.R.drawable.ic_media_pause);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        isMusicPlaying = !isMusicPlaying;
    }

    @Override
    public void onGigClick(Gig gig) {
        Intent intent = new Intent(this, GigDetailActivity.class);
        intent.putExtra("gigId", gig.getGigId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGigs();
    }
}
