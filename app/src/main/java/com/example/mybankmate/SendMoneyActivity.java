package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.util.List;

public class SendMoneyActivity extends AppCompatActivity {

    private static final String TAG = "SendMoneyActivity";

    private static final int ADD_CONTACT_REQUEST = 100;

    private Spinner fromAccountSpinner, toRecipientSpinner;
    private EditText amountInput, messageInput;
    private Button continueButton;
    private ImageView manageContactsButton, backButton;

    private FirebaseAuth auth;
    private DatabaseReference userRef, contactsRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);

        auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();

        fromAccountSpinner = findViewById(R.id.from_account_spinner);
        toRecipientSpinner = findViewById(R.id.to_recipient_spinner);
        amountInput = findViewById(R.id.amount_input);
        messageInput = findViewById(R.id.message_input);
        continueButton = findViewById(R.id.continue_button);
        manageContactsButton = findViewById(R.id.add);
        backButton = findViewById(R.id.back_button);

        if (uid != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            contactsRef = FirebaseDatabase.getInstance().getReference("contacts").child(uid);
            usersRef = FirebaseDatabase.getInstance().getReference("users");

            fetchFromAccounts();
            fetchContacts();
        }

        manageContactsButton.setOnClickListener(v -> {
            Intent intent = new Intent(SendMoneyActivity.this, AddContactActivity.class);
            startActivityForResult(intent, ADD_CONTACT_REQUEST);
        });

        backButton.setOnClickListener(v -> onBackPressed());

        continueButton.setOnClickListener(v -> {
            String selectedAccount = fromAccountSpinner.getSelectedItem().toString();
            String recipient = toRecipientSpinner.getSelectedItem().toString();
            String amountStr = amountInput.getText().toString().trim();
            String message = messageInput.getText().toString().trim();

            if (selectedAccount.isEmpty() || recipient.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(SendMoneyActivity.this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                processTransaction(selectedAccount, recipient, amount);
            } catch (NumberFormatException e) {
                Toast.makeText(SendMoneyActivity.this, "Invalid amount entered.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFromAccounts() {
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

                ArrayAdapter<String> adapter = new ArrayAdapter<>(SendMoneyActivity.this, android.R.layout.simple_spinner_item, accounts);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                fromAccountSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching accounts: " + error.getMessage());
            }
        });
    }

    private void fetchContacts() {
        contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> contacts = new ArrayList<>();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    String contactName = contactSnapshot.child("name").getValue(String.class);
                    String contactEmail = contactSnapshot.child("email").getValue(String.class);

                    if (contactName != null && contactEmail != null) {
                        contacts.add(contactName + " (" + contactEmail + ")");
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(SendMoneyActivity.this, android.R.layout.simple_spinner_item, contacts);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                toRecipientSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching contacts: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CONTACT_REQUEST && resultCode == RESULT_OK) {
            fetchContacts(); // Refresh contacts after adding a new contact
        }
    }
    private void processTransaction(String selectedAccount, String recipient, double amount) {
        // Extract recipient email from the spinner
        String recipientEmail = recipient.substring(recipient.indexOf("(") + 1, recipient.indexOf(")"));

        // Deduct amount from the sender's account
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Determine whether it's a checking or savings account
                String balanceKey = selectedAccount.contains("Checking") ? "checkingBalance" : "savingsBalance";
                String currentBalanceStr = snapshot.child(balanceKey).getValue(String.class);

                if (currentBalanceStr == null) {
                    Toast.makeText(SendMoneyActivity.this, "Invalid account balance.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double currentBalance = Double.parseDouble(currentBalanceStr);
                if (currentBalance < amount) {
                    Toast.makeText(SendMoneyActivity.this, "Insufficient funds.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Deduct the amount
                double updatedBalance = currentBalance - amount;
                userRef.child(balanceKey).setValue(String.valueOf(updatedBalance))
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Amount deducted from sender: $" + amount);
                                updateRecipientBalance(recipientEmail, amount);
                            } else {
                                Toast.makeText(SendMoneyActivity.this, "Transaction failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error updating sender balance: " + error.getMessage());
            }
        });
    }

    private void updateRecipientBalance(String email, double amount) {
        // Search for the recipient by email in the "users" reference
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot recipientSnapshot : snapshot.getChildren()) {
                        String recipientKey = recipientSnapshot.getKey();
                        String currentBalanceStr = recipientSnapshot.child("checkingBalance").getValue(String.class); // Assuming funds are added to checking

                        if (currentBalanceStr == null) {
                            Toast.makeText(SendMoneyActivity.this, "Invalid recipient balance.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double currentBalance = Double.parseDouble(currentBalanceStr);
                        double updatedBalance = currentBalance + amount;

                        // Update recipient's balance
                        usersRef.child(recipientKey).child("checkingBalance").setValue(String.valueOf(updatedBalance))
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SendMoneyActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Amount added to recipient: $" + amount);
                                    } else {
                                        Toast.makeText(SendMoneyActivity.this, "Failed to update recipient balance.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(SendMoneyActivity.this, "Recipient not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error updating recipient balance: " + error.getMessage());
            }
        });
    }

}
