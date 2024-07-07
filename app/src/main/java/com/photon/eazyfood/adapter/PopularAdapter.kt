package com.photon.eazyfood.adapter



import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.photon.eazyfood.DetailActivity
//import com.photon.eazyfood.DetailsActivity
import com.photon.eazyfood.databinding.PopularItemBinding

class PopularAdapter(
    private val item: List<String>,
    private val price: List<String>,
    private val image: List<Int>,
    private val requireContext: Context
) : RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {

        return PopularViewHolder(
            PopularItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {

        val item = item[position]
        val image = image[position]
        val price = price[position]
        holder.bind(item, price, image)
        holder.itemView.setOnClickListener {

            // set Click Listner to open food details

            val intent = Intent(requireContext, DetailActivity::class.java)
            intent.putExtra("MenuItemName", item)
            intent.putExtra("MenuItemImage", image)
            requireContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return item.size

    }

    class PopularViewHolder(val binding: PopularItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val imageView = binding.foodImage

        fun bind(item: String, price: String, image: Int) {

            binding.foodName.text = item
            binding.foodPrice.text = price
            imageView.setImageResource(image)


        }


    }

}