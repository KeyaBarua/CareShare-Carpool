package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RiderTracking extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    double riderLat, riderLng;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private LocationRequest mLocationRequest;
    private GeoFire geoFire;
    private Marker driverMarker, riderMarker;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Polyline directions;
    private Button completeRide;
    IGoogleAPI mService;
    private String riderId;
    private double totalEarnings=0;
    private String ridecost="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //init
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mService = RetrofitClient.getClient("https://maps.googleapis.com").create(IGoogleAPI.class);
        completeRide = findViewById(R.id.completeRide);

        //complete ride
        completeRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference driverEarnings = FirebaseDatabase.getInstance().getReference().child("RideFare").child(riderId);
                driverEarnings.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            ridecost = dataSnapshot.child("cost").getValue().toString();
                        }
                        totalEarnings += Double.parseDouble(ridecost);
                        DatabaseReference driverHistory = FirebaseDatabase.getInstance().getReference("DriverHistory").child(currentUser.getUid());
                        driverHistory.child("earning").setValue(totalEarnings);
                        driverHistory.child("Timestamp").setValue(System.currentTimeMillis()/1000);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                Intent intent = new Intent(RiderTracking.this, PaymentDriver.class);
                intent.putExtra("riderId", riderId);
                startActivity(intent);
                RiderTracking.this.finish();
            }
        });

        if (getIntent() != null){

            riderLat = getIntent().getDoubleExtra("riderLat", -1.0);
            riderLng = getIntent().getDoubleExtra("riderLng", -1.0);
            riderId = getIntent().getStringExtra("riderId");

        }

        setUpLocation();
        updateLocation();
        fetchLastLocation();
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
                            .title("Rider").icon(BitmapDescriptorFactory.fromResource(R.drawable.rider_icon)));
                    String url = getDirections(driverMarker.getPosition(), riderMarker.getPosition(), "driving");
                    new FetchURL(RiderTracking.this).execute(url, "driving");
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
