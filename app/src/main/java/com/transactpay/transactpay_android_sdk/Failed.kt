package com.transactpay.transactpay_android_sdk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity

class Failed : AppCompatActivity() {

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.failed)

        val status = intent.getStringExtra("status")
        val code = intent.getStringExtra("code")
        val message = intent.getStringExtra("message")

        findViewById<TextView>(R.id.status).setText(status.toString())
        findViewById<TextView>(R.id.code).setText(code.toString())
        findViewById<TextView>(R.id.messge).setText(message.toString())

        findViewById<Button>(R.id.payButton).setOnClickListener{
            val intent = Intent(this@Failed, MainActivity::class.java)
            startActivity(intent)
        }

    }
}
