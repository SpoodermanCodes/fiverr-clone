package com.example.fiverr;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
    private ImageButton btnMusic;
    private LinearLayout equalizerView;
    private View eqBar1, eqBar2, eqBar3;
    private AnimatorSet equalizerAnimator;
    private boolean isMusicPlaying = false;
    private boolean showingMyGigs = false;
    private String currentCategory = "All";

    // ★ FIX: Store listener as a field so we can remove it — prevents listener leak crash
    private ValueEventListener gigsListener;

    // Runtime permission launcher for SMS + notifications
    private ActivityResultLauncher<String[]> permissionLauncher;

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
        gigsRef = FirebaseDatabase.getInstance("https://fiverr-d61c9-default-rtdb.firebaseio.com").getReference("gigs");

        rvGigs = findViewById(R.id.rvGigs);
        tvNoGigs = findViewById(R.id.tvNoGigs);
        EditText etSearch = findViewById(R.id.etSearch);
        btnMusic = findViewById(R.id.btnMusic);
        equalizerView = findViewById(R.id.equalizerView);
        eqBar1 = findViewById(R.id.eqBar1);
        eqBar2 = findViewById(R.id.eqBar2);
        eqBar3 = findViewById(R.id.eqBar3);
        ImageButton btnProfile = findViewById(R.id.btnProfile);

        // Setup RecyclerView
        adapter = new GigAdapter(this);
        rvGigs.setLayoutManager(new LinearLayoutManager(this));
        rvGigs.setAdapter(adapter);

        // Register permission launcher before any possible request
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                results -> {} // results handled silently; features degrade gracefully if denied
        );

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Music toggle (manual start — tapping the button)
        btnMusic.setOnClickListener(v -> toggleMusic());

        // Profile
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Category chips
        setupCategoryChips();

        // Bottom nav
        findViewById(R.id.navExplore).setOnClickListener(v -> {
            showingMyGigs = false;
            applyFilters(etSearch.getText().toString());
        });

        findViewById(R.id.navMyGigs).setOnClickListener(v -> {
            showingMyGigs = true;
            applyFilters(etSearch.getText().toString());
        });

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        findViewById(R.id.fabCreateGig).setOnClickListener(v ->
                startActivity(new Intent(this, CreateGigActivity.class)));

        // Request SMS + notification permissions after UI is drawn to avoid ANR
        rvGigs.post(this::requestRuntimePermissions);
    }

    private void requestRuntimePermissions() {
        List<String> needed = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.RECEIVE_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.READ_SMS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!needed.isEmpty()) {
            permissionLauncher.launch(needed.toArray(new String[0]));
        }
    }

    private void setupCategoryChips() {
        View.OnClickListener chipListener = v -> {
            TextView chip = (TextView) v;
            currentCategory = chip.getText().toString();
            EditText etSearch = findViewById(R.id.etSearch);
            applyFilters(etSearch.getText().toString());
        };

        findViewById(R.id.chipAll).setOnClickListener(chipListener);
        findViewById(R.id.chipDesign).setOnClickListener(chipListener);
        findViewById(R.id.chipDev).setOnClickListener(chipListener);
        findViewById(R.id.chipMarketing).setOnClickListener(chipListener);
        findViewById(R.id.chipWriting).setOnClickListener(chipListener);
        findViewById(R.id.chipData).setOnClickListener(chipListener);
    }

    // ★ FIX: Attach the Firebase listener in onStart / detach in onStop
    // This prevents listener accumulation across multiple resume cycles,
    // which was causing IndexOutOfBoundsException in RecyclerView
    @Override
    protected void onStart() {
        super.onStart();
        attachGigsListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachGigsListener();
        stopEqualizer();
    }

    private void attachGigsListener() {
        if (gigsListener != null) return; // Already attached

        gigsListener = new ValueEventListener() {
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
                // Apply current filters after data loads
                EditText etSearch = findViewById(R.id.etSearch);
                String query = etSearch != null ? etSearch.getText().toString() : "";
                applyFilters(query);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log but don't crash
            }
        };
        gigsRef.addValueEventListener(gigsListener);
    }

    private void detachGigsListener() {
        if (gigsListener != null) {
            gigsRef.removeEventListener(gigsListener);
            gigsListener = null;
        }
    }

    private void applyFilters(String query) {
        filteredGigs.clear();
        String userId = String.valueOf(session.getUserId());
        String lowerQuery = (query != null) ? query.toLowerCase() : "";

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

        // Use a new list to avoid RecyclerView layout pass conflicts
        adapter.setGigs(new ArrayList<>(filteredGigs));
        tvNoGigs.setVisibility(filteredGigs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void toggleMusic() {
        Intent serviceIntent = new Intent(this, BackgroundMusicService.class);
        if (isMusicPlaying) {
            serviceIntent.setAction(BackgroundMusicService.ACTION_PAUSE);
            btnMusic.setImageResource(android.R.drawable.ic_media_play);
            stopEqualizer();
        } else {
            serviceIntent.setAction(BackgroundMusicService.ACTION_PLAY);
            btnMusic.setImageResource(android.R.drawable.ic_media_pause);
            startEqualizer();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        isMusicPlaying = !isMusicPlaying;
    }

    private void startEqualizer() {
        equalizerView.setVisibility(View.VISIBLE);

        ObjectAnimator bar1 = ObjectAnimator.ofFloat(eqBar1, "scaleY", 0.3f, 1f, 0.5f, 1f, 0.3f);
        bar1.setDuration(900);
        bar1.setRepeatCount(ObjectAnimator.INFINITE);
        bar1.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator bar2 = ObjectAnimator.ofFloat(eqBar2, "scaleY", 1f, 0.3f, 1f, 0.6f, 1f);
        bar2.setDuration(700);
        bar2.setRepeatCount(ObjectAnimator.INFINITE);
        bar2.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator bar3 = ObjectAnimator.ofFloat(eqBar3, "scaleY", 0.5f, 1f, 0.3f, 1f, 0.5f);
        bar3.setDuration(800);
        bar3.setRepeatCount(ObjectAnimator.INFINITE);
        bar3.setInterpolator(new AccelerateDecelerateInterpolator());

        equalizerAnimator = new AnimatorSet();
        equalizerAnimator.playTogether(bar1, bar2, bar3);
        equalizerAnimator.start();
    }

    private void stopEqualizer() {
        if (equalizerAnimator != null) {
            equalizerAnimator.cancel();
            equalizerAnimator = null;
        }
        eqBar1.setScaleY(1f);
        eqBar2.setScaleY(1f);
        eqBar3.setScaleY(1f);
        equalizerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onGigClick(Gig gig) {
        Intent intent = new Intent(this, GigDetailActivity.class);
        intent.putExtra("gigId", gig.getGigId());
        startActivity(intent);
    }

    // ★ FIX: Removed the onResume() override that was calling loadGigs() and
    // adding duplicate Firebase listeners. The onStart/onStop pair handles this correctly.
}
