package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailField, passwordField;
    private Button loginButton;
    private FirebaseAuth auth;
    private DatabaseReference usersRef, adminsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        adminsRef = FirebaseDatabase.getInstance().getReference("admins");

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        TextView forgotPassword = findViewById(R.id.forgotPassword);

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Check for admin login first
                authenticateAdmin(email, password);
            }
        });

        forgotPassword.setOnClickListener(v -> resetPassword());
    }

    /**
     * Authenticate an admin user.
     */
    private void authenticateAdmin(String email, String password) {
        adminsRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot adminSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = adminSnapshot.child("password").getValue(String.class);
                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Admin login success
                            Log.d(TAG, "Admin login successful.");
                            Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                }

                // If not an admin, check as a regular user
                Log.d(TAG, "Not an admin. Checking regular user login.");
                authenticateRegularUser(email, password);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Admin authentication failed: " + databaseError.getMessage());
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Authenticate a regular user.
     */
    private void authenticateRegularUser(String email, String password) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        Boolean isFirstLogin = userSnapshot.child("isFirstLogin").getValue(Boolean.class);

                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Check if it's the user's first login
                            if (Boolean.TRUE.equals(isFirstLogin)) {
                                Log.d(TAG, "First login detected. Redirecting to ResetPasswordActivity.");
                                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                                intent.putExtra("accountNumber", userSnapshot.getKey());
                                startActivity(intent);
                            } else {
                                Log.d(TAG, "Regular user login successful. Redirecting to HomeActivity.");
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.putExtra("userId", userSnapshot.getKey());
                                startActivity(intent);
                            }
                            finish();
                            return;
                        }
                    }
                }

                // Invalid email or password
                Log.d(TAG, "Invalid email or password for regular user.");
                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "User authentication failed: " + databaseError.getMessage());
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Reset user password.
     */
    private void resetPassword() {
        String email = emailField.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
