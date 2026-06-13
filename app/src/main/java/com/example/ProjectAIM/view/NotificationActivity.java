package com.example.ProjectAIM.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ProjectAIM.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * View for notification settings.
 * Manages SMS permission and saved toggle state so the app only enables SMS alerts
 * when the user has chosen to allow them.
 */
public class NotificationActivity extends AppCompatActivity {

    // Preference keys keep the user's SMS setting available after the app closes
    private static final String PREFS = "notif_prefs";
    private static final String PREF_SMS_ENABLED = "pref_sms_enabled";

    private Chip chipPermissionState;
    private MaterialSwitch switchEnableSms;

    // Handles the runtime SMS permission result after Android shows the permission prompt
    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    // Keep the switch, saved preference, and permission display aligned after approval
                    switchEnableSms.setChecked(true);
                    setPrefSmsEnabled(true);
                    updatePermissionUI(true);
                    Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    // Turn SMS back off when permission is denied so the UI does not show a false enabled state
                    switchEnableSms.setChecked(false);
                    setPrefSmsEnabled(false);
                    updatePermissionUI(false);
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    // Initializes notification settings and restores the user's
    // saved SMS preference state
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        chipPermissionState = findViewById(R.id.chipPermissionState);
        switchEnableSms = findViewById(R.id.switchEnableSms);
        Button buttonBackToInventory = findViewById(R.id.buttonBackToInventory);

        // Sync the UI with both the saved preference and Android's real permission state
        syncPermissionUI();

        // Permission is requested only when the user actively enables SMS alerts
        configureSmsToggle();

        buttonBackToInventory.setOnClickListener(view -> finish());
    }

    // Revalidates permission status because Android settings can
    // change while the application is not active
    @Override
    protected void onResume() {
        super.onResume();

        // Recheck permission in case the user changed SMS access in system settings
        syncPermissionUI();
    }

    // Handles enabling and disabling SMS notifications from the settings switch
    private void configureSmsToggle() {
        switchEnableSms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setPrefSmsEnabled(isChecked);

            if (isChecked) {
                if (!hasSmsPermission()) {
                    smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
                } else {
                    updatePermissionUI(true);
                }
            } else {
                updatePermissionUI(false);
            }
        });
    }

    // Keeps the screen accurate when saved preference and actual Android permission differ
    private void syncPermissionUI() {
        boolean userWantsSms = getPrefSmsEnabled();
        boolean hasSmsPermission = hasSmsPermission();

        switchEnableSms.setChecked(userWantsSms);
        updatePermissionUI(userWantsSms && hasSmsPermission);
    }

    // Checks Android's current permission state instead of relying only on saved app preference
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Keeps the permission chip aligned with whether SMS alerts can actually be used
    private void updatePermissionUI(boolean active) {
        chipPermissionState.setText(active
                ? getString(R.string.permission_granted)
                : getString(R.string.permission_denied));

        @ColorRes int backgroundColor = active
                ? android.R.color.holo_green_light
                : android.R.color.holo_red_light;

        chipPermissionState.setChipBackgroundColorResource(backgroundColor);
        chipPermissionState.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    // Reads the user's saved SMS choice so the setting persists between app sessions
    private boolean getPrefSmsEnabled() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_SMS_ENABLED, false);
    }

    // Saves the user's SMS choice separately from Android's actual permission state
    private void setPrefSmsEnabled(boolean enabled) {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_SMS_ENABLED, enabled)
                .apply();
    }
}