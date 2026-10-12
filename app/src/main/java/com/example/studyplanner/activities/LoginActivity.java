package com.example.studyplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studyplanner.R;
import com.example.studyplanner.utils.PrefsManager;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Optional local sign-in. No credentials are sent anywhere and no password is stored; it only
 * captures a display name for the dashboard. The user can skip straight to the app.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private EditText inputName;
    private EditText inputEmail;
    private EditText inputPassword;
    private PrefsManager prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new PrefsManager(this);
        setContentView(R.layout.activity_login);

        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);

        findViewById(R.id.buttonLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.buttonSkip).setOnClickListener(v -> continueToApp(""));
    }

    private void attemptLogin() {
        layoutEmail.setError(null);
        layoutPassword.setError(null);

        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString();
        boolean valid = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.login_error_email));
            valid = false;
        }
        if (password.length() < 6) {
            layoutPassword.setError(getString(R.string.login_error_password));
            valid = false;
        }
        if (!valid) {
            return;
        }

        String name = inputName.getText().toString().trim();
        if (name.isEmpty()) {
            name = email.substring(0, email.indexOf('@'));
        }
        continueToApp(name);
    }

    private void continueToApp(String name) {
        prefs.setUserName(name);
        prefs.setLoggedIn(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
