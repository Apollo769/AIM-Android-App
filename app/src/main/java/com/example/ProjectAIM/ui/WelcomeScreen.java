package com.example.ProjectAIM.ui;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectAIM.R;

public class WelcomeScreen extends AppCompatActivity {

    EditText editUsername, editPassword;
    Button buttonSignIn, buttonCreateAccount;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        // link the input fields and buttons to layout
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // create or open a local database for user login info
        db = openOrCreateDatabase("AIM_DB", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS users(username TEXT PRIMARY KEY, password TEXT)");

        // set click events for both buttons
        buttonSignIn.setOnClickListener(this::signIn);
        buttonCreateAccount.setOnClickListener(this::createAccount);
    }

    private void signIn(View v) {
        // get username and password input
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // make sure both fields are filled
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        // check if username and password match an existing record
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username=? AND password=?",
                new String[]{username, password});

        if (cursor.moveToFirst()) {
            // login success
            Toast.makeText(this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
            cursor.close();

            // go to the inventory screen after successful login
            startActivity(new Intent(this, InventoryActivity.class));
            finish();
        }
        else {
            // login failed
            Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    private void createAccount(View v) {
        // get new username and password
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // check if both fields are filled
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        // make sure username isn't already taken
        Cursor check = db.rawQuery("SELECT * FROM users WHERE username=?", new String[]{username});
        if (check.moveToFirst()) {
            Toast.makeText(this, "Username already exists.", Toast.LENGTH_SHORT).show();
        }
        else {
            // insert new user record into database
            db.execSQL("INSERT INTO users(username, password) VALUES(?, ?)", new Object[]{username, password});
            Toast.makeText(this, "Account created successfully. You can now sign in.", Toast.LENGTH_SHORT).show();
        }
        check.close();
    }
}
