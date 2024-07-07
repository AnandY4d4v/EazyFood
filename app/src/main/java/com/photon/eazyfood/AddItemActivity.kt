package com.photon.eazyfood

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.photon.eazyfood.databinding.ActivityAddItemBinding
import com.photon.eazyfood.model.AllMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddItemActivity : AppCompatActivity() {

    // Food item details
    private lateinit var foodName : String
    private lateinit var foodPrice : String
    private lateinit var foodDescription : String
    private var foodImageUri : Uri?=null
    private lateinit var foodIngredient : String

    private lateinit var autn : FirebaseAuth
    private lateinit var database : FirebaseDatabase

    private lateinit var binding : ActivityAddItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.image.visibility = View.GONE
        // Initialize

        autn = FirebaseAuth.getInstance()
        database= FirebaseDatabase.getInstance()

        binding.addItemBtn.setOnClickListener {

            // Get all text form edittext
            foodName = binding.enterFoodName.text.toString().trim()
            foodPrice = binding.enterFoodPrice.text.toString().trim()
            foodDescription = binding.description.text.toString().trim()
            foodIngredient = binding.ingredients.text.toString().trim()

            if (foodName.isBlank() || foodPrice.isBlank() || foodDescription.isBlank() || foodIngredient.isBlank()){

                Toast.makeText(this, "Please fill the all details", Toast.LENGTH_SHORT).show()
            } else{
                dataUpload()
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }

        }
        binding.addImage.setOnClickListener {
            picImage.launch("image/*")
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

    }

    private fun dataUpload() {
        // Get Reference to the "Menu" node in the database
        val menuRef = database.getReference("Menu")

        // Generate unique key for new menu
        val newItemKey = menuRef.push().key
        Toast.makeText(this, "Inside Upload", Toast.LENGTH_SHORT).show()
        Log.d("dataUpload", "Generated new item key: $newItemKey")

        if (foodImageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("Menu_Images/${newItemKey}.jpg")
            val uploadTask = imageRef.putFile(foodImageUri!!)

            Log.d("dataUpload", "Starting image upload")
            uploadTask.addOnSuccessListener {
                Log.d("dataUpload", "Image upload successful")
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    Log.d("dataUpload", "Image URL retrieved: $downloadUrl")

                    // Create a new menu item
                    val menuItem = AllMenu(
                        newItemKey,
                        foodName = foodName,
                        foodPrice = foodPrice,
                        foodDescription = foodDescription,
                        foodIngredient = foodIngredient,
                        foodImage = downloadUrl.toString()
                    )

                    newItemKey?.let { key ->
                        Log.d("dataUpload", "Uploading menu item with key: $key")
                        menuRef.child(key).setValue(menuItem).addOnSuccessListener {
                            Toast.makeText(this, "Add item successfully", Toast.LENGTH_SHORT).show()
                            Log.d("dataUpload", "Menu item added successfully")
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Add item failed", Toast.LENGTH_SHORT).show()
                            Log.e("dataUpload", "Failed to add menu item", e)
                        }
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                    Log.e("dataUpload", "Failed to retrieve download URL", e)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                Log.e("dataUpload", "Image upload failed", e)
            }
        } else {
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show()
            Log.d("dataUpload", "No image URI provided")
        }
    }


    private  val picImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null){
            binding.image.visibility = View.VISIBLE
            binding.image.setImageURI(it)
            foodImageUri = it
        }
    }
}