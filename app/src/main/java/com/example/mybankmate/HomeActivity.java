package com.example.mybankmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private TextView greetingTextView, bankingTextView, creditCardTextView;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        greetingTextView = findViewById(R.id.greetingTextView);
        bankingTextView = findViewById(R.id.bankingTextView);
        creditCardTextView = findViewById(R.id.creditCardTextView);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUserData();
    }

    private void loadUserData() {
        String userId = getIntent().getStringExtra("userId");

        if (userId != null) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String userName = dataSnapshot.child("email").getValue(String.class);
                        String bankingBalance = dataSnapshot.child("bankingBalance").getValue(String.class);
                        String creditCardBalance = dataSnapshot.child("creditCardBalance").getValue(String.class);

                        greetingTextView.setText(generateGreeting() + ", " + (userName != null ? userName : "User"));
                        bankingTextView.setText("Banking: $" + (bankingBalance != null ? bankingBalance : "0.00"));
                        creditCardTextView.setText("Credit Cards: $" + (creditCardBalance != null ? creditCardBalance : "0.00"));
                    } else {
                        Toast.makeText(HomeActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(HomeActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(HomeActivity.this, "User ID is missing. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
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
}
