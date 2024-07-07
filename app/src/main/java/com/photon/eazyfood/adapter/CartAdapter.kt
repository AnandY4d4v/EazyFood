package com.photon.eazyfood.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.photon.eazyfood.databinding.CartItemBinding

class CartAdapter(
    private val context: Context,
    private val name: MutableList<String>,
    private val price: MutableList<String>,
    private val cartDescription: MutableList<String>,
    private val image: MutableList<String>,
    private var cartQuantity: MutableList<Int>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    init {
        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        val cartItemsNumber = name.size
        itemQuantities = IntArray(cartItemsNumber) { 1 }
        cartRef = database.reference.child("Users").child(userId).child("Cart Items")
    }

    companion object {
        private var itemQuantities: IntArray = intArrayOf()
        private lateinit var cartRef: DatabaseReference
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = name.size

    fun getUpdatedItemQuantities(): MutableList<Int> {
        return itemQuantities.toMutableList()
    }

    inner class CartViewHolder(private val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                val itemQuantity = itemQuantities[position]
                cartFoodName.text = name[position]
                cartPrice.text = price[position]
                carItemQuantity.text = itemQuantity.toString()

                // Load image using Glide
                val uriString = image[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(cartImage)

                minusBtn.setOnClickListener {
                    decreaseQuantity(position)
                }
                plusBtn.setOnClickListener {
                    increaseQuantity(position)
                }
                deleteBtn.setOnClickListener {
                    val itemPosition = adapterPosition
                    if (itemPosition != RecyclerView.NO_POSITION) {
                        deleteItem(itemPosition)
                    }
                }
            }
        }

        private fun increaseQuantity(position: Int) {
            if (itemQuantities[position] < 10) {
                itemQuantities[position]++
                cartQuantity[position] = itemQuantities[position]
                binding.carItemQuantity.text = itemQuantities[position].toString()
            }
        }

        private fun decreaseQuantity(position: Int) {
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--
                cartQuantity[position] = itemQuantities[position]
                binding.carItemQuantity.text = itemQuantities[position].toString()
            }
        }

        private fun deleteItem(position: Int) {
            getUniqueKeyAtPosition(position) { uniqueKey ->
                if (uniqueKey != null) {
                    removeItems(position, uniqueKey)
                }
            }
        }

        private fun removeItems(position: Int, uniqueKey: String) {
            cartRef.child(uniqueKey).removeValue().addOnSuccessListener {
                // Log the sizes of the lists before removal
                Log.d("CartAdapter", "Sizes before removal - name: ${name.size}, image: ${image.size}, price: ${price.size}, cartDescription: ${cartDescription.size}, cartQuantity: ${cartQuantity.size}, itemQuantities: ${itemQuantities.size}")

                if (position < name.size && position < image.size && position < price.size && position < cartDescription.size && position < cartQuantity.size  && position < itemQuantities.size) {
                    // Remove items from the lists
                    name.removeAt(position)
                    image.removeAt(position)
                    price.removeAt(position)
                    cartDescription.removeAt(position)
                    cartQuantity.removeAt(position)

                    // Update itemQuantities array
                    itemQuantities = itemQuantities.filterIndexed { index, _ -> index != position }.toIntArray()

                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, name.size)

                    Toast.makeText(context, "Item removed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to remove item: index out of bounds", Toast.LENGTH_SHORT).show()
                    Log.e("CartAdapter", "Failed to remove item at position $position due to index out of bounds")
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
        }


        private fun getUniqueKeyAtPosition(positionRetrieve: Int, onComplete: (String?) -> Unit) {
            cartRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var uniqueKey: String? = null
                    snapshot.children.forEachIndexed { index, dataSnapshot ->
                        if (index == positionRetrieve) {
                            uniqueKey = dataSnapshot.key
                            return@forEachIndexed
                        }
                    }
                    onComplete(uniqueKey)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}
