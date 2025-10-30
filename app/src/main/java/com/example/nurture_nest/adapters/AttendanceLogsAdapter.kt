package com.example.nurture_nest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.AttendanceLog
import com.example.nurture_nest.databinding.ItemAttendanceLogBinding

class AttendanceLogsAdapter(private val logs: List<AttendanceLog>) :
    RecyclerView.Adapter<AttendanceLogsAdapter.LogViewHolder>() {

    inner class LogViewHolder(val binding: ItemAttendanceLogBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemAttendanceLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        holder.binding.tvLogDate.text = "Date: ${log.date}"
        holder.binding.tvLogStudentName.text = "Student: ${log.studentName}"
        holder.binding.tvLogStatus.text = "Status: ${log.status}"
    }

    override fun getItemCount() = logs.size
}