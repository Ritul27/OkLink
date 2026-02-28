package com.example.oklink

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.ComponentName
import android.content.pm.PackageManager
import android.net.Uri
import android.app.role.RoleManager
import android.os.Build


class MainActivity : AppCompatActivity() {

    private lateinit var autoBtn: Button
    private lateinit var manualBtn: Button
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔹 Request Browser Role
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)

            if (roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)) {

                    val intent =
                        roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER)

                    startActivity(intent)
                }
            }
        }

        autoBtn = findViewById(R.id.autoModeBtn)
        manualBtn = findViewById(R.id.manualModeBtn)

        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        updateAutoButton()

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val componentName = ComponentName(this, AutoScanActivity::class.java)
        val packageManager = packageManager

// Set correct button text on app start
        val isEnabled = prefs.getBoolean("auto_mode", false)
        autoBtn.text = if (isEnabled) "Disable Auto Protection" else "Enable Auto Protection"

        autoBtn.setOnClickListener {

            val currentState = prefs.getBoolean("auto_mode", false)
            val newState = !currentState

            // Save new state
            prefs.edit().putBoolean("auto_mode", newState).apply()

            if (newState) {

                // Enable AutoScanActivity
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )

                autoBtn.text = "Disable Auto Protection"

                // Open default app settings screen
                val intent = Intent(android.provider.Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)

            } else {
                // DISABLE AutoScanActivity
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )

                autoBtn.text = "Enable Auto Protection"
            }
        }

        manualBtn.setOnClickListener {
            val intent = Intent(this, ManualScanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateAutoButton() {
        val isEnabled = prefs.getBoolean("auto_mode", false)
        val componentName = ComponentName(this, AutoScanActivity::class.java)
        val packageManager = packageManager

        if (isEnabled) {
            packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } else {
            packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}