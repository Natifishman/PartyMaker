package com.example.partymaker;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.User;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class Register extends AppCompatActivity {
  private EditText etEmail, etUsername, etPassword;
  private Button btnRegister, btnPress;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    // connection between XML and Java
    etEmail = findViewById(R.id.etEmailR);
    etUsername = findViewById(R.id.etUsername);
    etPassword = findViewById(R.id.etPasswordR);
    btnRegister = findViewById(R.id.btnRegister);
    btnPress = findViewById(R.id.btnPressR);

    evantHandler();
  }

  public void sendNotification() {
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.cake)
            .setContentTitle("The registration was successful")
            .setContentText("Welcome " + etUsername.getText() + "!");
    NotificationManager mNotificationManager =
        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    mNotificationManager.notify(1, mBuilder.build());
  }

  private void evantHandler() {
    // register button
    btnRegister.setOnClickListener(view -> SignUp());
    // press here if you have an account
    btnPress.setOnClickListener(
        view -> {
          // when click on "press here" it takes you to LoginActivity
          Intent i = new Intent(Register.this, Login.class);
          startActivity(i);
        });
  }

  // connection between firebase and register button
  private void SignUp() {
    final String email = etEmail.getText().toString().toLowerCase();
    final String username = etUsername.getText().toString();
    String password = etPassword.getText().toString();
    if (email.matches("") || username.matches("") || password.matches("")) {
      Toast.makeText(Register.this, "input all to register", Toast.LENGTH_SHORT).show();
    } else {
      DBref.Auth.createUserWithEmailAndPassword(email, password)
          .addOnCompleteListener(
              Register.this,
              task -> {
                if (task.isSuccessful()) {
                  sendNotification();
                  // put email and username in database
                  User u = new User(email, username);
                  DBref.refUsers.child(email.replace('.', ' ')).setValue(u);
                  // Login Successful
                  Toast.makeText(Register.this, "Register Successful !", Toast.LENGTH_SHORT).show();
                  // when click on "Register" it takes you to LoginActivity
                  Intent i = new Intent(Register.this, Login.class);
                  startActivity(i);
                } else {
                  Exception e = task.getException();
                  if (e instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(Register.this, "האימייל כבר קיים במערכת. נסה להתחבר.", Toast.LENGTH_SHORT).show();
                  } else if (e instanceof FirebaseAuthWeakPasswordException) {
                    Toast.makeText(Register.this, "הסיסמה חלשה מדי. בחר סיסמה חזקה יותר (לפחות 6 תווים).", Toast.LENGTH_SHORT).show();
                  } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(Register.this, "כתובת האימייל אינה תקינה.", Toast.LENGTH_SHORT).show();
                  } else if (e instanceof FirebaseAuthException) {
                    String errorCode = ((FirebaseAuthException) e).getErrorCode();
                    Toast.makeText(Register.this, "שגיאה: " + errorCode, Toast.LENGTH_SHORT).show();
                  } else {
                    Toast.makeText(Register.this, "שגיאת הרשמה לא ידועה.", Toast.LENGTH_SHORT).show();
                  }
                }
              });
    }
  }
}
