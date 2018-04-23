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

    boolean amIBound = false;
    AppDatabase db;

    public CommunicationIntentService() {
        super("CommunicationIntentService");
    }


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

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SENDMSG.equals(action)) {
                final String param1 = intent.getStringExtra(MainActivity.USERNAME);
                address = intent.getStringExtra(MainActivity.ADDRESS);
                username = param1;

                db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                        "ChatLog").allowMainThreadQueries().build();

                handleActionFoo();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo() {
        try {
            //Logging in
            serverAddress = InetAddress.getByName(address);
            socket = new Socket(serverAddress, 1234);
            mos = new MessageOutputStream(socket.getOutputStream());
            mis = new MessageInputStream(socket.getInputStream());
            mos.writeMessage(new UpdateMessage(username, UpdateMessage.MY_NAME_IS));
            mos.writeMessage(new com.example.larsv.chatapplication.Messages.Message(
                    "A test", "ALL", username));

        } catch (UnknownHostException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        try {
            while(true) {
                //Using the MessageInputStream to read a message from the socket
                mom = mis.readMessage();

                if(mom == null) {
                    Log.i(TAG,"Server closed the connection prematurely");
                    return;
                }

                //Determine what type of message you received
                if(mom.getType() == MotherOfAllMessages.MESSAGE) {
                    com.example.larsv.chatapplication.Messages.Message msg =
                            (com.example.larsv.chatapplication.Messages.Message)mom;

                    Intent broadcastIntent = new Intent(ChatActivity.BROADCAST);
                    broadcastIntent.putExtra(MSG_RECEIVED, msg.serialize());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                    db.userDao().insertAll(new MessageEntity(msg.getFrom(),msg.getTo(),msg.getContent()));
                }
                else if(mom.getType()==MotherOfAllMessages.USERS_ONLINE_MESSAGE) {
                    UsersOnline uo = (UsersOnline)mom;
                    Intent broadcastIntent = new Intent(PEOPLE_ONLINE);
                    broadcastIntent.putExtra(PEOPLE_ONLINE, uo.toStringArray());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                }
                else if(mom.getType() == MotherOfAllMessages.UPDATE_MESSAGE) {
                    //Can be used to determine if a user is online.
                    UpdateMessage umsg = (UpdateMessage)mom;
                    if(umsg.getStatus() == UpdateMessage.NOT_ONLINE) {

                    }
                    if(umsg.getStatus() == UpdateMessage.IS_ONLINE) {

                    }else if( umsg.getStatus() == UpdateMessage.LOGIN_OK){
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



    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    public static Socket getSocket(){
        return socket;
    }

}
