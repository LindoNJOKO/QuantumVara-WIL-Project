package com.example.nurture_nest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentWindow : AppCompatActivity() {

    private lateinit var etAccountHolder: EditText
    private lateinit var etAccountNumber: EditText
    private lateinit var etBankName: EditText
    private lateinit var etBranchCode: EditText
    private lateinit var etAmount: EditText
    private lateinit var etPaymentReference: EditText
    private lateinit var btnSaveBankDetails: Button
    private lateinit var btnProcessPayment: Button

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "testUser"

    private lateinit var paymentSheet: PaymentSheet
    private var paymentIntentClientSecret: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_window)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        etAccountHolder = findViewById(R.id.etAccountHolder)
        etAccountNumber = findViewById(R.id.etAccountNumber)
        etBankName = findViewById(R.id.etBankName)
        etBranchCode = findViewById(R.id.etBranchCode)
        etAmount = findViewById(R.id.etAmount)
        etPaymentReference = findViewById(R.id.etPaymentReference)
        btnSaveBankDetails = findViewById(R.id.btnSaveBankDetails)
        btnProcessPayment = findViewById(R.id.btnProcessPayment)

        // Initialize Stripe PaymentSheet
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        // Save Bank Details
        btnSaveBankDetails.setOnClickListener {
            val bankData = hashMapOf(
                "accountHolder" to etAccountHolder.text.toString(),
                "accountNumber" to etAccountNumber.text.toString(),
                "bankName" to etBankName.text.toString(),
                "branchCode" to etBranchCode.text.toString()
            )

            db.collection("users").document(userId)
                .collection("bankDetails").document("main")
                .set(bankData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Bank details saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving bank details", Toast.LENGTH_SHORT).show()
                }
        }

        // Process Payment (Stripe)
        btnProcessPayment.setOnClickListener {
            val amountText = etAmount.text.toString()
            if (amountText.isBlank()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amountDouble = try {
                amountText.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amountCents = (amountDouble * 100).toInt()
            createPaymentIntent(amountCents)
        }
    }

    // Create payment intent from backend
    private fun createPaymentIntent(amount: Int) {
        val request = PaymentRequest(amount = amount)
        ApiClient.instance.createPaymentIntent(request).enqueue(object : Callback<PaymentResponse> {
            override fun onResponse(call: Call<PaymentResponse>, response: Response<PaymentResponse>) {
                if (response.isSuccessful) {
                    paymentIntentClientSecret = response.body()?.clientSecret
                    presentPaymentSheet()
                } else {
                    Toast.makeText(this@PaymentWindow, "Failed to create payment intent", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                Toast.makeText(this@PaymentWindow, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // Present Stripe PaymentSheet
    private fun presentPaymentSheet() {
        paymentIntentClientSecret?.let { secret ->
            val config = PaymentSheet.Configuration("NurtureNest Tuition")
            paymentSheet.presentWithPaymentIntent(secret, config)
        }
    }

    // Handle Stripe result
    private fun onPaymentSheetResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(this, "✅ Payment successful!", Toast.LENGTH_LONG).show()
                savePaymentRecord()
                // Open ReceiptsActivity
                startActivity(Intent(this, ReceiptsActivity::class.java))
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "⚠️ Payment canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "❌ Error: ${result.error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Save payment record to Firestore + Room
    private fun savePaymentRecord() {
        // Save to Firestore
        val paymentData = hashMapOf(
            "amount" to etAmount.text.toString(),
            "reference" to etPaymentReference.text.toString(),
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(userId)
            .collection("payments").add(paymentData)
            .addOnSuccessListener {
                Toast.makeText(this, "Payment recorded in Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving payment record", Toast.LENGTH_SHORT).show()
            }

        // Save to Room
        val dbRoom = AppDatabase.getDatabase(this)
        val receipt = ReceiptEntity(
            item = etPaymentReference.text.toString().ifBlank { "Tuition Fee" },
            amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0,
            date = System.currentTimeMillis()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            dbRoom.receiptDao().insertReceipt(receipt)
        }
    }
}
