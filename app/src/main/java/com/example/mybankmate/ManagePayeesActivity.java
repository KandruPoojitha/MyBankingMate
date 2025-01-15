package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManagePayeesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PayeeAdapter payeeAdapter;
    private ArrayList<Payee> payeeList = new ArrayList<>();
    private TextView noPayeesMessage;
    private Button addPayeesButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_payees);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view);
        noPayeesMessage = findViewById(R.id.no_payees_message);
        addPayeesButton = findViewById(R.id.add_payees_button);

        // Set up Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("payees");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        payeeAdapter = new PayeeAdapter(payeeList, this::navigateToEditPayee);
        recyclerView.setAdapter(payeeAdapter);

        // Fetch payees from Firebase
        fetchPayees(userId);

        // Handle Add Payee button click
        addPayeesButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagePayeesActivity.this, AddPayeeActivity.class);
            startActivity(intent);
        });

        // Handle back button click
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void fetchPayees(String userId) {
        databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                payeeList.clear();
                if (snapshot.exists()) {
                    // Populate payee list if data exists
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Payee payee = dataSnapshot.getValue(Payee.class);
                        if (payee != null) {
                            payee.setId(dataSnapshot.getKey()); // Add Firebase key for editing/deleting
                            payeeList.add(payee);
                        }
                    }
                    // Show RecyclerView and hide "No payees" message
                    recyclerView.setVisibility(View.VISIBLE);
                    noPayeesMessage.setVisibility(View.GONE);
                } else {
                    // No payees found
                    recyclerView.setVisibility(View.GONE);
                    noPayeesMessage.setVisibility(View.VISIBLE);
                }
                payeeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManagePayeesActivity.this, "Failed to load payees", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToEditPayee(Payee payee) {
        Intent intent = new Intent(this, EditPayeeActivity.class);
        intent.putExtra("payeeId", payee.getId());
        intent.putExtra("payeeName", payee.getName());
        intent.putExtra("payeeAccount", payee.getAccountId());
        startActivity(intent);
    }
}
