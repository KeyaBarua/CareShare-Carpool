package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PaymentRider extends AppCompatActivity {

    String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_payment_rider);
        getSupportActionBar().hide();

        //init
        TextView distanceTravelled = findViewById(R.id.distanceTravelled);
        TextView totalCost = findViewById(R.id.totalCost);
        TextView discountCost = findViewById(R.id.discountCost);
        Button rateDriver = findViewById(R.id.rateButton);

        if (getIntent() != null){
           driverId = getIntent().getStringExtra("driverId");
        }

        DatabaseReference costDetails = FirebaseDatabase.getInstance().getReference();
        costDetails.child("RideFare").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String distance = dataSnapshot.child("distance").getValue().toString();
                String cost = dataSnapshot.child("cost").getValue().toString();
                double total = Double.parseDouble(cost);
                double discount = total - (0.2*total);
                discount = Math.round(discount);
                distanceTravelled.setText(distance);
                totalCost.setText(cost);
                discountCost.setText(String.valueOf(discount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rateDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentRider.this, RateDriver.class);
                intent.putExtra("DriverId", driverId);
                startActivity(intent);
                PaymentRider.this.finish();
            }
        });
    }

}
