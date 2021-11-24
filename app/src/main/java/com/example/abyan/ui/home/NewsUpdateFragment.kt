package com.example.abyan.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView
import com.example.abyan.R
import com.example.abyan.adapter.PostAdapter
import com.example.abyan.databinding.FragmentNewsUpdateBinding
import com.example.abyan.model.User
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class NewsUpdateFragment : Fragment() {
    private lateinit var binding: FragmentNewsUpdateBinding
    private val sharedViewModel: ApplicationViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteKey: String
    companion object {
        val DELETEKEY = "deleteKey"
        private const val TAG = "AppTesting"


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deleteKey = it.getString(DELETEKEY).toString()
        }
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event

            setCurrentFragment("home")

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
            sharedViewModel.deletePost(deleteKey)
            recyclerView = binding.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = PostAdapter(requireContext(), sharedViewModel.postList,sharedViewModel.currentUserData.role.toString())
        }
    else {
            recyclerView = binding.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = PostAdapter(requireContext(), sharedViewModel.postList,sharedViewModel.currentUserData.role.toString())
        }
        binding.floatingActionButton.setOnClickListener { setCurrentFragment("send message") }
        binding.bottomNavigation.selectedItemId = R.id.news
        Log.d(
            TAG,"news fragment bottomnav selectedid ${binding.bottomNavigation.selectedItemId }" +
                "\nhome id ${R.id.home}" +
                "\nmap id ${R.id.map}" +
                "\nnews id ${R.id.news}")
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.news->setCurrentFragment("news")
                R.id.home->setCurrentFragment("home")
                R.id.map->setCurrentFragment("map")
            }
            true
        }
        checkRoles()
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



    fun checkRoles(){
        Log.d(TAG,"checkRoles ${sharedViewModel.currentUserData.role.toString()}")
        when {
            sharedViewModel.currentUserData.role.equals("responder") -> {
                binding.bottomNavigation.menu[2].isVisible
                binding.floatingActionButton.hide()

            }
            sharedViewModel.currentUserData.role.equals("user") -> {
                binding.bottomNavigation.menu[2].isVisible = false
                binding.floatingActionButton.hide()




            }
            sharedViewModel.currentUserData.role.equals("admin") -> {

            }
        }
    }



}