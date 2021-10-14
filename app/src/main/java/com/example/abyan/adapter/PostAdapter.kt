package com.example.abyan.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.abyan.R
import com.example.abyan.model.Coordinate
import com.example.abyan.model.Post
import com.example.abyan.ui.home.NewsUpdateFragmentDirections
import com.example.abyan.ui.home.PostMessageFragmentDirections
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PostAdapter(private val context: Context,
                  private val post: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>(){

    class PostViewHolder(val view: View) : RecyclerView.ViewHolder(view)  {
            val title = view.findViewById<TextView>(R.id.textview_post_title)
            val body = view.findViewById<TextView>(R.id.textview_post_body)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val layout = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.list_message, parent, false)
        return PostViewHolder(layout)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = post[position]
        holder.view.setOnClickListener {
            var c = MaterialAlertDialogBuilder(context)
            .setTitle("Post Selected")
            .setNeutralButton("Cancel"){dialog, which ->
                dialog.dismiss()
            }
            .setNegativeButton("Edit"){dialog, which ->

                val action = NewsUpdateFragmentDirections.actionNewsUpdateFragmentToPostMessageFragment(editKey = item.key)
                holder.view.findNavController().navigate(action)
            }
            .setPositiveButton("Delete") { dialog, which ->
                val action = NewsUpdateFragmentDirections.actionNewsUpdateFragmentSelf(deleteKey = item.key)
                holder.view.findNavController().navigate(action)
            }.show()
        }
        holder.title.text = item.title.toString()
        holder.body.text = item.body.toString()
    }

    override fun getItemCount(): Int {
        return post.size
    }


}