package com.example.vic_power

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ServicesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_services, container, false)

        val btnContact = view.findViewById<Button>(R.id.btnContact)

        btnContact.setOnClickListener {
            showContactForm()
        }

        return view
    }

    private fun showContactForm() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_contact_form, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)
        val etMessage = dialogView.findViewById<EditText>(R.id.etMessage)
        val btnSend = dialogView.findViewById<Button>(R.id.btnSend)

        //Auto-fill from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("user_info", android.content.Context.MODE_PRIVATE)
        etName.setText(sharedPref.getString("username", ""))
        etEmail.setText(sharedPref.getString("email", ""))
        etPhone.setText(sharedPref.getString("phone", ""))

        btnSend.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val message = etMessage.text.toString().trim()

            var isValid = true

            // Clear previous errors
            etName.error = null
            etEmail.error = null
            etMessage.error = null

            // Validate required fields
            if (name.isEmpty()) {
                etName.error = "Full Name is required"
                isValid = false
            }
            if (email.isEmpty()) {
                etEmail.error = "Email Address is required"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Enter a valid email"
                isValid = false
            }
            if (message.isEmpty()) {
                etMessage.error = "Message is required"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            // Save to Firestore
            val db = Firebase.firestore
            val data = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "message" to message,
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("contactMessages")
                .add(data)
                .addOnSuccessListener {
                    val snackbar = Snackbar.make(dialogView, "Message sent successfully!", Snackbar.LENGTH_SHORT)
                    val snackbarView = snackbar.view

                    val params = snackbarView.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.TOP
                    snackbarView.layoutParams = params

                    snackbar.show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    val snackbar = Snackbar.make(dialogView, "Error sending message", Snackbar.LENGTH_SHORT)
                    val snackbarView = snackbar.view

                    val params = snackbarView.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.TOP
                    snackbarView.layoutParams = params

                    Log.e("Firestore", "Error: ", e)
                }
        }

            dialog.show()
    }
}
