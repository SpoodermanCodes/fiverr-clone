package com.example.fiverr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fiverr.utils.SessionManager;

public class WelcomeActivity extends AppCompatActivity {

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

        Button btnLogin = findViewById(R.id.btnWelcomeLogin);
        Button btnRegister = findViewById(R.id.btnWelcomeRegister);

        // Check if already logged in — done after setContentView to avoid broken back-stack loop.
        // If HomeActivity crashed and Android restarted us, the session would still be saved,
        // causing an infinite flicker loop. We detect this by checking a "launching" flag:
        // if it's still set when WelcomeActivity starts, HomeActivity never finished cleanly.
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            Class<?> dest = session.isAdmin() ? AdminDashboardActivity.class : HomeActivity.class;
            startActivity(new Intent(this, dest));
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
        });
    }
}
