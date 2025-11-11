package com.example.vic_power

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var notification: View
    private lateinit var notificationText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page)



        notification = findViewById(R.id.notificationBanner)
        notificationText = findViewById(R.id.notificationText)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        form_slide()
        setupRegisterLink()



    }


    private fun showNotification(message: String) {
        notificationText.text = message
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_top_left)
        val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_top_left)

        notification.visibility = View.VISIBLE
        notification.startAnimation(slideIn)

        // Auto dismiss after 3 seconds using main looper
        Handler(Looper.getMainLooper()).postDelayed({
            notification.startAnimation(slideOut)
            notification.visibility = View.GONE
        }, 3000)
    }

    fun form_slide(){
        val loginCard = findViewById<CardView>(R.id.loginCard)
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        loginCard.startAnimation(slideDown)
    }

    fun setupRegisterLink() {
        val registerTextView = findViewById<TextView>(R.id.registerLink)
        val text = "Already have an account? Login"
        val spannable = SpannableString(text)
        val start = text.indexOf("Login")
        val end = start + "Login".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@Register, Login::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = Color.CYAN
            }
        }

        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        registerTextView.text = spannable
        registerTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    fun validate_inputs(view: View) {
        val usernameField = findViewById<EditText>(R.id.user_name)
        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val roleField = findViewById<EditText>(R.id.role)

        val username = usernameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        val roleInput = roleField.text.toString().trim()

        var isValid = true

        if (username.isEmpty()) {
            usernameField.error = "Username is required"
            isValid = false
        }

        if (email.isEmpty()) {
            emailField.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Enter a valid email"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordField.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordField.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (!isValid) return

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val role = if (roleInput.isNotEmpty()) "employee" else "user"
                    val userData = hashMapOf(
                        "name" to username,
                        "email" to email,
                        "role" to role
                    )
                    db.collection("users").document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            user.sendEmailVerification()
                            showNotification("Registration successful! Please verify your email.")
                            showPopup(R.drawable.successfull, "Success", "Registration successful! Please verify your email.\n\nPress ok to return to login, and confirm your account before login..")

                        }
                        .addOnFailureListener { e ->
                            showPopup(R.drawable.errors, "Error", "Failed to save user: ${e.message}")

                            showNotification("Failed to save user: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                if (e is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                    emailField.error = "This email already has an account"
                    showNotification("This email already has an account. Please log in instead.")

                  //  startActivity(Intent(this, Login::class.java)) finish()


                } else {
                    showNotification("Registration failed: ${e.message}")
                }
            }
    }



    fun showPopup(iconRes: Int, title: String, message: String) {
        val dialogView = layoutInflater.inflate(R.layout.custom_popup, null)

        val icon = dialogView.findViewById<ImageView>(R.id.popupIcon)
        val titleView = dialogView.findViewById<TextView>(R.id.popupTitle)
        val messageView = dialogView.findViewById<TextView>(R.id.popupMessage)
        val okButton = dialogView.findViewById<Button>(R.id.popupOkButton)

        icon.setImageResource(iconRes)
        titleView.text = title
        messageView.text = message

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        okButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, Login::class.java))
            finish()
            dialog.dismiss()
        }

        dialog.show()
    }

}