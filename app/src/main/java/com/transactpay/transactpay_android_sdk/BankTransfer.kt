package com.transactpay.transactpay_android_sdk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.text.TextWatcher
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.transactpay.transactpay_android_sdk.Transactpay_start.Companion.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale

class BankTransfer : AppCompatActivity() {


    private lateinit var countdownTextView: TextView

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bank_transfer)

        val merchantName = intent.getStringExtra("MERCHANT_NAME")
        val amountString = intent.getStringExtra("AMOUNT")
        val email = intent.getStringExtra("EMAIL")
        val apiKey = intent.getStringExtra("API_KEY")
        val encryptKey : String = intent.getStringExtra("XMLKEY").toString()
        val referenceNumber : String = intent.getStringExtra("REFERENCE_NUMBER").toString()

        // Format the amount
        val amount = amountString?.toDoubleOrNull()
        val formattedAmount = amount?.let {
            // Format the amount without currency symbol
            val numberFormat = java.text.NumberFormat.getNumberInstance(Locale.getDefault())
            numberFormat.maximumFractionDigits = 2
            numberFormat.minimumFractionDigits = 2
            val amountFormatted = numberFormat.format(it)
            // Prefix with "NGN"
            "NGN $amountFormatted"
        } ?: "Invalid amount"

        // Use the data as needed in this activity
        findViewById<TextView>(R.id.merchantNameTextView).text = merchantName
        findViewById<TextView>(R.id.shopName).text = merchantName
        findViewById<TextView>(R.id.amountTextView).text = formattedAmount
        findViewById<TextView>(R.id.emailTextView).text = email
        countdownTextView = findViewById(R.id.countdown)

        // Countdown Timer Logic
        val countdownTime = 10 * 60 * 1000L

        val timer = object : CountDownTimer(countdownTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60

                val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                countdownTextView.text = "Account details is valid for this transaction only and will expire in $timeFormatted minutes"
            }

            override fun onFinish() {
                countdownTextView.text = "The transaction has expired."
                findViewById<Button>(R.id.payButton).isEnabled = false
            }
        }

        timer.start()

        val rsaPublicKeyXml = EncryptionUtils.decodeBase64AndExtractKey(encryptKey)
        val url = "https://payment-api-service.transactpay.ai/payment/order/pay"
        try {
            CoroutineScope(Dispatchers.IO).launch {

                val response = postEncryptedPayload(url,  apiKey.toString(), rsaPublicKeyXml, referenceNumber)

                val jsonResponse = JSONObject(response)

                // Log the actual response body content
                Log.d(TAG, "HTTP Response Body: $jsonResponse")

                val status = jsonResponse.getString("status")
                val data = jsonResponse.getJSONObject("data")
                val paymentDetail = data.getJSONObject("paymentDetail");

                withContext(Dispatchers.Main) {
                    if (status == "success") {

                        // Show the account number
                        findViewById<TextView>(R.id.accountNumber).setText(paymentDetail.getString("recipientAccount"))

                    } else {
                        //redirect to failed page
                        val intent = Intent(this@BankTransfer, Failed::class.java).apply {
                            putExtra("status", jsonResponse.getString("status"))
                            putExtra("code", jsonResponse.getString("statusCode"))
                            putExtra("message", jsonResponse.getString("message"))
                        }
                        startActivity(intent)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "This is the error $e")
        }

    }

    private suspend fun postEncryptedPayload(
        url: String,
        apiKey : String,
        publicKeyXml : String,
        reference : String
    ): String? {
        return try {
            val payload = """
                    {
                        "reference": "$reference",
                        "paymentoption": "bank-transfer",
                        "country": "NG",
                        "BankTransfer": {
                            "bankcode": "000017"
                        }
                    }
                    """.trimIndent()

            val encryptedData = EncryptionUtils.encryptPayloadRSA(payload, publicKeyXml) ?: throw Exception("Encryption failed")

            val json = """
                {
                    "data": "$encryptedData"
                }
            """.trimIndent()

            Log.d(TAG, "API KEY IS : $apiKey")
            Log.d(TAG, "Payload is : $payload")

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody: RequestBody = json.toRequestBody(mediaType)

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("accept", "application/json")
                .addHeader("api-key", "$apiKey")
                .addHeader("content-type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                response.body?.string()
            } else {
                response.body?.string()
            }
        } catch (e: Exception) {
            "Exception: ${e.message}"
        }
    }

    private fun formatAmount(amount: Double?): String {
        val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault())
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2
        return amount?.let {
            val amountFormatted = numberFormat.format(it)
            "NGN $amountFormatted"
        } ?: "Invalid amount"
    }

}
