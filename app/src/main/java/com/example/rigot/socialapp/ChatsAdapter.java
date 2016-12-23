package com.example.rigot.socialapp;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rigot on 11/19/2016.
 */

public class ChatsAdapter extends ArrayAdapter<ChatMessage> {

    Context mContext;
    List<ChatMessage> messages;
    int mResource;
    private FirebaseUser user;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = null;
    private iActivity activity;


    public ChatsAdapter(Context context, int resource, ArrayList<ChatMessage> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.messages = objects;
        this.mResource = resource;
        this.activity = (ChatWindow) context;
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();

            holder.msgBackground = (RelativeLayout)convertView.findViewById(R.id.msg_layout);
            holder.chatMsg = (TextView)convertView.findViewById(R.id.textViewMsg);
            holder.chatTime = (TextView)convertView.findViewById(R.id.textViewChatTime);
            holder.deleteButton = (ImageView)convertView.findViewById(R.id.MessageDel);
            holder.imgChat = (ImageView)convertView.findViewById(R.id.imageViewChat);

            convertView.setTag(holder);
        }else
            holder = (ViewHolder) convertView.getTag();

        //here we determine who sent the message and change the look appropriately by changing the message backgound color
        ChatMessage message = messages.get(position);
        if(message.getSentByUid().equals(user.getUid())){
            holder.msgBackground.setBackgroundColor(Color.parseColor("#33A2FF"));
        }else{
            holder.msgBackground.setBackgroundColor(Color.parseColor("#42f48f"));
        }


        //delete button actions
        holder.deleteButton.setTag(message.getMsgId());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.deleteChat(holder.deleteButton.getTag().toString());
            }
        });

        if (!message.isRead()) {
            activity.updateIsRead(message.getMsgId());
        }

        if(message.getImagePath()!=null){//if there is an image associated with the message
            if(holder.imgChat.getVisibility() == View.GONE)
                holder.imgChat.setVisibility(View.VISIBLE);

            Picasso.with(mContext).load(message.getImagePath()).fit().into(holder.imgChat);
        }else{
            holder.imgChat.setVisibility(View.GONE);
        }
        if(!message.getTextMsg().equals("")){    //if text portion of the message isn't empty
            holder.chatMsg.setText(message.getTextMsg());
        }

        try {
            date = format.parse(messages.get(position).getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        PrettyTime p = new PrettyTime();
        if(date!=null)
            holder.chatTime.setText(p.format(date));


        return convertView;
    }

    static class ViewHolder{
        ImageView deleteButton, imgChat;
        TextView chatMsg, chatTime;
        RelativeLayout msgBackground;
    }

    public interface iActivity {
        public void deleteChat(String msgId);

        public void updateIsRead(String msgId);
    }
}
