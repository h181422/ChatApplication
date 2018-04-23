package com.example.larsv.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class UsersListAdapter extends RecyclerView.Adapter{
    private static final int VIEW_TYPE_USER = 1;
    final String TAG = "TAGTAG";

    private Context mContext;
    private List<String> usersList;
    ViewGroup p;

    public UsersListAdapter(Context context, List<String> userList) {
        mContext = context;
        usersList = userList;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        p = parent;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_test, parent, false);

            view.setOnClickListener(mChatListener);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }
    private View.OnClickListener mChatListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent goToChat = new Intent(mContext, ChatActivity.class);
            TextView tv = (TextView) v.findViewById(R.id.user_txt);
            goToChat.putExtra(MenuActivity.SEND_TO, tv.getText().toString());
            mContext.startActivity(goToChat);
        }
    };

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String user = (String) usersList.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER:
                ((ReceivedMessageHolder) holder).bind(user);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }
    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_USER;
    }



    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView userText;
        ReceivedMessageHolder(View itemView) {
            super(itemView);
            userText = (TextView) itemView.findViewById(R.id.user_txt);
        }

        void bind(String user) {
            userText.setText(user);
        }
        public TextView getUSerText(){
            return userText;
        }
    }
}
