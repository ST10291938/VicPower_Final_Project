package com.example.vic_power

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
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
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var notification: View
    private lateinit var notificationText: TextView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        notification = findViewById(R.id.notificationBanner)
        notificationText = findViewById(R.id.notificationText)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        form_slide()
        setupRegisterLink()
        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )





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


    fun validate_inputs(view: View) {
        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.loginBtn)

        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        var isValid = true

        // Email validation
        if (email.isEmpty()) {
            emailField.error = "Email is required"
            showNotification("Email is required")
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Enter a valid email"
            showNotification("Enter a valid email")
            isValid = false
        }

        // Password validation
        if (password.isEmpty()) {
            passwordField.error = "Password is required"
            showNotification("Password is required")
            isValid = false
        } else if (password.length < 6) {
            passwordField.error = "Password must meet minimum requirements"
            showNotification("Password must meet minimum requirements")
            isValid = false
        }

        if (!isValid) return

        loginBtn.isEnabled = false

        // Firebase login
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user

                if (user != null && !user.isEmailVerified) {
                    emailField.error = "Please verify your email before logging in"
                    showNotification("Please verify your email before logging in")
                    auth.signOut()
                    loginBtn.isEnabled = true
                    return@addOnSuccessListener
                }

                if (user != null) {
                    db.collection("users").document(user.uid).get()
                        .addOnSuccessListener { document ->
                            loginBtn.isEnabled = true
                            if (document != null && document.exists()) {
                                val role = document.getString("role") ?: "user"
                                val username = document.getString("name") ?: ""
                                val emailStored = document.getString("email") ?: ""

                                // Save user info in SharedPreferences
                                val sharedPref = getSharedPreferences("user_info", MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString("role", role)
                                    putString("username", username)
                                    putString("email", emailStored)
                                    putString("userId",user.uid)
                                    apply()
                                }

                                // Redirect based on role
                                val intent = when (role) {
                                    "employee" -> Intent(this@Login, home_employee::class.java)
                                    else -> Intent(this@Login, user_home::class.java)
                                }
                                startActivity(intent)
                                finish()
                            } else {
                                emailField.error = "No user data found"
                                showNotification("No user data found")
                            }
                        }
                        .addOnFailureListener {
                            loginBtn.isEnabled = true
                            emailField.error = "Try again later"
                            showNotification("Try again later")
                        }
                }
            }
            .addOnFailureListener { e ->
                loginBtn.isEnabled = true
                // Show detailed Firebase exception if needed
                val msg = if (e is FirebaseAuthInvalidUserException || e is FirebaseAuthInvalidCredentialsException) {
                    "Incorrect email or password!!"
                } else {
                    "Login failed: ${e.message}"
                }
                showNotification(msg)
            }
    }


    fun setupRegisterLink() {
        val registerTextView = findViewById<TextView>(R.id.registerLink)
        val text = "Don’t have an account? Register"
        val spannable = SpannableString(text)
        val start = text.indexOf("Register")
        val end = start + "Register".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@Login, Register::class.java)
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



}