package com.arpit.deskbuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
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
        info.setText("\nChild Device Setup\n\n" +
                "1. Grant Location Permissions.\n" +
                "2. Grant Usage Access Permissions.\n" +
                "3. Start Live Tracking.\n");
        info.setTextSize(16);
        info.setPadding(0, 32, 0, 32);
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));

        Button btnLocation = new Button(this);
        btnLocation.setText("Grant Location Permission");
        // TODO: Add location permission logic
        root.addView(btnLocation, new LinearLayout.LayoutParams(-1, -2));

        Button btnUsage = new Button(this);
        btnUsage.setText("Grant Usage Access");
        btnUsage.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        root.addView(btnUsage, new LinearLayout.LayoutParams(-1, -2));

        Button btnStart = new Button(this);
        btnStart.setText("Start Parental Tracking");
        // TODO: Start foreground service for tracking
        root.addView(btnStart, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }
}
