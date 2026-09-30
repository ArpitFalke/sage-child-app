package com.arpit.deskbuddy;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_LOCATION = 1001;
    private AppLockManager lockManager;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lockManager = new AppLockManager(this);
        buildUi();
        requestEssentialPermissions();
    }

    private void requestEssentialPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION);
        }
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 64, 48, 48);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("SAGE Child Care");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        Button btnStart = new Button(this);
        btnStart.setText("Start Parental Tracking");
        btnStart.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, TrackingService.class);
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent);
                else startService(intent);
                Toast.makeText(this, "Tracking Started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please grant Location permission first", Toast.LENGTH_SHORT).show();
                requestEssentialPermissions();
            }
        });
        root.addView(btnStart, new LinearLayout.LayoutParams(-1, -2));

        Button btnStop = new Button(this);
        btnStop.setText("Stop Parental Tracking");
        btnStop.setOnClickListener(v -> {
            if (!lockManager.isPinSet()) {
                promptSetPin();
            } else {
                promptVerifyPin();
            }
        });
        root.addView(btnStop, new LinearLayout.LayoutParams(-1, -2));

        Button btnUsage = new Button(this);
        btnUsage.setText("Grant Usage Access");
        btnUsage.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        root.addView(btnUsage, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }

    private void promptSetPin() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
            .setTitle("Set Parental PIN")
            .setMessage("Enter a 4-digit PIN to lock settings")
            .setView(input)
            .setPositiveButton("Save", (dialog, which) -> {
                String pin = input.getText().toString();
                if (pin.length() == 4) {
                    lockManager.setPin(pin);
                    Toast.makeText(this, "PIN Set", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }

    private void promptVerifyPin() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
            .setTitle("Enter Parental PIN")
            .setView(input)
            .setPositiveButton("Unlock", (dialog, which) -> {
                if (lockManager.verifyPin(input.getText().toString())) {
                    stopService(new Intent(this, TrackingService.class));
                    Toast.makeText(this, "Tracking Stopped", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }
}
