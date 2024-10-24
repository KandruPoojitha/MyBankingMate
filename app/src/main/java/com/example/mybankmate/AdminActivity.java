package com.example.mybankmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AdminActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button addUserButton, addAdminButton;
    private FirebaseDatabase database;
    private DatabaseReference usersRef, adminsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        adminsRef = database.getReference("admins");

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        addUserButton = findViewById(R.id.addUserButton);
        addAdminButton = findViewById(R.id.addAdminButton);

        addUserButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(AdminActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                addUser(email, password);
            }
        });

        addAdminButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(AdminActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                addAdmin(email, password);
            }
        });
    }

    private void addUser(final String email, final String password) {
        String accountNumber = generateAccountNumber();

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("password", password); // Note: Consider hashing the password for security
        userData.put("accountNumber", accountNumber);
        userData.put("balance", "0"); // Initial balance set to 0

        usersRef.child(accountNumber).setValue(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AdminActivity.this, "User added successfully with Account Number: " + accountNumber, Toast.LENGTH_LONG).show();
                emailField.setText("");
                passwordField.setText("");
            } else {
                Toast.makeText(AdminActivity.this, "Failed to add user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAdmin(final String email, final String password) {
        Map<String, String> adminData = new HashMap<>();
        adminData.put("email", email);
        adminData.put("password", password); // Note: Consider hashing the password for security

        String adminId = adminsRef.push().getKey(); // Generate a unique key for the admin
        if (adminId != null) {
            adminsRef.child(adminId).setValue(adminData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Admin added successfully", Toast.LENGTH_LONG).show();
                    emailField.setText("");
                    passwordField.setText("");
                } else {
                    Toast.makeText(AdminActivity.this, "Failed to add admin", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String generateAccountNumber() {
        Random random = new Random();
        int accountNumber = 1000000000 + random.nextInt(900000000);
        return String.valueOf(accountNumber);
    }
}
