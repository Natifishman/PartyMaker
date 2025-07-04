package com.example.partymaker.ui.auth;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
  private Button btnWhite, btnBlack, btnReset, btnHelp, btnHide;
  private RelativeLayout rltReset;
  private TextView tvForgotPass, tvHelp, tvInstructions, tvHide;
  private EditText etInputEmail;
  private ImageView imgWhiteCake;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_reset_password);

    // this 3 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    actionBar.hide();

    btnBlack = findViewById(R.id.btnBlack);
    btnWhite = findViewById(R.id.btnWhite);
    btnHelp = findViewById(R.id.btnHelp);
    btnReset = findViewById(R.id.btnReset);
    btnHide = findViewById(R.id.btnHide);
    etInputEmail = findViewById(R.id.etInputEmail);
    tvForgotPass = findViewById(R.id.tvForgotPass);
    tvHelp = findViewById(R.id.tvHelp);
    tvInstructions = findViewById(R.id.tvInstructions);
    tvHide = findViewById(R.id.tvHide);
    rltReset = findViewById(R.id.rltReset);
    imgWhiteCake = findViewById(R.id.imgWhiteCake);

    eventHandler();
  }

  private void ResetPass() {
    final String email = etInputEmail.getText().toString(); // the user email
    if (email.matches("")) {
      Toast.makeText(ResetPasswordActivity.this, "input email", Toast.LENGTH_SHORT).show();
    } else {
      FirebaseAuth.getInstance()
          .sendPasswordResetEmail(email)
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  Toast.makeText(
                          ResetPasswordActivity.this, "Email successfully sent", Toast.LENGTH_SHORT)
                      .show();
                } else {
                  Toast.makeText(
                          ResetPasswordActivity.this, "Email Error sending", Toast.LENGTH_SHORT)
                      .show();
                }
              });
    }
  }

  private void eventHandler() {
    btnReset.setOnClickListener(v -> ResetPass());
    btnWhite.setOnClickListener(
        view -> {
          // code that invert color(In this case rom White/Black to Black/White)
          final float[] NEGATIVE = {
            -1.0f, 0, 0, 0, 255, 0, -1.0f, 0, 0, 255, 0, 0, -1.0f, 0, 255, 0, 0, 0, 1.0f, 0
          }; // every number is a color  (Red,Green,Blue,Alpha)

          imgWhiteCake.setColorFilter(
              new ColorMatrixColorFilter(NEGATIVE)); // set color to the color above
          imgWhiteCake.setColorFilter(0x00ff0000, PorterDuff.Mode.ADD); // invert back
          imgWhiteCake = findViewById(R.id.imgWhiteCake);
          rltReset.setBackgroundColor(Color.WHITE);
          tvForgotPass.setTextColor(Color.BLACK);
          tvHelp.setTextColor(Color.BLACK);
          tvInstructions.setTextColor(Color.BLACK);
          tvHide.setTextColor(Color.BLACK);
          etInputEmail.setHintTextColor(Color.BLACK);
          etInputEmail.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
          etInputEmail.setTextColor(Color.BLACK);
          btnReset.setTextColor(Color.BLACK);
          btnHelp.setTextColor(Color.BLACK);
          btnHide.setTextColor(Color.BLACK);
          btnWhite.setVisibility(View.INVISIBLE);
          btnBlack.setVisibility(View.VISIBLE);
        });
    btnBlack.setOnClickListener(
        view -> {
          // code that invert color(In this case rom Black/White to White/Black)
          final float[] NEGATIVE = {
            -1.0f, 0, 0, 0, 255, 0, -1.0f, 0, 0, 255, 0, 0, -1.0f, 0, 255, 0, 0, 0, 1.0f, 0
          }; // every number is a color  (Red,Green,Blue,Alpha)

          imgWhiteCake.setColorFilter(
              new ColorMatrixColorFilter(NEGATIVE)); // set color to the color above
          rltReset.setBackgroundColor(Color.BLACK);
          tvForgotPass.setTextColor(Color.WHITE);
          tvHelp.setTextColor(Color.WHITE);
          tvInstructions.setTextColor(Color.WHITE);
          tvHide.setTextColor(Color.WHITE);
          etInputEmail.setHintTextColor(Color.WHITE);
          etInputEmail.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
          etInputEmail.setTextColor(Color.WHITE);
          btnReset.setTextColor(Color.WHITE);
          btnHelp.setTextColor(Color.WHITE);
          btnHide.setTextColor(Color.WHITE);
          btnBlack.setVisibility(View.INVISIBLE);
          btnWhite.setVisibility(View.VISIBLE);
        });
    btnHelp.setOnClickListener(
        view -> {
          tvInstructions.setVisibility(View.VISIBLE);
          tvHelp.setVisibility(View.INVISIBLE);
          btnHelp.setVisibility(View.INVISIBLE);
          tvHide.setVisibility(View.VISIBLE);
          btnHide.setVisibility(View.VISIBLE);
        });
    btnHide.setOnClickListener(
        view -> {
          tvInstructions.setVisibility(View.INVISIBLE);
          tvHelp.setVisibility(View.VISIBLE);
          btnHelp.setVisibility(View.VISIBLE);
          tvHide.setVisibility(View.INVISIBLE);
          btnHide.setVisibility(View.INVISIBLE);
        });
  }
}
