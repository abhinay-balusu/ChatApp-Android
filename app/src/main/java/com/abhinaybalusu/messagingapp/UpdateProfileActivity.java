package com.abhinaybalusu.messagingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateProfileActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private FirebaseUser fUser;

    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private ImageView iconImageView;

    private Uri gallaryImageURI = null;

    public static final int CAMERA_REQUEST = 102;

    private Switch gSwitch;
    private TextView gValueTextView;
    private String gender = "";

    private StorageReference mStorageReference;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST)
        {
            if(resultCode == RESULT_OK)
            {
                gallaryImageURI = data.getData();

                Bitmap imageBitmap;

                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),gallaryImageURI);
                    iconImageView.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        firstNameTextView = (TextView)findViewById(R.id.firstNameEditText);
        lastNameTextView = (TextView)findViewById(R.id.lastNameEditText);

        gSwitch = (Switch)findViewById(R.id.gSwitch);
        gValueTextView = (TextView)findViewById(R.id.gValueTextView);

        iconImageView = (ImageView)findViewById(R.id.profileIconImageView_update);

        auth = FirebaseAuth.getInstance();
        fUser = auth.getCurrentUser();

        mStorageReference = FirebaseStorage.getInstance().getReference();

        if(fUser != null)
        {
            mDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(fUser.getUid());

            ValueEventListener getUserDataListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    firstNameTextView.setText(dataSnapshot.child("fName").getValue().toString());
                    lastNameTextView.setText(dataSnapshot.child("lName").getValue().toString());
                    gValueTextView.setText(dataSnapshot.child("gender").getValue().toString());

                    if(dataSnapshot.child("gender").getValue().toString().equals("Male"))
                    {
                        gSwitch.setChecked(true);
                    }
                    else
                    {
                        gSwitch.setChecked(false);
                    }
                    gender = dataSnapshot.child("gender").getValue().toString();
                    Picasso.with(getApplicationContext()).load(dataSnapshot.child("profileIcon").getValue().toString()).into(iconImageView);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            };

            mDatabase.addValueEventListener(getUserDataListener);
        }

        gSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    gender = gSwitch.getTextOn().toString();
                }
                else
                {
                    gender = gSwitch.getTextOff().toString();
                }
                gValueTextView.setText(gender);
            }
        });

        findViewById(R.id.profileIconImageView_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent camIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                camIntent.setType("image/*");
                startActivityForResult(camIntent,CAMERA_REQUEST);
            }
        });

        findViewById(R.id.updateButton_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabase = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child(fUser.getUid());
                mDatabase.child("fName").setValue(firstNameTextView.getText().toString());
                mDatabase.child("lName").setValue(lastNameTextView.getText().toString());
                mDatabase.child("gender").setValue(gender);

                if(gallaryImageURI != null) {


                    StorageReference childRef = mStorageReference.child("Photos").child(gallaryImageURI.getLastPathSegment());
                    childRef.putFile(gallaryImageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {


                            mDatabase.child("profileIcon").setValue(taskSnapshot.getDownloadUrl().toString());

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(UpdateProfileActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_SHORT).show();

                        }
                    });

                }

                Toast.makeText(UpdateProfileActivity.this, "Profile Updated Successfully",
                        Toast.LENGTH_SHORT).show();
                finish();

            }
        });

        findViewById(R.id.cancelButton_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

    }
}
