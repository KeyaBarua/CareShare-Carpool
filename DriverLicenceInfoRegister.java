package com.example.caresharecarpool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class DriverLicenceInfoRegister extends AppCompatActivity {

    Button btnCamera, btnUpload;
    ImageView ivLicence;
    ProgressBar progressBar;
    private  final int SELECT_IMAGE =1;

    private Uri filepath;

    FirebaseStorage storage;
    StorageReference reference;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data!=null && data.getData()!=null){

            filepath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filepath);
                ivLicence.setImageBitmap(bitmap);

            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //to remove the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_driver_licence_info_register);
        getSupportActionBar().hide();

        btnCamera = findViewById(R.id.btnCamera);
        btnUpload = findViewById(R.id.btnUpload);
        ivLicence = findViewById(R.id.ivLicense);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        btnUpload.setVisibility(View.GONE);


        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent,"SELECT IMAGE"), SELECT_IMAGE );
                btnUpload.setVisibility(View.VISIBLE);
                btnUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (filepath != null){
                            progressBar.setVisibility(View.VISIBLE);
                            StorageReference ref = reference.child("images/" + UUID.randomUUID().toString());
                            ref.putFile(filepath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(DriverLicenceInfoRegister.this, "Image Uploaded", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(DriverLicenceInfoRegister.this, LoginDriver.class));
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(DriverLicenceInfoRegister.this, "ERROR!!" + e.getMessage(), Toast.LENGTH_LONG).show();

                                        }
                                    });

                        }

                    }
                });


            }
        });


    }
}
