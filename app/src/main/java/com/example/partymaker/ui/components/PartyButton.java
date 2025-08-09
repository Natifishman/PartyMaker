package com.example.partymaker.ui.components;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.partymaker.R;
import com.google.android.material.button.MaterialButton;

public class PartyButton extends MaterialButton {
    
    private static final float SCALE_DOWN = 0.95f;
    private static final float SCALE_NORMAL = 1.0f;
    private static final int ANIMATION_DURATION_DOWN = 100;
    private static final int ANIMATION_DURATION_UP = 200;
    
    private AnimatorSet currentAnimation;
    
    public PartyButton(@NonNull Context context) {
        super(context);
        init();
    }
    
    public PartyButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public PartyButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Set up touch listener for animations
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        animateButtonDown();
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        break;
                        
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        animateButtonUp();
                        break;
                }
                return false;
            }
        });
        
        // Set up ripple effect
        setupRippleEffect();
        
        // Set default style
        setTextAppearance(R.style.TextAppearance_PartyMaker_Button);
        setCornerRadius(getResources().getDimensionPixelSize(R.dimen.corner_radius_large));
        setElevation(getResources().getDimension(R.dimen.elevation_level2));
    }
    
    private void animateButtonDown() {
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        
        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(this, "scaleX", SCALE_DOWN);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(this, "scaleY", SCALE_DOWN);
        
        currentAnimation = new AnimatorSet();
        currentAnimation.playTogether(scaleXDown, scaleYDown);
        currentAnimation.setDuration(ANIMATION_DURATION_DOWN);
        currentAnimation.start();
    }
    
    private void animateButtonUp() {
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        
        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(this, "scaleX", SCALE_NORMAL);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(this, "scaleY", SCALE_NORMAL);
        
        currentAnimation = new AnimatorSet();
        currentAnimation.playTogether(scaleXUp, scaleYUp);
        currentAnimation.setDuration(ANIMATION_DURATION_UP);
        currentAnimation.setInterpolator(new OvershootInterpolator());
        currentAnimation.start();
    }
    
    private void setupRippleEffect() {
        // Enhanced ripple effect with custom color
        int rippleColor = getContext().getColor(R.color.ripple_primary);
        ColorStateList colorStateList = ColorStateList.valueOf(rippleColor);
        
        RippleDrawable rippleDrawable = new RippleDrawable(
            colorStateList,
            getBackground(),
            null
        );
        
        setBackground(rippleDrawable);
    }
    
    public void animatePulse() {
        ObjectAnimator scaleXPulse = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 1.1f, 1.0f);
        ObjectAnimator scaleYPulse = ObjectAnimator.ofFloat(this, "scaleY", 1.0f, 1.1f, 1.0f);
        
        AnimatorSet pulseAnimation = new AnimatorSet();
        pulseAnimation.playTogether(scaleXPulse, scaleYPulse);
        pulseAnimation.setDuration(300);
        pulseAnimation.start();
    }
    
    public void animateSuccess() {
        // Animate color change to success
        int successColor = getContext().getColor(R.color.accent_success);
        animateBackgroundColor(successColor);
        animatePulse();
    }
    
    public void animateError() {
        // Animate shake effect for error
        ObjectAnimator shake = ObjectAnimator.ofFloat(this, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(500);
        shake.start();
        
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }
    
    private void animateBackgroundColor(int toColor) {
        // Animate background tint color change
        ObjectAnimator colorAnimation = ObjectAnimator.ofArgb(
            this,
            "backgroundTintList",
            getBackgroundTintList().getDefaultColor(),
            toColor
        );
        colorAnimation.setDuration(300);
        colorAnimation.start();
    }
}