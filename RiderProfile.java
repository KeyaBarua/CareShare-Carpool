package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class RiderProfile extends AppCompatActivity {

    private TextView rFullname, rUsername, rUniversity, rTel, rEmail;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private ImageView backButton1;
    private Button btnRiderProfileUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_rider_profile);
        getSupportActionBar().hide();

        //initialisation
        rFullname = findViewById(R.id.rprofile_fullname);
        rUsername = findViewById(R.id.rprofile_username);
        rEmail = findViewById(R.id.rprofile_email);
        rUniversity = findViewById(R.id.rprofile_university);
        rTel = findViewById(R.id.rprofile_tel);
        backButton1 = findViewById(R.id.backButton1);
        btnRiderProfileUpdate = findViewById(R.id.btnriderProfileUpdate);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        //retrieving data fron Firebase
        reference.child("Student").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                rFullname.setText(dataSnapshot.child("fullname").getValue().toString());
                rUsername.setText(dataSnapshot.child("username").getValue().toString());
                rEmail.setText(dataSnapshot.child("email").getValue().toString());
                rUniversity.setText(dataSnapshot.child("university").getValue().toString());
                rTel.setText(dataSnapshot.child("phone").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //back button
        backButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RiderProfile.this, HomepageofRider.class));
                RiderProfile.this.finish();
            }
        });

        //update profile
        btnRiderProfileUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RiderProfile.this, UpdateRiderProfile.class));
            }
        });
    }
}
