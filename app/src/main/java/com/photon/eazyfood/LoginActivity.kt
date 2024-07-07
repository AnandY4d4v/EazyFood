package com.photon.eazyfood

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.photon.eazyfood.databinding.ActivityLoginBinding
import com.photon.eazyfood.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private var name: String? = null
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        binding.loginBtn.setOnClickListener {
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
            } else {
                signInWithEmail(email, password)
            }
        }

        binding.notHave.setOnClickListener {
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }

        binding.googleBtn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                updateUi(user)
                Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show()
            } else {
                if (task.exception is FirebaseAuthInvalidUserException) {
                    createNewAccount(email, password)
                } else {
                    Toast.makeText(this, "Sign-in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Sign-in failed", task.exception)
                }
            }
        }
    }

    private fun createNewAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveUserData()
                val user = auth.currentUser
                updateUi(user)
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
            } else {
                if (task.exception is FirebaseAuthUserCollisionException) {
                    Toast.makeText(this, "This email is already in use. Please try signing in.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Account creation failed", task.exception)
                }
            }
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount? = task.result
                    val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener { tasek ->
                        if (tasek.isSuccessful) {
                            val user = auth.currentUser
                            updateUi(user)
                            Toast.makeText(this, "Signed in successfully with Google", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Google sign-in failed: ${tasek.exception?.message}", Toast.LENGTH_SHORT).show()
                            Log.e("LoginActivity", "Google sign-in failed", tasek.exception)
                        }
                    }
                } else {
                    Toast.makeText(this, "Google sign-in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Google sign-in failed", task.exception)
                }
            } else {
                Toast.makeText(this, "Google sign-in canceled", Toast.LENGTH_SHORT).show()
                Log.e("LoginActivity", "Google sign-in canceled")
            }
        }

    private fun saveUserData() {
        email = binding.email.text.toString().trim()
        password = binding.password.text.toString().trim()
        val user = UserModel(name, email, password)
        val userUid = FirebaseAuth.getInstance().currentUser!!.uid

        database.child("Users").child(userUid).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("LoginActivity", "User data saved successfully")
            } else {
                Log.e("LoginActivity", "Failed to save user data: ${task.exception?.message}", task.exception)
            }
        }
    }

    private fun updateUi(user: FirebaseUser?) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
