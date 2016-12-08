package com.abhinaybalusu.messagingapp;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

/**
 * Created by abhinaybalusu on 11/18/16.
 */
public class MessageCustomAdapter extends ArrayAdapter<Message> {

    List<Message> mData;
    Context mContext;
    int mResource;

    private FirebaseAuth auth;
    private FirebaseUser fUser;

    public MessageCustomAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);

        this.mContext = context;
        this.mData = objects;
        this.mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);
        }

        TextView msgTV = (TextView)convertView.findViewById(R.id.msgTextView);

        TextView timeTV = (TextView)convertView.findViewById(R.id.timeTextView);

        ImageView mImageView = (ImageView)convertView.findViewById(R.id.mImageView_Chat);

        Message msg = mData.get(position);

        auth = FirebaseAuth.getInstance();
        fUser = auth.getCurrentUser();

        msgTV.setText(msg.getMessageText());

        PrettyTime pt = new PrettyTime();
        timeTV.setText(pt.format(new Date(msg.getDateAndTimePosted())));

        //timeTV.setText(msg.getDateAndTimePosted());

        if(msg.getmIcon() == null || msg.getmIcon().equals(""))
        {
            mImageView.setVisibility(View.GONE);
        }
        else {
            mImageView.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(msg.getmIcon()).into(mImageView);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100,100);
        params.weight = 1.0f;

        if(msg.getSender().equals(fUser.getUid().toString()))
        {
            msgTV.setGravity(Gravity.RIGHT);
            params.gravity = Gravity.RIGHT;
            timeTV.setGravity(Gravity.RIGHT);
        }
        else
        {
            msgTV.setGravity(Gravity.LEFT);
            params.gravity = Gravity.LEFT;
            timeTV.setGravity(Gravity.LEFT);
        }

        mImageView.setLayoutParams(params);


        return convertView;
    }
}

