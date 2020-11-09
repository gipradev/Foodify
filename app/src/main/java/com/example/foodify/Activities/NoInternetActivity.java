package com.example.foodify.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.foodify.R;


public class NoInternetActivity extends AppCompatActivity {

    private Window window;
    private ImageButton refresh;
    private AnimationSet animSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.BLACK);
        }

        refresh = (ImageButton) findViewById(R.id.buttonRefresh);

        animSet = new AnimationSet(true);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setFillAfter(true);
        animSet.setFillEnabled(true);

        final RotateAnimation animRotate = new RotateAnimation(0.0f, -360.0f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        animRotate.setDuration(3000);
        animRotate.setFillAfter(true);
        animSet.addAnimation(animRotate);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh.startAnimation(animSet);

                new Handler().postDelayed(new Runnable() {
                    @Override

                    public void run() {
                        startActivity(new Intent(getApplicationContext(),SplashScreen.class));
                        finish();

                    }
                },3000);

            }
        });

    }
}
