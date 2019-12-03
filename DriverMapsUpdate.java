package com.example.careshare;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class DriverMapsUpdate extends Fragment implements OnMapReadyCallback{

    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private Location currentLocation;
    private LocationRequest mLocationRequest;
    private DatabaseReference reference;
    private static final int REQUEST_CODE = 101;
    private LatLng latLng;
    private FusedLocationProviderClient fusedLocationProviderClient;


    public DriverMapsUpdate() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        getLastLocation();
        return inflater.inflate(R.layout.fragment_driver_maps_update, container, false);
    }

    private void getLastLocation() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLocation = location;
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    SupportMapFragment supportMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.driver_map);
                    supportMapFragment.getMapAsync(DriverMapsUpdate.this);
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.driver_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setMyLocationEnabled(true);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are here.");
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
        map.addMarker(markerOptions);


        //check if location is enabled on the device
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE: if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }
            break;
        }
    }
}
