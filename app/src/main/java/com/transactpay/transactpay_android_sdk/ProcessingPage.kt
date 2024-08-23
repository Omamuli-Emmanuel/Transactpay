package com.transactpay.transactpay_android_sdk

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.NumberFormat
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.transactpay.transactpay_android_sdk.Transactpay_start.Companion.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale

class ProcessingPage : AppCompatActivity() {

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.processingpage)
        //generate random ref number
        fun generateReferenceNumber(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            return (1..12)
                .map { chars.random() }
                .joinToString("")
        }

        // Retrieve the data from the intent
        val fname = intent.getStringExtra("Fname")
        val lname = intent.getStringExtra("Lname")
        val mobile = intent.getStringExtra("Phone")
        val amountString = intent.getStringExtra("AMOUNT")
        val email = intent.getStringExtra("EMAIL")
        val apiKey = intent.getStringExtra("API_KEY")
        val hashKey : String = intent.getStringExtra("XMLKEY").toString()

        Log.d(TAG, apiKey.toString())

        // Format the amount
        val amount = amountString?.toIntOrNull()
        val formattedAmount = amount?.let {
            // Format the amount without currency symbol
            val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
            numberFormat.maximumFractionDigits = 2
            numberFormat.minimumFractionDigits = 2
            val amountFormatted = numberFormat.format(it)
            // Prefix with "NGN"
            "NGN $amountFormatted"
        } ?: "Invalid amount"

        // Generate a reference number
        val referenceNumber = generateReferenceNumber()

        val rsaPublicKeyXml = EncryptionUtils.decodeBase64AndExtractKey(hashKey)
        val url = "https://payment-api-service.transactpay.ai/payment/order/create"
        try {
            CoroutineScope(Dispatchers.IO).launch {

                val response = postEncryptedPayload(
                    url, fname, lname, mobile, email, amount, referenceNumber, apiKey.toString(), rsaPublicKeyXml
                )

                val jsonResponse = JSONObject(response)

                // Log the actual response body content
                Log.d(TAG, "HTTP Response Body: $jsonResponse")

                val status = jsonResponse.getString("status")

                if (status == "success") {
                    // Parse the JSON response
                    //check order status
                    val data = jsonResponse.getJSONObject("data")
                    val order = data.getJSONObject("order")
                    val customer = data.getJSONObject("customer")
                    val subsidiary = data.getJSONObject("subsidiary")

                    Log.d(TAG, "First Reference $referenceNumber")

                    // Create an intent and pass the data to the next activity
                    val intent = Intent(this@ProcessingPage, Transactpay_start::class.java).apply {
                        putExtra("Fname", customer.optString("firstName"))
                        putExtra("Lname", customer.optString("Daniels"))
                        putExtra("Phone", customer.optString("mobile"))
                        putExtra("MERCHANT_NAME", subsidiary.optString("name"))
                        putExtra("AMOUNT", order.optString("amount"))
                        putExtra("EMAIL", customer.optString("email"))
                        putExtra("REF", customer.optString("email"))
                        putExtra("APIKEY", apiKey)
                        putExtra("XMLKEY", hashKey)
                        putExtra("REFERENCE_NUMBER", referenceNumber)
                    }
                    startActivity(intent)
                } else {
                    //redirect to failed page
                    val intent = Intent(this@ProcessingPage, Failed::class.java).apply {
                        putExtra("status", jsonResponse.getString("status"))
                        putExtra("code", jsonResponse.getString("statusCode"))
                        putExtra("message", jsonResponse.getString("message"))
                    }
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "This is the error $e")
        }

    }
    private suspend fun postEncryptedPayload(
        url: String,
        firstName : String?,
        lastName : String?,
        mobile : String?,
        email : String?,
        amount : Int?,
        ref : String,
        apiKey : String,
        publicKeyXml : String
    ): String? {
        return try {
            val payload = """
                    {
                        "customer": {
                            "firstname": "$firstName",
                            "lastname": "$lastName",
                            "mobile": "$mobile",
                            "country": "NG",
                            "email": "$email"
                        },
                        "order": {
                            "amount": $amount,
                            "reference": "$ref",
                            "description": "Pay",
                            "currency": "NGN"
                        },
                        "payment": {
                            "RedirectUrl": "https://www.hi.com"
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

    private fun generateReferenceNumber(): String {
        return "REF-${System.currentTimeMillis()}"
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
