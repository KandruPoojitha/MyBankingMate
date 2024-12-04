package com.example.mybankmate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private TextView greetingTextView, checkingAccountDetails, savingsAccountDetails;
    private Button interacButton;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();

        if (uid != null) {
            // Initialize Views
            greetingTextView = view.findViewById(R.id.greetingTextView);
            checkingAccountDetails = view.findViewById(R.id.checkingAccountDetails);
            savingsAccountDetails = view.findViewById(R.id.savingsAccountDetails);
            interacButton = view.findViewById(R.id.btn_interac_transfer);

            // Set greeting message
            String email = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "User";
            setGreetingMessage(email);

            // Reference user data in Firebase
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            // Fetch user account details
            fetchAndDisplayUserDetails();

            // Set up Interac button click listener
            interacButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SendMoneyActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "User not logged in.");
            Toast.makeText(requireContext(), "Error: User not logged in.", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private void setGreetingMessage(String email) {
        // Determine the greeting message based on the time of day
        String greeting;
        int hour = new Date().getHours();
        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening";
        } else {
            greeting = "Good Night";
        }

        greetingTextView.setText(String.format("%s, %s", greeting, email));
    }

    private void fetchAndDisplayUserDetails() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String checkingAccountNumber = snapshot.child("checkingAccountNumber").getValue(String.class);
                    String checkingBalance = snapshot.child("checkingBalance").getValue(String.class);
                    String savingsAccountNumber = snapshot.child("savingsAccountNumber").getValue(String.class);
                    String savingsBalance = snapshot.child("savingsBalance").getValue(String.class);

                    // Update UI with account details
                    checkingAccountDetails.setText(
                            String.format("Account: %s\nBalance: $%s",
                                    checkingAccountNumber != null ? checkingAccountNumber : "N/A",
                                    checkingBalance != null ? checkingBalance : "0.00")
                    );
                    savingsAccountDetails.setText(
                            String.format("Account: %s\nBalance: $%s",
                                    savingsAccountNumber != null ? savingsAccountNumber : "N/A",
                                    savingsBalance != null ? savingsBalance : "0.00")
                    );
                } else {
                    Log.e(TAG, "No data exists for user: " + userRef.getKey());
                    Toast.makeText(getContext(), "No user details found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching user details: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
