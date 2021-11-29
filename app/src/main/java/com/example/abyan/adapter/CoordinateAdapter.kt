package com.example.abyan.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.abyan.ui.home.MapListViewFragmentDirections
import com.example.abyan.R
import com.example.abyan.adapter.CoordinateAdapter.CoordinateViewHolder
import com.example.abyan.model.Coordinate

class CoordinateAdapter(private val context: Context,
                        private val coordinate: List<Coordinate>): RecyclerView.Adapter<CoordinateViewHolder>() {


    class CoordinateViewHolder(val view: View) : RecyclerView.ViewHolder(view)  {
        val icon = view.findViewById<ImageView>(R.id.image_type)
        val fullname = view.findViewById<TextView>(R.id.textView_fullname)
        val type = view.findViewById<TextView>(R.id.textview_type)
        val age = view.findViewById<TextView>(R.id.textview_age)
        val timeDate = view.findViewById<TextView>(R.id.textview_timedate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoordinateViewHolder {
        val layout = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.list_coordinates, parent, false)
        return CoordinateViewHolder(layout)
    }

    override fun onBindViewHolder(holder: CoordinateViewHolder, position: Int) {
        val item = coordinate[position]
        holder.fullname.text = item.fullname.toString()
        holder.type.text = "${item.type.toString()}"
        holder.age.text = "Age: ${item.age.toString()}"
        holder.timeDate.text = "${item.dateTime.toString()}"
        holder.icon.setImageResource(setMarker(item))
    holder.view.setOnClickListener {
        val action = MapListViewFragmentDirections.actionMapListViewFragmentToSendLocationFragment(item.key)
        holder.view.findNavController().navigate(action)
    }
    }


    override fun getItemCount(): Int {
        return coordinate.size
    }
    private fun setMarker(coordinate: Coordinate) : Int {
        var pin: Int = R.drawable.pin_null_red


        var type = coordinate.type
        var status = coordinate.status
        when (type) {
            "ambulance" -> {
                when (status) {
                    "need help" -> {
                        pin = R.drawable.pin_ambulance_red
                    }
                    "ongoing" -> {
                        pin = R.drawable.pin_ambulance_blue
                    }
                    "done" -> {
                        pin = R.drawable.pin_ambulance_green

                    }
                }
            }
            "car accident" -> {
                when (status) {
                    "need help" -> {
                        pin = R.drawable.pin_car_accident_red
                    }
                    "ongoing" -> {
                        pin = R.drawable.pin_car_accident_blue
                    }
                    "done" -> {
                        pin = R.drawable.pin_car_accident_green

                    }
                }

            }
            "fire" -> {
                when (status) {
                    "need help" -> {
                        pin = R.drawable.pin_fire_red

                    }
                    "ongoing" -> {
                        pin = R.drawable.pin_fire_blue

                    }
                    "done" -> {
                        pin = R.drawable.pin_fire_green

                    }
                }

            }
            "crime" -> {
                when (status) {
                    "need help" -> {
                        pin = R.drawable.pin_crime_red

                    }
                    "ongoing" -> {
                        pin = R.drawable.pin_crime_blue

                    }
                    "done" -> {
                        pin = R.drawable.pin_crime_green

                    }
                }

            }
            null -> {
                when (status) {
                    "need help" -> {

                    }
                    "ongoing" -> {
                        pin = R.drawable.pin_null_blue

                    }
                    "done" -> {
                        pin = R.drawable.pin_null_green

                    }
                }
            }
        }

        return pin


    }

}