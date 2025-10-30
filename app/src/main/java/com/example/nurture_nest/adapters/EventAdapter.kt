package com.example.nurture_nest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.databinding.ItemUpcomingEventBinding
import com.example.nurture_nest.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter (
    private val events: List<Event>,
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(val binding: ItemUpcomingEventBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemUpcomingEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding)
    }
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        with(holder.binding) {
            tvEventTitle.text = event.title
            tvEventDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(event.date))
            tvEventTime.text = event.time
            tvEventType.text = event.eventType

            root.setOnClickListener { onEventClick(event) }
        }
    }

    override fun getItemCount() = events.size
}