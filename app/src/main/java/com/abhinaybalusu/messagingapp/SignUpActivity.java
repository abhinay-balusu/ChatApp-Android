package com.abhinaybalusu.messagingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SignUpActivity extends AppCompatActivity {

    private EditText fNameET;
    private EditText lNameET;
    private EditText emailETSignUP;
    private EditText passwordETSignUP;
    private EditText retypePasswordET;
    private FirebaseAuth firebaseAuth;

    private ImageView profileImageView;

    private String emailSignUp, passwordSignUp, fName, lName;

    private Switch genderSwitch;
    private TextView genderValueTextView;

    public static final int CAMERA_REQUEST = 101;
    private Uri gallaryImageURI = null;

    private StorageReference mStorageReference;

    private ProgressDialog progressDialog;

    private String gender = "Male";

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
                    profileImageView.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        fNameET = (EditText)findViewById(R.id.fNameET);
        lNameET = (EditText)findViewById(R.id.lNameET);
        emailETSignUP = (EditText)findViewById(R.id.emailET_SignUP);
        passwordETSignUP = (EditText)findViewById(R.id.passwordET_SignUP);
        retypePasswordET = (EditText)findViewById(R.id.rpEditText);

        genderSwitch = (Switch)findViewById(R.id.genderSwitch);
        genderValueTextView = (TextView)findViewById(R.id.genderValueTextView);

        genderSwitch.setChecked(true);
        genderValueTextView.setText(gender);

        genderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    gender = genderSwitch.getTextOn().toString();
                }
                else
                {
                    gender = genderSwitch.getTextOff().toString();
                }

                genderValueTextView.setText(gender);
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("loading.....");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        firebaseAuth = FirebaseAuth.getInstance();

        mStorageReference = FirebaseStorage.getInstance().getReference();

        profileImageView = (ImageView)findViewById(R.id.profileImageView);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent camIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                camIntent.setType("image/*");
                startActivityForResult(camIntent,CAMERA_REQUEST);
            }
        });

        findViewById(R.id.signUpBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validateForm()) {
                    return;
                }

                if(gallaryImageURI == null)
                {
                    gallaryImageURI = Uri.EMPTY;
                }

                if(passwordETSignUP.getText().toString().equalsIgnoreCase(retypePasswordET.getText().toString()))
                {

                    progressDialog.show();

                    FirebaseDatabase dbRef = FirebaseDatabase.getInstance();
                    final DatabaseReference ref = dbRef.getReference();
                    FirebaseUser fUser = firebaseAuth.getCurrentUser();

                    emailSignUp = emailETSignUP.getText().toString();
                    passwordSignUp = passwordETSignUP.getText().toString();
                    fName = fNameET.getText().toString();
                    lName = lNameET.getText().toString();

                    StorageReference childRef = mStorageReference.child("Photos").child(gallaryImageURI.getLastPathSegment());
                    childRef.putFile(gallaryImageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                            firebaseAuth.createUserWithEmailAndPassword(emailSignUp, passwordSignUp)
                                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            if (!task.isSuccessful()) {
                                                Toast.makeText(SignUpActivity.this, "Account Not Created and User should select a different email if not",
                                                        Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                            else
                                            {
                                                Toast.makeText(SignUpActivity.this, "User has been created",
                                                        Toast.LENGTH_SHORT).show();
                                                System.out.println(task.getResult());

                                                User user = new User();
                                                user.setEmail(emailSignUp);
                                                user.setfName(fName);
                                                user.setlName(lName);

                                                Date date = new Date();
                                                SimpleDateFormat sd = new SimpleDateFormat("MM/dd/yy");
                                                String newDate = sd.format(date);

                                                user.setDateCreated(newDate);
                                                user.setProfileIcon(taskSnapshot.getDownloadUrl().toString());
                                                user.setGender(gender);

                                                ref.child("Users").child(task.getResult().getUser().getUid()).setValue(user);
                                                progressDialog.dismiss();
                                                finish();

                                            }

                                        }
                                    });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(SignUpActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }
                    });


                }

                else
                {
                    Toast.makeText(SignUpActivity.this, "Password and Retype password Fields should be same",
                            Toast.LENGTH_SHORT).show();

                    progressDialog.dismiss();

                }



            }
        });

        findViewById(R.id.cancelButton_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailETSignUP.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailETSignUP.setError("Required.");
            valid = false;
        } else {
            emailETSignUP.setError(null);
        }

        String password = passwordETSignUP.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordETSignUP.setError("Required.");
            valid = false;
        } else {
            passwordETSignUP.setError(null);
        }

        return valid;
    }
}
