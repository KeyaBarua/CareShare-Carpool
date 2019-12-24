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

public class UpdateRiderPassword extends AppCompatActivity {

    private EditText etEnterEmailRider;
    private Button btnUpdateRiderPwd;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_update_rider_password);
        getSupportActionBar().hide();

        //init
        etEnterEmailRider = findViewById(R.id.etEnterEmailRider);
        btnUpdateRiderPwd = findViewById(R.id.btnUpdateRiderPwd);
        mAuth = FirebaseAuth.getInstance();

        btnUpdateRiderPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = etEnterEmailRider.getText().toString().trim();
                if (email.equals("")){
                    etEnterEmailRider.setError("Enter Email ID");
                    etEnterEmailRider.requestFocus();
                }
                else {
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(UpdateRiderPassword.this, "An email is sent to reset your password. Please click on the link to reset password.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(UpdateRiderPassword.this, LoginRider.class));
                                UpdateRiderPassword.this.finish();
                            }
                            else {
                                Toast.makeText(UpdateRiderPassword.this, "Something went worng. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
