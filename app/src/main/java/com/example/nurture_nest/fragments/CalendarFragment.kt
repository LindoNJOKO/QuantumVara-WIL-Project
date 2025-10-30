package com.example.nurture_nest.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nurture_nest.R
import android.content.Intent
import android.provider.CalendarContract
import android.widget.CalendarView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.adapters.EventAdapter
import com.example.nurture_nest.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class CalendarFragment : Fragment() {
    private lateinit var calendarView: CalendarView
    private lateinit var rvUpcomingEvents: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private val eventsList = mutableListOf<Event>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        calendarView = view.findViewById(R.id.calendarView)
        rvUpcomingEvents = view.findViewById(R.id.rvUpcomingEvents)

        setupRecyclerView()
        loadUpcomingEvents()
        setupCalendarListener()

        return view
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(eventsList) { event ->
            showEventDialog(event)
        }
        rvUpcomingEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }

    private fun loadUpcomingEvents() {
        val currentTime = System.currentTimeMillis()

        db.collection("events")
            .whereGreaterThanOrEqualTo("date", currentTime)
            .orderBy("date", Query.Direction.ASCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error loading events", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                eventsList.clear()
                snapshot?.documents?.forEach { doc ->
                    val event = doc.toObject(Event::class.java)
                    event?.let { eventsList.add(it) }
                }
                eventAdapter.notifyDataSetChanged()
            }
    }

    private fun setupCalendarListener() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            val selectedDate = calendar.timeInMillis

            loadEventsForDate(selectedDate)
        }
    }

    private fun loadEventsForDate(date: Long) {
        val startOfDay = getStartOfDay(date)
        val endOfDay = getEndOfDay(date)

        db.collection("events")
            .whereGreaterThanOrEqualTo("date", startOfDay)
            .whereLessThanOrEqualTo("date", endOfDay)
            .get()
            .addOnSuccessListener { snapshot ->
                eventsList.clear()
                snapshot.documents.forEach { doc ->
                    val event = doc.toObject(Event::class.java)
                    event?.let { eventsList.add(it) }
                }

                if (eventsList.isEmpty()) {
                    Toast.makeText(requireContext(), "No events on this date", Toast.LENGTH_SHORT).show()
                }

                eventAdapter.notifyDataSetChanged()
            }
    }

    private fun showEventDialog(event: Event) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(event.title)
            .setMessage("${event.description}\n\nType: ${event.eventType}\nTime: ${event.time}\nLocation: ${event.location}")
            .setPositiveButton("Add to Calendar") { _, _ ->
                addEventToPhoneCalendar(event)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun addEventToPhoneCalendar(event: Event) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.date)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.date + 3600000) // +1 hour
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open calendar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getStartOfDay(date: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(date: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
