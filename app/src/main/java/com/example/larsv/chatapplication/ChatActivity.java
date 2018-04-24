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
    public static final String MESSENGER_MESSAGE = "com.example.larsv.chatapplication.messenger.MS";

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

        //get the name of whoever you are going to send messages to
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            sendTo = extras.getString(MenuActivity.SEND_TO);
            //Tells you who you are chatting with
            Toast.makeText(this, sendTo, Toast.LENGTH_SHORT).show();
        }

        //Shared preferences
        sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        username = sharedPref.getString(getResources().getString(R.string.myUsernameKey), "Stranger");
        chatWindow = (EditText)findViewById(R.id.edittext_chatbox);
        //Save who you are currently chatting with to the preferences
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("SendToKey", sendTo);
        editor.commit();


        //RecyclerView that shows messages
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
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                sendTo).allowMainThreadQueries().build();
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

    //Binds to the communication thread
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, CommunicationIntentService.class), mConnection,
                Context.BIND_AUTO_CREATE);
        sendMessengerMessage(sendTo);
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

    //Open a new thread to send a message containing you, the receiver and the content
    //Also send the message to your own screen and the chat log database
    public void sendMessage(View view){
        String msgToSend = chatWindow.getText().toString();
        if(!msgToSend.equals("")){
            chatWindow.setText("");
            final com.example.larsv.chatapplication.Messages.Message msg =
                    new com.example.larsv.chatapplication.Messages.Message(
                            msgToSend, sendTo, username);
            if(!username.equals(sendTo)){
                mMessageList.add(msg);
                mMessageAdapter.notifyItemInserted(mMessageList.size() - 1);
            }
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
                                if(socket != null){
                                    if (!socket.isClosed()){
                                        try {
                                            socket.close();
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }
                                socket = null;
                                Intent backToStart = new Intent(getApplicationContext(),
                                        MainActivity.class);
                                startActivity(backToStart);
                            }
                        }
                    }
            ).start();
            if(!username.equals(sendTo))
                db.userDao().insertAll(new MessageEntity(msg.getFrom(),msg.getTo(),msg.getContent()));
        }
    }


    //gets socket when connected to communication service
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
    //Send a handler message to the communication thread
    //NOT used
    void sendMessengerMessage(String msgToSend){
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, CommunicationIntentService.MSG_SAY_HELLO, 0, 0);
        Bundle data = new Bundle();
        data.putString(MESSENGER_MESSAGE, msgToSend);
        msg.setData(data);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //Receives the received messages from the communication thread and prints to screen
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            com.example.larsv.chatapplication.Messages.Message msgReceived =
                    new com.example.larsv.chatapplication.Messages.Message(
                            intent.getByteArrayExtra(CommunicationIntentService.MSG_RECEIVED));
            if((msgReceived.getFrom().equals(sendTo) && msgReceived.getTo().equals(username)) ||
                    (msgReceived.getTo().equals("ALL") && sendTo.equals("ALL"))){
                mMessageList.add(msgReceived);
                mMessageAdapter.notifyItemInserted(mMessageList.size() - 1);
                mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount() - 1);
            }
        }
    }

}
