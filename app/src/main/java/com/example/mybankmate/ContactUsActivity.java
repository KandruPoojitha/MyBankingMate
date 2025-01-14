package com.example.mybankmate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        setupExpandableContact(R.id.contact1, R.id.contact1_details, R.id.toggle_contact1);
        setupExpandableContact(R.id.contact2, R.id.contact2_details, R.id.toggle_contact2);
    }

    private void setupExpandableContact(int contactId, int contactDetailsId, int toggleIconId) {
        TextView contact = findViewById(contactId);
        LinearLayout contactDetails = findViewById(contactDetailsId);
        ImageView toggleIcon = findViewById(toggleIconId);

        contact.setOnClickListener(v -> {
            if (contactDetails.getVisibility() == View.GONE) {
                contactDetails.setVisibility(View.VISIBLE);
                toggleIcon.setImageResource(R.drawable.ic_minus);
            } else {
                contactDetails.setVisibility(View.GONE);
                toggleIcon.setImageResource(R.drawable.ic_plus);
            }
        });
    }
}