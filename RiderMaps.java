package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.careshare.directionhelpers.DataParser;
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
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private Marker marker1, marker2;
    private LatLng pickup, destination;
    private Button getDirection, findDriver;
    private AutocompleteSupportFragment autocompleteSupportFragment1;
    private static final int REQUEST_CODE = 101;
    private String tel;
    private TextView viewSeats;
    public String driverId;

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    private Dialog dialog;

    private IFCMService mFCMservice;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_maps);

        dialog = new Dialog(this);
        mFCMservice = FCMClient.getClient("https://fcm.googleapis.com/").create(IFCMService.class);

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
        getDirection = findViewById(R.id.getDirection);
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
                marker1 = mMap.addMarker(new MarkerOptions().position(pickup).title("Your pickup"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 12f));
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
                marker2 = mMap.addMarker(new MarkerOptions().position(destination).title("Your Destination"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination,12f));
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        //get direction button
        getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pickup!= null && destination!= null) {
                    Routing routing = new Routing.Builder()
                            .key("AIzaSyDlIslLbq2PCPh-FAAzGq2xggoMaIsv0f0")
                            .travelMode(AbstractRouting.TravelMode.DRIVING)
                            .withListener(RiderMaps.this)
                            .alternativeRoutes(false)
                            .waypoints(pickup, destination)
                            .build();
                    routing.execute();
                }
            }
        });

        //find driver button
        findDriver = findViewById(R.id.findDriver);
        findDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getClosestDriver();
            }
        });
    }

    public String getCurrentUserId(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String id = user.getUid();
        return id;
    }

    private int radius = 1; //1 km
    private boolean driverFound = false;
    private String driverFoundId;
    private void getClosestDriver(){
        final DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference("DriversAvailable");
        final GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                //if driver is found
                if (!driverFound) {
                    driverFound = true;
                    driverFoundId = key;
                }
                driverId = driverFoundId;
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child("Driver").child(driverFoundId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Driver driver = dataSnapshot.getValue(Driver.class);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(driver.getFullname())
                                .flat(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.caricon)));
                        //custom toast
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
                                final TextView viewDetails = dialog.findViewById(R.id.viewDetails);
                                final TextView viewCarNumber = dialog.findViewById(R.id.viewcarNumber);
                                viewSeats = dialog.findViewById(R.id.viewSeats);
                                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
                                reference1.child("Car").child(driverFoundId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Car car = dataSnapshot.getValue(Car.class);
                                        viewDetails.setText(car.getName());
                                        viewCarNumber.setText(car.getNumber());
                                        viewSeats.setText(car.getSeats());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                Button requestRide = dialog.findViewById(R.id.requestRide);
                                requestRide.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RideRequest");
                                        ref.setValue(driverFoundId);
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                                        reference.child("Student").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                tel = dataSnapshot.child("phone").getValue().toString();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        StudentTel studentTel = new StudentTel(tel);
                                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("RideRequest").child(driverFoundId);
                                        GeoFire geoFire1 = new GeoFire(ref1);
                                        ref1.setValue(studentTel);
                                        geoFire1.setLocation(currentUser.getUid(), new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                Toast.makeText(RiderMaps.this, "Request Sent. Wait till your driver gets back to you. You can call or whatsapp your driver", Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            }
                                        });//end of geolocation

                                        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
                                        tokens.orderByKey().equalTo(driverFoundId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                                                for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                                                    Token token = postSnapshot.getValue(Token.class);
                                                    String json_lat_lng = new Gson().toJson(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                                                    Notification notification = new Notification("Notification", json_lat_lng);
                                                    String location = currentLocation.toString();
                                                    Sender content = new Sender(token.getToken(), notification);
                                                    mFCMservice.sendMessage(content).enqueue(new Callback<FCMResponse>() {
                                                        @Override
                                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                            if (response.body().success == 1){
                                                                Toast.makeText(RiderMaps.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                Toast.makeText(RiderMaps.this, "Failed", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                                            Log.e("Error", t.getMessage());
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });//end of token value listener
                                    }
                                });//end of request button

                                viewName.setText(driver.getFullname());
                                viewTel.setText(driver.getPhone());
                                //cross
                                cross.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });//end of cross
                                dialog.show();
                            }
                        });//end of infoWindow
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

                //if driver is not found
                if (!driverFound){
                    radius++;
                    getClosestDriver();
                }
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
    public void onRoutingFailure(RouteException e) {

        if (e!=null){
            Toast.makeText(this, "Error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
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

    private void erasePolylines(){
        for (Polyline line:polylines){
            line.remove();
        }
        polylines.clear();
    }
}
