package com.abhinaybalusu.messagingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class DetailedChatActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser fUser;

    private EditText messageEditText;

    private String rId = "";

    private DatabaseReference mDatabase;

    private ArrayList<Message> messageArrayList;
    private ArrayList<String> messagesKeysList;

    private ListView chatListView;

    private ImageView mImageView;

    private Uri gallaryImageURI = null;

    private ImageView pImageView;
    private TextView pNameTextView;

    private StorageReference mStorageReference;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SignUpActivity.CAMERA_REQUEST)
        {
            if(resultCode == RESULT_OK)
            {
                gallaryImageURI = data.getData();

                Bitmap imageBitmap;

                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),gallaryImageURI);
                    mImageView.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_chat);

        messageEditText = (EditText)findViewById(R.id.messageEditText);

        mImageView = (ImageView)findViewById(R.id.mImageView);

        pImageView = (ImageView)findViewById(R.id.pImageView);

        pNameTextView = (TextView)findViewById(R.id.pNameTextView);

        messageArrayList = new ArrayList<Message>();
        messagesKeysList = new ArrayList<String>();

        chatListView = (ListView)findViewById(R.id.chatListView);

        Log.d("FriendsID",getIntent().getExtras().get("friendsID").toString());

        rId = getIntent().getExtras().get("friendsID").toString();

        mImageView.setImageResource(R.drawable.gallery);

        mStorageReference = FirebaseStorage.getInstance().getReference();

        if(rId.equals(""))
        {

        }
        else
        {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("Users").child(rId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    pNameTextView.setText(dataSnapshot.child("fName").getValue().toString()+" "+dataSnapshot.child("lName").getValue().toString());
                    Picasso.with(getApplicationContext()).load(dataSnapshot.child("profileIcon").getValue().toString()).into(pImageView);


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        auth = FirebaseAuth.getInstance();
        fUser = auth.getCurrentUser();

        if(fUser != null)
        {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("Messages").orderByChild("sender").equalTo(fUser.getUid().toString()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    messageArrayList.clear();
                    messagesKeysList.clear();

                    for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                        Message msg = messageSnapshot.getValue(Message.class);
                        msg.setmId(messageSnapshot.getKey());

                        if(msg.getReceiver().equals(rId))
                        {
                            messageArrayList.add(msg);
                            messagesKeysList.add(messageSnapshot.getKey());
                        }

                    }

                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("Messages").orderByChild("sender").equalTo(rId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                                Message msg = messageSnapshot.getValue(Message.class);
                                msg.setmId(messageSnapshot.getKey());

                                if(msg.getReceiver().equals(fUser.getUid().toString()))
                                {
                                    messageArrayList.add(msg);
                                    messagesKeysList.add(messageSnapshot.getKey());
                                }

                            }

                            Collections.sort(messageArrayList);
                            MessageCustomAdapter messageAdapter = new MessageCustomAdapter(getApplicationContext(), R.layout.row_layout_message, messageArrayList);
                            chatListView.setAdapter(messageAdapter);

                            chatListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                                    if(messageArrayList.get(position).getSender().toString().equals(fUser.getUid().toString()))
                                    {
                                        mDatabase.child("Messages").child(messageArrayList.get(position).getmId().toString()).removeValue();
                                    }
                                    else
                                    {
                                        Toast.makeText(DetailedChatActivity.this, "You Cannot Delete Other User's Message",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    return false;
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent camIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                camIntent.setType("image/*");
                startActivityForResult(camIntent,SignUpActivity.CAMERA_REQUEST);
            }
        });

        findViewById(R.id.sendMessageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = messageEditText.getText().toString();

                if(message.equals(""))
                {
                    Toast.makeText(DetailedChatActivity.this, "Please Enter some Text to Post",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    FirebaseDatabase dbRef = FirebaseDatabase.getInstance();
                    final DatabaseReference ref = dbRef.getReference();

                    Date date = new Date();
                    SimpleDateFormat sd = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
                    String newDate = sd.format(date);

                    final Message msg = new Message();

                    msg.setMessageText(messageEditText.getText().toString());
                    msg.setReadStatus("false");
                    msg.setReceiver(rId);
                    msg.setSender(fUser.getUid());
                    msg.setDateAndTimePosted(newDate);

                    if(gallaryImageURI != null) {


                        StorageReference childRef = mStorageReference.child("Photos").child(gallaryImageURI.getLastPathSegment());
                        childRef.putFile(gallaryImageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {


                                msg.setmIcon(taskSnapshot.getDownloadUrl().toString());
                                ref.child("Messages").push().setValue(msg);

                                messageEditText.setText("");
                                gallaryImageURI = Uri.EMPTY;

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(DetailedChatActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_SHORT).show();

                            }
                        });

                    }

                    else
                    {
                        msg.setmIcon("");

                        ref.child("Messages").push().setValue(msg);

                        messageEditText.setText("");
                        gallaryImageURI = null;
                    }

                }

            }
        });
    }
}
