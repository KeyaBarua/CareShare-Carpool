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

public class SignupDriver extends AppCompatActivity {

    private EditText etNameDriver, etMail, etLicense, etUname, etPwd, etPhnum, etConfirm;
    private RadioButton male, female;
    private Button btnNext;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String gender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signup_driver);
        getSupportActionBar().hide();

        //initialisation
        etNameDriver = findViewById(R.id.etDriverName);
        etMail = findViewById(R.id.etDriverMail);
        etLicense = findViewById(R.id.etLicence);
        etUname = findViewById(R.id.etUname);
        etPwd = findViewById(R.id.etPwd);
        etConfirm = findViewById(R.id.ConfirmPassword);
        etPhnum = findViewById(R.id.etPhnum);
        progressBar = findViewById(R.id.progressBar);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        btnNext = findViewById(R.id.btnNext);

        //Firebase initialisation
        databaseReference = FirebaseDatabase.getInstance().getReference("Driver");
        mAuth = FirebaseAuth.getInstance();

        //sign up button
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String fullName = etNameDriver.getText().toString().trim();
                final String mail = etMail.getText().toString().trim();
                final String userName = etUname.getText().toString().trim();
                final String phone = etPhnum.getText().toString().trim();
                final String license = etLicense.getText().toString().trim();
                String password = etPwd.getText().toString().trim();
                String confirmPassword = etConfirm.getText().toString().trim();

                if (male.isChecked()){
                    gender = "Male";
                }
                if (female.isChecked()){
                    gender = "Female";
                }

                //checking if any field is empty
                if(fullName.isEmpty()){
                    etNameDriver.setError("Enter all fields!!");
                    etNameDriver.requestFocus();
                    return;
                }
                if(mail.isEmpty()){
                    etMail.setError("Enter all fields!!");
                    etMail.requestFocus();
                    return;
                }
                if(userName.isEmpty()){
                    etUname.setError("Enter all fields!!");
                    etUname.requestFocus();
                    return;
                }
                if(password.isEmpty()){
                    etPwd.setError("Enter all fields!!");
                    etPwd.requestFocus();
                    return;
                }
                if(confirmPassword.isEmpty() || !password.equals(confirmPassword)){
                    etConfirm.setError("Password does not match. Please try again!");
                    etConfirm.requestFocus();
                    return;
                }
                if(phone.isEmpty()){
                    etPhnum.setError("Enter all fields!!");
                    etPhnum.requestFocus();
                    return;
                }
                if(license.isEmpty()){
                    etLicense.setError("Enter all fields!!");
                    etLicense.requestFocus();
                    return;
                }

                //checking if the email entered is a correct email
                if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
                    etMail.setError("Enter a valid email address!!");
                    etMail.requestFocus();
                    return;
                }

                //checking the validity of password
                if (password.length()<8){
                    etPwd.setError("Minimum length of password is 8!!");
                    etPwd.requestFocus();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //authentication
                mAuth.createUserWithEmailAndPassword(mail, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                //create user with email and password
                                if (task.isSuccessful()){
                                    Driver information = new Driver(fullName, userName, mail, gender, phone, license);

                                    FirebaseDatabase.getInstance().getReference("Driver")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(information).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //if the data input in firebase is successful
                                            if (task.isSuccessful()){
                                                progressBar.setVisibility(View.GONE);
                                                mAuth.getCurrentUser().sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                //if email sending is successful
                                                                if (task.isSuccessful()){
                                                                    Toast.makeText(SignupDriver.this, "An email has been sent for verification. Please verify to continue.",
                                                                            Toast.LENGTH_LONG).show();
                                                                    SignupDriver.this.finish();
                                                                    startActivity(new Intent(SignupDriver.this, LoginDriver.class));
                                                                }
                                                                else {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    Toast.makeText(SignupDriver.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                }

                                                            }
                                                        });
                                            }
                                            else {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(SignupDriver.this, "Something went wrong!!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                                else {
                                    try {
                                        throw task.getException();
                                    }catch (FirebaseAuthUserCollisionException emailExists){
                                        Toast.makeText(SignupDriver.this, "This email is already in use", Toast.LENGTH_LONG).show();

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
