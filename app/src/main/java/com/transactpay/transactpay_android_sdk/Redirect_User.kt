package com.transactpay.transactpay_android_sdk

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.NumberFormat
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class Redirect_User : AppCompatActivity() {

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.redirect_user)

        // Retrieve the data from the intent
        val merchantName = intent.getStringExtra("MERCHANT_NAME")
        val amountString = intent.getStringExtra("AMOUNT")
        val email = intent.getStringExtra("EMAIL")
        val apiKey = intent.getStringExtra("API_KEY")
        val mode = intent.getStringExtra("MODE")

        // Format the amount
        val amount = amountString?.toDoubleOrNull()
        val formattedAmount = amount?.let {
            // Format the amount without currency symbol
            val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
            numberFormat.maximumFractionDigits = 2
            numberFormat.minimumFractionDigits = 2
            val amountFormatted = numberFormat.format(it)
            // Prefix with "NGN"
            "NGN $amountFormatted"
        } ?: "Invalid amount"

        // Use the data as needed in this activity
        findViewById<TextView>(R.id.merchantNameTextView).text = merchantName
        findViewById<TextView>(R.id.amountTextView).text = formattedAmount
        findViewById<TextView>(R.id.emailTextView).text = email
//        findViewById<TextView>(R.id.apiKeyTextView).text = apiKey
//        findViewById<TextView>(R.id.modeTextView).text = mode

        //back to options icon and click
        val backToOptions : ImageView = findViewById(R.id.backToOptions)
        backToOptions.setOnClickListener {
            val intent = Intent(this, CardActivity::class.java).apply {
                putExtra("MERCHANT_NAME", merchantName)
                putExtra("AMOUNT", amountString)
                putExtra("EMAIL", email)
                putExtra("API_KEY", apiKey)
                putExtra("MODE", mode)
            }
            startActivity(intent)
        }

    }
}
