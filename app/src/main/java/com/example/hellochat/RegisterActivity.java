package com.example.hellochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private EditText UserEmail,UserPassword;
    private TextView AlreadyHaveAnAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;
    private RadioGroup mType;
    private RadioButton memberType;
    private String radio,access;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        InitializeFields();
        mType=(RadioGroup) findViewById(R.id.radiogroup);

        mType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                memberType =(RadioButton) mType.findViewById(i);

            radio="User";


                switch (i){

                    case R.id.admin_radiobutton:
                        radio = memberType.getText().toString();
                        Toast.makeText(RegisterActivity.this, "admin" + radio, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.user_radiobutton:
                        radio = memberType.getText().toString();
                        Toast.makeText(RegisterActivity.this, "user" + radio, Toast.LENGTH_SHORT).show();

                    default:
                        break;

                }
            }
        });

        AlreadyHaveAnAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();

            }

        });
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"Please enter email..",Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Please enter password..",Toast.LENGTH_SHORT).show();

        }
        else if(radio.equals("Admin")&&(email.lastIndexOf("xyz")==-1||(email.lastIndexOf("xyz")<(email.lastIndexOf("@")))))
        {
            Toast.makeText(this, "You are not admin", Toast.LENGTH_SHORT).show();

        }
        else
        {
            loadingBar.setTitle("Creating new Account");
            loadingBar.setMessage("Please wait while we are creating a new account for you");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        final String currentUserID=mAuth.getCurrentUser().getUid();
                        RootRef.child("Users").child(currentUserID).setValue("");


                        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( RegisterActivity.this,  new OnSuccessListener<InstanceIdResult>() {
                            @Override
                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                final String deviceToken = instanceIdResult.getToken();
                                //Log.e("Token",mToken);
                                RootRef.child("Users").child(currentUserID).child("device_token")
                                        .setValue(deviceToken);
                                Toast.makeText(RegisterActivity.this, ""+radio, Toast.LENGTH_SHORT).show();
                                RootRef.child("Users").child(currentUserID).child("privilage")
                                        .setValue(radio);


                                if(radio.equals("Admin"))
                                {
                                    access="Yes";
                                }
                                else
                                {
                                    access="No";
                                }
                                RootRef.child("Users").child(currentUserID).child("access")
                                        .setValue(access);




                            }
                        });




                        SendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        String message=task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error"+ message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });


        }

    }

    private void InitializeFields() {
        CreateAccountButton=(Button) findViewById(R.id.register_button);
        UserEmail=(EditText) findViewById(R.id.register_email);
        UserPassword=(EditText) findViewById(R.id.register_password);
        AlreadyHaveAnAccountLink=(TextView)findViewById(R.id.already_have_an_account_link);
        loadingBar=new ProgressDialog(this);
    }
    private void SendUserToLoginActivity() {
        Intent loginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }
    private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
