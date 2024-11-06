package com.example.mybankmate;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.ViewHolder> {
    private final ArrayList<Admin> adminList;
    private final DatabaseReference adminsRef;

    public AdminAdapter(ArrayList<Admin> adminList, DatabaseReference adminsRef) {
        this.adminList = adminList;
        this.adminsRef = adminsRef;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Admin admin = adminList.get(position);
        holder.adminEmail.setText(admin.getEmail());

        holder.editButton.setOnClickListener(v -> editAdmin(holder.itemView.getContext(), admin));
        holder.deleteButton.setOnClickListener(v -> deleteAdmin(holder.itemView.getContext(), admin));
    }

    private void editAdmin(Context context, Admin admin) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Admin");

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setText(admin.getEmail());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newEmail = input.getText().toString().trim();
            if (!newEmail.isEmpty()) {
                adminsRef.child(admin.getAdminId()).child("email").setValue(newEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Admin updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteAdmin(Context context, Admin admin) {
        adminsRef.child(admin.getAdminId()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Admin deleted", Toast.LENGTH_SHORT).show();
                        adminList.remove(admin);
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView adminEmail;
        public Button editButton, deleteButton;

        @SuppressLint("WrongViewCast")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            adminEmail = itemView.findViewById(R.id.adminEmail);
            editButton = itemView.findViewById(R.id.editAdminButton);
            deleteButton = itemView.findViewById(R.id.deleteAdminButton);
        }
    }
}
