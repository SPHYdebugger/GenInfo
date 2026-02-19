package com.comismar.informes.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.comismar.informes.R;
import com.comismar.informes.view.utils.AppSettings;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            Class<?> targetActivity = AppSettings.isRememberLoginEnabled(this)
                    ? MainActivity.class
                    : LoginActivity.class;

            Intent intent = new Intent(SplashActivity.this, targetActivity);
            startActivity(intent);
            finish();
        }, 3000);
    }
}
