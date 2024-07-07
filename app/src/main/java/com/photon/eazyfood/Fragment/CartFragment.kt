package com.photon.eazyfood.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.photon.eazyfood.PayOutActivity
import com.photon.eazyfood.adapter.CartAdapter
import com.photon.eazyfood.databinding.FragmentCartBinding
import com.photon.eazyfood.model.CartItems

class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var foodNames: MutableList<String>
    private lateinit var foodPrices: MutableList<String>
    private lateinit var foodDescriptions: MutableList<String>
    private lateinit var foodImageUri: MutableList<String>
    private lateinit var foodIngredient: MutableList<String>
    private lateinit var quantities: MutableList<Int>
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Handle any fragment arguments here
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCartBinding.inflate(inflater, container, false)

        binding.proceedBtn.setOnClickListener {
            // Proceed to checkout only if the adapter is initialized
            if (::cartAdapter.isInitialized) {
                getOrderItemDetails()
            } else {
                Toast.makeText(context, "Cart items are not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }

        auth = FirebaseAuth.getInstance()
        retrieveCartItems()

        return binding.root
    }

    private fun getOrderItemDetails() {
        val orderItReference: DatabaseReference =
            database.reference.child("Users").child(userId).child("Cart Items")

        val foodName = mutableListOf<String>()
        val foodPrice = mutableListOf<String>()
        val foodDescription = mutableListOf<String>()
        val foodImage = mutableListOf<String>()
        val foodIngredient = mutableListOf<String>()

        // Get item quantities
        val foodQuantities = cartAdapter.getUpdatedItemQuantities()

        orderItReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    // get cartItems to the respective list
                    val orderItems = foodSnapshot.getValue(CartItems::class.java)

                    // Add items details into the list
                    orderItems?.foodName?.let { foodName.add(it) }
                    orderItems?.foodPrice?.let { foodPrice.add(it) }
                    orderItems?.foodDescription?.let { foodDescription.add(it) }
                    orderItems?.foodImage?.let { foodImage.add(it) }
                    //orderItems?.foodIngredient?.let { foodIngredient.add(it) }
                }

                orderNow(
                    foodName,
                    foodPrice,
                    foodDescription,
                    foodImage,
                    foodIngredient,
                    foodQuantities
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Order making failed. please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun orderNow(
        foodName: MutableList<String>,
        foodPrice: MutableList<String>,
        foodDescription: MutableList<String>,
        foodImage: MutableList<String>,
        foodIngredient: MutableList<String>,
        foodQuantities: MutableList<Int>
    ) {
        if (isAdded && context != null) {
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            intent.putExtra("FoodItemName", foodName as ArrayList<String>)
            intent.putExtra("FoodItemPrice", foodPrice as ArrayList<String>)
            intent.putExtra("FoodItemDescription", foodDescription as ArrayList<String>)
            intent.putExtra("FoodItemImage", foodImage as ArrayList<String>)
            intent.putExtra("FoodItemIngredient", foodIngredient as ArrayList<String>)
            intent.putExtra("FoodItemQuantities", foodQuantities as ArrayList<Int>)
            startActivity(intent)
        }
    }

    private fun retrieveCartItems() {
        // database reference to the firebase
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""
        val foodReference: DatabaseReference =
            database.reference.child("Users").child(userId).child("Cart Items")

        // list to store cart items
        foodNames = mutableListOf()
        foodPrices = mutableListOf()
        foodDescriptions = mutableListOf()
        foodImageUri = mutableListOf()
        //foodIngredient = mutableListOf()
        quantities = mutableListOf()

        // fetch data from the database
        foodReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    // get the cartItems object from the child node
                    val cartItems = foodSnapshot.getValue(CartItems::class.java)

                    // Add cart items details to the list
                    cartItems?.foodName?.let { foodNames.add(it) }
                    cartItems?.foodPrice?.let { foodPrices.add(it) }
                    cartItems?.foodDescription?.let { foodDescriptions.add(it) }
                    cartItems?.foodImage?.let { foodImageUri.add(it) }
                    cartItems?.foodQuantity?.let { quantities.add(it) }
                    //cartItems?.foodIngredient?.let { foodIngredient.add(it) }
                }
                setAdapter()
            }

            private fun setAdapter() {
                cartAdapter = CartAdapter(
                    requireContext(),
                    foodNames,
                    foodPrices,
                    foodDescriptions,
                    foodImageUri,
                    quantities
                )
                binding.rv2.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.rv2.adapter = cartAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Data not fetched", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        // Add any companion object members if needed
    }
}
