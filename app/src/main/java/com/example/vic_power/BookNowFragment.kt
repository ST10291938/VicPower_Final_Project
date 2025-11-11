package com.example.vic_power

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class BookNowFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_now, container, false)

        val etService = view.findViewById<EditText>(R.id.etService)
        val etDate = view.findViewById<EditText>(R.id.etDate)
        val etTime = view.findViewById<EditText>(R.id.etTime)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmitBooking)

        val calendar = Calendar.getInstance()

        // Date picker
        etDate.setOnClickListener {
            DatePickerDialog(requireContext(),
                { _, year, month, day ->
                    etDate.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time picker
        etTime.setOnClickListener {
            TimePickerDialog(requireContext(),
                { _, hour, minute ->
                    etTime.setText(String.format("%02d:%02d", hour, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        btnSubmit.setOnClickListener {
            val service = etService.text.toString().trim()
            val date = etDate.text.toString().trim()
            val time = etTime.text.toString().trim()
            val notes = etNotes.text.toString().trim()

            var isValid = true

            etService.error = null
            etDate.error = null
            etTime.error = null

            if (service.isEmpty()) {
                etService.error = "Service Name is required"
                isValid = false
            }
            if (date.isEmpty()) {
                etDate.error = "Date is required"
                isValid = false
            }
            if (time.isEmpty()) {
                etTime.error = "Time is required"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            val sharedPref = requireActivity().getSharedPreferences("user_info", MODE_PRIVATE)
            val userId = sharedPref.getString("userId", "") ?: ""
            val name = sharedPref.getString("username", "Unknown") ?: "Unknown"

            val db = Firebase.firestore
            val booking = hashMapOf(
                "service" to service,
                "date" to date,
                "time" to time,
                "notes" to notes,
                "status" to "Pending",
                "createdAt" to FieldValue.serverTimestamp(),
                "userId" to userId,
                "name" to name
            )

            db.collection("bookings")
                .add(booking)
                .addOnSuccessListener {
                    val snackbar = Snackbar.make(view, "Booking submitted successfully!", Snackbar.LENGTH_SHORT)
                    val snackbarView = snackbar.view
                    val params = snackbarView.layoutParams as CoordinatorLayout.LayoutParams
                    params.gravity = Gravity.TOP
                    snackbarView.layoutParams = params
                    snackbar.show()


                    etService.text.clear()
                    etDate.text.clear()
                    etTime.text.clear()
                    etNotes.text.clear()
                }
                .addOnFailureListener { e ->
                    Snackbar.make(requireView(), "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
        }


        return view
    }

}
