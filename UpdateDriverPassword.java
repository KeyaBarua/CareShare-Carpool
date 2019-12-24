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

public class UpdateDriverPassword extends AppCompatActivity {

    private EditText etEnterEmailDriver;
    private Button btnUpdateDriverPwd;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_update_driver_password);
        getSupportActionBar().hide();

        //init
        etEnterEmailDriver = findViewById(R.id.etEnterEmailDriver);
        btnUpdateDriverPwd = findViewById(R.id.btnUpdateDriverPwd);

        //firebase init
        mAuth = FirebaseAuth.getInstance();

        //update password button
        btnUpdateDriverPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEnterEmailDriver.getText().toString().trim();
                if (email.equals("")){
                    etEnterEmailDriver.setError("Enter Email ID");
                    etEnterEmailDriver.requestFocus();
                }
                else {
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(UpdateDriverPassword.this, "An email is sent to reset your password. Please click on the link to reset password.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(UpdateDriverPassword.this, LoginDriver.class));
                                UpdateDriverPassword.this.finish();
                            }
                            else {
                                Toast.makeText(UpdateDriverPassword.this, "Something went worng. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
