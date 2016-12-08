package com.abhinaybalusu.messagingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private ImageView userIconImageView;
    private TextView userNameTextView;
    private Button updateButton;

    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private FirebaseUser fUser;

    private ArrayList<User> usersList;
    private ArrayList<String> keysList;

    private ListView usersListView;

    private TextView friendsLabelTextView;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userIconImageView = (ImageView)findViewById(R.id.userProfileImageView);
        userNameTextView = (TextView)findViewById(R.id.userNameTextView);
        updateButton = (Button)findViewById(R.id.updateButton);

        friendsLabelTextView = (TextView)findViewById(R.id.friendsLabelTextView);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("loading.....");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        usersList = new ArrayList<User>();
        keysList = new ArrayList<String>();

        usersListView = (ListView)findViewById(R.id.friendsListView);

        auth = FirebaseAuth.getInstance();
        fUser = auth.getCurrentUser();

        //auth.signOut();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if(fUser != null)
        {
            progressDialog.show();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mDatabase.child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    userNameTextView.setText(dataSnapshot.child("fName").getValue().toString()+" "+dataSnapshot.child("lName").getValue().toString());
                    if(dataSnapshot.child("profileIcon") != null)
                    {
                        Picasso.with(getApplicationContext()).load(dataSnapshot.child("profileIcon").getValue().toString()).into(userIconImageView);

                    }


                    mDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            keysList.clear();
                            usersList.clear();

                            for (final DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                                User user = userSnapshot.getValue(User.class);
                                String id = userSnapshot.getKey();



                                if(fUser.getUid().toString().equals(id))
                                {

                                }
                                else
                                {
                                    keysList.add(id);
                                    usersList.add(user);
                                }

                                if(usersList.size()==0)
                                {
                                    friendsLabelTextView.setText("No Friends Available");
                                    friendsLabelTextView.setGravity(Gravity.CENTER);
                                    friendsLabelTextView.setTextColor(Color.RED);
                                }
                                else
                                {
                                    friendsLabelTextView.setText("Friends");
                                    friendsLabelTextView.setGravity(Gravity.LEFT);
                                    friendsLabelTextView.setTextColor(Color.BLACK);
                                }

                                CustomAdapter usersAdapter = new CustomAdapter(getApplicationContext(), R.layout.row_layout_friend, usersList);
                                usersListView.setAdapter(usersAdapter);

                            }



                            for(int i =0;i<usersList.size();i++)
                            {
                                final int finalI = i;

                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Messages");
                                mDatabase.orderByChild("sender").equalTo(keysList.get(i)).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        int count = 0;
                                        for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                                            Message msg = messageSnapshot.getValue(Message.class);
                                            msg.setmId(messageSnapshot.getKey());

                                            if(msg.getReceiver().equals(fUser.getUid().toString()))
                                            {
                                                if(msg.getReadStatus().equalsIgnoreCase("false"))
                                                {
                                                    count ++;
                                                }
                                            }

                                        }

                                        User tUser = usersList.get(finalI);
                                        for(User user:usersList){

                                            if(user.getfName().equals(tUser.getfName()) &&
                                                    user.getlName().equals(tUser.getlName())){
                                                usersList.get(usersList.indexOf(user)).setUnreadMessagesCount(count);
                                                break;
                                            }
                                        }

                                        CustomAdapter usersAdapter = new CustomAdapter(getApplicationContext(), R.layout.row_layout_friend, usersList);
                                        usersListView.setAdapter(usersAdapter);

                                        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                                                usersList.get(position).setUnreadMessagesCount(0);

                                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Messages");
                                                mDatabase.orderByChild("sender").equalTo(keysList.get(position)).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        for(DataSnapshot message:dataSnapshot.getChildren()) {
                                                            Message msg = message.getValue(Message.class);
                                                            if (msg.getSender().equals(keysList.get(position)) &&
                                                                    msg.getReceiver().equals(fUser.getUid())) {
                                                                //texts.setRead("yes");
                                                                mDatabase.child(message.getKey()).child("readStatus").setValue("true");
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                                Intent intent = new Intent(ChatActivity.this, DetailedChatActivity.class);
                                                intent.putExtra("friendsID",keysList.get(position));
                                                startActivity(intent);

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                            progressDialog.dismiss();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                            progressDialog.dismiss();

                        }

                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    progressDialog.dismiss();

                }
            });

        }

        findViewById(R.id.updateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ChatActivity.this, UpdateProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.item_signout)
        {
            auth.signOut();
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
