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

public class DriverHistory extends AppCompatActivity {

    Long timeStamp = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_driver_history);
        getSupportActionBar().hide();

        TextView driverEarning = findViewById(R.id.rideEarning);
        TextView date = findViewById(R.id.rideDay);
        TextView noRecentEarnings = findViewById(R.id.noRecentEarnings);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("DriverHistory")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    noRecentEarnings.setVisibility(View.VISIBLE);
                    date.setVisibility(View.GONE);
                    driverEarning.setVisibility(View.GONE);
                }
                else {
                    noRecentEarnings.setVisibility(View.GONE);
                    date.setVisibility(View.VISIBLE);
                    driverEarning.setVisibility(View.VISIBLE);
                    String earnings = dataSnapshot.child("earning").getValue().toString();
                    timeStamp = Long.valueOf(dataSnapshot.child("Timestamp").getValue().toString());
                    driverEarning.setText(earnings);
                    date.setText(getDate(timeStamp));
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
