package com.example.abyan.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView
import com.example.abyan.R
import com.example.abyan.adapter.PostAdapter
import com.example.abyan.databinding.FragmentNewsUpdateBinding
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class NewsUpdateFragment : Fragment() {
    private lateinit var binding: FragmentNewsUpdateBinding
    private val sharedViewModel: ApplicationViewModel by activityViewModels()
    lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteKey: String
    var isOnline: Boolean = false
    companion object {
        val DELETEKEY = "deleteKey"
        private const val TAG = "AppTesting"


    }

    override fun onStart() {
        super.onStart()
        binding.bottomNavigation.selectedItemId = R.id.news

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deleteKey = it.getString(DELETEKEY).toString()
        }
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            view?.findNavController()?.navigateUp()
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
        connectionListener()
        database = sharedViewModel.database
        if (!deleteKey.equals("null")){
            Log.d("appTesting", "delete key function success: $deleteKey")
            var c = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Post?")
                .setNegativeButton("Cancel") { dialog, which ->

                }.setPositiveButton("Confirm"){ dialog, which ->
                    if (isOnline){deletePost(deleteKey)
                    }else{
                        Toast.makeText(context, "Client Offline", Toast.LENGTH_SHORT).show()}

                }.show()

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
        Log.d(
            TAG,"news fragment bottomnav selectedid ${binding.bottomNavigation.selectedItemId }" +
                "\nhome id ${R.id.home}" +
                "\nmap id ${R.id.map}" +
                "\nnews id ${R.id.news}")
        view.findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.news
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

    fun deletePost(key:String) {
        database.child("post").child("$key").removeValue()
            .addOnSuccessListener {
                sharedViewModel.postList.forEach {
                        post ->
                    if (post.key==key){
                        sharedViewModel.postList.remove(post)
                    }
                }
                try{recyclerView = binding.recyclerView
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.adapter = PostAdapter(requireContext(), sharedViewModel.postList,sharedViewModel.currentUserData.role.toString())
                }catch (e: java.lang.Exception){
                    Log.d(TAG,e.localizedMessage.toString())
                }
}
            .addOnFailureListener {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    fun connectionListener() {
        val connectedRef = Firebase.database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    isOnline = connected
                    Log.d(TAG, "connectionListener: connected")
                } else {
                    isOnline = connected
                    Log.d(TAG, "connectionListener: not connected")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "ConnectionListener was cancelled")
            }
        })

    }

}