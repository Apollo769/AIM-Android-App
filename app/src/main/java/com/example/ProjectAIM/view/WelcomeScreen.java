package com.example.ProjectAIM.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectAIM.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * View for the sign-in and account creation screen.
 * Uses Firebase Auth so credentials are not stored locally in plaintext.
 */
public class WelcomeScreen extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private FirebaseAuth auth;

    private static final int MIN_PASSWORD_LENGTH = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        // Connect the login inputs and buttons used by this screen
        editEmail = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        Button buttonSignIn = findViewById(R.id.buttonSignIn);
        Button buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Firebase handles authentication instead of storing credentials in SQLite
        auth = FirebaseAuth.getInstance();

        buttonSignIn.setOnClickListener(this::signIn);
        buttonCreateAccount.setOnClickListener(this::createAccount);
    }

    private void signIn(View view) {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Stop sign-in early when required email/password input is missing or invalid
        if (hasInvalidEmailPasswordInput(email, password)) {
            return;
        }

        // Firebase Auth performs the credential check asynchronously
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();

                        // Close this screen so the user cannot return to login with the back button
                        startActivity(new Intent(this, InventoryActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createAccount(View view) {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Stop account creation early when required email/password input is missing or invalid
        if (hasInvalidEmailPasswordInput(email, password)) {
            return;
        }

        // Enforce a basic password length rule before sending the request to Firebase
        if (password.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(this, "Password must be at least 8 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase creates the account without exposing password storage to the app
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account created successfully. You can now sign in.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Account could not be created.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Centralizes shared validation so sign-in and account creation follow the same input rules
    private boolean hasInvalidEmailPasswordInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }
}