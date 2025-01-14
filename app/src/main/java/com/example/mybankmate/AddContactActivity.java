package com.example.mybankmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddContactActivity extends AppCompatActivity {

    private static final String TAG = "AddContactActivity";

    private EditText contactNameInput, contactEmailInput, confirmEmailInput, contactMobileInput;
    private Button addContactButton;
    private ImageView backButton;

    private FirebaseAuth auth;
    private DatabaseReference contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();

        if (uid != null) {
            contactsRef = FirebaseDatabase.getInstance().getReference("contacts").child(uid);
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initialize views
        contactNameInput = findViewById(R.id.contact_name_input);
        contactEmailInput = findViewById(R.id.contact_email_input);
        confirmEmailInput = findViewById(R.id.confirm_email_input);
        contactMobileInput = findViewById(R.id.contact_mobile_input);
        addContactButton = findViewById(R.id.add_contact_button);
        backButton = findViewById(R.id.back_button);

        // Add contact on button click
        addContactButton.setOnClickListener(v -> addContact());

        // Back button functionality
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void addContact() {
        String name = contactNameInput.getText().toString().trim();
        String email = contactEmailInput.getText().toString().trim();
        String confirmEmail = confirmEmailInput.getText().toString().trim();
        String mobile = contactMobileInput.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(mobile)) {
            Toast.makeText(this, "Please provide at least an email or mobile number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(email) && !email.equals(confirmEmail)) {
            Toast.makeText(this, "Email addresses do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare contact data
        Map<String, Object> contactData = new HashMap<>();
        contactData.put("name", name);
        if (!TextUtils.isEmpty(email)) {
            contactData.put("email", email);
        }
        if (!TextUtils.isEmpty(mobile)) {
            contactData.put("mobile", mobile);
        }

        // Push contact data to Firebase
        contactsRef.push().setValue(contactData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Contact added successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK); // Notify parent activity
                        finish(); // Close the activity
                    } else {
                        Log.e(TAG, "Error adding contact: " + task.getException().getMessage());
                        Toast.makeText(this, "Failed to add contact.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
