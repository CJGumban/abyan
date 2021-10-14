package com.example.abyan.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.abyan.adapter.CoordinateAdapter
import com.example.abyan.databinding.FragmentMapListViewBinding
import com.example.abyan.model.Coordinate
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.android.material.tabs.TabLayout


class MapListViewFragment : Fragment() {
    private lateinit var binding: FragmentMapListViewBinding
    private val applicationViewModel: ApplicationViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    var coordinateList: MutableList<Coordinate> = ArrayList()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMapListViewBinding.inflate(inflater, container, false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Apptesting", " mapListView coordinateList: ${coordinateList.toString()}")

        coordinateList.clear()
        applicationViewModel.coordinatelist.forEach { coordinate ->
            if (coordinate.status == "need help"){
                coordinateList.add(coordinate)
            }
        }
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CoordinateAdapter(requireContext(), coordinateList)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d("Apptesting","tab selected ${ tab?.position}")

                when(tab?.position){
                    0->{
                        coordinateList.clear()
                        applicationViewModel.coordinatelist.forEach { coordinate ->
                        if (coordinate.status == "need help"){
                            coordinateList.add(coordinate)
                        }
                    }}
                    1->{
                        coordinateList.clear()
                        applicationViewModel.coordinatelist.forEach { coordinate ->
                        if (coordinate.status == "ongoing"){
                            coordinateList.add(coordinate)
                        }
                    }}
                    2->{
                        coordinateList.clear()
                        applicationViewModel.coordinatelist.forEach { coordinate ->
                        if (coordinate.status == "done"){
                            coordinateList.add(coordinate)
                        }
                    }}
                    
                }

                recyclerView = binding.recyclerView
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = CoordinateAdapter(requireContext(), coordinateList)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })


        binding.topAppBar.setNavigationOnClickListener {
            val action = MapListViewFragmentDirections.actionMapListViewFragmentToSendLocationFragment()
            view.findNavController().navigate(action)
        }




    }

}