package com.arpit.deskbuddy;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int REQ_NOTIFICATIONS = 1001;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (Settings.canDrawOverlays(this)) startSage();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 64, 48, 48);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("DeskBuddy SAGE");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView info = new TextView(this);
        info.setText("\nSAGE Android Edition\n\n" +
                "1. Allow Display over other apps.\n" +
                "2. Return here.\n" +
                "3. SAGE will appear.\n\n" +
                "Tap SAGE = pet\nDrag SAGE = move\nLong press = controls\nTap elsewhere = walk there");
        info.setTextSize(16);
        info.setPadding(0, 32, 0, 32);
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));

        Button overlay = new Button(this);
        overlay.setText("Allow Overlay Permission");
        overlay.setOnClickListener(v -> requestOverlayPermission());
        root.addView(overlay, new LinearLayout.LayoutParams(-1, -2));

        Button start = new Button(this);
        start.setText("Start SAGE");
        start.setOnClickListener(v -> startSage());
        root.addView(start, new LinearLayout.LayoutParams(-1, -2));

        Button stop = new Button(this);
        stop.setText("Stop SAGE");
        stop.setOnClickListener(v -> stopService(new Intent(this, PetService.class)));
        root.addView(stop, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }

    private void requestOverlayPermission() {
        try {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    private void startSage() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
            return;
        }
        Intent intent = new Intent(this, PetService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent);
        else startService(intent);
    }
}
