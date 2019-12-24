package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateDriverProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference ref;
    private EditText changeDriverName, changeDriverTel, changeDriverUsername, changeDriverLicense;
    private Button saveDriverUpdate;
    private ImageView backbutton3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_update_driver_profile);
        getSupportActionBar().hide();

        //initialisation
        changeDriverName = findViewById(R.id.ChangeDriverName);
        changeDriverTel = findViewById(R.id.ChangeDriverTel);
        changeDriverLicense = findViewById(R.id.ChangeDriverLicense);
        changeDriverUsername = findViewById(R.id.ChangeDriverUserName);
        saveDriverUpdate = findViewById(R.id.saveDriverUpdate);
        backbutton3 = findViewById(R.id.backButton3);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference();

        //retrieving data from firebase and set in edit text
        ref.child("Driver").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                changeDriverName.setText(dataSnapshot.child("fullname").getValue().toString());
                changeDriverTel.setText(dataSnapshot.child("phone").getValue().toString());
                changeDriverLicense.setText(dataSnapshot.child("license").getValue().toString());
                changeDriverUsername.setText(dataSnapshot.child("username").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //saving the updated data in database
        saveDriverUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.child("Driver").child(currentUser.getUid()).child("fullname").setValue(changeDriverName.getText().toString().trim());
                ref.child("Driver").child(currentUser.getUid()).child("phone").setValue(changeDriverTel.getText().toString().trim());
                ref.child("Driver").child(currentUser.getUid()).child("license").setValue(changeDriverLicense.getText().toString().trim());
                ref.child("Driver").child(currentUser.getUid()).child("username").setValue(changeDriverUsername.getText().toString().trim());
                Toast.makeText(UpdateDriverProfile.this, "Update Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UpdateDriverProfile.this, DriverUserProfile.class));
                UpdateDriverProfile.this.finish();
            }
        });

        //back button
        backbutton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UpdateDriverProfile.this, DriverUserProfile.class));
                UpdateDriverProfile.this.finish();
            }
        });
    }
}
