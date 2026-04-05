package com.example.fiverr;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fiverr.models.Gig;
import com.example.fiverr.utils.SessionManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateGigActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etPrice;
    private Spinner spinnerCategory;
    private SessionManager session;
    private DatabaseReference gigsRef;

    private final String[] categories = {"Design", "Development", "Marketing", "Writing", "Data", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_gig);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);
        gigsRef = FirebaseDatabase.getInstance().getReference("gigs");

        etTitle = findViewById(R.id.etGigTitle);
        etDescription = findViewById(R.id.etGigDescription);
        etPrice = findViewById(R.id.etGigPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        Button btnPost = findViewById(R.id.btnPostGig);

        // Setup category spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnPost.setOnClickListener(v -> postGig());
    }

    private void postGig() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String category = spinnerCategory.getSelectedItem().toString();

        if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
            showError(getString(R.string.error_empty_fields));
            return;
        }

        if (title.length() < 5) {
            showError("Title must be at least 5 characters long");
            return;
        }

        if (desc.length() < 20) {
            showError("Description must be at least 20 characters long");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0 || price > 10000) {
                showError("Please enter a valid price between $1 and $10,000");
                return;
            }
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_gig_fields));
            return;
        }

        String usrId = String.valueOf(session.getUserId());
        String usrName = session.getUsername();

        Gig gig = new Gig(title, desc, category, price, usrId, usrName);
        String key = gigsRef.push().getKey();
        if (key != null) {
            gig.setGigId(key);
            gigsRef.child(key).setValue(gig).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Snackbar.make(findViewById(R.id.main), getString(R.string.success_gig_posted),
                                    Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(getColor(R.color.colorPrimary))
                            .setTextColor(getColor(R.color.white))
                            .show();
                    findViewById(R.id.main).postDelayed(this::finish, 1000);
                } else {
                    showError(getString(R.string.error_generic));
                }
            });
        }
    }

    private void showError(String msg) {
        Snackbar.make(findViewById(R.id.main), msg, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.colorError))
                .setTextColor(getColor(R.color.white))
                .show();
    }
}
