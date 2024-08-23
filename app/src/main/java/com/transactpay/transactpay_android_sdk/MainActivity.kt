package com.transactpay.transactpay_android_sdk


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.transactpay.transactpay_android_sdk.Transactpay_start.Companion.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.withTimeoutOrNull
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody

class MainActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            // Find views
            val firstName : EditText = findViewById(R.id.fname)
            val lastName : EditText = findViewById(R.id.lname)
            val phone : EditText = findViewById(R.id.phone)
            val amountEditText: EditText = findViewById(R.id.amountEditText)
            val emailEditText: EditText = findViewById(R.id.emailEditText)
            val apiKeyEditText: EditText = findViewById(R.id.apikeyEditText)
            val encryptionKey : EditText = findViewById(R.id.encryptionKey)
            val submitButton: Button = findViewById(R.id.submitButton)

            // Set up the submit button click listener
            // Set up the submit button click listener
            submitButton.setOnClickListener {
                val fname = firstName.text.toString()
                val lname = lastName.text.toString()
                val phoneNumber = phone.text.toString()
                val amount = amountEditText.text.toString()
                val email = emailEditText.text.toString()
                val apiKey = apiKeyEditText.text.toString()
                val encrypt = encryptionKey.text.toString()


                val intent = Intent(this@MainActivity, ProcessingPage::class.java).apply {
                    putExtra("Fname", fname)
                    putExtra("Lname", lname)
                    putExtra("Phone", phoneNumber)
                    putExtra("AMOUNT", amount)
                    putExtra("EMAIL", email)
                    putExtra("API_KEY", apiKey)
                    putExtra("XMLKEY", encrypt)
                }
                startActivity(intent)
                // Create an intent to start the next activity
            }
        }

}
