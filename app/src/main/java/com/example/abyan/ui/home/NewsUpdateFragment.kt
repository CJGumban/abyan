package com.example.abyan.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.R
import com.example.abyan.databinding.FragmentNewsUpdateBinding
import com.example.abyan.viewmodel.ApplicationViewModel


class NewsUpdateFragment : Fragment() {
    private lateinit var binding: FragmentNewsUpdateBinding
    private val applicationViewModel: ApplicationViewModel by activityViewModels()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewsUpdateBinding.inflate(inflater, container, false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //code for making bottomnav item visible
//      binding.bottomNavigation.menu[0].isVisible = false
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
        when(itemId) {
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
        }
    }}