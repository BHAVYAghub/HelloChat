package com.example.hellochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private String currentuserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        currentuserID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        InitializeFields();

        userName.setVisibility(View.INVISIBLE);
        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });
       RetrieveUserInfo();
    }



    private void UpdateSettings() {
        String setUserName= userName.getText().toString();
        String setStatus= userStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please write a user name", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please write status", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String ,String> profileMap=new HashMap<>();
            profileMap.put("uid",currentuserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            RootRef.child("Users").child(currentuserID).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                          SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        String message=task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }
    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))&&(dataSnapshot.hasChild("image")))
                {
                    String retrieveUserName =dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus =dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage =dataSnapshot.child("image").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);


                }
                else if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name")))
                {
                    String retrieveUserName =dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus =dataSnapshot.child("status").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);


                }
                else
                {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Update Your Profile info", Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void InitializeFields() {
        UpdateAccountSettings= (Button)findViewById(R.id.update_settings_button);
        userName=(EditText) findViewById(R.id.set_user_name);
        userStatus=(EditText) findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView)findViewById(R.id.set_profile_image);

    }
    private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
