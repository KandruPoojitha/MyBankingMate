package com.example.mybankmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailField, passwordField;
    private Button loginButton;
    private FirebaseDatabase database;
    private DatabaseReference usersRef, adminsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        adminsRef = database.getReference("admins");

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
                authenticateUser(email, password);
            }
        });

        forgotPassword.setOnClickListener(v -> resetPassword());
    }

    private void authenticateUser(String email, String password) {
        Log.d(TAG, "Authenticating user with email: " + email);

        // Check if the user is an admin
        adminsRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot adminSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = adminSnapshot.child("password").getValue(String.class);
                        if (storedPassword != null && storedPassword.equals(password)) {
                            Log.d(TAG, "Admin login successful.");
                            // Redirect to Admin Activity if password matches
                            Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                }
                // If not an admin, check if the user is a regular user
                checkUser(email, password);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Admin check failed: " + databaseError.getMessage());
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser(String email, String password) {
        Log.d(TAG, "Checking regular user with email: " + email);

        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        Boolean isFirstLogin = userSnapshot.child("isFirstLogin").getValue(Boolean.class);

                        if (storedPassword != null && storedPassword.equals(password)) {
                            if (Boolean.TRUE.equals(isFirstLogin)) {
                                Log.d(TAG, "First login, redirecting to ResetPasswordActivity.");
                                // Redirect to ResetPasswordActivity if this is the first login
                                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                                intent.putExtra("accountNumber", userSnapshot.getKey());
                                startActivity(intent);
                            } else {
                                Log.d(TAG, "Regular user login successful, redirecting to HomeActivity.");
                                // Redirect to HomeActivity if password matches and user has already reset password
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                            finish();
                            return;
                        }
                    }
                }
                Log.d(TAG, "Invalid email or password.");
                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "User check failed: " + databaseError.getMessage());
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

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
