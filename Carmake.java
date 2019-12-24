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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Carmake extends AppCompatActivity {

    EditText carName, carBrand, carNumber, seats;
    Button carSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_carmake);
        getSupportActionBar().hide();

        carName = findViewById(R.id.carName);
        carBrand = findViewById(R.id.carBrand);
        carNumber = findViewById(R.id.carnumber);
        seats = findViewById(R.id.seats);
        carSubmit = findViewById(R.id.carSubmit);

        carSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = carName.getText().toString().trim();
                String brand = carBrand.getText().toString().trim();
                String number = carNumber.getText().toString().trim();
                String seatNumber = seats.getText().toString().trim();

                Car car = new Car(name, brand, number, seatNumber);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Car");
                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(car).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Carmake.this, "DATA ENTRY SUCCESSFUL", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Carmake.this, DriverMaps.class));
                            Carmake.this.finish();
                        }
                        else {
                            Toast.makeText(Carmake.this, "DATA ENTRY FAILED", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}
