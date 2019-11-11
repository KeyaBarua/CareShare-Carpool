package com.example.caresharecarpool;

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

public class LoginRider extends AppCompatActivity {

    TextView tvSignup;
    FirebaseAuth mAuth;
    EditText etEmail, etPassword;
    Button btnLogin;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login_rider);
        getSupportActionBar().hide();
        tvSignup = findViewById(R.id.tvSignup);
        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginRider.this, SignupRider.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String studentEmail = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(studentEmail, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){
                                    if(mAuth.getCurrentUser().isEmailVerified()){

                                        LoginRider.this.finish();
                                        startActivity(new Intent(LoginRider.this, HomepageRider.class));

                                    }
                                    else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(LoginRider.this, "Please verify your email", Toast.LENGTH_LONG).show();
                                    }
                                }
                                else {
                                    Toast.makeText(LoginRider.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}
