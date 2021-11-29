package com.example.abyan.ui.home

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs

import com.example.abyan.databinding.FragmentPostMessageBinding
import com.example.abyan.model.Post
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import java.util.*


class PostMessageFragment : Fragment() {
    private lateinit var binding: FragmentPostMessageBinding
    private val applicationViewModel: ApplicationViewModel by activityViewModels()
    private lateinit var editKey: String
    var postToEdit: Post? = null
    val args: PostMessageFragmentArgs by navArgs()
    var auth: FirebaseAuth = Firebase.auth
    var database: DatabaseReference = Firebase.database.reference
    var isOnline: Boolean = false
    companion object {
        val EDITKEY = "editKey"
        val TAG = "PostMessageFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            editKey = it.getString(PostMessageFragment.EDITKEY).toString()
            applicationViewModel.postList.forEach {post->
                if (post.key == editKey){
                    postToEdit = post
                }
            }

        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPostMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectionListener()
        if (postToEdit!=null){
            binding.edittextBody.setText(postToEdit?.body.toString())
        }
        binding.topAppBar.setNavigationOnClickListener {
            view?.findNavController()?.navigateUp()
        }
        binding.topAppBar.menu[0].setOnMenuItemClickListener { item->
            run {
                if (isOnline){post()
                    binding.progressBar.show()
                } else {
                    Toast.makeText(context, "Client Offline", Toast.LENGTH_SHORT).show()}

            }
            true
        }

    }

    private fun post() {
        var body = binding.edittextBody.text.toString()
        if (postToEdit!=null){
            editPost(post = postToEdit!!, body = body)
            postToEdit = null
            editKey = null.toString()
        }else {
            writePost(body)
        }
    }

    fun writePost(body: String = "") {
        var tsLong = System.currentTimeMillis() / 1000
        Log.d(ApplicationViewModel.TAG, " timestamp  $tsLong")
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = tsLong * 1000L
        val date: String = DateFormat.format("MMM dd, yyyy", cal).toString()
        val userId = auth.currentUser?.uid.toString()
        val key = database.push().key
        if (key == null) {
            Log.w(ApplicationViewModel.TAG, "Couldn't get push key for posts")
            return
        }
        Log.w(ApplicationViewModel.TAG, "$key")
        val message = Post(key, body, date)
        val messageValues = message.toMap()
        val childUpdates = hashMapOf<String, Any>(
            "/post/$key" to messageValues,
            "user-post/$userId/$key" to messageValues

        )
     database.updateChildren(childUpdates)
        .addOnSuccessListener { done(true,"")
           Toast.makeText(context, "Post Success", Toast.LENGTH_SHORT).show()
       }.addOnFailureListener {
           done(false,it.localizedMessage)
       }
    }





    fun editPost(post: Post,body: String = "") {
        post.body = body
        val postValues = post.toMap()
        val childUpdates = hashMapOf<String, Any>(
            "post/${post.key}" to postValues,
            "user-post/${post.key}" to postValues,
        )
        database.updateChildren(childUpdates)
            .addOnSuccessListener { done(true,"")
                Toast.makeText(context, "Post Success", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                done(false,it.localizedMessage)
            }
    }

    fun done(success: Boolean,exception:String){
        if (success){
            val action =
                PostMessageFragmentDirections.actionPostMessageFragmentToNewsUpdateFragment()
            view?.findNavController()?.navigate(action)
            binding.progressBar.hide()
        }else {
            Toast.makeText(requireContext(), exception, Toast.LENGTH_SHORT).show()
            binding.progressBar.hide()
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