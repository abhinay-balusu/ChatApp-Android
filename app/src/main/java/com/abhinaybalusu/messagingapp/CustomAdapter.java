package com.abhinaybalusu.messagingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by abhinaybalusu on 11/17/16.
 */
public class CustomAdapter
        extends ArrayAdapter<User> {

    List<User> mData;
    Context mContext;
    int mResource;

    public CustomAdapter(Context context, int resource, List<User> objects) {
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

        TextView userName = (TextView)convertView.findViewById(R.id.userNameTextView);
        TextView countTV = (TextView)convertView.findViewById(R.id.unreadMessagesCountTV);
        countTV.setVisibility(View.GONE);
        ImageView userIcon = (ImageView) convertView.findViewById(R.id.userIconImageView);

        User user = mData.get(position);

        userName.setText(user.getfName()+" "+user.getlName());

        if(user.getUnreadMessagesCount() > 0)
        {
            countTV.setVisibility(View.VISIBLE);
            countTV.setText(String.valueOf(user.getUnreadMessagesCount()));
        }
        else
        {
            countTV.setVisibility(View.GONE);
        }


        Picasso.with(mContext).load(user.getProfileIcon()).into(userIcon);


        return convertView;
    }
}
