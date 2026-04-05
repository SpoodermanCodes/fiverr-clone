package com.example.fiverr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fiverr.db.DatabaseHelper;
import com.example.fiverr.models.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPhone, etPassword, etConfirmPw, etAge;
    private RadioGroup rgGender;
    private CheckBox cbWebDev, cbMobileDev, cbGraphicDesign, cbContentWriting, cbDataScience, cbDigitalMarketing;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = DatabaseHelper.getInstance(this);

        etUsername = findViewById(R.id.etRegUsername);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etRegPhone);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPw = findViewById(R.id.etRegConfirmPassword);
        etAge = findViewById(R.id.etRegAge);
        rgGender = findViewById(R.id.rgGender);

        cbWebDev = findViewById(R.id.cbWebDev);
        cbMobileDev = findViewById(R.id.cbMobileDev);
        cbGraphicDesign = findViewById(R.id.cbGraphicDesign);
        cbContentWriting = findViewById(R.id.cbContentWriting);
        cbDataScience = findViewById(R.id.cbDataScience);
        cbDigitalMarketing = findViewById(R.id.cbDigitalMarketing);

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String username = getText(etUsername);
        String email = getText(etEmail);
        String phone = getText(etPhone);
        String password = getText(etPassword);
        String confirmPw = getText(etConfirmPw);
        String ageStr = getText(etAge);

        // Validations
        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                password.isEmpty() || confirmPw.isEmpty() || ageStr.isEmpty()) {
            showError(getString(R.string.error_empty_fields));
            return;
        }

        if (username.length() < 4) {
            showError("Username must be at least 4 characters long");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.error_invalid_email));
            return;
        }

        if (phone.length() < 10) {
            showError("Please enter a valid 10-digit phone number");
            return;
        }

        if (password.length() < 6) {
            showError(getString(R.string.error_weak_password));
            return;
        }

        if (!password.equals(confirmPw)) {
            showError(getString(R.string.error_passwords_mismatch));
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 13 || age > 100) {
                showError("Please enter a valid age between 13 and 100");
                return;
            }
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_age));
            return;
        }

        // Gender
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            showError(getString(R.string.error_select_gender));
            return;
        }
        String gender;
        if (selectedGenderId == R.id.rbMale) gender = "Male";
        else if (selectedGenderId == R.id.rbFemale) gender = "Female";
        else gender = "Other";

        // Skills
        List<String> skills = new ArrayList<>();
        if (cbWebDev.isChecked()) skills.add("Web Development");
        if (cbMobileDev.isChecked()) skills.add("Mobile Development");
        if (cbGraphicDesign.isChecked()) skills.add("Graphic Design");
        if (cbContentWriting.isChecked()) skills.add("Content Writing");
        if (cbDataScience.isChecked()) skills.add("Data Science");
        if (cbDigitalMarketing.isChecked()) skills.add("Digital Marketing");

        if (skills.isEmpty()) {
            showError(getString(R.string.error_select_skills));
            return;
        }

        // Check duplicates
        if (dbHelper.usernameExists(username)) {
            showError("This username is already take, please choose another");
            return;
        }
        if (dbHelper.emailExists(email)) {
            showError("This email address is already registered");
            return;
        }

        // Create user
        String skillsStr = String.join(", ", skills);
        User user = new User(username, email, password, phone, age, gender, skillsStr);
        long result = dbHelper.createUser(user);

        if (result > 0) {
            Snackbar.make(findViewById(R.id.main), getString(R.string.success_registration),
                            Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(R.color.colorPrimary))
                    .setTextColor(getColor(R.color.white))
                    .show();

            // Go back to welcome after short delay
            findViewById(R.id.main).postDelayed(() -> {
                Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }, 1000);
        } else {
            showError(getString(R.string.error_generic));
        }
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void showError(String message) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.colorError))
                .setTextColor(getColor(R.color.white))
                .show();
    }
}
