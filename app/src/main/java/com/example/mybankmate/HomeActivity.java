package com.example.mybankmate;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mybankmate.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private TextView greetingTextView, bankingAccountsText, creditCardsText;
    private Button btnInteracTransfer, btnTransfer, btnPayments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Views
        greetingTextView = findViewById(R.id.greetingTextView);
        bankingAccountsText = findViewById(R.id.banking_accounts_text);
        creditCardsText = findViewById(R.id.credit_cards_text);
        btnInteracTransfer = findViewById(R.id.btn_interac_transfer);
        btnTransfer = findViewById(R.id.btn_transfer);
        btnPayments = findViewById(R.id.btn_payments);

        // Initialize Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Load user data
        loadUserData(usersRef);

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void loadUserData(DatabaseReference usersRef) {
        String userId = getIntent().getStringExtra("userId");

        if (userId != null) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get user data from Firebase
                        String userName = dataSnapshot.child("email").getValue(String.class);
                        String bankingBalance = dataSnapshot.child("bankingBalance").getValue(String.class);
                        String creditCardBalance = dataSnapshot.child("creditCardBalance").getValue(String.class);

                        // Set data to TextViews (with null checks to prevent crashes)
                        greetingTextView.setText(generateGreeting() + ", " + (userName != null ? userName : "User"));
                        bankingAccountsText.setText("Banking: $" + (bankingBalance != null ? bankingBalance : "0.00"));
                        creditCardsText.setText("Credit Cards: $" + (creditCardBalance != null ? creditCardBalance : "0.00"));
                    } else {
                        Toast.makeText(HomeActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to load user data: " + databaseError.getMessage());
                    Toast.makeText(HomeActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(HomeActivity.this, "User ID is missing. Please log in again.", Toast.LENGTH_SHORT).show();
            finish(); // Redirect back to Login if user ID is null
        }
    }

    private String generateGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 18) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : null;

            if (userId == null) {
                Log.e(TAG, "User is not authenticated. Redirecting to login.");
                navigateToLogin();
                return false;
            }

            if (id == R.id.nav_home) {
                return true; // Stay on HomeActivity
            } else if (id == R.id.nav_accounts) {
                Log.d(TAG, "Navigating to AccountsActivity with userId: " + userId);
                Intent intent = new Intent(HomeActivity.this, AccountsActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_move_money) {
                Intent intent = new Intent(HomeActivity.this, MoveMoneyActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_more) {
                Intent intent = new Intent(HomeActivity.this, MoreActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
