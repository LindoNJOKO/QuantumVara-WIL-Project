package com.example.nurture_nest

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LunchOrdering : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "testUser"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lunch_ordering)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnSaveLunchOrder = findViewById<Button>(R.id.btnSaveLunchOrder)

        btnSaveLunchOrder.setOnClickListener {
            val lunchOrder = hashMapOf(
                "Monday" to getMealsForDay(
                    listOf(
                        findViewById(R.id.cbMondayChicken),
                        findViewById(R.id.cbMondayVeggie)
                    )
                ),
                "Tuesday" to getMealsForDay(
                    listOf(
                        findViewById(R.id.cbTuesdayPasta),
                        findViewById(R.id.cbTuesdaySoup)
                    )
                ),
                "Wednesday" to getMealsForDay(
                    listOf(
                        findViewById(R.id.cbWednesdayBeef),
                        findViewById(R.id.cbWednesdayFruit)
                    )
                ),
                "Thursday" to getMealsForDay(
                    listOf(
                        findViewById(R.id.cbThursdayFish),
                        findViewById(R.id.cbThursdayVeggie)
                    )
                ),
                "Friday" to getMealsForDay(
                    listOf(
                        findViewById(R.id.cbFridayPizza),
                        findViewById(R.id.cbFridaySoup)
                    )
                )
            )

            db.collection("users").document(userId)
                .collection("lunchOrders")
                .add(lunchOrder)
                .addOnSuccessListener {
                    Toast.makeText(this, "Lunch order saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving order", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getMealsForDay(checkBoxes: List<CheckBox>): List<String> {
        return checkBoxes.filter { it.isChecked }.map { it.text.toString() }
    }
}
