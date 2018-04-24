package com.example.larsv.chatapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.larsv.chatapplication.Messages.MessageOutputStream;
import com.example.larsv.chatapplication.Messages.MotherOfAllMessages;
import com.example.larsv.chatapplication.Messages.UpdateMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    public final static String SEND_TO = "SEND_TO";
    public final static String REQUEST_NEW = "REQUEST_NEW";

    TextView txtWelcome;
    String[] usersOnline;
    String username;
    String sendTo = "ALL";

    private RecyclerView mUserRecycler;
    private UsersListAdapter mUserAdapter;
    private List<String> mUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //Shared preferences
        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        username = sharedPref.getString(getResources().getString(R.string.myUsernameKey), "Stranger");
        txtWelcome = (TextView) findViewById(R.id.txtWelcome);
        String welcomeDisplay = getResources().getString(R.string.welcome_) + " "
                + username;
        txtWelcome.setText(welcomeDisplay);

        IntentFilter myIntentFilter = new IntentFilter(CommunicationIntentService.PEOPLE_ONLINE);
        LocalBroadcastManager.getInstance(this).registerReceiver(new MenuActivity.MyReceiver(),
                myIntentFilter);


        Log.i("TAGTAG", "on create i menu");

        mUserList = new ArrayList<String>();
        mUserRecycler = (RecyclerView) findViewById(R.id.usersRecycler);
        mUserAdapter = new UsersListAdapter(this, mUserList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mUserRecycler.setLayoutManager(llm);
        mUserRecycler.setAdapter(mUserAdapter);



    }

    @Override
    protected void onStart() {
        super.onStart();
        sendReq();
    }
    public void sendReq(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    new MessageOutputStream(CommunicationIntentService.getSocket().getOutputStream()).
                            writeMessage(new UpdateMessage(username, UpdateMessage.REQUEST_USERS_ONLINE));
                    Log.i("TAGTAG", "Request for uo sent");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void buttonChatWithAll(View view){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(SEND_TO, sendTo);
        startActivity(intent);

    }
    private Activity getActivity(){
        return this; //???
    }


    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("TAGTAG", "onReceive");
            String[] ppl = intent.getStringArrayExtra(CommunicationIntentService.PEOPLE_ONLINE);
            //mUserList = Arrays.asList(intent.getStringArrayExtra(CommunicationIntentService.PEOPLE_ONLINE));
            if(ppl != null){
                mUserList.clear();
                for(String s : ppl){
                    mUserList.add(s);
                }
            }

            if(intent.getBooleanExtra(REQUEST_NEW, false)){
                sendReq();
            }
            mUserAdapter.notifyDataSetChanged();
        }
    }
}
