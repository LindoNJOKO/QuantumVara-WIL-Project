package com.example.nurture_nest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.databinding.ItemStudentAttendanceBinding
import com.example.nurture_nest.models.StudentAttendance

class AttendanceAdapter(
    private val students: List<StudentAttendance>
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(val binding: ItemStudentAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemStudentAttendanceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val student = students[position]
        holder.binding.tvStudentName.text = student.name

        // Update selection if previously chosen
        when (student.status) {
            "Present" -> holder.binding.radioGroup.check(holder.binding.rbPresent.id)
            "Absent" -> holder.binding.radioGroup.check(holder.binding.rbAbsent.id)
            "Late" -> holder.binding.radioGroup.check(holder.binding.rbLate.id)
            "Excused" -> holder.binding.radioGroup.check(holder.binding.rbExcused.id)
            else -> holder.binding.radioGroup.clearCheck()
        }

        holder.binding.radioGroup.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
            student.status = when (checkedId) {
                holder.binding.rbPresent.id -> "Present"
                holder.binding.rbAbsent.id -> "Absent"
                holder.binding.rbLate.id -> "Late"
                holder.binding.rbExcused.id -> "Excused"
                else -> null
            }
        }
    }

    override fun getItemCount() = students.size
}