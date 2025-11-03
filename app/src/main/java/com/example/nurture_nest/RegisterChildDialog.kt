package com.example.nurture_nest

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.nurture_nest.R
import com.example.nurture_nest.model.Child
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterChildDialog : DialogFragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_register_child, null)

        val spinnerParents = view.findViewById<Spinner>(R.id.spinnerParents)
        val spinnerTeachers = view.findViewById<Spinner>(R.id.spinnerTeachers)
        val etChildName = view.findViewById<EditText>(R.id.etChildName)
        val btnRegisterChild = view.findViewById<Button>(R.id.btnRegisterChild)

        val parentNames = mutableListOf<String>()
        val parentIds = mutableListOf<String>()
        val teacherNames = mutableListOf<String>()
        val teacherIds = mutableListOf<String>()

        lifecycleScope.launch {
            try {
                // 🔹 Fetch Parents (handles lowercase and uppercase)
                val parentsSnap = db.collection("users")
                    .whereIn("role", listOf("parent", "Parent"))
                    .get()
                    .await()

                parentNames.clear()
                parentIds.clear()
                for (doc in parentsSnap.documents) {
                    val name = doc.getString("name") ?: "Unnamed Parent"
                    parentNames.add(name)
                    parentIds.add(doc.id)
                }

                val parentAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    parentNames
                )
                parentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerParents.adapter = parentAdapter

                // 🔹 Fetch Teachers (handles lowercase and uppercase)
                val teachersSnap = db.collection("users")
                    .whereIn("role", listOf("teacher", "Teacher"))
                    .get()
                    .await()

                teacherNames.clear()
                teacherIds.clear()
                for (doc in teachersSnap.documents) {
                    val name = doc.getString("name") ?: "Unnamed Teacher"
                    teacherNames.add(name)
                    teacherIds.add(doc.id)
                }

                val teacherAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    teacherNames
                )
                teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerTeachers.adapter = teacherAdapter

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading users: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        btnRegisterChild.setOnClickListener {
            val childName = etChildName.text.toString().trim()
            if (childName.isEmpty()) {
                Toast.makeText(requireContext(), "Enter child's name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedParentPos = spinnerParents.selectedItemPosition
            val selectedTeacherPos = spinnerTeachers.selectedItemPosition

            if (selectedParentPos == -1 || selectedTeacherPos == -1) {
                Toast.makeText(requireContext(), "Select both a parent and a teacher", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parentId = parentIds[selectedParentPos]
            val teacherId = teacherIds[selectedTeacherPos]
            val childId = db.collection("children").document().id

            val newChild = Child(
                id = childId,
                name = childName,
                parentId = parentId,
                teacherId = teacherId
            )

            lifecycleScope.launch {
                try {
                    db.collection("children").document(childId).set(newChild).await()

                    db.collection("users").document(parentId)
                        .update("childrenIds", FieldValue.arrayUnion(childId))
                    db.collection("users").document(teacherId)
                        .update("childrenIds", FieldValue.arrayUnion(childId))

                    Toast.makeText(requireContext(), "Child registered successfully!", Toast.LENGTH_SHORT).show()
                    dismiss()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
}
