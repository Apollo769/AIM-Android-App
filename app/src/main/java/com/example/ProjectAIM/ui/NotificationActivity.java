package com.example.ProjectAIM.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ProjectAIM.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;

public class NotificationActivity extends AppCompatActivity {

    // shared preference keys for saving SMS toggle state
    private static final String PREFS = "notif_prefs";
    private static final String PREF_SMS_ENABLED = "pref_sms_enabled";

    private Chip chipPermissionState;      // shows SMS permission status
    private MaterialSwitch switchEnableSms; // switch to enable or disable SMS

    // handles requesting SMS permission at runtime
    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    // user allowed permission
                    switchEnableSms.setChecked(true);
                    setPrefSmsEnabled(true);
                    updatePermissionUI(true);
                    Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    // user denied permission
                    switchEnableSms.setChecked(false);
                    setPrefSmsEnabled(false);
                    updatePermissionUI(false);
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // link UI elements
        chipPermissionState = findViewById(R.id.chipPermissionState);
        switchEnableSms = findViewById(R.id.switchEnableSms);
        Button buttonBackToInventory = findViewById(R.id.buttonBackToInventory);

        // check if SMS was enabled before and if permission is granted
        boolean userWantsSms = getPrefSmsEnabled();
        boolean hasPerm = hasSmsPermission();

        // update switch and chip based on saved state
        switchEnableSms.setChecked(userWantsSms);
        updatePermissionUI(userWantsSms && hasPerm);

        // when switch is toggled on or off
        switchEnableSms.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            setPrefSmsEnabled(isChecked);
            if (isChecked) {
                // if enabled, make sure permission is granted
                if (!hasSmsPermission()) {
                    // request permission if not already granted
                    smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
                } else {
                    // already granted, update chip UI
                    updatePermissionUI(true);
                }
            } else {
                // switch turned off, mark permission as inactive
                updatePermissionUI(false);
            }
        });

        // go back to inventory screen
        buttonBackToInventory.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh UI in case permission changed while activity wasn’t active
        boolean userWantsSms = getPrefSmsEnabled();
        boolean hasPerm = hasSmsPermission();
        switchEnableSms.setChecked(userWantsSms);
        updatePermissionUI(userWantsSms && hasPerm);
    }

    // checks if SEND_SMS permission is granted
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // updates chip color and text based on permission state
    private void updatePermissionUI(boolean active) {
        chipPermissionState.setText(active
                ? getString(R.string.permission_granted)
                : getString(R.string.permission_denied));
        @ColorRes int bg = active ? android.R.color.holo_green_light : android.R.color.holo_red_light;
        chipPermissionState.setChipBackgroundColorResource(bg);
        chipPermissionState.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    // get saved preference for SMS toggle
    private boolean getPrefSmsEnabled() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_SMS_ENABLED, false);
    }

    // save current SMS toggle state
    private void setPrefSmsEnabled(boolean enabled) {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_SMS_ENABLED, enabled)
                .apply();
    }
}
