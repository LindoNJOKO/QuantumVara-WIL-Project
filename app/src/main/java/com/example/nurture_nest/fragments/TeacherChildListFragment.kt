package com.example.nurture_nest.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Child(
    val id: String = "",
    val name: String = "",
    val parentName: String = "",
    val parentPhone: String = "",
    val allergies: String = "",
    val medicalConditions: String = "",
    val emergencyContactName: String = "",
    val emergencyContactNumber: String = "",
    val additionalNotes: String = ""
)

class TeacherChildListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TeacherChildAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val childList = mutableListOf<Child>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_child_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewChildren)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TeacherChildAdapter(childList)
        recyclerView.adapter = adapter

        fetchAssignedChildren()

        return view
    }

    private fun fetchAssignedChildren() {
        val teacherId = auth.currentUser?.uid ?: return

        db.collection("children")
            .whereEqualTo("teacherId", teacherId)
            .get()
            .addOnSuccessListener { result ->
                childList.clear()

                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "No children assigned yet.", Toast.LENGTH_SHORT).show()
                }

                for (doc in result) {
                    val childId = doc.id
                    val name = doc.getString("name") ?: "Unknown"
                    val parentId = doc.getString("parentId") ?: ""
                    val child = Child(id = childId, name = name)

                    // Fetch details from "childDetails" using parentId
                    db.collection("childDetails").document(parentId)
                        .get()
                        .addOnSuccessListener { detailDoc ->
                            val updatedChild = child.copy(
                                parentName = detailDoc.getString("preferredName") ?: "N/A",
                                allergies = detailDoc.getString("allergies") ?: "N/A",
                                medicalConditions = detailDoc.getString("medicalConditions") ?: "N/A",
                                emergencyContactName = detailDoc.getString("emergencyContactName") ?: "N/A",
                                emergencyContactNumber = detailDoc.getString("emergencyContactNumber") ?: "N/A",
                                additionalNotes = detailDoc.getString("additionalNotes") ?: "None"
                            )
                            childList.add(updatedChild)
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to fetch details for $name", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading children: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
