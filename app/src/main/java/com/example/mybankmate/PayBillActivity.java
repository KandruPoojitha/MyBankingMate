package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PayBillActivity extends AppCompatActivity {

    private Spinner fromAccountSpinner, payeeSpinner;
    private EditText amountInput;
    private TextView dateText;
    private Button proceedButton;

    private DatabaseReference databaseReference;
    private String userId;

    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;

    private double amount;
    private String selectedAccount;
    private String selectedPayee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_bills);

        fromAccountSpinner = findViewById(R.id.from_account_spinner);
        payeeSpinner = findViewById(R.id.payee_spinner);
        amountInput = findViewById(R.id.amount_input);
        dateText = findViewById(R.id.date_text);
        proceedButton = findViewById(R.id.proceed_button);
        ImageView managePayeesIcon = findViewById(R.id.manage_payees_icon);
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        managePayeesIcon.setOnClickListener(v -> {
            Intent intent = new Intent(PayBillActivity.this, ManagePayeesActivity.class);
            startActivity(intent);
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setDateToToday();
        fetchAccounts();
        fetchPayees();

        PaymentConfiguration.init(this, "pk_test_51PlVh8P9Bz7XrwZPnWMN2upZk3x00s3soZgJgM5QTMuwCNoZPBdGtmPRXB29vBnFvOXjEAv2vntLuQaWbPpEHOmP00D7pelv0B");
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        proceedButton.setOnClickListener(v -> validateAndProceed());
    }

    private void setDateToToday() {
        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        dateText.setText(currentDate);
    }

    private void fetchAccounts() {
        databaseReference.child("users").child(userId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<String> accounts = new ArrayList<>();
                    String checkingAccountNumber = snapshot.child("checkingAccountNumber").getValue(String.class);
                    String checkingBalance = snapshot.child("checkingBalance").getValue(String.class);
                    String savingsAccountNumber = snapshot.child("savingsAccountNumber").getValue(String.class);
                    String savingsBalance = snapshot.child("savingsBalance").getValue(String.class);

                    if (checkingAccountNumber != null && checkingBalance != null) {
                        accounts.add("Checking - " + checkingAccountNumber + " ($" + checkingBalance + ")");
                    }
                    if (savingsAccountNumber != null && savingsBalance != null) {
                        accounts.add("Savings - " + savingsAccountNumber + " ($" + savingsBalance + ")");
                    }

                    ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(PayBillActivity.this, android.R.layout.simple_spinner_item, accounts);
                    accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    fromAccountSpinner.setAdapter(accountAdapter);
                } else {
                    Toast.makeText(PayBillActivity.this, "No accounts found for this user.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PayBillActivity.this, "Failed to load accounts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPayees() {
        databaseReference.child("payees").child(userId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<String> payees = new ArrayList<>();
                    for (DataSnapshot payeeSnapshot : snapshot.getChildren()) {
                        String payeeName = payeeSnapshot.child("name").getValue(String.class);
                        String payeeAccountId = payeeSnapshot.child("accountId").getValue(String.class);

                        if (payeeName != null && payeeAccountId != null) {
                            payees.add(payeeName + " - " + payeeAccountId);
                        }
                    }

                    ArrayAdapter<String> payeeAdapter = new ArrayAdapter<>(PayBillActivity.this, android.R.layout.simple_spinner_item, payees);
                    payeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    payeeSpinner.setAdapter(payeeAdapter);
                } else {
                    Toast.makeText(PayBillActivity.this, "No payees found. Add a payee first.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PayBillActivity.this, "Failed to load payees: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndProceed() {
        String amountStr = amountInput.getText().toString();

        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        amount = Double.parseDouble(amountStr);

        if (amount <= 0) {
            Toast.makeText(this, "Enter a valid amount greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedAccount = (String) fromAccountSpinner.getSelectedItem();

        if (selectedAccount == null) {
            Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show();
            return;
        }

        double availableBalance = getAvailableBalance(selectedAccount);

        if (amount > availableBalance) {
            Toast.makeText(this, "Insufficient funds in the selected account", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedPayee = (String) payeeSpinner.getSelectedItem();

        if (selectedPayee == null) {
            Toast.makeText(this, "Please select a payee", Toast.LENGTH_SHORT).show();
            return;
        }

        createPaymentIntent();
    }

    private double getAvailableBalance(String selectedAccount) {
        try {
            String balanceStr = selectedAccount.substring(selectedAccount.indexOf("$") + 1, selectedAccount.indexOf(")"));
            return Double.parseDouble(balanceStr);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    private void createPaymentIntent() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String backendUrl = "https://8494-76-68-243-40.ngrok-free.app/create-payment-intent";

                JSONObject payload = new JSONObject();
                payload.put("amount", (int) (amount * 100));

                String response = HttpUtils.post(backendUrl, payload.toString());
                JSONObject responseJson = new JSONObject(response);

                paymentIntentClientSecret = responseJson.getString("clientSecret");

                runOnUiThread(this::presentPaymentSheet);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(PayBillActivity.this, "Failed to create PaymentIntent", Toast.LENGTH_SHORT).show());
            }
        });
    }
    private void presentPaymentSheet() {
        if (paymentIntentClientSecret == null) {
            Toast.makeText(this, "PaymentIntent not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("My Bank Mate")
                .build();

        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
    }
    private void onPaymentSheetResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
            updateAccountBalance();
        } else if (result instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) result;
            Toast.makeText(this, "Payment Failed: " + failedResult.getError(), Toast.LENGTH_LONG).show();
        }
    }
    private void updateAccountBalance() {
        String accountType = selectedAccount.contains("Checking") ? "checkingBalance" : "savingsBalance";

        System.out.println("Updating account balance for: " + accountType);
        System.out.println("Deducting amount: " + amount);

        databaseReference.child("users").child(userId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String currentBalanceStr = snapshot.child(accountType).getValue(String.class);

                    if (currentBalanceStr != null) {
                        double currentBalance = Double.parseDouble(currentBalanceStr);
                        double newBalance = currentBalance - amount;

                        if (newBalance < 0) {
                            runOnUiThread(() -> Toast.makeText(PayBillActivity.this, "Insufficient funds, cannot update balance.", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        databaseReference.child("users").child(userId).child(accountType).setValue(String.valueOf(newBalance))
                                .addOnSuccessListener(aVoid -> {
                                    System.out.println("Firebase update successful for balance: " + newBalance);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(PayBillActivity.this, "Failed to update balance.", Toast.LENGTH_SHORT).show();
                                    System.err.println("Firebase update failed: " + e.getMessage());
                                });
                    } else {
                        Toast.makeText(PayBillActivity.this, "Current balance not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PayBillActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PayBillActivity.this, "Error fetching user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                System.err.println("Firebase error: " + error.getMessage());
            }
        });
    }

}
