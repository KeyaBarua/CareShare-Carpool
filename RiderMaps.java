package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.careshare.directionhelpers.FetchURL;
import com.example.careshare.directionhelpers.TaskLoadedCallback;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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

public class RiderMaps extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

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
    private Marker marker1, marker2;
    private LatLng pickup, destination;
    private Button findDriver;
    private AutocompleteSupportFragment autocompleteSupportFragment1;
    private static final int REQUEST_CODE = 101;
    private TextView viewSeats;
    private static double baseFare = 50.0;
    private static double time_rate = 2.0;
    private static double distance_rate = 18.0;
    private static double avgSpeed = 6;
    private Dialog dialog, fareDialog;
    private Polyline directions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_maps);

        dialog = new Dialog(this);
        fareDialog = new Dialog(this);

        //Firebase initialisation
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("StudentLocation");
        //Geofire
        geoFire = new GeoFire(reference);


        requestLocation(); //method for requesting location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getRiderCurrentLocation();

        //initialisation
        String api_key = "AIzaSyDlIslLbq2PCPh-FAAzGq2xggoMaIsv0f0";
        if (!Places.isInitialized()){
            Places.initialize(this, api_key);
        }

        //places api
        placesClient = Places.createClient(this);

        //getting suggestion in the search bar
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
                if (marker1 != null){
                    marker1.remove();
                }
                if (pickup != null){
                    marker1 = mMap.addMarker(new MarkerOptions().position(pickup).title("Your pickup"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 12f));
                }
                else {
                    Toast.makeText(RiderMaps.this, "Please enter your pickup location", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                destination = place.getLatLng();
                if (marker2 != null){
                    marker2.remove();
                }

                if (destination != null){
                    marker2 = mMap.addMarker(new MarkerOptions().position(destination).title("Your Destination"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination,12f));
                    String url = getDirections(pickup, destination, "driving");
                    new FetchURL(RiderMaps.this).execute(url, "driving");
                    getFare();
                }
                else {
                    Toast.makeText(RiderMaps.this, "Please enter your destination", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        //find driver button
        findDriver = findViewById(R.id.findDriver);
        findDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (destination != null){
                    displayAllDrivers();
                }
                else {
                    Toast.makeText(RiderMaps.this, "Please enter your destination", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getDirections(LatLng origin, LatLng dest, String directionMode) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameters + "&key=AIzaSyDlIslLbq2PCPh-FAAzGq2xggoMaIsv0f0";
        return url;
    }

    private void getFare() {
        fareDialog.setContentView(R.layout.custompopup2);
        TextView rideDistance = fareDialog.findViewById(R.id.Ridedistance);
        TextView rideDuration = fareDialog.findViewById(R.id.RideDuration);
        TextView rideFare = fareDialog.findViewById(R.id.rideFare);
        Button btnOk = fareDialog.findViewById(R.id.btnOK);
        Location pickupLocation = new Location("");
        pickupLocation.setLatitude(pickup.latitude);
        pickupLocation.setLongitude(pickup.longitude);
        Location destLocation = new Location("");
        destLocation.setLatitude(destination.latitude);
        destLocation.setLongitude(destination.longitude);

        //distance
        double distanceInMeters = pickupLocation.distanceTo(destLocation);
        double distanceInKM = distanceInMeters/1000;
        distanceInKM = Math.round(distanceInKM);
        rideDistance.setText(String.valueOf(distanceInKM));

        //duration
        double hours = distanceInKM/avgSpeed;
        int durationRide;
        if (hours<1.0){
            durationRide = (int) hours/60;
        }
        else {
            durationRide =(int) hours;
        }
        rideDuration.setText(String.valueOf(durationRide));

        double fare = getPrice(distanceInKM, durationRide);
        fare = Math.round(fare);
        rideFare.setText(String.valueOf(fare));

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fareDialog.dismiss();
            }
        });

        //storing the data in database
        FareDetails fareDetails = new FareDetails(distanceInKM, fare, durationRide);
        DatabaseReference rideCost = FirebaseDatabase.getInstance().getReference("RideFare");
        rideCost.child(currentUser.getUid()).setValue(fareDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    fareDialog.show();
                }
                else {
                    Toast.makeText(RiderMaps.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private static double getPrice(double km, int min){
        return baseFare + (distance_rate * km) + (time_rate * min);
    }

    //method for displaying all drivers
    List<Marker> markerList = new ArrayList<>();
    private void displayAllDrivers(){

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), 5000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {
                for (Marker markerIt: markerList){
                    if (markerIt.getTag().equals(key)){
                        return;
                    }
                }
                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Driver").child(key);
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String driverName = dataSnapshot.child("fullname").getValue().toString();
                        final String drivertel = dataSnapshot.child("phone").getValue().toString();
                        LatLng driverLocation = new LatLng(location.latitude, location.longitude);
                        Marker mDriverMaker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(driverName).snippet(drivertel).icon(BitmapDescriptorFactory.fromResource(R.drawable.caricon)));
                        mDriverMaker.setTag(key);
                        markerList.add(mDriverMaker);

                        //custom Toast
                        final Toast toast = new Toast(RiderMaps.this);
                        toast.setGravity(Gravity.BOTTOM, 0, 100);
                        TextView tv = new TextView(RiderMaps.this);
                        tv.setBackgroundColor(Color.BLUE);
                        tv.setTextColor(Color.WHITE);
                        tv.setTextSize(14);
                        Typeface t = Typeface.create("serif", Typeface.BOLD_ITALIC);
                        tv.setTypeface(t);
                        tv.setPadding(10, 10, 10, 10);
                        tv.setText("Click on the car icon to view details of driver and click on the name of the driver to send ride request.");
                        toast.setView(tv);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.show();

                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {

                                dialog.setContentView(R.layout.custom_popup);
                                TextView cross = dialog.findViewById(R.id.cross);
                                TextView viewName = dialog.findViewById(R.id.viewDriverName);
                                TextView viewTel = dialog.findViewById(R.id.viewDriverTel);
                                TextView viewDest = dialog.findViewById(R.id.viewDriverDest);
                                TextView viewDriverRating = dialog.findViewById(R.id.viewDriverRating);
                                final TextView viewDetails = dialog.findViewById(R.id.viewDetails);
                                final TextView viewCarNumber = dialog.findViewById(R.id.viewcarNumber);
                                viewSeats = dialog.findViewById(R.id.viewSeats);
                                DatabaseReference carDetails = FirebaseDatabase.getInstance().getReference().child("Car").child(key);
                                carDetails.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            String carName = dataSnapshot.child("name").getValue().toString();
                                            String carNumber = dataSnapshot.child("number").getValue().toString();
                                            String carSeats = dataSnapshot.child("seats").getValue().toString();
                                            viewCarNumber.setText(carNumber);
                                            viewDetails.setText(carName);
                                            viewSeats.setText(carSeats);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });//end of car details

                                //driverRating
                                DatabaseReference driverRating = FirebaseDatabase.getInstance().getReference().child("Driver").child(key);
                                driverRating.child("rating").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        viewDriverRating.setText(dataSnapshot.getValue().toString());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //driver destination
                                DatabaseReference driverDest = FirebaseDatabase.getInstance().getReference().child("Driver").child(key);
                                driverDest.child("destination").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        viewDest.setText(dataSnapshot.getValue().toString());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //request ride button
                                Button requestRide = dialog.findViewById(R.id.requestRide);
                                requestRide.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        DatabaseReference rideRequest = FirebaseDatabase.getInstance().getReference("RideRequest");
                                        rideRequest.setValue(key);
                                        DatabaseReference request = FirebaseDatabase.getInstance().getReference().child("RideRequest").child(key);
                                        GeoFire geoFire1 = new GeoFire(request);
                                        geoFire1.setLocation(currentUser.getUid(), new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                dialog.dismiss();
                                            }
                                        });

                                        //setting up the rider id in driver's database
                                        DatabaseReference requestedRiderId = FirebaseDatabase.getInstance().getReference().child("Driver").child(key);
                                        requestedRiderId.child("RequestRiderID").setValue(currentUser.getUid());

                                        //intent
                                        Intent intent = new Intent(RiderMaps.this, DriverTracking.class);
                                        intent.putExtra("lat", pickup.latitude);
                                        intent.putExtra("lng", pickup.longitude);
                                        intent.putExtra("driverId", key);
                                        startActivity(intent);
                                        RiderMaps.this.finish();
                                    }
                                });//end of request button
                                viewName.setText(driverName);
                                viewTel.setText(drivertel);
                                //cross
                                cross.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });//end of cross
                                Button callDriver = dialog.findViewById(R.id.callDriver);
                                Button smsDriver = dialog.findViewById(R.id.smsDriver);
                                callDriver.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(Intent.ACTION_DIAL);
                                        intent.setData(Uri.parse("tel:" + drivertel));
                                        startActivity(intent);
                                    }
                                });//end of callDriver

                                smsDriver.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Uri uri = Uri.parse("smsto:" + drivertel);
                                        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                                        intent.setPackage("com.whatsapp");
                                        startActivity(intent);
                                    }
                                });
                                dialog.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onKeyExited(String key) {

                for (Marker markerIt:markerList){
                    if (markerIt.getTag().equals(key)){
                        markerIt.remove();
                    }
                }

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

                for (Marker markerIt:markerList) {
                    if (markerIt.getTag().equals(key)) {
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    /**
     * Method for fetching the current location
     */
    public void getRiderCurrentLocation() {

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
                            if (marker1 != null){
                                marker1.remove();
                            }
                            pickup = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            marker1 = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Pickup"));
                            marker1.hideInfoWindow();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),11.2f));
                            //listPoints.add(0, new LatLng(location.getLatitude(), location.getLongitude()));
                        }
                    });
                    getCityName(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        });
    }

    /**
     * method to get the current location name in the origin edittext.
     * @param latLng
     */
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getRiderCurrentLocation();
                }
                break;
        }
    }

    @Override
    public void onTaskDone(Object... values) {

        if (directions != null){
            directions.remove();
        }
        directions = mMap.addPolyline((PolylineOptions)values[0]);
    }
}
