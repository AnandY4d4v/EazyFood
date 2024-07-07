package com.photon.eazyfood.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photon.eazyfood.DetailActivity
import com.photon.eazyfood.databinding.MenuTestBinding
import com.photon.eazyfood.model.MenuItem

class MenuAdapter(
    private val menuItem: List<MenuItem>,
    private val requireContext:Context
): RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = MenuTestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }



    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(position)
    }
    override fun getItemCount(): Int =menuItem.size
    inner class MenuViewHolder(private val binding: MenuTestBinding) :
        RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
                    val position=adapterPosition
                    if(position!=RecyclerView.NO_POSITION) {
                        openDetailActivity(position)
                    }
                }
            }
        fun bind(position: Int) {

                val menuItem = menuItem[position]
                binding.apply {
                    menuFoodName.text = menuItem.foodName
                    menuFoodPrice.text = menuItem.foodPrice
                    val uri = Uri.parse(menuItem.foodImage)
                    Glide.with(requireContext).load(uri).into(menuImage)


                }

            }

        }



    private fun openDetailActivity(position: Int) {
        val menuItem = menuItem[position]

        // a intent to open details activity and pass data
        val intent = Intent(requireContext, DetailActivity::class.java).apply {
            putExtra("MenuItemName", menuItem.foodName)
            putExtra("MenuItemImage", menuItem.foodImage)
            putExtra("MenuPrice", menuItem.foodPrice)
            putExtra("MenuDescription", menuItem.foodDescription)
            putExtra("MenuIngredients", menuItem.foodIngredient)
        }
        // start the details activity
        requireContext.startActivity(intent)
    }

}


