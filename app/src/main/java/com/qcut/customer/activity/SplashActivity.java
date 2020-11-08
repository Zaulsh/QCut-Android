package com.qcut.customer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.qcut.customer.R;
import com.qcut.customer.utils.AppUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        AppUtils.initUIActivity(this);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 1500ms
                onShowLoginActivity();
            }
        }, 1500);
    }

    private void onShowLoginActivity() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        this.finish();
    }
}
