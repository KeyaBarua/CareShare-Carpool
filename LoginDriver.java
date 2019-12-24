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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginDriver extends AppCompatActivity {

    private TextView tvRegister, tvForgotDriverPassword;
    private FirebaseAuth mAuth;
    private EditText etDriverEmail, etDriverPassword;
    private Button btnDriverLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login_driver);
        getSupportActionBar().hide();

        //init
        tvRegister = findViewById(R.id.tvSignup);
        tvForgotDriverPassword = findViewById(R.id.tvForgotDriverPassword);
        etDriverEmail = findViewById(R.id.etDriverEmail);
        etDriverPassword = findViewById(R.id.etDriverPassword);
        btnDriverLogin = findViewById(R.id.btnDriverLogin);
        progressBar = findViewById(R.id.DriverProgressBar);
        progressBar.setVisibility(View.GONE);

        //Firebase initialisation
        mAuth = FirebaseAuth.getInstance();
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginDriver.this, SignupDriver.class));
            }
        });

        //forgot password
        tvForgotDriverPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginDriver.this, UpdateDriverPassword.class));
            }
        });
        //Login button
        btnDriverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String drivermail = etDriverEmail.getText().toString().trim();
                String driverpassword = etDriverPassword.getText().toString().trim();
                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(drivermail, driverpassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){
                                    if (mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified()){
                                        LoginDriver.this.finish();
                                        startActivity(new Intent(LoginDriver.this, DriverMaps.class));
                                    }
                                    else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(LoginDriver.this, "Please verify your email", Toast.LENGTH_LONG).show();
                                    }
                                }
                                else {
                                    Toast.makeText(LoginDriver.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}
