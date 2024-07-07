package com.photon.eazyfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.recyclerview.widget.LinearLayoutManager
import com.photon.eazyfood.adapter.RecentBuyAdapter
import com.photon.eazyfood.databinding.ActivityRecentOrderItemsBinding
import com.photon.eazyfood.model.OrderDetails

class RecentOrderItemsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecentOrderItemsBinding
    private lateinit var allFoodNames: ArrayList<String>
    private lateinit var allFoodImages: ArrayList<String>
    private lateinit var allFoodPrices: ArrayList<String>
    private lateinit var allFoodQuantities: ArrayList<Int>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentOrderItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        val recentBuyOrderItem =
            intent.getSerializableExtra("recentBuyOrderItem") as ArrayList<OrderDetails>
        recentBuyOrderItem?.let { orderDetails ->
            if (orderDetails.isNotEmpty()) {
                val recentOrderDetails = orderDetails[0]

                allFoodNames = recentOrderDetails.foodItemName as ArrayList<String>
                allFoodImages = recentOrderDetails.foodItemImage as ArrayList<String>
                allFoodPrices = recentOrderDetails.foodItemPrice as ArrayList<String>
                allFoodQuantities = recentOrderDetails.foodItemQuantities as ArrayList<Int>
            }
        }

        setAdapter()
    }

    private fun setAdapter() {
        val rv = binding.rv
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = RecentBuyAdapter(this,allFoodNames,allFoodImages,allFoodPrices,allFoodQuantities)

        rv.adapter = adapter
    }
}