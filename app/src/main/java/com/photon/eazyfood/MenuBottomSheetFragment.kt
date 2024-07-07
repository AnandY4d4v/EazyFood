package com.photon.eazyfood

import android.os.Bundle
import android.view.LayoutInflater
import com.photon.eazyfood.model.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.photon.eazyfood.adapter.MenuAdapter
import com.photon.eazyfood.databinding.FragmentMenuBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MenuBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var menuItems: MutableList<MenuItem>
    private lateinit var binding: FragmentMenuBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)
        binding.backBtnMenu.setOnClickListener {
            dismiss()
        }
        retrieveMenuItem()




        return binding.root
    }

    private fun retrieveMenuItem() {
        database = FirebaseDatabase.getInstance()
        val foodRef = database.reference.child("Menu")
        menuItems = mutableListOf()
        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let { menuItems.add(it) }
                }
                // ones data receive , set to adapter
                setAdapter()
            }

            private fun setAdapter() {
                val adapter = MenuAdapter(menuItems, requireContext())
                binding.mRv.layoutManager = LinearLayoutManager(requireContext())
                binding.mRv.adapter=adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }


        })
    }

    companion object {

    }
}