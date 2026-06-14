package com.example.a216295_cikguizwan_project2.ui.theme

import android.location.Geocoder
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickLocationScreen(
    onLocationSet: (LatLng, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

// Properly tie MapView lifecycle to the composable's lifecycle
    DisposableEffect(Unit) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    // Default initial camera coordinate (Bangi/UKM Area)
    val defaultBangi = LatLng(2.9289, 101.7812)

    var markerPosition by remember { mutableStateOf(defaultBangi) }
    var addressName by remember { mutableStateOf("Detecting location...") }

    var myGoogleMap by remember { mutableStateOf<com.google.android.gms.maps.GoogleMap?>(null) }
    var globalMarker by remember { mutableStateOf<Marker?>(null) }

    // Geocoder sensor to fetch real-time full addresses from coordinates
    fun getAddressFromLatLng(latLng: LatLng): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) ?: "Bangi, Selangor"
            } else {
                "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
            }
        } catch (e: Exception) {
            "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
        }
    }

    LaunchedEffect(Unit) {
        addressName = getAddressFromLatLng(defaultBangi)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pin Delivery Address") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4A148C), // Mealify Dark Purple Theme
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        onCreate(Bundle())
                        getMapAsync { googleMap ->
                            myGoogleMap = googleMap
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultBangi, 16f))

                            val markerOptions = MarkerOptions()
                                .position(defaultBangi)
                                .title("Delivery Location")
                                .draggable(true)

                            globalMarker = googleMap.addMarker(markerOptions)

                            // Map Tap Event Listener
                            googleMap.setOnMapClickListener { clickedLatLng ->
                                markerPosition = clickedLatLng
                                globalMarker?.position = clickedLatLng
                                addressName = getAddressFromLatLng(clickedLatLng)
                            }

                            // Marker Drag Event Listener
                            googleMap.setOnMarkerDragListener(object : com.google.android.gms.maps.GoogleMap.OnMarkerDragListener {
                                override fun onMarkerDragStart(m: Marker) {}
                                override fun onMarkerDrag(m: Marker) {}
                                override fun onMarkerDragEnd(m: Marker) {
                                    markerPosition = m.position
                                    addressName = getAddressFromLatLng(m.position)
                                }
                            })

                            // Emulator Core Location Sensor Button Integration
                            try {
                                googleMap.isMyLocationEnabled = true
                                googleMap.setOnMyLocationButtonClickListener {
                                    googleMap.myLocation?.let { loc ->
                                        val currLatLng = LatLng(loc.latitude, loc.longitude)
                                        markerPosition = currLatLng
                                        globalMarker?.position = currLatLng
                                        addressName = getAddressFromLatLng(currLatLng)
                                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLatLng, 16f))
                                    }
                                    false
                                }
                            } catch (e: SecurityException) {
                                e.printStackTrace()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    mapView.onResume()
                    myGoogleMap?.let { map ->
                        globalMarker?.position = markerPosition
                    }
                }
            )

            // Info Card and Confirmation Button Section
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📍 $addressName",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = {
                            // Returns selected data and pops backward safely
                            onLocationSet(markerPosition, addressName)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7CB342)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("Set This Location", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}