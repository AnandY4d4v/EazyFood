package com.photon.eazyfood

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.photon.eazyfood.databinding.ActivityDetailBinding
import com.photon.eazyfood.model.CartItems

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var foodImages : String
    private lateinit var foodIngredients : String
    private lateinit var foodDescriptions : String
    private lateinit var foodPrices : String
    private lateinit var foodNames:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        foodNames = intent.getStringExtra("MenuItemName").toString()
        foodImages = intent.getStringExtra("MenuItemImage").toString()
        foodPrices = intent.getStringExtra("MenuPrice").toString()
        foodDescriptions = intent.getStringExtra("MenuDescription").toString()
        foodIngredients = intent.getStringExtra("MenuIngredients").toString()

        binding.ingredientsTextView.text = foodIngredients
        binding.descriptionTextView.text = foodDescriptions
        Glide.with(this).load(Uri.parse(foodImages)).into(binding.foodImage)
        binding.detailsFoodName.text = foodNames




        binding.detailsBackBtn.setOnClickListener {
            finish()
        }
        binding.detailsAddCartBtn.setOnClickListener {
            addItemToCart()
        }

    }

    private fun addItemToCart() {


        val database = FirebaseDatabase.getInstance().reference
        val userUid = auth.currentUser?.uid ?: ""

        // Create a cartItem object
        val cartItems = CartItems(
            foodNames,
            foodPrices,
            foodDescriptions,
            foodImages,
            1
        )

        // Save data to cart item to firebase database
        database.child("Users").child(userUid).child("Cart Items").push().setValue(cartItems)
            .addOnSuccessListener {

                Toast.makeText(this, "Item added onto cart successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Item not added", Toast.LENGTH_SHORT).show()
            }
    }
}