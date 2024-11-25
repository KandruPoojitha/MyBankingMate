package com.example.mybankmate;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

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
                if (user.getEmail().toLowerCase().contains(text.toLowerCase())) {
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
        private final TextView emailText, checkingAccountText, savingsAccountText, editButton, deleteButton;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.userEmailText);
            checkingAccountText = itemView.findViewById(R.id.checkingAccountText);
            savingsAccountText = itemView.findViewById(R.id.savingsAccountText);
            editButton = itemView.findViewById(R.id.editUserButton);
            deleteButton = itemView.findViewById(R.id.deleteUserButton);
        }

        void bind(User user) {
            emailText.setText(user.getEmail());
            checkingAccountText.setText(user.getCheckingAccountNumber());
            savingsAccountText.setText(user.getSavingsAccountNumber());

            editButton.setOnClickListener(v -> editUser(user));
            deleteButton.setOnClickListener(v -> deleteUser(user));
        }

        private void editUser(User user) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("Edit User");

            View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_user, null);
            EditText emailEdit = view.findViewById(R.id.editEmail);
            emailEdit.setText(user.getEmail());

            dialog.setView(view);
            dialog.setPositiveButton("Update", (dialogInterface, i) -> {
                String newEmail = emailEdit.getText().toString();
                if (!newEmail.isEmpty()) {
                    usersRef.child(user.getUid()).child("email").setValue(newEmail)
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "User updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show());
                }
            });
            dialog.setNegativeButton("Cancel", null);
            dialog.show();
        }

        private void deleteUser(User user) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("Delete User")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Delete", (dialogInterface, i) -> {
                        usersRef.child(user.getUid()).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, "Deletion failed", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
