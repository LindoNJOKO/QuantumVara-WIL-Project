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
import com.example.nurture_nest.R
import com.example.nurture_nest.ChatListActivity
import com.example.nurture_nest.NotificationActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardFragment : Fragment() {

    private lateinit var fabCreateEvent: ExtendedFloatingActionButton
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)

        // Initialize UI elements
        fabCreateEvent = view.findViewById(R.id.fabCreateEvent)
        val chatBtn = view.findViewById<ImageButton>(R.id.btnChat)
        val notificationBtn = view.findViewById<Button>(R.id.btnAddNotification)

        // FAB - Create Event
        fabCreateEvent.setOnClickListener { showCreateEventDialog() }

        // Chat Button
        chatBtn?.setOnClickListener {
            startActivity(Intent(requireContext(), ChatListActivity::class.java))
        }

        // Notification Button
        notificationBtn?.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
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

        var selectedDate: Long? = null
        val calendar = Calendar.getInstance()

        btnSelectDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.timeInMillis
                    tvSelectedDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(selectedDate!!))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Create Event")
            .setView(dialogView)
            .setPositiveButton("Create", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val createBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            createBtn.setOnClickListener {
                val title = etTitle.text?.toString()?.trim() ?: ""
                val description = etDescription.text?.toString()?.trim() ?: ""
                val eventType = spinnerType.selectedItem?.toString() ?: ""
                val time = etTime.text?.toString()?.trim() ?: ""
                val location = etLocation.text?.toString()?.trim() ?: ""

                if (title.isEmpty() || selectedDate == null) {
                    Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                createEvent(title, description, eventType, selectedDate!!, time, location)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun createEvent(
        title: String,
        description: String,
        eventType: String,
        date: Long,
        time: String,
        location: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

        val event = hashMapOf(
            "title" to title,
            "description" to description,
            "eventType" to eventType,
            "date" to date,
            "time" to time,
            "location" to location,
            "createdBy" to userId,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("events")
            .add(event)
            .addOnSuccessListener { doc ->
                Toast.makeText(requireContext(), "✅ Event created!", Toast.LENGTH_SHORT).show()
                sendEventNotification(title, description, doc.id)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "❌ Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendEventNotification(title: String, description: String, eventId: String) {
        val notification = hashMapOf(
            "title" to title,
            "message" to description,
            "type" to "event",
            "eventId" to eventId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "📢 Notification sent!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "⚠️ Failed to send notification", Toast.LENGTH_SHORT).show()
            }
    }
}
