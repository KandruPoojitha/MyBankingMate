package com.example.mybankmate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private EditText newUserEmail, newUserPassword, searchUser;
    private Button addUserButton;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        // Initialize Firebase and UI elements
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        newUserEmail = findViewById(R.id.newUserEmail);
        newUserPassword = findViewById(R.id.newUserPassword);
        addUserButton = findViewById(R.id.addUserButton);
        searchUser = findViewById(R.id.searchUser);
        userRecyclerView = findViewById(R.id.userRecyclerView);

        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, usersRef);
        userRecyclerView.setAdapter(userAdapter);

        // Add User Button
        addUserButton.setOnClickListener(v -> {
            String email = newUserEmail.getText().toString().trim();
            String password = newUserPassword.getText().toString().trim();
            if (validateInput(email, password)) {
                addUser(email, password);
            }
        });

        // Search functionality
        searchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userAdapter.filter(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        loadUsers();
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addUser(final String email, final String password) {
        String accountNumber = generateAccountNumber();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("accountNumber", accountNumber);
                            userData.put("isActive", true);

                            usersRef.child(accountNumber).setValue(userData)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(this, "User added with Account Number: " + accountNumber, Toast.LENGTH_LONG).show();
                                            newUserEmail.setText("");
                                            newUserPassword.setText("");
                                        } else {
                                            Toast.makeText(this, "Failed to add user data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Failed to create user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateAccountNumber() {
        return String.valueOf(1000000000 + new Random().nextInt(900000000));
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserManagementActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
