package com.arpit.deskbuddy;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class MainActivity extends Activity {
    private static final int REQ_LOCATION = 1001;
    private static final int REQ_NOTIFICATIONS = 1002;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        requestEssentialPermissions();
    }

    private void requestEssentialPermissions() {
        // Request Location
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION);
        }
        // Request Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
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

        TextView info = new TextView(this);
        info.setText("\nChild Device Setup\n\n1. Location Permission Granted.\n2. Usage Access Granted.\n3. Start Tracking.\n");
        info.setTextSize(16);
        info.setPadding(0, 32, 0, 32);
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));

        Button btnUsage = new Button(this);
        btnUsage.setText("Grant Usage Access");
        btnUsage.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "Usage access not available on this device", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(btnUsage, new LinearLayout.LayoutParams(-1, -2));

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
        btnStop.setText("Stop Tracking");
        btnStop.setOnClickListener(v -> stopService(new Intent(this, TrackingService.class)));
        root.addView(btnStop, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }
}
