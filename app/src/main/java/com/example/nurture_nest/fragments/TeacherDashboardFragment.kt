package com.example.nurture_nest.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nurture_nest.adapters.AttendanceAdapter
import com.example.nurture_nest.databinding.FragmentTeacherDashboardBinding
import com.example.nurture_nest.model.StudentAttendance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TeacherDashboardFragment : Fragment() {

    private var _binding: FragmentTeacherDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var attendanceAdapter: AttendanceAdapter
    private val studentList = mutableListOf<StudentAttendance>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherDashboardBinding.inflate(inflater, container, false)
        setupRecyclerView()
        loadChildrenForTeacher() // 🔹 Fetch children from Firestore
        setupSubmitButton()
        return binding.root
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceAdapter(studentList)
        binding.rvStudentList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendanceAdapter
        }
    }

    /**
     * Loads all registered children linked to the current teacher
     */
    private fun loadChildrenForTeacher() {
        val teacherId = auth.currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 🔹 Get all children linked to this teacher
                val childrenSnap = db.collection("children")
                    .whereEqualTo("teacherId", teacherId)
                    .get()
                    .await()

                val children = childrenSnap.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    name?.let { StudentAttendance(it) }
                }

                withContext(Dispatchers.Main) {
                    studentList.clear()
                    studentList.addAll(children)
                    attendanceAdapter.notifyDataSetChanged()

                    if (studentList.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "No children registered for this teacher yet.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error loading children: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmitAttendance.setOnClickListener {
            val teacherId = auth.currentUser?.uid ?: "UnknownTeacher"
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val attendanceRecords = studentList.map {
                hashMapOf(
                    "studentName" to it.name,
                    "status" to (it.status ?: "Not Marked"),
                    "date" to currentDate,
                    "teacherId" to teacherId,
                    "timestamp" to System.currentTimeMillis()
                )
            }

            val attendanceCollection = db.collection("attendance")
                .document(currentDate)
                .collection("records")

            val batch = db.batch()
            for (record in attendanceRecords) {
                val docRef = attendanceCollection.document(record["studentName"].toString())
                batch.set(docRef, record)
            }

            batch.commit()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Attendance saved to Firebase ✅", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error saving attendance: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
