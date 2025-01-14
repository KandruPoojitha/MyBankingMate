package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentActivity extends AppCompatActivity {

    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;

    private String userId;
    private DatabaseReference databaseReference;
    private double amount;
    private String selectedAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get passed data
        amount = getIntent().getDoubleExtra("amount", 0);
        selectedAccount = getIntent().getStringExtra("selectedAccount");

        // Initialize Stripe
        PaymentConfiguration.init(this, "pk_test_51PlVh8P9Bz7XrwZPnWMN2upZk3x00s3soZgJgM5QTMuwCNoZPBdGtmPRXB29vBnFvOXjEAv2vntLuQaWbPpEHOmP00D7pelv0B");
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        // Create PaymentIntent and present the PaymentSheet
        createPaymentIntent();
    }

    private void createPaymentIntent() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String backendUrl = "https://8494-76-68-243-40.ngrok-free.app/create-payment-intent";

                JSONObject payload = new JSONObject();
                payload.put("amount", (int) (amount * 100)); // Convert amount to cents

                String response = HttpUtils.post(backendUrl, payload.toString());
                JSONObject responseJson = new JSONObject(response);

                paymentIntentClientSecret = responseJson.getString("clientSecret");

                // Present the PaymentSheet on the main thread
                runOnUiThread(this::presentPaymentSheet);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(PaymentActivity.this, "Failed to create PaymentIntent", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void presentPaymentSheet() {
        if (paymentIntentClientSecret == null) {
            Toast.makeText(this, "PaymentIntent not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configure the PaymentSheet
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("My Bank Mate")
                .build();

        // Present the PaymentSheet
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
    }

    private void onPaymentSheetResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
            updateAccountBalance();
        } else if (result instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        } else if (result instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) result;
            Toast.makeText(this, "Payment Failed: " + failedResult.getError(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateAccountBalance() {
        String accountType = selectedAccount.contains("Checking") ? "checkingBalance" : "savingsBalance";

        databaseReference.child("users").child(userId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double currentBalance = snapshot.child(accountType).getValue(Double.class);
                    double newBalance = currentBalance - amount;

                    // Update the balance in Firebase
                    databaseReference.child("users").child(userId).child(accountType).setValue(newBalance)
                            .addOnSuccessListener(aVoid -> navigateToMoveMoney())
                            .addOnFailureListener(e -> Toast.makeText(PaymentActivity.this, "Failed to update balance", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PaymentActivity.this, "Error updating balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMoveMoney() {
        Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
        intent.putExtra("navigateToFragment", "moveMoney");
        startActivity(intent);
        finish();
    }
}
