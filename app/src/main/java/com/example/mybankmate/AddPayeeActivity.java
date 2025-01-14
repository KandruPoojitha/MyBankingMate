package com.example.mybankmate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddPayeeActivity extends AppCompatActivity {

    private EditText payeeNameInput, payeeAccountIdInput;
    private Button savePayeeButton, deletePayeeButton;
    private String payeeId; // Used for delete functionality
    private boolean isEditMode = false; // Determines if editing an existing payee

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payee);

        payeeNameInput = findViewById(R.id.payee_name_input);
        payeeAccountIdInput = findViewById(R.id.payee_account_id_input);
        savePayeeButton = findViewById(R.id.save_payee_button);
        deletePayeeButton = findViewById(R.id.delete_payee_button);

        // Check if we're editing an existing payee
        if (getIntent().hasExtra("payeeId")) {
            isEditMode = true;
            payeeId = getIntent().getStringExtra("payeeId");
            payeeNameInput.setText(getIntent().getStringExtra("payeeName"));
            payeeAccountIdInput.setText(getIntent().getStringExtra("payeeAccountId"));
            deletePayeeButton.setVisibility(Button.VISIBLE); // Show delete button in edit mode
        } else {
            deletePayeeButton.setVisibility(Button.GONE); // Hide delete button for new payees
        }

        savePayeeButton.setOnClickListener(v -> {
            String name = payeeNameInput.getText().toString().trim();
            String accountId = payeeAccountIdInput.getText().toString().trim();

            if (!name.isEmpty() && !accountId.isEmpty()) {
                if (isEditMode) {
                    updatePayee(name, accountId);
                } else {
                    savePayee(name, accountId);
                }
            } else {
                Toast.makeText(AddPayeeActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        deletePayeeButton.setOnClickListener(v -> deletePayee());
    }

    private void savePayee(String name, String accountId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("payees");

        String newPayeeId = database.child(userId).push().getKey();
        Payee payee = new Payee(newPayeeId, name, accountId);

        if (newPayeeId != null) {
            database.child(userId).child(newPayeeId).setValue(payee).addOnSuccessListener(aVoid -> {
                Toast.makeText(AddPayeeActivity.this, "Payee added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(AddPayeeActivity.this, "Error saving payee", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updatePayee(String name, String accountId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("payees");

        if (payeeId != null) {
            Payee payee = new Payee(payeeId, name, accountId);
            database.child(userId).child(payeeId).setValue(payee).addOnSuccessListener(aVoid -> {
                Toast.makeText(AddPayeeActivity.this, "Payee updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(AddPayeeActivity.this, "Error updating payee", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void deletePayee() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("payees");

        if (payeeId != null) {
            database.child(userId).child(payeeId).removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(AddPayeeActivity.this, "Payee deleted successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(AddPayeeActivity.this, "Error deleting payee", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
