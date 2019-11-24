package com.example.hellochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentProvider;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserID,currentStats,senderUserID;


    private TextView userProfileName,userProfileStatus;
    private CircleImageView userProfileImage;
    private Button sendMessageRequestButton,declineRequestButton;
    private DatabaseReference userRef,ChatRequestRef,ContactsRef ,NotificationRef;
    private FirebaseAuth mAuth;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth=FirebaseAuth.getInstance();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        senderUserID=mAuth.getCurrentUser().getUid();

        receiverUserID=getIntent().getExtras().get("visit_user_id").toString();

        Toast.makeText(this, "UserID"+receiverUserID, Toast.LENGTH_SHORT).show();
        userProfileImage=(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName=(TextView) findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView)findViewById(R.id.visit_profile_status);
        sendMessageRequestButton=(Button) findViewById(R.id.send_message_request_button );
        declineRequestButton=(Button) findViewById(R.id.decline_message_request_button);


        currentStats="new";

        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {
    userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()&&(dataSnapshot.hasChild("image")))
            {
                String userImage=dataSnapshot.child("image").getValue().toString();
                String userName=dataSnapshot.child("name").getValue().toString();
                String userStatus=dataSnapshot.child("status").getValue().toString();
                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                userProfileName.setText(userName);
                userProfileStatus.setText(userStatus);
                ManageChatRequests();


            }
            else
            {
                String userName=dataSnapshot.child("name").getValue().toString();
                String userStatus=dataSnapshot.child("status").getValue().toString();
                userProfileName.setText(userName);
                userProfileStatus.setText(userStatus);
                ManageChatRequests();



            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {


        }
    });
    }

    private void ManageChatRequests() {
        ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserID))
                {
                    String request_type=dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                    if(request_type.equals("sent"))
                    {
                        currentStats="request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    }
                    else if(request_type.equals("received"))
                    {
                        currentStats="request_received";
                        sendMessageRequestButton.setText("Accept Chat Request");
                        declineRequestButton.setVisibility(View.VISIBLE);
                        declineRequestButton.setEnabled(true);
                        declineRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelChatRequest();
                            }
                        });
                    }
                }
                else
                {
                    ContactsRef.child(senderUserID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(receiverUserID))
                                    {
                                        currentStats="friends";
                                        sendMessageRequestButton.setText("Remove this Contact");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
            if(!senderUserID.equals(receiverUserID))
            {
                sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       sendMessageRequestButton.setEnabled(false);
                       if(currentStats.equals("new"))
                       {
                           SendChatRequest();
                       }
                       if(currentStats.equals("request_sent"))
                       {
                           CancelChatRequest();

                       }
                        if(currentStats.equals("request_received"))
                        {
                            AcceptChatRequest();

                        }
                        if(currentStats.equals("friends"))
                        {
                            RemoveSpecificContact();

                        }



                    }
                });

            }
            else
            {
                sendMessageRequestButton.setVisibility(View.INVISIBLE);
            }

    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                currentStats="new";
                                                sendMessageRequestButton.setText("send Chat Request");
                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }

                    }
                });

    }

    private void AcceptChatRequest() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                ChatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful())
                                                        {
                                                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    currentStats="friends";
                                                                    sendMessageRequestButton.setText("Remove this contacts");
                                                                    declineRequestButton.setVisibility(View.INVISIBLE);
                                                                    declineRequestButton.setEnabled(false);

                                                                }
                                                            });
                                                        }
                                                    }
                                                });

                                            }

                                        }
                                    });

                        }

                    }
                });

    }


    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ChatRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    sendMessageRequestButton.setEnabled(true);
                                                    currentStats="new";
                                                    sendMessageRequestButton.setText("send Chat Request");
                                                    declineRequestButton.setVisibility(View.INVISIBLE);
                                                    declineRequestButton.setEnabled(false);
                                                }
                                        }
                                    });
                        }

                    }
                });

    }

    private void SendChatRequest()
    {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");

                                                NotificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    currentStats = "request_sent";
                                                                    sendMessageRequestButton.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
