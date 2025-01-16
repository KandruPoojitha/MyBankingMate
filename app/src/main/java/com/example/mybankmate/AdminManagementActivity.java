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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminManagementActivity extends AppCompatActivity {

    private EditText adminUsernameField, adminPasswordField, searchAdminField;
    private Button addAdminButton;
    private RecyclerView adminRecyclerView;
    private DatabaseReference adminsRef;
    private ArrayList<Admin> adminList;
    private AdminAdapter adminAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);

        adminsRef = FirebaseDatabase.getInstance().getReference("admins");

        adminUsernameField = findViewById(R.id.adminEmail);
        adminPasswordField = findViewById(R.id.adminPassword);
        searchAdminField = findViewById(R.id.searchAdmin);
        addAdminButton = findViewById(R.id.addAdminButton);
        adminRecyclerView = findViewById(R.id.adminRecyclerView);

        adminRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adminList = new ArrayList<>();
        adminAdapter = new AdminAdapter(adminList, adminsRef);
        adminRecyclerView.setAdapter(adminAdapter);

        loadAdmins();

        addAdminButton.setOnClickListener(v -> {
            String email = adminUsernameField.getText().toString().trim();
            String password = adminPasswordField.getText().toString().trim();

            if (validateInput(email, password)) {
                checkIfAdminExists(email, password);
            }
        });

        searchAdminField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString().trim().toLowerCase();
                searchAdmins(searchQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAdmins() {
        adminsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adminList.clear();
                for (DataSnapshot adminSnapshot : snapshot.getChildren()) {
                    Admin admin = adminSnapshot.getValue(Admin.class);
                    if (admin != null) {
                        admin.setAdminId(adminSnapshot.getKey());
                        adminList.add(admin);
                    }
                }
                adminAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminManagementActivity.this, "Failed to load admins", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAdminExists(String username, String password) {
        adminsRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(AdminManagementActivity.this, "Admin with this username already exists.", Toast.LENGTH_SHORT).show();
                } else {
                    addAdmin(username, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminManagementActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAdmin(String username, String password) {
        String adminId = adminsRef.push().getKey();
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("username", username);
        adminData.put("password", password);

        if (adminId != null) {
            adminsRef.child(adminId).setValue(adminData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminManagementActivity.this, "Admin added successfully", Toast.LENGTH_SHORT).show();
                            adminUsernameField.setText("");
                            adminPasswordField.setText("");
                        } else {
                            Toast.makeText(AdminManagementActivity.this, "Failed to add admin", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void searchAdmins(String query) {
        adminsRef.orderByChild("username").startAt(query).endAt(query + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        adminList.clear();
                        for (DataSnapshot adminSnapshot : snapshot.getChildren()) {
                            Admin admin = adminSnapshot.getValue(Admin.class);
                            if (admin != null) {
                                admin.setAdminId(adminSnapshot.getKey());
                                adminList.add(admin);
                            }
                        }
                        adminAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminManagementActivity.this, "Failed to search admins", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInput(String username, String password) {
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
