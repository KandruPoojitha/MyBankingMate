package com.example.mybankmate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FAQActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        setupExpandableFAQ(R.id.question1, R.id.answer1, R.id.toggle1);
        setupExpandableFAQ(R.id.question2, R.id.answer2, R.id.toggle2);
        setupExpandableFAQ(R.id.question3, R.id.answer3, R.id.toggle3);
        setupExpandableFAQ(R.id.question4, R.id.answer4, R.id.toggle4);
        setupExpandableFAQ(R.id.question5, R.id.answer5, R.id.toggle5);
    }

    private void setupExpandableFAQ(int questionId, int answerId, int toggleIconId) {
        LinearLayout questionLayout = findViewById(questionId);
        LinearLayout answerLayout = findViewById(answerId);
        ImageView toggleIcon = findViewById(toggleIconId);

        questionLayout.setOnClickListener(v -> {
            if (answerLayout.getVisibility() == View.GONE) {
                answerLayout.setVisibility(View.VISIBLE);
                toggleIcon.setImageResource(R.drawable.ic_minus);
            } else {
                answerLayout.setVisibility(View.GONE);
                toggleIcon.setImageResource(R.drawable.ic_plus);
            }
        });
    }
}
