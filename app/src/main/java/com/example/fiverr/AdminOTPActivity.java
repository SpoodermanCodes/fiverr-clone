package com.example.fiverr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fiverr.db.DatabaseHelper;
import com.example.fiverr.models.User;
import com.example.fiverr.utils.SessionManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

public class AdminOTPActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword, etOtp;
    private LinearLayout layoutCredentials, layoutOtp;
    private TextView tvOtpMessage;
    private DatabaseHelper dbHelper;
    private String generatedOtp;
    private User adminUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_otp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = DatabaseHelper.getInstance(this);

        etUsername = findViewById(R.id.etAdminUsername);
        etPassword = findViewById(R.id.etAdminPassword);
        etOtp = findViewById(R.id.etOtp);
        layoutCredentials = findViewById(R.id.layoutCredentials);
        layoutOtp = findViewById(R.id.layoutOtp);
        tvOtpMessage = findViewById(R.id.tvOtpMessage);
        Button btnSendOtp = findViewById(R.id.btnSendOtp);
        Button btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        btnSendOtp.setOnClickListener(v -> validateCredentialsAndSendOtp());
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
    }

    private void validateCredentialsAndSendOtp() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.error_empty_fields));
            return;
        }

        adminUser = dbHelper.authenticateAdmin(username, password);
        if (adminUser == null) {
            showError(getString(R.string.error_admin_credentials));
            return;
        }

        // Generate simulated OTP
        generatedOtp = String.format("%06d", new Random().nextInt(999999));

        // Show OTP step
        layoutCredentials.setVisibility(View.GONE);
        layoutOtp.setVisibility(View.VISIBLE);

        String phone = adminUser.getPhone() != null ? adminUser.getPhone() : DatabaseHelper.ADMIN_PHONE;
        tvOtpMessage.setText(String.format(getString(R.string.simulated_otp_msg), generatedOtp)
                + "\nSent to: " + phone);

        Snackbar.make(findViewById(R.id.main), getString(R.string.otp_sent),
                        Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.colorPrimary))
                .setTextColor(getColor(R.color.colorOnPrimary))
                .show();
    }

    private void verifyOtp() {
        String enteredOtp = etOtp.getText() != null ? etOtp.getText().toString().trim() : "";

        if (enteredOtp.isEmpty()) {
            showError(getString(R.string.error_empty_fields));
            return;
        }

        if (enteredOtp.equals(generatedOtp)) {
            // OTP verified - save admin session and go to dashboard
            SessionManager session = new SessionManager(this);
            session.saveUserSession(adminUser);

            Snackbar.make(findViewById(R.id.main), getString(R.string.otp_verified),
                            Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColor(R.color.colorPrimary))
                    .setTextColor(getColor(R.color.colorOnPrimary))
                    .show();

            findViewById(R.id.main).postDelayed(() -> {
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }, 1000);
        } else {
            showError(getString(R.string.error_invalid_otp));
        }
    }

    private void showError(String msg) {
        Snackbar.make(findViewById(R.id.main), msg, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.colorError))
                .setTextColor(getColor(R.color.white))
                .show();
    }
}
