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
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private ImageButton btnAbout;
    private Button btnLogin, btnPress, btnResetPass;
    private CheckBox cbRememberMe;
    private SignInButton btnGoogleSignIn;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // this 2 lines disables the action bar only in this activity
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // connection between XML and Java
        btnAbout = findViewById(R.id.btnAbout);
        etEmail = findViewById(R.id.etEmailL);
        etPassword = findViewById(R.id.etPasswordL);
        btnLogin = findViewById(R.id.btnLogin);
        btnPress = findViewById(R.id.btnPressL);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnResetPass = findViewById(R.id.btnResetPass);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // start animation on ImageButton btnAbout
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(Login.this, R.anim.fadein);
        btnAbout.startAnimation(myFadeInAnimation);

        evantHandler();
    }

    // Login Button Onclick
    private void evantHandler() {
        btnLogin.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SignIn();
                    }

                    // connection between firebase and login button
                    private void SignIn() {
                        String email = etEmail.getText().toString();
                        String password = etPassword.getText().toString();
                        if (email.matches("") || password.matches("")) {
                            Toast.makeText(Login.this, "input both to login", Toast.LENGTH_SHORT).show();
                        } else {
                            final ProgressDialog pd =
                                    ProgressDialog.show(Login.this, "connecting", "please wait... ", true);
                            pd.show();
                            DBref.Auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(
                                            Login.this,
                                            task -> {
                                                if (task.isSuccessful()) {
                                                    // Saves IsChecked - True/False in app's cache
                                                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                                                    SharedPreferences.Editor editor = settings.edit();
                                                    editor.putBoolean(IS_CHECKED, cbRememberMe.isChecked());
                                                    editor.apply();

                                                    Intent intent = new Intent();
                                                    Toast.makeText(Login.this, "Connected", Toast.LENGTH_SHORT).show();
                                                    intent.setClass(getBaseContext(), MainActivity.class);
                                                    btnAbout.clearAnimation();
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(
                                                                    Login.this, "Invalid Email or Password", Toast.LENGTH_SHORT)
                                                            .show();
                                                    pd.dismiss();
                                                    btnResetPass.setVisibility(View.VISIBLE);
                                                }
                                            });
                        }
                    }
                });

        // Google Sign In button click listener
        btnGoogleSignIn.setOnClickListener(
                v -> signInWithGoogle());

        // Press Here Onclick
        btnPress.setOnClickListener(
                view -> {
                    // when click on "press here" it takes you to RegisterActivity
                    Intent i = new Intent(Login.this, Register.class);
                    btnAbout.clearAnimation();
                    startActivity(i);
                });
        btnResetPass.setOnClickListener(
                v -> {
                    Intent i = new Intent(Login.this, ResetPassword.class);
                    btnAbout.clearAnimation();
                    startActivity(i);
                });
        btnAbout.setOnClickListener(
                v -> {
                    Intent i = new Intent(Login.this, Intro.class);
                    startActivity(i);
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(Login.this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth
                .signInWithCredential(credential)
                .addOnCompleteListener(
                        this,
                        task -> {
                            if (task.isSuccessful()) {
                                // Sign in success
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    String email = firebaseUser.getEmail();
                                    String username = firebaseUser.getDisplayName();


                                    User u = new User(email, username);

                                    // Replace dots with spaces in the email to make it a valid key in Firebase
                                    assert email != null;
                                    DBref.refUsers.child(email.replace('.', ' ')).setValue(u);
                                }

                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(Login.this, task.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
    }
}