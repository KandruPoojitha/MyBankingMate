package com.example.mybankmate;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class AccountsFragment extends Fragment {

    private TextView checkingAccountDetails, savingsAccountDetails;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        checkingAccountDetails = view.findViewById(R.id.checkingAccountDetails);
        savingsAccountDetails = view.findViewById(R.id.savingsAccountDetails);

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId != null) {
            loadAccountDetails(userId);
        } else {
            Log.e("AccountsFragment", "No user authenticated.");
            Toast.makeText(getContext(), "Please log in to view account details.", Toast.LENGTH_SHORT).show();
        }

        return view;
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
                    Toast.makeText(getContext(), "No account data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load account details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
