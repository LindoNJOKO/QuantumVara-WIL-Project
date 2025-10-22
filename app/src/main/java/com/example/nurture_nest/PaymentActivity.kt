package com.example.nurture_nest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etReference: EditText
    private lateinit var btnPay: Button

    private lateinit var paymentSheet: PaymentSheet
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)

        // Initialize Stripe
        PaymentConfiguration.init(
            applicationContext,
            BuildConfig.STRIPE_PUBLISHABLE_KEY
        )

        // Initialize views
        etAmount = findViewById(R.id.etAmount)
        etReference = findViewById(R.id.etReference)
        btnPay = findViewById(R.id.btnPay)

        // Initialize payment sheet
        paymentSheet = PaymentSheet(this, ::onPaymentResult)

        // Payment button click
        btnPay.setOnClickListener {
            val amountText = etAmount.text.toString().trim()

            if (amountText.isEmpty()) {
                Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = try {
                (amountText.toDouble() * 100).toInt()
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount < 50) {
                Toast.makeText(this, "Minimum R0.50", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startPayment(amount)
        }
    }

    private fun startPayment(amount: Int) {
        Log.d("Payment", "Starting payment for R${amount / 100.0}")

        btnPay.isEnabled = false
        btnPay.text = "Processing..."

        val request = PaymentRequest(amount)

        ApiClient.instance.createPayment(request).enqueue(object : Callback<PaymentResponse> {
            override fun onResponse(call: Call<PaymentResponse>, response: Response<PaymentResponse>) {
                btnPay.isEnabled = true
                btnPay.text = "Pay Now"

                if (response.isSuccessful && response.body() != null) {
                    val clientSecret = response.body()!!.clientSecret
                    Log.d("Payment", "Got client secret")
                    showPaymentSheet(clientSecret)
                } else {
                    Log.e("Payment", "Failed: ${response.code()}")
                    Toast.makeText(
                        this@PaymentActivity,
                        "Payment setup failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                btnPay.isEnabled = true
                btnPay.text = "Pay Now"
                Log.e("Payment", "Error: ${t.message}", t)
                Toast.makeText(
                    this@PaymentActivity,
                    "Connection error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun showPaymentSheet(clientSecret: String) {
        val configuration = PaymentSheet.Configuration(
            merchantDisplayName = "NurtureNest"
        )

        paymentSheet.presentWithPaymentIntent(clientSecret, configuration)
    }

    private fun onPaymentResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                Log.d("Payment", "Payment successful!")
                Toast.makeText(this, "✅ Payment Successful!", Toast.LENGTH_LONG).show()
                savePaymentRecord()

                // Navigate to receipts
                startActivity(Intent(this, ReceiptsActivity::class.java))
                finish()
            }
            is PaymentSheetResult.Canceled -> {
                Log.d("Payment", "Payment canceled")
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Log.e("Payment", "Payment failed: ${result.error.message}")
                Toast.makeText(
                    this,
                    "Payment failed: ${result.error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun savePaymentRecord() {
        val amount = etAmount.text.toString()
        val reference = etReference.text.toString().ifBlank { "Tuition Payment" }
        val timestamp = System.currentTimeMillis()

        // Save to Firestore
        val paymentData = hashMapOf(
            "amount" to amount,
            "reference" to reference,
            "timestamp" to timestamp,
            "userId" to userId
        )

        db.collection("payments")
            .add(paymentData)
            .addOnSuccessListener {
                Log.d("Payment", "Saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("Payment", "Firestore save failed", e)
            }

        // Save to Room database
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val receipt = ReceiptEntity(
                    item = reference,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    date = timestamp
                )
                AppDatabase.getDatabase(this@PaymentActivity)
                    .receiptDao()
                    .insertReceipt(receipt)
                Log.d("Payment", "Saved to Room")
            } catch (e: Exception) {
                Log.e("Payment", "Room save failed", e)
            }
        }
    }
}
