package com.example.abyan.ui.home

import android.os.Bundle
import android.view.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController

import com.example.abyan.databinding.FragmentPostMessageBinding
import com.example.abyan.model.Post
import com.example.abyan.viewmodel.ApplicationViewModel






class PostMessageFragment : Fragment() {
    private lateinit var binding: FragmentPostMessageBinding
    private val applicationViewModel: ApplicationViewModel by activityViewModels()
    private lateinit var editKey: String
    var postToEdit: Post? = null

    companion object {
        val EDITKEY = "editKey"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            editKey = it.getString(PostMessageFragment.EDITKEY).toString()
            applicationViewModel.postList.forEach {post->
                if (post.key==editKey){
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
        if (postToEdit!=null){
            binding.edittextTitle.setText(postToEdit?.title.toString())
            binding.edittextBody.setText(postToEdit?.body.toString())
        }
        binding.topAppBar.setNavigationOnClickListener {
            val action =
            PostMessageFragmentDirections.actionPostMessageFragmentToNewsUpdateFragment()
            view?.findNavController()?.navigate(action)
        }
        binding.topAppBar.menu[0].setOnMenuItemClickListener { item->
            run {
                post()
                val action =
                    PostMessageFragmentDirections.actionPostMessageFragmentToNewsUpdateFragment()
                view?.findNavController()?.navigate(action)
            }
            true
        }

    }

    private fun post() {
        var title = binding.edittextTitle.text.toString()
        var body = binding.edittextBody.text.toString()
        if (postToEdit!=null){
            applicationViewModel.editPost(post = postToEdit!!,title, body)
            postToEdit = null
            editKey = null.toString()
        }else {
            applicationViewModel.writePost(title, body)
        }
    }


}