package com.example.larsv.chatapplication;

import android.app.IntentService;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.larsv.chatapplication.Messages.MessageInputStream;
import com.example.larsv.chatapplication.Messages.MessageOutputStream;
import com.example.larsv.chatapplication.Messages.MotherOfAllMessages;
import com.example.larsv.chatapplication.Messages.UpdateMessage;
import com.example.larsv.chatapplication.Messages.UsersOnline;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class CommunicationIntentService extends IntentService {
    public static final String ACTION_SENDMSG = "com.example.larsv.chatapplication.action.SENDMSG";
    public static final int MSG_SAY_HELLO = 0;
    public static final String MSG_RECEIVED = "com.example.larsv.chatapplication.broadcast.MSG_REC";
    public static final String LOGIN_DONE = "com.example.larsv.chatapplication.broadcast.LOGINDONE";
    public static final String PEOPLE_ONLINE = "com.example.larsv.chatapplication."
            +"broadcast.PEOPLE_ONLINE";


    final static String TAG = "TAGTAG";

    final Messenger mMessenger = new Messenger(new IncomingHandler());;

    InetAddress serverAddress = null;
    static Socket socket;
    MessageOutputStream mos;
    MessageInputStream mis;
    MotherOfAllMessages mom;
    String username = "Get the username";
    String address;
    String sendTo;

    boolean amIBound = false;
    AppDatabase db;

    public CommunicationIntentService() {
        super("CommunicationIntentService");
    }


    //Keep track of whether it's bound or not
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        amIBound = true;
        return mMessenger.getBinder();
    }
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        amIBound = true;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        boolean b = super.onUnbind(intent);
        amIBound = false;
        return b;
    }

    //The thread
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            //Sets up varables
            final String action = intent.getAction();
            if (ACTION_SENDMSG.equals(action)) {
                final String param1 = intent.getStringExtra(MainActivity.USERNAME);
                address = intent.getStringExtra(MainActivity.ADDRESS);
                username = param1;
                SharedPreferences sharedPref = getSharedPreferences("UserInfo",
                        Context.MODE_PRIVATE);
                sendTo = sharedPref.getString("sendToKey", "ALL");

                IncomingHandler ih = new IncomingHandler();

                Log.i(TAG, (sendTo==null)?"null":sendTo);
                if(sendTo != null)
                db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                        sendTo).allowMainThreadQueries().build();

                handleActionFoo();
            }
        }
    }


    //Helper method for on handle intent
    private void handleActionFoo() {
        try {
            //Logging in
            serverAddress = InetAddress.getByName(address);
            socket = new Socket(serverAddress, 1234);
            mos = new MessageOutputStream(socket.getOutputStream());
            mis = new MessageInputStream(socket.getInputStream());

        } catch (UnknownHostException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        try {
            //Receives messages and determines what to do with them
            while(true) {
                //Using the MessageInputStream to read a message from the socket
                mom = mis.readMessage();

                //save to correct database (one per client you chat with. + one for ALL)
                SharedPreferences sharedPref = getSharedPreferences("UserInfo",
                        Context.MODE_PRIVATE);
                sendTo = sharedPref.getString("SendToKey", "ALL");
                Log.i(TAG, sendTo);


                if(mom == null) {
                    Log.i(TAG,"Server closed the connection prematurely");
                    return;
                }

                //Determine what type of message you received
                if(mom.getType() == MotherOfAllMessages.MESSAGE) {
                    //Normal message: broadcast to the chat activity
                    com.example.larsv.chatapplication.Messages.Message msg =
                            (com.example.larsv.chatapplication.Messages.Message)mom;
                    Intent broadcastIntent = new Intent(ChatActivity.BROADCAST);
                    broadcastIntent.putExtra(MSG_RECEIVED, msg.serialize());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                    //or if server announces a change in users online: tell the menu
                    if(msg.getFrom().equals("Server")){
                        Intent broadcastIntent2 = new Intent(PEOPLE_ONLINE);
                        broadcastIntent2.putExtra(MenuActivity.REQUEST_NEW, true);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent2);
                    }
                    //Also save the message to the log database
                    db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                            msg.getTo().equals("ALL") ? "ALL" : msg.getFrom()
                    ).allowMainThreadQueries().build();
                    if(msg.getTo().equals(username) || msg.getTo().equals("ALL"))
                        db.userDao().insertAll(new MessageEntity(msg.getFrom(),
                                msg.getTo(),msg.getContent()));
                }
                else if(mom.getType()==MotherOfAllMessages.USERS_ONLINE_MESSAGE) {
                    //Broadcast list of online users to the menu activity
                    UsersOnline uo = (UsersOnline)mom;
                    Intent broadcastIntent = new Intent(PEOPLE_ONLINE);
                    broadcastIntent.putExtra(PEOPLE_ONLINE, uo.toStringArray());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                }
                else if(mom.getType() == MotherOfAllMessages.UPDATE_MESSAGE) {
                    //Response to login attempt: broadcast to main/login activity
                    UpdateMessage umsg = (UpdateMessage)mom;
                   if( umsg.getStatus() == UpdateMessage.LOGIN_OK){
                        Intent broadcastIntent = new Intent(LOGIN_DONE);
                        broadcastIntent.putExtra(LOGIN_DONE, umsg.serialize());
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                    }else if( umsg.getStatus() == UpdateMessage.LOGIN_FAILED){
                        Intent broadcastIntent = new Intent(LOGIN_DONE);
                        broadcastIntent.putExtra(LOGIN_DONE, umsg.serialize());
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                    }else if( umsg.getStatus() == UpdateMessage.CREATE_ACCOUNT_OK){
                        Intent broadcastIntent = new Intent(LOGIN_DONE);
                        broadcastIntent.putExtra(LOGIN_DONE, umsg.serialize());
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                    }else if( umsg.getStatus() == UpdateMessage.CREATE_ACCOUNT_FAILED){
                        Intent broadcastIntent = new Intent(LOGIN_DONE);
                        broadcastIntent.putExtra(LOGIN_DONE, umsg.serialize());
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                    }
                }

            }
        }catch(SocketException ex) {
            System.out.println("Closing receiver");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Handler currently not in use
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    //sendTo = msg.getData().getString(ChatActivity.MESSENGER_MESSAGE);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    public static Socket getSocket(){
        return socket;
    }
    public static void connectSocketIfDown(String address, int port) throws IOException {
        if(socket == null){
            socket = new Socket(address, port);
        }
        if(!socket.isConnected()){
            socket = new Socket(address, port);
        }
    }

}
