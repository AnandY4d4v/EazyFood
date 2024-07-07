package com.photon.eazyfood

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.photon.eazyfood.databinding.ActivitySignBinding
import com.photon.eazyfood.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignActivity : AppCompatActivity() {

    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userPassword: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    private val binding: ActivitySignBinding by lazy {
        ActivitySignBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Configure Google Sign-In
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        binding.signupBtn.setOnClickListener {
            userName = binding.name.text.toString()
            userEmail = binding.email.text.toString().trim()
            userPassword = binding.password.text.toString().trim()
            if (userEmail.isEmpty() || userName.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please Fill All the Details", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(userEmail, userPassword)
            }
        }

        binding.alreadyHave.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.googleBtn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    private fun createAccount(userEmail: String, userPassword: String) {
        auth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show()
                saveUserData()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Account Creation Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                Log.e("SignActivity", "createAccount: ${task.exception?.message}", task.exception)
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
                            updateUi()
                            Toast.makeText(this, "Account signed in successfully with Google", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Google sign-in failed: ${tasek.exception?.message}", Toast.LENGTH_SHORT).show()
                            Log.e("SignActivity", "signInWithCredential: ${tasek.exception?.message}", tasek.exception)
                        }
                    }
                } else {
                    Toast.makeText(this, "Google sign-in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("SignActivity", "getSignedInAccountFromIntent: ${task.exception?.message}", task.exception)
                }
            } else {
                Toast.makeText(this, "Google sign-in canceled", Toast.LENGTH_SHORT).show()
                Log.e("SignActivity", "Google sign-in canceled")
            }
        }

    private fun saveUserData() {
        userName = binding.name.text.toString()
        userEmail = binding.email.text.toString().trim()
        userPassword = binding.password.text.toString().trim()

        val user = UserModel(userName, userEmail, userPassword)
        val userId = auth.currentUser?.uid

        // Save data to Firebase Database
        userId?.let {
            database.child("Users").child(it).setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("SignActivity", "User data saved successfully")
                } else {
                    Log.e("SignActivity", "Failed to save user data: ${task.exception?.message}", task.exception)
                }
            }
        }
    }

    private fun updateUi() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
