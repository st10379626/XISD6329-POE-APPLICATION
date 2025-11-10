package com.example.nunosrealtyapp.ui.shared

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import com.example.nunosrealtyapp.databinding.ActivityMapBinding
import com.example.nunosrealtyapp.ui.customer.PropertyDetailActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: ActivityMapBinding
    private val viewModel: MapViewModel by viewModels()
    private var googleMap: GoogleMap? = null
    private val markers = mutableMapOf<String, Marker>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupUI()
        setupListeners()

        val mapFragment = supportFragmentManager
            .findFragmentById(com.example.nunosrealtyapp.R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupUI() {
        binding.locationTextView.text = "Sandton, Gauteng"
    }

    private fun setupListeners() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchLocation(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.length >= 3) {
                        viewModel.searchLocation(it)
                    } else if (it.isEmpty()) {
                        viewModel.loadProperties()
                    }
                }
                return true
            }
        })

        binding.currentLocationButton.setOnClickListener {
            getDeviceLocation()
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            setOnMarkerClickListener(this@MapActivity)

            val sandton = LatLng(-26.1076, 28.0567)
            moveCamera(CameraUpdateFactory.newLatLngZoom(sandton, 12f))
        }

        observeViewModel()
        viewModel.loadProperties()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.properties.collect { properties ->
                googleMap?.let { updateMapMarkers(it, properties) }
            }
        }

        lifecycleScope.launch {
            viewModel.currentLocation.collect { location ->
                location?.let {
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude), 13f
                        )
                    )
                }
            }
        }

        lifecycleScope.launch {
            viewModel.selectedLocation.collect { locationName ->
                binding.locationTextView.text = locationName
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val propertyId = marker.tag as? String ?: return false
        viewModel.getProperty(propertyId)?.let { property ->
            showPropertyDialog(property, marker.position)
        }
        return true
    }

    private fun updateMapMarkers(map: GoogleMap, properties: List<com.example.nunosrealtyapp.data.model.Property>) {
        map.clear()
        markers.clear()

        properties.forEach { property ->
            val position = LatLng(property.latitude, property.longitude)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(property.title)
                    .snippet("R${property.price}${if (property.isForSale) "M" else "/m"}")
            )
            marker?.tag = property.id
            marker?.let { markers[property.id] = it }
        }
    }

    private fun showPropertyDialog(property: com.example.nunosrealtyapp.data.model.Property, destination: LatLng) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(property.title)
            .setMessage(
                "${property.city}, ${property.province}\n" +
                        "Beds: ${property.beds} | Baths: ${property.baths}\n" +
                        "Price: R${property.price}${if (property.isForSale) "M" else "/m"}"
            )
            .setPositiveButton("View Details") { _, _ ->
                startActivity(Intent(this, PropertyDetailActivity::class.java).apply {
                    putExtra("property_id", property.id)
                })
            }
            .setNeutralButton("Get Directions") { _, _ ->
                userLocation?.let {
                    val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${it.latitude},${it.longitude}&destination=${destination.latitude},${destination.longitude}&travelmode=driving")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun getDeviceLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    userLocation = it
                    viewModel.setCurrentLocation(it)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
