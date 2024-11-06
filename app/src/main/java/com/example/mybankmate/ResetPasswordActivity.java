package com.example.mybankmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResetPasswordActivity extends AppCompatActivity {

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

        newPasswordField = findViewById(R.id.newPasswordField);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordField.getText().toString().trim();
            if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
                Toast.makeText(ResetPasswordActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {
                resetPassword(newPassword);
            }
        });
    }

    private void resetPassword(String newPassword) {
        auth.getCurrentUser().updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        usersRef.child(accountNumber).child("isFirstLogin").setValue(false)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(ResetPasswordActivity.this, "Password reset successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ResetPasswordActivity.this, HomeActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(ResetPasswordActivity.this, "Failed to update user data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Failed to reset password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
