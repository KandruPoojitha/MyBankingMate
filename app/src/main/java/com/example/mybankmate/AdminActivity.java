package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {

    private Button userManagementButton, adminManagementButton, logoutButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        auth = FirebaseAuth.getInstance();

        userManagementButton = findViewById(R.id.userManagementButton);
        adminManagementButton = findViewById(R.id.adminManagementButton);
        logoutButton = findViewById(R.id.logoutButton);

        userManagementButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, UserManagementActivity.class);
            startActivity(intent);
        });

        // Navigate to Admin Management
        adminManagementButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminManagementActivity.class);
            startActivity(intent);
        });

        // Logout functionality
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(AdminActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
