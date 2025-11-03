package com.example.nurture_nest.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.nurture_nest.ChatListActivity
import com.example.nurture_nest.NotificationActivity
import com.example.nurture_nest.R
import com.example.nurture_nest.RegisterChildDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardFragment : Fragment() {

    private lateinit var btnRegisterChild: MaterialButton
    private lateinit var btnAddNotification: MaterialButton
    private lateinit var fabCreateEvent: ExtendedFloatingActionButton
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)

        // Buttons and FAB
        btnRegisterChild = view.findViewById(R.id.btnRegisterChild)
        btnAddNotification = view.findViewById(R.id.btnAddNotification)
        fabCreateEvent = view.findViewById(R.id.fabAdd)

        // Chat button
        val chatBtn = view.findViewById<ImageButton>(R.id.btnChat)

        // Click Listeners
        btnRegisterChild.setOnClickListener {
            val dialog = RegisterChildDialog()
            dialog.show(parentFragmentManager, "RegisterChildDialog")
        }

        btnAddNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }

        fabCreateEvent.setOnClickListener {
            showCreateEventDialog()
        }

        chatBtn.setOnClickListener {
            startActivity(Intent(requireContext(), ChatListActivity::class.java))
        }

        return view
    }

    private fun showCreateEventDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_event, null)

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etEventTitle)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etEventDescription)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerEventType)
        val btnSelectDate = dialogView.findViewById<Button>(R.id.btnSelectDate)
        val tvSelectedDate = dialogView.findViewById<TextView>(R.id.tvSelectedDate)
        val etTime = dialogView.findViewById<TextInputEditText>(R.id.etEventTime)
        val etLocation = dialogView.findViewById<TextInputEditText>(R.id.etEventLocation)

        var selectedDate = 0L

        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.timeInMillis
                    tvSelectedDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(selectedDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Create Event")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val title = etTitle.text.toString()
                val description = etDescription.text.toString()
                val type = spinnerType.selectedItem.toString()
                val time = etTime.text.toString()
                val location = etLocation.text.toString()

                if (title.isNotEmpty() && selectedDate > 0) {
                    createEvent(title, description, type, selectedDate, time, location)
                } else {
                    Toast.makeText(requireContext(), "Please fill required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createEvent(
        title: String,
        description: String,
        eventType: String,
        date: Long,
        time: String,
        location: String
    ) {
        val event = hashMapOf(
            "title" to title,
            "description" to description,
            "eventType" to eventType,
            "date" to date,
            "time" to time,
            "location" to location,
            "createdBy" to FirebaseAuth.getInstance().currentUser?.uid,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("events")
            .add(event)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
                sendEventNotification(title, description, documentReference.id)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to create event: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendEventNotification(title: String, description: String, eventId: String) {
        val notificationData = hashMapOf(
            "title" to title,
            "message" to description,
            "type" to "event",
            "eventId" to eventId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("notifications")
            .add(notificationData)
            .addOnSuccessListener {
                // Notification sent to all parents
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to send notification", Toast.LENGTH_SHORT).show()
            }
    }
}
