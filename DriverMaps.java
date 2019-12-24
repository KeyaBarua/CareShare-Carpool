package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.careshare.directionhelpers.FetchURL;
import com.example.careshare.directionhelpers.TaskLoadedCallback;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DriverMaps extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    private Location currentLocation;
    private Geocoder geocoder;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference reference;
    private TextView signOut, driverProfile, driverHistory, carmake;
    private GeoFire geoFire;
    private Marker marker1, marker2;
    private PlacesClient placesClient;
    private LatLng destination;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private static final int REQUEST_CODE = 101;
    private Dialog dialog;
    double riderLat, riderLng;
    private Polyline directions;
    private String customerId = "";
    private String riderPhone="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);

        //getAssignedCustomer();

        //dialog init
        dialog = new Dialog(this);

        //sign out
        signOut = findViewById(R.id.signOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DriverMaps.this, LoginDriver.class));
                DriverMaps.this.finish();
            }
        });

        //driver history
        driverHistory = findViewById(R.id.driverHistory);
        driverHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverMaps.this, DriverHistory.class));
            }
        });


        //profile
        driverProfile = findViewById(R.id.driverProfile);
        driverProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverMaps.this, DriverUserProfile.class));
            }
        });

        //car make
        carmake = findViewById(R.id.carMake);
        carmake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverMaps.this, Carmake.class));
            }
        });

        //firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("DriversAvailable");
        geoFire = new GeoFire(reference);

        //location methods
        setUpLocation();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        updateLocation();
        fetchLastLocation();

        //places api
        String api_key = "AIzaSyDlIslLbq2PCPh-FAAzGq2xggoMaIsv0f0";
        if (!Places.isInitialized()){
            Places.initialize(this, api_key);
        }
        placesClient = Places.createClient(this);
        autocompleteSupportFragment = (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.rider_autocomplete_fragment);
        autocompleteSupportFragment.setCountry("bd");
        autocompleteSupportFragment.setHint("Set your destination");
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                destination = place.getLatLng();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriverDestination");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(currentUser.getUid(), new GeoLocation(destination.latitude, destination.longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (marker2 != null){
                            marker2.remove();
                        }
                        marker2 = mMap.addMarker(new MarkerOptions().position(destination).title("Your Destination"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination,12f));
                        String url = getDirections(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), destination, "driving");
                        new FetchURL(DriverMaps.this).execute(url, "driving");
                    }
                });
                String destLocation = getCityName(destination);
                DatabaseReference setDest = FirebaseDatabase.getInstance().getReference().child("Driver").child(currentUser.getUid());
                setDest.child("destination").setValue(destLocation);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

    }

    private void getAssignedCustomer() {

        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Driver").child(currentUser.getUid()).child("RequestRiderID");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    customerId = dataSnapshot.getValue().toString();
                }
                getAssignedCustomerLocation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    Marker riderMarker;
    private void getAssignedCustomerLocation() {
        DatabaseReference assignedCustomerLocRef = FirebaseDatabase.getInstance().getReference().child("RideRequest").child(currentUser.getUid()).child(customerId).child("l");
        assignedCustomerLocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    Toast.makeText(DriverMaps.this, "No new Request", Toast.LENGTH_LONG).show();
                }
                else {
                    riderLat = Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                    riderLng = Double.parseDouble(dataSnapshot.child("1").getValue().toString());

                    LatLng latLng = new LatLng(riderLat, riderLng);
                    if (riderMarker != null) {
                        riderMarker.remove();
                    }
                    riderMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("New Rider").icon(BitmapDescriptorFactory.fromResource(R.drawable.rider_icon)));
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            dialog.setContentView(R.layout.custom_popup3);
                            Button navigate = dialog.findViewById(R.id.navigate);
                            Button callRider = dialog.findViewById(R.id.callRider);
                            Button smsRider = dialog.findViewById(R.id.smsRider);
                            Button driverCancel = dialog.findViewById(R.id.driverCancel);

                            navigate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(DriverMaps.this, RiderTracking.class);
                                    intent.putExtra("riderLat", riderLat);
                                    intent.putExtra("riderLng", riderLng);
                                    intent.putExtra("riderId", customerId);
                                    startActivity(intent);
                                }
                            });

                            //call rider
                            callRider.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DatabaseReference riderTel = FirebaseDatabase.getInstance().getReference().child("Student").child(customerId);
                                    riderTel.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                riderPhone = dataSnapshot.child("phone").getValue().toString();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + riderPhone));
                                    startActivity(intent);
                                }
                            });

                            //sms rider
                            smsRider.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DatabaseReference riderTel = FirebaseDatabase.getInstance().getReference().child("Student").child(customerId);
                                    riderTel.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                riderPhone = dataSnapshot.child("phone").getValue().toString();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    Uri uri = Uri.parse("smsto:" + riderPhone);
                                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                                    intent.setPackage("com.whatsapp");
                                    startActivity(intent);
                                }
                            });

                            driverCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DatabaseReference request = FirebaseDatabase.getInstance().getReference().child("RideRequest").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child(customerId);
                                    request.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(DriverMaps.this, "Ride Cancelled", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            });
                            dialog.show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //get location Name
    private String getCityName(LatLng latLng){

        String address = null;
        geocoder = new Geocoder(DriverMaps.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    private String getDirections(LatLng origin, LatLng dest, String directionMode) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameters + "&key=AIzaSyDlIslLbq2PCPh-FAAzGq2xggoMaIsv0f0";
        return url;
    }

    //getting permission
    private void setUpLocation() {
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

    //getting updated location
    private void updateLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        }
        else {
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    //fetching current location
    private void fetchLastLocation() {

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null){
                    currentLocation = location;
                    final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map1);
                    mapFragment.getMapAsync(DriverMaps.this);
                    geoFire.setLocation(currentUser.getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (marker1 != null){
                                marker1.remove();
                            }
                            marker1 = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("You are here"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),15.2f));
                        }
                    });getAssignedCustomer();
                }
            }
        });

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    fetchLastLocation();
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onTaskDone(Object... values) {
        if (directions != null){
            directions.remove();
        }
        directions = mMap.addPolyline((PolylineOptions)values[0]);
    }
}
