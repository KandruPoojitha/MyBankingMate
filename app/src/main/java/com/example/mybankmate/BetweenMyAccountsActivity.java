package com.example.mybankmate;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BetweenMyAccountsActivity extends AppCompatActivity {

    private static final String TAG = "BetweenMyAccounts";

    private Spinner fromAccountSpinner, toAccountSpinner;
    private TextView amountInput;
    private Button btn50, btn100, btn250, continueButton;

    private ImageView  backButton;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    private String fromAccountKey, toAccountKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_between_my_accounts);

        auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();

        fromAccountSpinner = findViewById(R.id.from_account_spinner);
        toAccountSpinner = findViewById(R.id.to_account_spinner);
        amountInput = findViewById(R.id.amount_input);
        btn50 = findViewById(R.id.btn_50);
        btn100 = findViewById(R.id.btn_100);
        btn250 = findViewById(R.id.btn_250);
        continueButton = findViewById(R.id.continue_button);
        backButton = findViewById(R.id.back_button);

        if (uid != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            fetchAccounts();
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
        }

        backButton.setOnClickListener(v -> onBackPressed());

        btn50.setOnClickListener(v -> setAmount("50"));
        btn100.setOnClickListener(v -> setAmount("100"));
        btn250.setOnClickListener(v -> setAmount("250"));

        continueButton.setOnClickListener(v -> transferBetweenAccounts());
    }

    private void fetchAccounts() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> accounts = new ArrayList<>();
                String checkingAccount = snapshot.child("checkingAccountNumber").getValue(String.class);
                String checkingBalance = snapshot.child("checkingBalance").getValue(String.class);
                String savingsAccount = snapshot.child("savingsAccountNumber").getValue(String.class);
                String savingsBalance = snapshot.child("savingsBalance").getValue(String.class);

                if (checkingAccount != null && checkingBalance != null) {
                    accounts.add("Checking Account: " + checkingAccount + " (Balance: $" + checkingBalance + ")");
                }
                if (savingsAccount != null && savingsBalance != null) {
                    accounts.add("Savings Account: " + savingsAccount + " (Balance: $" + savingsBalance + ")");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(BetweenMyAccountsActivity.this, android.R.layout.simple_spinner_item, accounts);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                fromAccountSpinner.setAdapter(adapter);
                toAccountSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching accounts: " + error.getMessage());
            }
        });
    }

    private void setAmount(String amount) {
        amountInput.setText(amount);
    }

    private void transferBetweenAccounts() {
        String selectedFromAccount = fromAccountSpinner.getSelectedItem().toString();
        String selectedToAccount = toAccountSpinner.getSelectedItem().toString();
        String amountStr = amountInput.getText().toString().trim();

        if (selectedFromAccount.equals(selectedToAccount)) {
            Toast.makeText(this, "From Account and To Account cannot be the same.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        if (selectedFromAccount.contains("Checking")) {
            fromAccountKey = "checkingBalance";
        } else if (selectedFromAccount.contains("Savings")) {
            fromAccountKey = "savingsBalance";
        }

        if (selectedToAccount.contains("Checking")) {
            toAccountKey = "checkingBalance";
        } else if (selectedToAccount.contains("Savings")) {
            toAccountKey = "savingsBalance";
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fromBalanceStr = snapshot.child(fromAccountKey).getValue(String.class);
                String toBalanceStr = snapshot.child(toAccountKey).getValue(String.class);

                if (fromBalanceStr == null || toBalanceStr == null) {
                    Toast.makeText(BetweenMyAccountsActivity.this, "Error retrieving account balances.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double fromBalance = Double.parseDouble(fromBalanceStr);
                double toBalance = Double.parseDouble(toBalanceStr);

                if (fromBalance < amount) {
                    Toast.makeText(BetweenMyAccountsActivity.this, "Insufficient funds in the selected account.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update balances
                double updatedFromBalance = fromBalance - amount;
                double updatedToBalance = toBalance + amount;

                Map<String, Object> updates = new HashMap<>();
                updates.put(fromAccountKey, String.valueOf(updatedFromBalance));
                updates.put(toAccountKey, String.valueOf(updatedToBalance));

                userRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(BetweenMyAccountsActivity.this, "Transfer successful!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Transfer completed: $" + amount + " from " + fromAccountKey + " to " + toAccountKey);
                        finish();
                    } else {
                        Toast.makeText(BetweenMyAccountsActivity.this, "Transfer failed. Please try again.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating balances: " + task.getException().getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching account balances: " + error.getMessage());
            }
        });
    }
}

