package com.example.careshare;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.careshare.Helper.IGoogleAPI;
import com.example.careshare.Helper.RetrofitClient;
import com.example.careshare.directionhelpers.FetchURL;
import com.example.careshare.directionhelpers.TaskLoadedCallback;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    double riderLat, riderLng;
    String driverId;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private LocationRequest mLocationRequest;
    private GeoFire geoFire;
    private Marker driverMarker, riderMarker;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Polyline directions;
    private Button rideCancel, rideComplete;
    IGoogleAPI mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.driverTracking);
        mapFragment.getMapAsync(DriverTracking.this);

        //init
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mService = RetrofitClient.getClient("https://maps.googleapis.com").create(IGoogleAPI.class);
        rideCancel = findViewById(R.id.rideCancel);
        rideComplete = findViewById(R.id.rideComplete);

        //ride cancel
        rideCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference request = FirebaseDatabase.getInstance().getReference().child("RideRequest").child(driverId)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                request.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(DriverTracking.this, "Ride Cancelled", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(DriverTracking.this, RiderMaps.class));
                        DriverTracking.this.finish();
                    }
                });
            }
        });

        rideComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverTracking.this, PaymentRider.class);
                intent.putExtra("driverId", driverId);
                startActivity(intent);
                DriverTracking.this.finish();
                recordRide();
            }
        });


        if (getIntent() != null){

            riderLat = getIntent().getDoubleExtra("lat", -1.0);
            riderLng = getIntent().getDoubleExtra("lng", -1.0);
            driverId = getIntent().getStringExtra("driverId");
        }

        setUpLocation();
        updateLocation();
        fetchLastLocation();
    }

    private void recordRide() {
        DatabaseReference history = FirebaseDatabase.getInstance().getReference("RiderHistory").child(currentUser.getUid());
        history.child("DriverId").setValue(driverId);
        history.child("Timestamp").setValue(System.currentTimeMillis()/1000);
    }

    //getting permission
    private void setUpLocation() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    //getting updated location
    private void updateLocation() {
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

    }

    //fetching current location
    private void fetchLastLocation() {

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null){
                    currentLocation = location;
                    if (driverMarker != null){
                        driverMarker.remove();
                    }
                    driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("Driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.caricon)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
                    if (directions != null){
                        directions.remove();
                    }

                    riderMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(riderLat, riderLng))
                            .title("Rider").icon(BitmapDescriptorFactory.defaultMarker()));
                    String url = getDirections(driverMarker.getPosition(), riderMarker.getPosition(), "driving");
                    new FetchURL(DriverTracking.this).execute(url, "driving");
                }
            }
        });

    }

    //get direction
    private String getDirections(LatLng origin, LatLng dest, String directionMode) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameters + "&key=AIzaSyDlIslLbq2PCPh-FAAzGq2xggoMaIsv0f0";
        return url;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            fetchLastLocation();
        }
    };

    @Override
    public void onTaskDone(Object... values) {

        if (directions != null){
            directions.remove();
        }
        directions = mMap.addPolyline((PolylineOptions)values[0]);
    }
}
