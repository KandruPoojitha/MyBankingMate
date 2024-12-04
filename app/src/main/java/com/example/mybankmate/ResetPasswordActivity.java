package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPasswordActivity";

    private EditText newPasswordField;
    private Button resetPasswordButton;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private String userId;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");

        if (userId == null || email == null) {
            Toast.makeText(this, "Invalid user. Please log in again.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        newPasswordField = findViewById(R.id.newPasswordField);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordField.getText().toString().trim();
            if (isPasswordValid(newPassword)) {
                resetPassword(newPassword);
            }
        });
    }

    private boolean isPasswordValid(String password) {
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void resetPassword(String newPassword) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        reauthenticateAndResetPassword(newPassword);
    }

    private void reauthenticateAndResetPassword(String newPassword) {
        String password = ""; // Retrieve securely, e.g., from a secure source or by asking the user

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        auth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        auth.getCurrentUser().updatePassword(newPassword)
                                .addOnCompleteListener(passwordUpdateTask -> {
                                    if (passwordUpdateTask.isSuccessful()) {
                                        updateIsFirstLogin();
                                    } else {
                                        Toast.makeText(this, "Password update failed: " + passwordUpdateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Reauthentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    }
                });
    }

    private void updateIsFirstLogin() {
        usersRef.child(userId).child("isFirstLogin").setValue(false)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset successful. Please log in again.", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        Toast.makeText(this, "Failed to update user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
