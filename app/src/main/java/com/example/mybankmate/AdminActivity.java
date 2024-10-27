package com.example.mybankmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AdminActivity extends AppCompatActivity {

    private EditText userEmailField, userPasswordField, adminEmailField, adminPasswordField;
    private Button addUserButton, addAdminButton;
    private TextView totalUsersText, activeAccountsText;
    private FirebaseDatabase database;
    private DatabaseReference usersRef, adminsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase and UI elements
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        adminsRef = database.getReference("admins");
        auth = FirebaseAuth.getInstance();

        userEmailField = findViewById(R.id.userEmail);
        userPasswordField = findViewById(R.id.userPassword);
        adminEmailField = findViewById(R.id.adminEmail);
        adminPasswordField = findViewById(R.id.adminPassword);
        addUserButton = findViewById(R.id.addUserButton);
        addAdminButton = findViewById(R.id.addAdminButton);
        totalUsersText = findViewById(R.id.totalUsers);
        activeAccountsText = findViewById(R.id.activeAccounts);

        // Set listeners for adding user/admin
        addUserButton.setOnClickListener(v -> {
            String email = userEmailField.getText().toString().trim();
            String password = userPasswordField.getText().toString().trim();

            if (validateInput(email, password)) {
                addUser(email, password);
            }
        });

        addAdminButton.setOnClickListener(v -> {
            String email = adminEmailField.getText().toString().trim();
            String password = adminPasswordField.getText().toString().trim();

            if (validateInput(email, password)) {
                addAdmin(email, password);
            }
        });

        // Call method to update counts dynamically
        updateDashboardStats();
    }

    private void updateDashboardStats() {
        // Add a ValueEventListener to count total and active users
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalUsers = 0;
                int activeAccounts = 0;

                // Iterate through each user in the snapshot to update counts
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    totalUsers++;
                    Boolean isActive = userSnapshot.child("isActive").getValue(Boolean.class);
                    if (isActive != null && isActive) {
                        activeAccounts++;
                    }
                }

                // Update the TextViews with the latest counts
                totalUsersText.setText("Total Users: " + totalUsers);
                activeAccountsText.setText("Active Accounts: " + activeAccounts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addUser(final String email, final String password) {
        String accountNumber = generateAccountNumber();

        // Create an account in Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // User data including initial active status
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("accountNumber", accountNumber);
                            userData.put("balance", 0); // Initial balance set to 0
                            userData.put("isActive", true); // Default active status

                            // Store user data in Realtime Database
                            usersRef.child(user.getUid()).setValue(userData)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(AdminActivity.this, "User added successfully with Account Number: " + accountNumber, Toast.LENGTH_LONG).show();
                                            userEmailField.setText("");
                                            userPasswordField.setText("");
                                        } else {
                                            Toast.makeText(AdminActivity.this, "Failed to add user data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(AdminActivity.this, "Failed to create user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addAdmin(final String email, final String password) {
        // Create an admin in Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> adminData = new HashMap<>();
                            adminData.put("email", email);
                            adminData.put("role", "admin");

                            // Store admin data in Realtime Database
                            adminsRef.child(user.getUid()).setValue(adminData)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(AdminActivity.this, "Admin added successfully", Toast.LENGTH_LONG).show();
                                            adminEmailField.setText("");
                                            adminPasswordField.setText("");
                                        } else {
                                            Toast.makeText(AdminActivity.this, "Failed to add admin data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(AdminActivity.this, "Failed to create admin: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private String generateAccountNumber() {
        return String.valueOf(1000000000 + new Random().nextInt(900000000));
    }
}
