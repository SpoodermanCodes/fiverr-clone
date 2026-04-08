package com.example.fiverr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fiverr.utils.SessionManager;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Register launcher first, before any logic
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                results -> {} // degrade gracefully if denied
        );

        // Request permissions on first launch only — welcome screen is the right place,
        // not inside HomeActivity where dialogs interfere with the back stack
        requestPermissionsIfNeeded();

        Button btnLogin = findViewById(R.id.btnWelcomeLogin);
        Button btnRegister = findViewById(R.id.btnWelcomeRegister);

        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            Class<?> dest = session.isAdmin() ? AdminDashboardActivity.class : HomeActivity.class;
            startActivity(new Intent(this, dest));
            finish();
            return;
        }

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void requestPermissionsIfNeeded() {
        java.util.List<String> needed = new java.util.ArrayList<>();

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
}