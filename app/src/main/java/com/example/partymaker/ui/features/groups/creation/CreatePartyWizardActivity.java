package com.example.partymaker.ui.features.groups.creation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.partymaker.R;
import com.example.partymaker.databinding.ActivityCreatePartyWizardBinding;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class CreatePartyWizardActivity extends AppCompatActivity {
    
    private ActivityCreatePartyWizardBinding binding;
    private CreatePartyViewModel viewModel;
    private int currentStep = 0;
    private List<WizardStep> steps;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePartyWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        viewModel = new ViewModelProvider(this).get(CreatePartyViewModel.class);
        
        setupWizardSteps();
        setupNavigation();
        showCurrentStep();
    }
    
    private void setupWizardSteps() {
        steps = new ArrayList<>();
        // For now, use placeholder fragments until proper implementation
        steps.add(new WizardStep("Party Type", new PlaceholderFragment("Choose your party type")));
        steps.add(new WizardStep("Details", new PlaceholderFragment("Enter party details")));
        steps.add(new WizardStep("Date & Time", new PlaceholderFragment("Select date and time")));
        steps.add(new WizardStep("Location", new PlaceholderFragment("Choose location")));
        steps.add(new WizardStep("Guests", new PlaceholderFragment("Invite guests")));
        steps.add(new WizardStep("Review", new PlaceholderFragment("Review and create")));
    }
    
    private void setupNavigation() {
        binding.btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                animateStepTransition(false);
                currentStep--;
                showCurrentStep();
            }
        });
        
        binding.btnNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                if (currentStep < steps.size() - 1) {
                    animateStepTransition(true);
                    currentStep++;
                    showCurrentStep();
                } else {
                    createParty();
                }
            }
        });
        
        binding.btnSkip.setOnClickListener(v -> {
            if (currentStep < steps.size() - 1) {
                currentStep++;
                showCurrentStep();
            }
        });
    }
    
    private void showCurrentStep() {
        WizardStep step = steps.get(currentStep);
        
        // Update progress
        float progress = (currentStep + 1) / (float) steps.size();
        animateProgress(progress);
        
        // Update step title
        binding.tvStepTitle.setText(step.getTitle());
        binding.tvStepNumber.setText(String.format("Step %d of %d", currentStep + 1, steps.size()));
        
        // Update buttons
        binding.btnBack.setVisibility(currentStep > 0 ? View.VISIBLE : View.INVISIBLE);
        binding.btnNext.setText(currentStep == steps.size() - 1 ? "Create Party" : "Next");
        binding.btnSkip.setVisibility(currentStep < steps.size() - 2 ? View.VISIBLE : View.GONE);
        
        // Load fragment
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, step.getFragment())
            .commit();
    }
    
    private void animateProgress(float progress) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(
            binding.progressIndicator,
            "progress",
            binding.progressIndicator.getProgress(),
            progress * 100
        );
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }
    
    private void animateStepTransition(boolean forward) {
        float translationX = forward ? -50f : 50f;
        
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(binding.fragmentContainer, "alpha", 1f, 0f);
        ObjectAnimator slideOut = ObjectAnimator.ofFloat(binding.fragmentContainer, "translationX", 0f, translationX);
        
        AnimatorSet outAnimation = new AnimatorSet();
        outAnimation.playTogether(fadeOut, slideOut);
        outAnimation.setDuration(150);
        
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.fragmentContainer, "alpha", 0f, 1f);
        ObjectAnimator slideIn = ObjectAnimator.ofFloat(binding.fragmentContainer, "translationX", -translationX, 0f);
        
        AnimatorSet inAnimation = new AnimatorSet();
        inAnimation.playTogether(fadeIn, slideIn);
        inAnimation.setDuration(150);
        inAnimation.setStartDelay(150);
        
        outAnimation.start();
        inAnimation.start();
    }
    
    private boolean validateCurrentStep() {
        Fragment currentFragment = steps.get(currentStep).getFragment();
        if (currentFragment instanceof WizardStepFragment) {
            return ((WizardStepFragment) currentFragment).validate();
        }
        return true;
    }
    
    private void createParty() {
        // Show loading
        binding.progressOverlay.setVisibility(View.VISIBLE);
        
        // Create party with collected data
        viewModel.createParty().observe(this, result -> {
            if (result.isSuccess()) {
                // Show success animation
                showSuccessAnimation();
            } else {
                // Show error
                binding.progressOverlay.setVisibility(View.GONE);
                showError(result.getError());
            }
        });
    }
    
    private void showSuccessAnimation() {
        // Show success image with animation
        binding.lottieSuccess.setVisibility(View.VISIBLE);
        
        // Animate the success image with scale and fade
        binding.lottieSuccess.setAlpha(0f);
        binding.lottieSuccess.setScaleX(0.5f);
        binding.lottieSuccess.setScaleY(0.5f);
        
        binding.lottieSuccess.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .withEndAction(() -> {
                // After animation, wait a bit then finish
                binding.lottieSuccess.postDelayed(() -> finish(), 1500);
            })
            .start();
    }
    
    private void showError(String error) {
        // Show error message
    }
    
    // Inner classes
    private static class WizardStep {
        private final String title;
        private final Fragment fragment;
        
        public WizardStep(String title, Fragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }
        
        public String getTitle() {
            return title;
        }
        
        public Fragment getFragment() {
            return fragment;
        }
    }
    
    public interface WizardStepFragment {
        boolean validate();
        void saveData();
    }
    
    // Placeholder fragment for wizard steps
    public static class PlaceholderFragment extends Fragment implements WizardStepFragment {
        private String message;
        
        public PlaceholderFragment() {
            // Required empty constructor
        }
        
        public PlaceholderFragment(String message) {
            this.message = message;
        }
        
        @Override
        public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container,
                android.os.Bundle savedInstanceState) {
            TextView textView = new TextView(getContext());
            textView.setText(message != null ? message : "Wizard step placeholder");
            textView.setGravity(android.view.Gravity.CENTER);
            textView.setPadding(32, 32, 32, 32);
            textView.setTextSize(18);
            return textView;
        }
        
        @Override
        public boolean validate() {
            return true; // Always valid for placeholder
        }
        
        @Override
        public void saveData() {
            // No data to save for placeholder
        }
    }
}