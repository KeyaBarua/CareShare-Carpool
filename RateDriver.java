package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RateDriver extends AppCompatActivity {

    private RatingBar driverRatingBar;
    String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_rate_driver);
        getSupportActionBar().hide();

        if (getIntent() != null){
            driverId = getIntent().getStringExtra("DriverId");
        }

        driverRatingBar = findViewById(R.id.DriverratingBar);
        driverRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                DatabaseReference driverRating = FirebaseDatabase.getInstance().getReference("DriverRating");
                driverRating.child(driverId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(rating).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RateDriver.this, "Thank you for your rating!!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RateDriver.this, RiderMaps.class));
                        RateDriver.this.finish();
                    }
                });
                calculateDriverRating();
            }
        });

    }

    int ratingSum = 0;
    int ratingTotal = 0;
    double ratingAvg = 0;
    private void calculateDriverRating() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("DriverRating");
        reference.child(driverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()){

                    ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                    ratingTotal++;
                }
                if (ratingTotal != 0){
                    ratingAvg = ratingSum/ratingTotal;
                }

                DatabaseReference setRating = FirebaseDatabase.getInstance().getReference().child("Driver").child(driverId);
                setRating.child("rating").setValue(ratingAvg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
