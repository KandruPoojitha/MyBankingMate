package com.example.mybankmate;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class AccountsActivity extends AppCompatActivity {

    private TextView checkingAccountDetails, savingsAccountDetails;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        checkingAccountDetails = findViewById(R.id.checkingAccountDetails);
        savingsAccountDetails = findViewById(R.id.savingsAccountDetails);

        String userId = getIntent().getStringExtra("userId");

        if (userId != null) {
            loadAccountDetails(userId);
        } else {
            // Fallback: check FirebaseAuth for the current user
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
                loadAccountDetails(userId);
            } else {
                Log.e(TAG, "No user authenticated. Redirecting to login.");
                navigateToLogin();
            }
        }
    }

    private void loadAccountDetails(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String checkingAccount = snapshot.child("checkingAccountNumber").getValue(String.class);
                    String checkingBalance = snapshot.child("checkingBalance").getValue(String.class);
                    String savingsAccount = snapshot.child("savingsAccountNumber").getValue(String.class);
                    String savingsBalance = snapshot.child("savingsBalance").getValue(String.class);

                    checkingAccountDetails.setText(
                            String.format("Account: %s\nBalance: $%s",
                                    checkingAccount != null ? checkingAccount : "N/A",
                                    checkingBalance != null ? checkingBalance : "0.00")
                    );
                    savingsAccountDetails.setText(
                            String.format("Account: %s\nBalance: $%s",
                                    savingsAccount != null ? savingsAccount : "N/A",
                                    savingsBalance != null ? savingsBalance : "0.00")
                    );
                } else {
                    Toast.makeText(AccountsActivity.this, "No account data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccountsActivity.this, "Failed to load account details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(AccountsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
