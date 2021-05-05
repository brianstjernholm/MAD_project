package dk.au.mad21spring.appproject.gruppe2.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.activities.MessageActivity;
import dk.au.mad21spring.appproject.gruppe2.models.Chat;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.utils.Constants;
import dk.au.mad21spring.appproject.gruppe2.viewmodels.ChatFragmentViewModel;

public class MessageAdapter extends
        RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    private boolean ischat;

    private ChatFragmentViewModel vm;

    String theLastMessage;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    //Setting up ViewHolder
    //Either message is from current user(right side) or from "other" user (left side)
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Constants.MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    //Setting up list items
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);

        holder.show_message.setText(chat.getMessage());

        if (imageurl.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageurl).into(holder.profile_image);
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        public ImageView profile_image;

        public ViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isSenderCurrentUser(position)) {
            return Constants.MSG_TYPE_RIGHT;
        } else return Constants.MSG_TYPE_LEFT;
    }

    private boolean isSenderCurrentUser(int position) {
        return mChat.get(position).getSender()
                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }
}
