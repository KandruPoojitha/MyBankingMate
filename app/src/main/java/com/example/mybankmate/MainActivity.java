package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Already on Home, no action needed
                return true;
            } else if (id == R.id.nav_accounts) {
                startActivity(new Intent(MainActivity.this, AccountsActivity.class));
                return true;
            } else if (id == R.id.nav_move_money) {
                startActivity(new Intent(MainActivity.this, MoveMoneyActivity.class));
                return true;
            } else if (id == R.id.nav_more) {
                startActivity(new Intent(MainActivity.this, MoreActivity.class));
                return true;
            }

            return false;
        });

    }
}
