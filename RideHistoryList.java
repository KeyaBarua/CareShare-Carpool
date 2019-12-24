package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

public class RideHistoryList extends AppCompatActivity {

    TextView HistorydriverName, historyRiderName, rideDate, noRecentRide, recentRideTitle, dateTitle, driverNameTitle, riderNameTitle;
    Long timeStamp = 0L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ride_history_list);
        getSupportActionBar().hide();

        HistorydriverName = findViewById(R.id.driverNameHistory);
        historyRiderName = findViewById(R.id.riderNameHistory);
        rideDate = findViewById(R.id.rideDate);
        noRecentRide = findViewById(R.id.noRecentRide);
        recentRideTitle = findViewById(R.id.recentRideTitle);
        dateTitle = findViewById(R.id.dateTitle);
        driverNameTitle = findViewById(R.id.driverNameTitle);
        riderNameTitle = findViewById(R.id.riderNameTitle);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("RiderHistory")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    noRecentRide.setVisibility(View.VISIBLE);
                    HistorydriverName.setVisibility(View.GONE);
                    historyRiderName.setVisibility(View.GONE);
                    rideDate.setVisibility(View.GONE);
                    recentRideTitle.setVisibility(View.GONE);
                    dateTitle.setVisibility(View.GONE);
                    driverNameTitle.setVisibility(View.GONE);
                    riderNameTitle.setVisibility(View.GONE);
                } else {
                    noRecentRide.setVisibility(View.GONE);
                    HistorydriverName.setVisibility(View.VISIBLE);
                    historyRiderName.setVisibility(View.VISIBLE);
                    rideDate.setVisibility(View.VISIBLE);
                    recentRideTitle.setVisibility(View.VISIBLE);
                    dateTitle.setVisibility(View.VISIBLE);
                    driverNameTitle.setVisibility(View.VISIBLE);
                    riderNameTitle.setVisibility(View.VISIBLE);
                    String driverId = dataSnapshot.child("DriverId").getValue().toString();
                    timeStamp = Long.valueOf(dataSnapshot.child("Timestamp").getValue().toString());

                    //getting driver name
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Driver").child(driverId);
                    reference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String driverName = dataSnapshot.child("fullname").getValue().toString();
                            HistorydriverName.setText(driverName);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //getting rider name
                    DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference().child("Student").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    reference2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String riderName = dataSnapshot.child("fullname").getValue().toString();
                            historyRiderName.setText(riderName);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    rideDate.setText(getDate(timeStamp));
                }
            }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
        });

    }

    private String getDate(Long timeStamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timeStamp * 1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();
        return date;
    }
}
