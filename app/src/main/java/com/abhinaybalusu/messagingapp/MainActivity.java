package com.abhinaybalusu.messagingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private ProgressDialog progressDialog;

    private EditText emailET;
    private EditText passwordET;

    private String email, password = "";

    private FirebaseAuth auth;
    private FirebaseUser fUser;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    private static final int RC_SIGN_IN = 9001;
    private static final String GTAG = "GoogleActivity";
    private User googleUser = null;

    private static final String FTAG = "FacebookLogin";
    private CallbackManager mCallbackManager;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode,resultCode,data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                googleUser = new User();
                googleUser.setEmail(account.getEmail());
                if(account.getPhotoUrl() != null)
                {
                    googleUser.setProfileIcon(account.getPhotoUrl().toString());
                }

                googleUser.setfName(account.getGivenName());
                googleUser.setlName(account.getFamilyName());
                googleUser.setGender("");

                Date date = new Date();
                SimpleDateFormat sd = new SimpleDateFormat("MM/dd/yy");
                String newDate = sd.format(date);

                googleUser.setDateCreated(newDate);

                firebaseAuthWithGoogle(account);
            } else {

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(GTAG, "firebaseAuthWithGoogle:" + acct.getId());

        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

//        if(fUser != null)
//        {
//            signInWithMultipleAccounts(credential);
//        }
//        else {
            auth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(GTAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(GTAG, "signInWithCredential", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Google Login Success",
                                        Toast.LENGTH_SHORT).show();
                                FirebaseDatabase dbRef = FirebaseDatabase.getInstance();
                                final DatabaseReference ref = dbRef.getReference();

                                goToChatActivity();

                                ref.child("Users").child(task.getResult().getUser().getUid()).setValue(googleUser);
                            }
                            progressDialog.hide();
                        }
                    });
        //}

        //signInWithMultipleAccounts(credential);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("loading.....");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        emailET = (EditText)findViewById(R.id.emailET);
        passwordET = (EditText)findViewById(R.id.passwordET);

        auth = FirebaseAuth.getInstance();
        //auth.signOut();

        fUser = auth.getCurrentUser();

        if(fUser != null)
        {
            goToChatActivity();
        }
        else
        {
            Toast.makeText(MainActivity.this, "Please Login to Continue", Toast.LENGTH_LONG).show();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .enableAutoManage(MainActivity.this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    email = emailET.getText().toString();
                    password = passwordET.getText().toString();

                    progressDialog.show();

                    if (email.isEmpty() || password.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Please Enter all the details")
                                .setTitle("Alert")
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else
                    {
                        //authenticate user
                        auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (!task.isSuccessful()) {
                                            if (password.length() < 6) {
                                                Toast.makeText(MainActivity.this, "Password length should be >6", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_LONG).show();
                                            goToChatActivity();
                                        }

                                        progressDialog.dismiss();
                                    }
                                });
                    }

                }
            });

            findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                    startActivity(intent);
                }
            });

            mCallbackManager = CallbackManager.Factory.create();
            LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
            loginButton.setReadPermissions("email", "public_profile");
            loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(FTAG, "facebook:onSuccess:" + loginResult);

                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    Log.d("LoginActivity", response.toString());
                                    Log.d("LoginActivity", object.toString());

                                    // Application code
                                    try {
                                        String email = object.getString("email");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,first_name,last_name,picture");
                    request.setParameters(parameters);
                    request.executeAsync();

                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.d(FTAG, "facebook:onCancel");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(FTAG, "facebook:onError", error);
                }
            });

            findViewById(R.id.gmailButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });
        }


    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(FTAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

//        if(fUser != null)
//        {
//            signInWithMultipleAccounts(credential);
//        }
//        else{

            auth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(FTAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(FTAG, "signInWithCredential", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this, "Facebook Login Success",
                                        Toast.LENGTH_SHORT).show();
                                goToChatActivity();
                            }

                            // ...
                        }
                    });
        //}

    }

    public void signInWithMultipleAccounts(final AuthCredential credential)
    {
        auth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(FTAG, "linkWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                            FirebaseUser prevUser = fUser;

                            try {
                                fUser = Tasks.await(auth.signInWithCredential(credential)).getUser();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

                        // ...
                    }
                });
    }

    public void goToChatActivity()
    {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d(GTAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
