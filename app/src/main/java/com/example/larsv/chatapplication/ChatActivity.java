package com.example.larsv.chatapplication;

import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.CalendarContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.larsv.chatapplication.Messages.MessageInputStream;
import com.example.larsv.chatapplication.Messages.MessageOutputStream;
import com.example.larsv.chatapplication.Messages.MotherOfAllMessages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;



public class ChatActivity extends AppCompatActivity {
    public static final String BROADCAST = "com.example.larsv.chatapplication.broadcast.BROADCAST";

    SharedPreferences sharedPref;
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private List<MotherOfAllMessages> mMessageList;
    EditText chatWindow;
    String username;

    MessageOutputStream messageOutputStream;
    MessageInputStream messageInputStream;
    static Socket socket;
    String sendTo;

    AppDatabase db;
    List<MessageEntity> msgLog;

    Messenger mService = null;
    boolean mBound;

    public ChatActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String newString;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            newString= extras.getString(MenuActivity.SEND_TO);
            Toast.makeText(this, newString, Toast.LENGTH_SHORT).show();
        }

        //Shared preferences
        sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        username = sharedPref.getString(getResources().getString(R.string.myUsernameKey), "Stranger");
        chatWindow = (EditText)findViewById(R.id.edittext_chatbox);

        //RecyclerView
        mMessageList = new ArrayList<MotherOfAllMessages>();
        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this, mMessageList, username);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(llm);
        mMessageRecycler.setAdapter(mMessageAdapter);

        //broadcast
        IntentFilter myIntentFilter = new IntentFilter(BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(new MyReceiver(),
                myIntentFilter);

        //Database
        new Thread(new Runnable(){
            @Override
            public void run() {

            }
        });
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "ChatLog").allowMainThreadQueries().build();
        msgLog = db.userDao().getAll();
        for(MessageEntity messageEntity: msgLog){
            com.example.larsv.chatapplication.Messages.Message msg =
                    new com.example.larsv.chatapplication.Messages.Message(
                            messageEntity.getContent(), messageEntity.getReceiver(),
                            messageEntity.getSender());
            mMessageList.add(msg);
        }
        mMessageAdapter.notifyDataSetChanged();
        mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount() - 1);
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, CommunicationIntentService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }


    public void sendMessage(View view){
        String msgToSend = chatWindow.getText().toString();
        if(!msgToSend.equals("")){
            chatWindow.setText("");
            final com.example.larsv.chatapplication.Messages.Message msg =
                    new com.example.larsv.chatapplication.Messages.Message(
                            msgToSend,"ALL",username);
            mMessageList.add(msg);
            mMessageAdapter.notifyItemInserted(mMessageList.size() - 1);
            mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount() - 1);

            //Send
            new Thread(
                    new Runnable(){
                        @Override
                        public void run() {
                            try{
                                socket = CommunicationIntentService.getSocket();
                                if(!socket.isConnected()){
                                    Intent backToStart = new Intent(getApplicationContext(),
                                            MainActivity.class);
                                    startActivity(backToStart);
                                }
                                MessageOutputStream mos = new MessageOutputStream(
                                        socket.getOutputStream());
                                mos.writeMessage(msg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            ).start();
            db.userDao().insertAll(new MessageEntity(msg.getFrom(),msg.getTo(),msg.getContent()));
        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
            socket = CommunicationIntentService.getSocket();
        }
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mBound = false;
        }
    };

    void sendMessengerMessage(){
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, CommunicationIntentService.MSG_SAY_HELLO, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            com.example.larsv.chatapplication.Messages.Message msgReceived =
                    new com.example.larsv.chatapplication.Messages.Message(
                            intent.getByteArrayExtra(CommunicationIntentService.MSG_RECEIVED));
            mMessageList.add(msgReceived);
            mMessageAdapter.notifyItemInserted(mMessageList.size() - 1);
            mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount() - 1);
        }
    }

}
