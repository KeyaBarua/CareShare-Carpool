package com.example.careshare;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class DriverProfile extends Fragment {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference reference;
    ImageView ivDriverPic;

    public DriverProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_profile, null);
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_driver_profile, container, false);
        Button btnUpdate = view.findViewById(R.id.btnDriverUpdateProfile);
        ivDriverPic = view.findViewById(R.id.DriverProfilePic);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        reference.child("Driver").child(currentUser.getUid()).child("ProfileImages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String link = dataSnapshot.getValue(String.class);
                Picasso.get().load(link).fit().centerCrop().into(ivDriverPic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView profileDFullname = view.findViewById(R.id.dprofile_fullname);
        final TextView profileDUsername = view.findViewById(R.id.dprofile_username);
        final TextView profileDEmail = view.findViewById(R.id.dprofile_email);
        final TextView profileDlicense = view.findViewById(R.id.dprofile_license);
        final TextView profileDTel = view.findViewById(R.id.dprofile_tel);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Driver").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fullname = dataSnapshot.child("fullname").getValue().toString();
                String username = dataSnapshot.child("username").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String lic = dataSnapshot.child("license").getValue().toString();
                String tel = dataSnapshot.child("phone").getValue().toString();

                profileDFullname.setText("NAME: " + fullname);
                profileDUsername.setText("USERNAME: " + username);
                profileDEmail.setText("EMAIL: " + email);
                profileDlicense.setText("LICENSE NO.: " + lic);
                profileDTel.setText("PHONE: " + tel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
