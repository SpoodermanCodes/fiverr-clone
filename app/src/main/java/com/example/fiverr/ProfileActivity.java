package com.example.fiverr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fiverr.db.DatabaseHelper;
import com.example.fiverr.models.User;
import com.example.fiverr.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SessionManager session = new SessionManager(this);
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        User user = db.getUserById(session.getUserId());

        TextView tvUsername = findViewById(R.id.tvProfileUsername);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        TextView tvPhone = findViewById(R.id.tvProfilePhone);
        TextView tvStatus = findViewById(R.id.tvProfileStatus);
        TextView tvSkills = findViewById(R.id.tvProfileSkills);
        Button btnLogout = findViewById(R.id.btnLogout);

        if (user != null) {
            tvUsername.setText(user.getUsername());
            tvEmail.setText(user.getEmail());
            tvPhone.setText(user.getPhone());
            tvSkills.setText(user.getSkills());

            boolean isActive = "active".equals(user.getStatus());
            tvStatus.setText(isActive ? getString(R.string.status_active) : getString(R.string.status_dormant));
            tvStatus.setTextColor(getColor(isActive ? R.color.colorStatusActive : R.color.colorStatusDormant));
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            session.clearSession();
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
