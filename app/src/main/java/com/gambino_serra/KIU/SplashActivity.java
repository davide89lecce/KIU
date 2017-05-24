package com.gambino_serra.KIU;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * La classe modella lo splash screen di avvio dell'applicazione Kiu.
 */
public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);

        ImageView img = (ImageView) findViewById(R.id.icon_img);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent in = new Intent(SplashActivity.this, Login.class);
                startActivity(in);
                finish();
                }
        }, SPLASH_DISPLAY_LENGTH);
    }
}