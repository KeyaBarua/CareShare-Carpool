package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.service.Common;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class RiderMaps extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private Location currentLocation;
    private Geocoder geocoder;
    private LocationRequest mLocationRequest;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference reference;
    private GeoFire geoFire;
    private Marker marker;
    private LatLng pickup, destination;
    private AutocompleteSupportFragment autocompleteSupportFragment1;
    private static final int REQUEST_CODE = 101;

    private boolean isDriverfound = false;
    private String driverId = "";
    private int radius = 1; //1 km
    private int distance = 1; //1 km
    private static int LIMIT = 3;
    private Button findDriver;

    private PolylineOptions polylineOptions, blackPolyLineOptions;
    private Polyline blackPolyLie, grayPolyLine;
    private List<LatLng> polylineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_maps);

        //initialisation
        String api_key = "AIzaSyDlIslLbq2PCPh-FAAzGq2xggoMaIsv0f0";
        if (!Places.isInitialized()){
            Places.initialize(this, api_key);
        }
        findDriver = findViewById(R.id.findDriver);
        polylines = new ArrayList<>();

        //places api
        placesClient = Places.createClient(this);
        final AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteSupportFragment1 =
                (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment1);
        autocompleteSupportFragment.setCountry("bd");//pick-up location
        autocompleteSupportFragment1.setCountry("bd");//destination location
        autocompleteSupportFragment1.setHint("Enter pickup location");
        autocompleteSupportFragment.setHint("Enter your destination");
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteSupportFragment1.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteSupportFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                pickup = place.getLatLng();
                marker = mMap.addMarker(new MarkerOptions().position(destination).title("Your Pickup"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                destination = place.getLatLng();
                marker = mMap.addMarker(new MarkerOptions().position(destination).title("Your Destination"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination,12f));
                getDirection();

            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
        
        //findDriver button
        findDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findDriverForRide();
            }
        });

        //Firebase initialisation
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("StudentLocation");
        //Geofire
        geoFire = new GeoFire(reference);

        requestLocation(); //method for requesting location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        updateRiderLocation();
        getRiderCurrentLocation();
    }

    private void findDriverForRide() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Driver");
        GeoFire geoFireDriver = new GeoFire(ref);

        GeoQuery geoQuery = geoFireDriver.queryAtLocation(new GeoLocation(currentLocation.getLatitude(),currentLocation.getLongitude()), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!isDriverfound){
                    isDriverfound = true;
                    driverId = key;
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                //if driver not found, increase radius
                if (!isDriverfound){

                    radius++;
                    findDriverForRide();

                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getDirection() {
    }

    /**
     * Method for fetching the current location
     */
    private void getRiderCurrentLocation() {

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {

                if (location != null){
                    currentLocation = location;

                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(RiderMaps.this);
                    geoFire.setLocation(currentUser.getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (marker != null){
                                marker.remove();
                            }
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("You are here"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                            loadallDriversAvailable();
                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),15.2f));
                        }
                    });
                    getCityName(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        });
    }

    private void loadallDriversAvailable() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Driver");
        GeoFire geoFireDriver = new GeoFire(ref);

        GeoQuery geoQuery = geoFireDriver.queryAtLocation(new GeoLocation(currentLocation.getLatitude(),currentLocation.getLongitude()), distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                FirebaseDatabase.getInstance().getReference().child("Driver")
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Driver driver = dataSnapshot.getValue(Driver.class);
                                mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(driver.getPhone())
                                .flat(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.caricon)));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if (distance <= LIMIT){
                    distance++;
                    loadallDriversAvailable();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getCityName(LatLng latLng){

        geocoder = new Geocoder(RiderMaps.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1);
            String address = addresses.get(0).getAddressLine(0);
            //sourceEdit.setText(address);
            autocompleteSupportFragment1.setText(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for updating location
     */
    private void updateRiderLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        }
        else {
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    /**
     * Method for requesting location updates using LocationRequest
     */
    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        }
        else {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000);
            mLocationRequest.setFastestInterval(3000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setBuildingsEnabled(false);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    //location callback
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            /*LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are here");
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.2f));
            mMap.addMarker(markerOptions);*/

            getRiderCurrentLocation();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getRiderCurrentLocation();
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};


    /**
     * method for if routing fails
     * @param e
     */
    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null){
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Something went wrong. Try again!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {
    }

    /*private void erasePolylines(){
        for (Polyline line:polylines){
            line.remove();
        }
        polylines.clear();
    }*/
}
