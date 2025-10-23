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
    private lateinit var btnSaveBankDetails: Button
    private lateinit var btnProcessPayment: Button
    private lateinit var btnViewReceipt: Button

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
        btnSaveBankDetails = findViewById(R.id.btnSaveBankDetails)
        btnProcessPayment = findViewById(R.id.btnProcessPayment)
        btnViewReceipt = findViewById(R.id.btnViewReceipts)

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

        btnProcessPayment.setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        btnViewReceipt.setOnClickListener {
            startActivity(Intent(this, ReceiptsActivity::class.java))
        }
    }


}
