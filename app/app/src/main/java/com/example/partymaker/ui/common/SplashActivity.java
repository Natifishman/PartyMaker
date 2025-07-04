package com.example.partymaker.ui.common;

import static com.example.partymaker.utilities.Constants.IS_CHECKED;
import static com.example.partymaker.utilities.Constants.PREFS_NAME;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.ui.auth.LoginActivity;

/**
 * SplashActivity displays the initial splash screen with animations,
 * then navigates the user to the appropriate screen (Main or Login).
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // Duration to stay on splash screen (ms)

    private ImageView imgLogo;
    private View dot1, dot2, dot3;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeViews();
        startSplashFlow();
    }

    // Initialize views and handler
    private void initializeViews() {
        imgLogo = findViewById(R.id.imgLogo);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        handler = new Handler(Looper.getMainLooper());
    }

    // Starts splash screen animations and navigation logic
    private void startSplashFlow() {
        animateLogo();
        animateLoadingDots();
        scheduleNextScreen();
    }

    // Applies fade-in animation to the logo
    private void animateLogo() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.tween);
        imgLogo.startAnimation(fadeIn);
    }

    // Animates loading dots in a wave effect
    private void animateLoadingDots() {
        handler.postDelayed(() -> animateDot(dot1), 500);
        handler.postDelayed(() -> animateDot(dot2), 700);
        handler.postDelayed(() -> animateDot(dot3), 900);

        // Repeats the dot animation loop every 1.5 seconds
        handler.postDelayed(this::animateLoadingDots, 1500);
    }

    // Single dot animation (scale + alpha)
    private void animateDot(View dot) {
        if (dot == null) return;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dot, "scaleX", 1.0f, 1.3f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dot, "scaleY", 1.0f, 1.3f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(dot, "alpha", 0.5f, 1.0f, 0.5f);

        scaleX.setDuration(600);
        scaleY.setDuration(600);
        alpha.setDuration(600);

        scaleX.start();
        scaleY.start();
        alpha.start();
    }

    // Schedules the transition to the next activity after the splash delay
    private void scheduleNextScreen() {
        handler.postDelayed(this::navigateToNextScreen, SPLASH_DELAY);
    }

    // Navigates to MainActivity or LoginActivity based on user state
    private void navigateToNextScreen() {
        if (isFinishing()) return;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = prefs.getBoolean(IS_CHECKED, false);

        Class<?> destination = shouldNavigateToMain(rememberMe)
                ? MainActivity.class
                : LoginActivity.class;

        startActivity(new Intent(this, destination));
        finish();
    }

    // Checks if user is authenticated and 'Remember Me' is checked
    private boolean shouldNavigateToMain(boolean rememberMeChecked) {
        return DBRef.Auth.getCurrentUser() != null && rememberMeChecked;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null); // Prevent memory leaks
        }
    }
}
