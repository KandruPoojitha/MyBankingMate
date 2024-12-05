package com.example.mybankmate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Random;

public class UserManagementActivity extends AppCompatActivity {

    private EditText newUserEmail, newUserPassword, newUserMobile, searchUser;
    private AutoCompleteTextView newUserAddress;
    private Button addUserButton;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    private PlacesClient placesClient;
    private List<String> addressSuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCQsIJqnQs7sbv6gr-LT1amFavJoPFvCpQ");
        }
        placesClient = Places.createClient(this);

        newUserEmail = findViewById(R.id.newUserEmail);
        newUserPassword = findViewById(R.id.newUserPassword);
        newUserMobile = findViewById(R.id.newUserMobile);
        newUserAddress = findViewById(R.id.newUserAddress);
        addUserButton = findViewById(R.id.addUserButton);
        searchUser = findViewById(R.id.searchUser);
        userRecyclerView = findViewById(R.id.userRecyclerView);

        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, usersRef);
        userRecyclerView.setAdapter(userAdapter);

        addressSuggestions = new ArrayList<>();

        // Set address suggestions
        newUserAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) { // Trigger suggestions after 3 characters
                    fetchAddressSuggestions(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        addUserButton.setOnClickListener(v -> {
            String email = newUserEmail.getText().toString().trim();
            String password = newUserPassword.getText().toString().trim();
            String mobile = newUserMobile.getText().toString().trim();
            String address = newUserAddress.getText().toString().trim();
            if (validateInput(email, password, mobile, address)) {
                addUser(email, password, mobile, address);
            }
        });

        searchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userAdapter.filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadUsers();
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
            newUserAddress.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, addressSuggestions));
        }).addOnFailureListener(e -> {
            Toast.makeText(UserManagementActivity.this, "Error fetching address suggestions", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInput(String email, String password, String mobile, String address) {
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mobile) || !mobile.matches("\\d{10}")) {
            Toast.makeText(this, "Invalid mobile number", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Address cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addUser(final String email, final String password, final String mobile, final String address) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = task.getResult().getUser().getUid();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("mobile", mobile);
                        userData.put("address", address);
                        usersRef.child(userId).setValue(userData)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(this, "User added successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null) userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserManagementActivity.this, "Error loading users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
