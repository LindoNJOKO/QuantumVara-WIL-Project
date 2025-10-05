package com.example.nurture_nest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaymentWindow : AppCompatActivity() {

    private lateinit var etAccountHolder: EditText
    private lateinit var etAccountNumber: EditText
    private lateinit var etBankName: EditText
    private lateinit var etBranchCode: EditText
    private lateinit var etPaymentAmount: EditText
    private lateinit var etPaymentReference: EditText
    private lateinit var btnSaveBankDetails: Button
    private lateinit var btnProcessPayment: Button

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "testUser"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_window)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Views
        etAccountHolder = findViewById(R.id.etAccountHolder)
        etAccountNumber = findViewById(R.id.etAccountNumber)
        etBankName = findViewById(R.id.etBankName)
        etBranchCode = findViewById(R.id.etBranchCode)
        etPaymentAmount = findViewById(R.id.etPaymentAmount)
        etPaymentReference = findViewById(R.id.etPaymentReference)
        btnSaveBankDetails = findViewById(R.id.btnSaveBankDetails)
        btnProcessPayment = findViewById(R.id.btnProcessPayment)

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

        // Process Payment
        btnProcessPayment.setOnClickListener {
            val paymentData = hashMapOf(
                "amount" to etPaymentAmount.text.toString(),
                "reference" to etPaymentReference.text.toString(),
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users").document(userId)
                .collection("payments").add(paymentData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Payment processed!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error processing payment", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
