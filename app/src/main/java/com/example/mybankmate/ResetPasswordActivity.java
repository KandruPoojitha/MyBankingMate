package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPasswordActivity";

    private EditText newPasswordField;
    private Button resetPasswordButton;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private String accountNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        accountNumber = getIntent().getStringExtra("accountNumber");

        if (accountNumber == null) {
            Log.e(TAG, "Account number is null!");
            Toast.makeText(this, "Invalid account. Please log in again.", Toast.LENGTH_SHORT).show();
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

        auth.getCurrentUser().updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Password reset successfully for the current user.");
                        updateIsFirstLogin(); // Update flag after resetting password
                    } else {
                        Log.e(TAG, "Password reset failed: " + task.getException());
                        Toast.makeText(this, "Failed to reset password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateIsFirstLogin() {
        usersRef.child(accountNumber).child("isFirstLogin").setValue(false)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "isFirstLogin updated successfully for account: " + accountNumber);
                        Toast.makeText(this, "Password reset successful", Toast.LENGTH_SHORT).show();
                        navigateToLogin(); // Redirect to Home after a successful reset
                    } else {
                        Log.e(TAG, "Failed to update isFirstLogin: " + task.getException());
                        Toast.makeText(this, "Failed to update user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        intent.putExtra("userId", accountNumber);
        startActivity(intent);
        finish();
    }

}
