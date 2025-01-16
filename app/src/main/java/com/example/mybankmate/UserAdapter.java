package com.example.mybankmate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userList;
    private final List<User> filteredList;
    private final DatabaseReference usersRef;
    private Context context;

    public UserAdapter(List<User> userList, DatabaseReference usersRef) {
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList);
        this.usersRef = usersRef;
    }

    public void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(userList);
        } else {
            for (User user : userList) {
                if (user.getEmail() != null && user.getEmail().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = filteredList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView emailText, checkingAccountText, savingsAccountText, editButton, deleteButton, disableButton;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.userEmailText);
            checkingAccountText = itemView.findViewById(R.id.checkingAccountText);
            savingsAccountText = itemView.findViewById(R.id.savingsAccountText);
            editButton = itemView.findViewById(R.id.editUserButton);
            deleteButton = itemView.findViewById(R.id.deleteUserButton);
            disableButton = itemView.findViewById(R.id.disableUserButton);
        }

        void bind(User user) {
            if (user.getUid() == null || user.getUid().isEmpty()) {
                // If UID is missing, retrieve it by email
                usersRef.orderByChild("email").equalTo(user.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String uid = childSnapshot.getKey();
                                user.setUid(uid);  // Update the user object with the correct UID
                                bindUserData(uid, user);  // Continue with the binding process
                                break;
                            }
                        } else {
                            Toast.makeText(context, "User UID not found for: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to retrieve UID", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            // Proceed with binding if UID is already available
            bindUserData(user.getUid(), user);
        }

        private void bindUserData(String uid, User user) {
            emailText.setText(user.getEmail());
            checkingAccountText.setText(user.getCheckingAccountNumber());
            savingsAccountText.setText(user.getSavingsAccountNumber());

            usersRef.child(uid).child("isDisabled").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isDisabled = snapshot.getValue(Boolean.class) != null && snapshot.getValue(Boolean.class);
                    disableButton.setText(isDisabled ? "Enable" : "Disable");
                    disableButton.setTextColor(isDisabled ? context.getResources().getColor(android.R.color.holo_green_dark) : context.getResources().getColor(android.R.color.holo_orange_dark));

                    disableButton.setOnClickListener(v -> toggleUserStatus(user, isDisabled));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to check user status", Toast.LENGTH_SHORT).show();
                }
            });

            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("USER_ID", uid);
                context.startActivity(intent);
            });

            deleteButton.setOnClickListener(v -> deleteUser(user));
        }

        private void deleteUser(User user) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("Delete User")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Delete", (dialogInterface, i) -> {
                        usersRef.child(user.getUid()).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "User deleted from database", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, "Database deletion failed", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void toggleUserStatus(User user, boolean isDisabled) {
            if (user.getUid() == null) {
                Toast.makeText(context, "User UID is missing. Cannot update status.", Toast.LENGTH_SHORT).show();
                return;
            }

            usersRef.child(user.getUid()).child("isDisabled").setValue(!isDisabled)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, isDisabled ? "User enabled" : "User disabled", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update user status", Toast.LENGTH_SHORT).show());
        }
    }
}
