package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);
        lottieAnimationView.setAnimation(R.raw.splash_animation);
        lottieAnimationView.playAnimation();

        // Delay and start the main activity after the animation completes
        new Handler().postDelayed(() -> {
            Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(mainIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}
