package com.example.partymaker;

import static com.example.partymaker.utilities.Constants.IS_CHECKED;
import static com.example.partymaker.utilities.Constants.PREFS_NAME;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.example.partymaker.data.DBref;

public class Login extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private ImageButton btnAbout;
    private Button btnLogin, btnPress, btnResetPass;
    private TextView tvReset;
    private CheckBox cbRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //this 2 lines disables the action bar only in this activity
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //connection between XML and Java
        btnAbout =  (ImageButton) findViewById(R.id.btnAbout);
        etEmail = (EditText) findViewById(R.id.etEmailL);
        etPassword = (EditText) findViewById(R.id.etPasswordL);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnPress = (Button) findViewById(R.id.btnPressL);
        cbRememberMe = (CheckBox) findViewById(R.id.cbRememberMe);
        btnResetPass = (Button) findViewById(R.id.btnResetPass);
//        tvReset = (TextView) findViewById(R.id.tvReset);

        //start animation on ImageButton btnAbout
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(Login.this, R.anim.fadein);
        btnAbout.startAnimation(myFadeInAnimation);

        evantHandler();
    }

    //Login Button Onclick
    private void evantHandler() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
            //connection between firebase and login button
            private void SignIn() {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                if (email.matches("")||password.matches("")) {
                    Toast.makeText(Login.this, "input both to login", Toast.LENGTH_SHORT).show();
                }
                else {
                    final ProgressDialog pd = ProgressDialog.show(Login.this, "connecting", "please wait... ", true);
                    pd.show();
                    DBref.Auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //Saves IsChecked - True/False in app's cache
                                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putBoolean(IS_CHECKED, cbRememberMe.isChecked());
                                editor.commit();

                                Intent intent = new Intent();
                                Toast.makeText(Login.this, "Connected", Toast.LENGTH_SHORT).show();
                                intent.setClass(getBaseContext(), MainActivity.class);
                                btnAbout.clearAnimation();
                                startActivity(intent);
                            } else {
                                Toast.makeText(Login.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                                btnResetPass.setVisibility(View.VISIBLE);
                                tvReset.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });
        //Press Here Onclick
        btnPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //when click on "press here" it takes you to RegisterActivity
                Intent i = new Intent(Login.this, Register.class);
                btnAbout.clearAnimation();
                startActivity(i);
            }
        });
        btnResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, ResetPassword.class);
                btnAbout.clearAnimation();
                startActivity(i);
            }
        });
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, Intro.class);
                startActivity(i);
            }
        });
    }
}
