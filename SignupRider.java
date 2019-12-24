package com.example.careshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupRider extends AppCompatActivity {

    private EditText etFullName, etUniversity, etStudentEmail, etUserName, etPassword, etPhone, etConfirmPassword;
    private RadioButton rbMale, rbFemale;
    private Button btnSignup;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String gender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signup_rider);
        getSupportActionBar().hide();

        //initialisation
        etFullName = findViewById(R.id.etFullName);
        etUniversity = findViewById(R.id.etUniversity);
        etStudentEmail = findViewById(R.id.etStudentEmail);
        etPhone = findViewById(R.id.etPhone);
        etUserName = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);

        //Firebase initialisation
        databaseReference = FirebaseDatabase.getInstance().getReference("Student");
        mAuth = FirebaseAuth.getInstance();

        //signup button
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String fullName = etFullName.getText().toString().trim();
                final String University = etUniversity.getText().toString().trim();
                final String studentEmail = etStudentEmail.getText().toString().trim();
                final String userName = etUserName.getText().toString().trim();
                final String phone = etPhone.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPwd = etConfirmPassword.getText().toString().trim();

                if (rbMale.isChecked()){
                    gender = "Male";
                }
                if (rbFemale.isChecked()){
                    gender = "Female";
                }

                //checking if any field is empty
                if(fullName.isEmpty()){
                    etFullName.setError("Enter all fields!!");
                    etFullName.requestFocus();
                    return;
                }
                if(University.isEmpty()){
                    etUniversity.setError("Enter all fields!!");
                    etUniversity.requestFocus();
                    return;
                }
                if(studentEmail.isEmpty()){
                    etStudentEmail.setError("Enter all fields!!");
                    etStudentEmail.requestFocus();
                    return;
                }
                if(userName.isEmpty()){
                    etUserName.setError("Enter all fields!!");
                    etUserName.requestFocus();
                    return;
                }
                if(password.isEmpty()){
                    etPassword.setError("Enter all fields!!");
                    etPassword.requestFocus();
                    return;
                }
                if(confirmPwd.isEmpty() || !password.equals(confirmPwd)){
                    etConfirmPassword.setError("Password does not match. Please try again!");
                    etConfirmPassword.requestFocus();
                    return;
                }
                if(phone.isEmpty()){
                    etPhone.setError("Enter all fields!!");
                    etPhone.requestFocus();
                    return;
                }

                //checking if the email entered is a correct email
                if (!Patterns.EMAIL_ADDRESS.matcher(studentEmail).matches()){
                    etStudentEmail.setError("Enter a valid email address!!");
                    etStudentEmail.requestFocus();
                    return;
                }
                if (!studentEmail.endsWith(".edu")){
                    etStudentEmail.setError("Enter a student email address!!");
                    etStudentEmail.requestFocus();
                    return;
                }

                //checking the validity of password
                if (password.length()<8){
                    etPassword.setError("Minimum length of password is 8!!");
                    etPassword.requestFocus();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //Authentication
                mAuth.createUserWithEmailAndPassword(studentEmail, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){
                                    Student information = new Student(fullName, userName, studentEmail, gender, University, phone);

                                    FirebaseDatabase.getInstance().getReference("Student")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(information).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                progressBar.setVisibility(View.GONE);
                                                mAuth.getCurrentUser().sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){
                                                                    Toast.makeText(SignupRider.this, "An email has been sent for verification. Please verify to continue.",
                                                                            Toast.LENGTH_LONG).show();
                                                                    startActivity(new Intent(SignupRider.this, LoginRider.class));
                                                                }
                                                                else {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    Toast.makeText(SignupRider.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                }

                                                            }
                                                        });
                                            }
                                            else {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(SignupRider.this, "Something went wrong!!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                                else {
                                    try {
                                        throw task.getException();
                                    }catch (FirebaseAuthUserCollisionException emailExists){
                                        Toast.makeText(SignupRider.this, "This email is already in use", Toast.LENGTH_LONG).show();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
            }
        });

    }
}
