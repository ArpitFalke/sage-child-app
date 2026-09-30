package com.sage.childapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPermissions = findViewById<Button>(R.id.btnEnablePermissions)
        val btnStartTracking = findViewById<Button>(R.id.btnStartTracking)

        // We will add permission logic here in Step 3
        btnPermissions.setOnClickListener {
            // TODO: Request Location and Usage Stats permissions
        }

        // We will add location tracking logic here in Step 4
        btnStartTracking.setOnClickListener {
            // TODO: Start foreground service for GPS tracking
        }
    }
}
