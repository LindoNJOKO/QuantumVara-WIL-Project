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
import com.example.nurture_nest.models.StudentAttendance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        setupSubmitButton()
        return binding.root
    }

    private fun setupRecyclerView() {
        // Replace with actual student data from Firestore if you have a class list
        studentList.addAll(
            listOf(
                StudentAttendance("Alice Moyo"),
                StudentAttendance("Brian Dlamini"),
                StudentAttendance("Carla Naidoo"),
                StudentAttendance("Daniel Mthembu"),
                StudentAttendance("Evelyn Khumalo")
            )
        )

        attendanceAdapter = AttendanceAdapter(studentList)
        binding.rvStudentList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendanceAdapter
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

            val attendanceCollection = db.collection("attendance").document(currentDate).collection("records")

            // Batch write for all students
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
