package com.example.mybankmate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
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

public class ProfileActivity extends AppCompatActivity {

    private EditText legalName, pronouns, genderIdentity, mobilePhone, email;
    private AutoCompleteTextView address;

    private ImageButton editPersonalInfo, editContactInfo, editAddress;
    private Button saveButton;

    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private String userId;
    private PlacesClient placesClient;
    private List<String> addressSuggestions;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCQsIJqnQs7sbv6gr-LT1amFavJoPFvCpQ");
        }
        placesClient = Places.createClient(this);
        legalName = findViewById(R.id.legalName);
        pronouns = findViewById(R.id.pronouns);
        genderIdentity = findViewById(R.id.genderIdentity);
        mobilePhone = findViewById(R.id.mobilePhone);
        email = findViewById(R.id.email);
        address = findViewById(R.id.address);

        editPersonalInfo = findViewById(R.id.editPersonalInfo);
        editContactInfo = findViewById(R.id.editContactInfo);
        editAddress = findViewById(R.id.editAddress);
        saveButton = findViewById(R.id.saveButton);

        loadUserData();

        editPersonalInfo.setOnClickListener(v -> toggleEditing(true, legalName, pronouns, genderIdentity));
        editContactInfo.setOnClickListener(v -> toggleEditing(true, mobilePhone, email));
        editAddress.setOnClickListener(v -> toggleEditing(true, address));

        saveButton.setOnClickListener(v -> {
            if (isEditing) {
                saveUserData();
            }
        });
        addressSuggestions = new ArrayList<>();
        address.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    fetchAddressSuggestions(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    legalName.setText(snapshot.child("legalName").getValue(String.class));
                    pronouns.setText(snapshot.child("pronouns").getValue(String.class));
                    genderIdentity.setText(snapshot.child("gender").getValue(String.class));
                    mobilePhone.setText(snapshot.child("mobile").getValue(String.class));
                    email.setText(snapshot.child("email").getValue(String.class));
                    address.setText(snapshot.child("address").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEditing(boolean enable, EditText... fields) {
        for (EditText field : fields) {
            field.setEnabled(enable);
        }
        isEditing = enable;
        saveButton.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void saveUserData() {
        String updatedLegalName = legalName.getText().toString().trim();
        String updatedPronouns = pronouns.getText().toString().trim();
        String updatedGender = genderIdentity.getText().toString().trim();
        String updatedMobile = mobilePhone.getText().toString().trim();
        String updatedEmail = email.getText().toString().trim();
        String updatedAddress = address.getText().toString().trim();

        if (validateInput(updatedLegalName, updatedMobile, updatedEmail, updatedAddress)) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("legalName", updatedLegalName);
            updates.put("pronouns", updatedPronouns);
            updates.put("gender", updatedGender);
            updates.put("mobile", updatedMobile);
            updates.put("email", updatedEmail);
            updates.put("address", updatedAddress);

            userRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    toggleEditing(false, legalName, pronouns, genderIdentity, mobilePhone, email, address);
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validateInput(String legalName, String mobile, String email, String address) {
        if (TextUtils.isEmpty(legalName)) {
            Toast.makeText(this, "Legal name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mobile) || mobile.length() != 10 || !mobile.matches("\\d+")) {
            Toast.makeText(this, "Enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Address cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void fetchAddressSuggestions(String query) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            addressSuggestions.clear();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                addressSuggestions.add(prediction.getFullText(null).toString());
            }
            address.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, addressSuggestions));
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching address suggestions", Toast.LENGTH_SHORT).show();
        });
    }
}
