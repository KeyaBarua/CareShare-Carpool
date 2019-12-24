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
import com.google.firebase.storage.StorageReference;

public class UpdateRiderProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference ref;
    private EditText changeRiderName, changeRiderTel, changeRiderUsername, changeRiderUniv;
    private Button saveRiderUpdate;
    private ImageView backbutton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_update_rider_profile);
        getSupportActionBar().hide();

        //initialisation
        changeRiderName = findViewById(R.id.ChangeRiderName);
        changeRiderTel = findViewById(R.id.ChangeRiderTel);
        changeRiderUniv = findViewById(R.id.ChangeRiderUniv);
        changeRiderUsername = findViewById(R.id.ChangeRiderUserName);
        saveRiderUpdate = findViewById(R.id.saveRiderUpdate);
        backbutton2 = findViewById(R.id.backButton2);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference();

        //retrieving data from firebase and set in edit text
        ref.child("Student").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                changeRiderName.setText(dataSnapshot.child("fullname").getValue().toString());
                changeRiderTel.setText(dataSnapshot.child("phone").getValue().toString());
                changeRiderUniv.setText(dataSnapshot.child("university").getValue().toString());
                changeRiderUsername.setText(dataSnapshot.child("username").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //saving the updated data in database
        saveRiderUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.child("Student").child(currentUser.getUid()).child("fullname").setValue(changeRiderName.getText().toString().trim());
                ref.child("Student").child(currentUser.getUid()).child("phone").setValue(changeRiderTel.getText().toString().trim());
                ref.child("Student").child(currentUser.getUid()).child("university").setValue(changeRiderUniv.getText().toString().trim());
                ref.child("Student").child(currentUser.getUid()).child("username").setValue(changeRiderUsername.getText().toString().trim());
                Toast.makeText(UpdateRiderProfile.this, "Update Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UpdateRiderProfile.this, RiderProfile.class));
                UpdateRiderProfile.this.finish();
            }
        });

        //back button
        backbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UpdateRiderProfile.this, RiderProfile.class));
                UpdateRiderProfile.this.finish();
            }
        });
    }
}
