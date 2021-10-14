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
import com.example.abyan.R
import com.example.abyan.adapter.PostAdapter
import com.example.abyan.databinding.FragmentNewsUpdateBinding
import com.example.abyan.model.Coordinate
import com.example.abyan.viewmodel.ApplicationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


class NewsUpdateFragment : Fragment() {
    private lateinit var binding: FragmentNewsUpdateBinding
    private val applicationViewModel: ApplicationViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteKey: String
    companion object {
        val DELETEKEY = "deleteKey"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deleteKey = it.getString(DELETEKEY).toString()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewsUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (deleteKey!=null||deleteKey!=""){
            Log.d("appTesting", "delete key function success: $deleteKey")
            applicationViewModel.deletePost(deleteKey)
            recyclerView = binding.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = PostAdapter(requireContext(), applicationViewModel.postList)
        }
    else {
            recyclerView = binding.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = PostAdapter(requireContext(), applicationViewModel.postList)
        }
        //code for making bottomnav item visible
//      binding.bottomNavigation.menu[0].isVisible = false
        binding.floatingActionButton.setOnClickListener { setCurrentFragment("send message") }
        binding.bottomNavigation.selectedItemId = R.id.news
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.news->setCurrentFragment("news")
                R.id.home->setCurrentFragment("home")
                R.id.map->setCurrentFragment("map")
            }
            true
        }
    }


        fun setCurrentFragment(itemId: String) {
           try {

               when (itemId) {
                   "home" -> {
                       val action =
                           NewsUpdateFragmentDirections.actionNewsUpdateFragmentToHomeFragment()
                       view?.findNavController()?.navigate(action)
                   }
                   "map" -> {
                       val action =
                           NewsUpdateFragmentDirections.actionNewsUpdateFragmentToSendLocationFragment()
                       view?.findNavController()?.navigate(action)
                   }

                   "send message" -> {
                       val action =
                           NewsUpdateFragmentDirections.actionNewsUpdateFragmentToPostMessageFragment()
                       view?.findNavController()?.navigate(action)
                   }
               }
           }catch (e: Exception){Log.e("AppTesting", "${e.message}")}
        }





}