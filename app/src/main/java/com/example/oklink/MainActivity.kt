package com.example.oklink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import android.content.Intent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val autoBtn = findViewById<Button>(R.id.autoModeBtn)
        val manualBtn = findViewById<Button>(R.id.manualModeBtn)

        autoBtn.setOnClickListener {
            Toast.makeText(this, "Auto mode works when you open links!", Toast.LENGTH_LONG).show()
        }

        manualBtn.setOnClickListener {
            val intent = Intent(this, ManualScanActivity::class.java)
            startActivity(intent)
        }
    }
}