package com.photon.eazyfood.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.photon.eazyfood.databinding.FragmentSearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.photon.eazyfood.adapter.MenuAdapter
import com.photon.eazyfood.model.MenuItem


class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: MenuAdapter
    private lateinit var database: FirebaseDatabase
    private val originalMenuItem = mutableListOf<MenuItem>()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        // Retrieve menu item from database
        retrieveMenuItem()

        // Setup Search View
        setupSearchView()




        return binding.root
    }

    private fun retrieveMenuItem() {

        // get database reference
        database = FirebaseDatabase.getInstance()

        // Reference ton the menu node
        val foodReference : DatabaseReference = database.reference.child("Menu")
        foodReference.addListenerForSingleValueEvent(object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(foodSnapshot in snapshot.children){

                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        originalMenuItem.add(it)
                    }
                }
                showAllMenu()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun showAllMenu() {

        val filterMenuItems =ArrayList(originalMenuItem)
        setAdapter(filterMenuItems)
    }

    private fun setAdapter(filterMenuItem: List<MenuItem>) {
        adapter = MenuAdapter(filterMenuItem,requireContext())
        binding.searchRv.layoutManager = LinearLayoutManager(requireContext())
        binding.searchRv.adapter = adapter
    }


    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {

                filterMenuItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterMenuItems(newText)
                return true
            }
        })

    }

    private fun filterMenuItems(query: String) {

        val filterMenuItem = originalMenuItem.filter {
            it.foodName?.contains(query,ignoreCase = true) == true
        }

        setAdapter(filterMenuItem)

    }




    companion object {

    }
}


