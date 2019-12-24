package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PaymentDriver extends AppCompatActivity {

    private String riderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_payment_driver);
        getSupportActionBar().hide();

        //init
        TextView distanceTravelledDriver = findViewById(R.id.distanceTravelledDriver);
        TextView totalCost = findViewById(R.id.totalCostDriver);
        TextView discountCost = findViewById(R.id.discountCostDriver);

        if (getIntent() != null){
            riderId = getIntent().getStringExtra("riderId");
        }

        DatabaseReference costDetails = FirebaseDatabase.getInstance().getReference();
        costDetails.child("RideFare").child(riderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String distance = dataSnapshot.child("distance").getValue().toString();
                String cost = dataSnapshot.child("cost").getValue().toString();
                double total = Double.parseDouble(cost);
                double discount = total - (0.2*total);
                discount = Math.round(discount);
                distanceTravelledDriver.setText(distance);
                totalCost.setText(cost);
                discountCost.setText(String.valueOf(discount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
