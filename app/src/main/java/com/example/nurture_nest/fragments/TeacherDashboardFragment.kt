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
        loadChildrenForTeacher()
        return binding.root
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceAdapter(studentList) { student, selectedStatus ->
            saveAttendanceImmediately(student, selectedStatus)
        }

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
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val childrenSnap = db.collection("children")
                    .whereEqualTo("teacherId", teacherId)
                    .get()
                    .await()

                val attendanceSnap = db.collection("attendance")
                    .document(currentDate)
                    .collection("records")
                    .whereEqualTo("teacherId", teacherId)
                    .get()
                    .await()

                val alreadyMarked = attendanceSnap.documents.mapNotNull { it.getString("studentName") }

                val children = childrenSnap.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    name?.let { StudentAttendance(it) }
                }.filterNot { alreadyMarked.contains(it.name) } // remove already-marked

                withContext(Dispatchers.Main) {
                    studentList.clear()
                    studentList.addAll(children)
                    attendanceAdapter.notifyDataSetChanged()

                    if (studentList.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "All attendance already marked for today 🎉",
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

    /**
     * Saves the attendance record instantly when teacher marks a student
     */
    private fun saveAttendanceImmediately(student: StudentAttendance, status: String) {
        val teacherId = auth.currentUser?.uid ?: "UnknownTeacher"
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val record = hashMapOf(
            "studentName" to student.name,
            "status" to status,
            "date" to currentDate,
            "teacherId" to teacherId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("attendance")
            .document(currentDate)
            .collection("records")
            .document(student.name)
            .set(record)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Marked ${student.name} as $status ✅", Toast.LENGTH_SHORT).show()
                studentList.remove(student)
                attendanceAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save attendance: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
