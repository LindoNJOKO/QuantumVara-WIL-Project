package com.example.nurture_nest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.R
import com.example.nurture_nest.model.StudentAttendance

class AttendanceAdapter(
    private val students: MutableList<StudentAttendance>,
    private val onStatusSelected: (StudentAttendance, String) -> Unit
) : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvStudentName)
        val rgStatus: RadioGroup = view.findViewById(R.id.rgAttendanceStatus)
        val rbPresent: RadioButton = view.findViewById(R.id.rbPresent)
        val rbAbsent: RadioButton = view.findViewById(R.id.rbAbsent)
        val rbSick: RadioButton = view.findViewById(R.id.rbSick)
        val rbLate: RadioButton = view.findViewById(R.id.rbLate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        holder.tvName.text = student.name

        holder.rgStatus.setOnCheckedChangeListener { _, checkedId ->
            val status = when (checkedId) {
                R.id.rbPresent -> "Present"
                R.id.rbAbsent -> "Absent"
                R.id.rbSick -> "Sick"
                R.id.rbLate -> "Late"
                else -> ""
            }
            if (status.isNotEmpty()) {
                onStatusSelected(student, status)
            }
        }
    }

    override fun getItemCount(): Int = students.size
}
