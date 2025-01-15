package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MoreFragment extends Fragment {

    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        auth = FirebaseAuth.getInstance();

        TextView profileText = view.findViewById(R.id.profile);
        profileText.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });
        TextView faqText = view.findViewById(R.id.faq);
        faqText.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FAQActivity.class);
            startActivity(intent);
        });
        TextView contactText = view.findViewById(R.id.contact);
        contactText.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ContactUsActivity.class);
            startActivity(intent);
        });
        TextView privacyText = view.findViewById(R.id.privacy);
        privacyText.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PrivacyLegalActivity.class);
            startActivity(intent);
        });


        TextView resetPasswordText = view.findViewById(R.id.resetPassword);
        resetPasswordText.setOnClickListener(v -> resetPassword());

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void resetPassword() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            if (!TextUtils.isEmpty(email)) {
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Password reset email sent!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to send reset email. Try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getContext(), "No email associated with this account", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
