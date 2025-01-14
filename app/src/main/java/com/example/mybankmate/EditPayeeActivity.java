package com.example.mybankmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditPayeeActivity extends AppCompatActivity {

    private EditText payeeNameField, payeeAccountField;
    private Button saveButton, deleteButton;
    private DatabaseReference payeeRef;
    private String payeeId, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_payee);

        // Initialize views
        payeeNameField = findViewById(R.id.payee_name_input);
        payeeAccountField = findViewById(R.id.payee_account_input);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);
        ImageView backButton = findViewById(R.id.back_button);

        // Get current user ID and payee details from intent
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        payeeId = getIntent().getStringExtra("payeeId");
        String payeeName = getIntent().getStringExtra("payeeName");
        String payeeAccount = getIntent().getStringExtra("payeeAccount");

        // Initialize Firebase reference
        payeeRef = FirebaseDatabase.getInstance().getReference("payees").child(userId).child(payeeId);

        // Set payee details in fields
        payeeNameField.setText(payeeName);
        payeeAccountField.setText(payeeAccount);

        // Back button click listener
        backButton.setOnClickListener(v -> finish());

        // Save button click listener
        saveButton.setOnClickListener(v -> savePayee());

        // Delete button click listener
        deleteButton.setOnClickListener(v -> deletePayee());
    }

    private void savePayee() {
        String updatedName = payeeNameField.getText().toString().trim();
        String updatedAccount = payeeAccountField.getText().toString().trim();

        if (TextUtils.isEmpty(updatedName) || TextUtils.isEmpty(updatedAccount)) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        payeeRef.child("name").setValue(updatedName);
        payeeRef.child("accountId").setValue(updatedAccount)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditPayeeActivity.this, "Payee updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(EditPayeeActivity.this, "Failed to update payee", Toast.LENGTH_SHORT).show());
    }

    private void deletePayee() {
        payeeRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditPayeeActivity.this, "Payee deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(EditPayeeActivity.this, "Failed to delete payee", Toast.LENGTH_SHORT).show());
    }
}
