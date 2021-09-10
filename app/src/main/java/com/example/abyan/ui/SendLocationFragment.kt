package com.example.abyan.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.abyan.R
import com.example.abyan.databinding.FragmentLoginBinding
import com.example.abyan.databinding.FragmentSendLocationBinding
import com.example.abyan.viewmodel.LoginSignUpViewModel

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.GoogleMap




class SendLocationFragment : Fragment() {
    private lateinit var binding: FragmentSendLocationBinding
    private val sharedViewModel : LoginSignUpViewModel by activityViewModels()
    private val REQUEST_LOCATION_PERMISSION = 1
    private var mMap: GoogleMap? = null

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }


    fun getmMap(mMap:GoogleMap):GoogleMap{
        return mMap
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mMap?.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }
    private val callback = OnMapReadyCallback { googleMap ->


        enableMyLocation()
        getmMap(googleMap)

        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))



        val kyoto = LatLng(sharedViewModel.lat()!!, sharedViewModel.lng()!!)
        // Set the map type to Hybrid.
        // Set the map type to Hybrid.
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        // Add a marker on the map coordinates.
        // Add a marker on the map coordinates.
        googleMap.addMarker(
            MarkerOptions()
                .position(kyoto)
                .title("Ivisan")
        )

        // Move the camera to the map coordinates and zoom in closer.
        // Move the camera to the map coordinates and zoom in closer.
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(kyoto))
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(15f))
        // Display traffic.
        // Display traffic.
        googleMap.isTrafficEnabled = true

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = FragmentSendLocationBinding.inflate(inflater, container, false)
        binding = fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)


    }
}