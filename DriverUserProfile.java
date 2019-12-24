package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverUserProfile extends AppCompatActivity {

    private TextView driverName, driverUsername, driverEmail, driverLicense, driverphone;
    private Button updateProfile;
    private ImageView backbutton;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_driver_user_profile);
        getSupportActionBar().hide();

        //init
        driverName = findViewById(R.id.dprofile_fullname);
        driverUsername = findViewById(R.id.dprofile_username);
        driverEmail = findViewById(R.id.dprofile_email);
        driverLicense = findViewById(R.id.dprofile_license);
        driverphone = findViewById(R.id.dprofile_tel);
        backbutton = findViewById(R.id.backButton);
        updateProfile = findViewById(R.id.btnDriverUpdateProfile);

        //back button
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverUserProfile.this, DriverMaps.class));
                DriverUserProfile.this.finish();
            }
        });

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverUserProfile.this, UpdateDriverProfile.class));
            }
        });


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("Driver").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driverName.setText(dataSnapshot.child("fullname").getValue().toString());
                driverUsername.setText(dataSnapshot.child("username").getValue().toString());
                driverEmail.setText(dataSnapshot.child("email").getValue().toString());
                driverLicense.setText(dataSnapshot.child("license").getValue().toString());
                driverphone.setText(dataSnapshot.child("phone").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
