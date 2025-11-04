package com.example.nurture_nest.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.R

class TeacherChildAdapter(private val children: List<Child>) :
    RecyclerView.Adapter<TeacherChildAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvChildName: TextView = view.findViewById(R.id.tvChildName)
        val tvAllergies: TextView = view.findViewById(R.id.tvAllergies)
        val tvMedicalConditions: TextView = view.findViewById(R.id.tvMedicalConditions)
        val tvEmergencyContact: TextView = view.findViewById(R.id.tvEmergencyContact)
        val tvNotes: TextView = view.findViewById(R.id.tvNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_child, parent, false)
        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val child = children[position]
        holder.tvChildName.text = "👶 ${child.name}"
        holder.tvAllergies.text = "Allergies: ${child.allergies}"
        holder.tvMedicalConditions.text = "Medical: ${child.medicalConditions}"
        holder.tvEmergencyContact.text = "Emergency: ${child.emergencyContactName} (${child.emergencyContactNumber})"
        holder.tvNotes.text = "Notes: ${child.additionalNotes}"
    }

    override fun getItemCount(): Int = children.size
}
