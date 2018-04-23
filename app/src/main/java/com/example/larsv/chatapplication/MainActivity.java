package com.example.larsv.chatapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.larsv.chatapplication.Messages.LoginMessage;
import com.example.larsv.chatapplication.Messages.Message;
import com.example.larsv.chatapplication.Messages.MessageInputStream;
import com.example.larsv.chatapplication.Messages.MessageOutputStream;
import com.example.larsv.chatapplication.Messages.UpdateMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    public static final String USERNAME = "USERNAME";
    public static final String ADDRESS = "ADDRESS";
    private String username;
    private String address;
    EditText txtUsername;
    EditText txtPassword;
    EditText txtIP;
    TextView txtHello;
    Button btnSend;
    Button btnCreate;

    static InetAddress serverAddress = null;
    static Socket socket;
    static MessageOutputStream mos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtIP = (EditText) findViewById(R.id.txtIP);
        txtHello = (TextView) findViewById(R.id.textView);
        btnSend = (Button) findViewById(R.id.buttonSend);
        btnCreate = (Button) findViewById(R.id.buttonCreate);
        txtUsername.setHint(R.string.username);
        txtPassword.setHint(R.string.password);

        IntentFilter myIntentFilter = new IntentFilter(CommunicationIntentService.LOGIN_DONE);
        LocalBroadcastManager.getInstance(this).registerReceiver(new MyReceiver(),
                myIntentFilter);
    }

    public void buttonLogin(View view){
        username = txtUsername.getText().toString();
        address = txtIP.getText().toString();
        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.myUsernameKey), username);
        editor.commit();

        Intent intent = new Intent(this, CommunicationIntentService.class);
        intent.setAction(CommunicationIntentService.ACTION_SENDMSG);
        intent.putExtra(USERNAME, username);
        intent.putExtra(ADDRESS, address);
        startService(intent);

        new LoginTask().execute(new LoginMessage(username, txtPassword.getText().toString(),
                LoginMessage.LOGIN));


    }
    public void buttonCreate(View view){
        username = txtUsername.getText().toString();
        address = txtIP.getText().toString();
        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.myUsernameKey), username);
        editor.commit();

        Intent intent = new Intent(this, CommunicationIntentService.class);
        intent.setAction(CommunicationIntentService.ACTION_SENDMSG);
        intent.putExtra(USERNAME, username);
        intent.putExtra(ADDRESS, address);
        startService(intent);

        new LoginTask().execute(new LoginMessage(username, txtPassword.getText().toString(),
                LoginMessage.CREATE_ACCOUNT));

    }

    public void loggedIn(){
        Intent intentToMenu = new Intent(this, MenuActivity.class);
        startActivity(intentToMenu);
    }

    //Alerted when login is done
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] didItWork = intent.getByteArrayExtra(CommunicationIntentService.LOGIN_DONE);
            UpdateMessage umsg = new UpdateMessage(didItWork);
            btnCreate.setAlpha(1f);
            btnCreate.setClickable(true);
            btnCreate.setText("Create");
            btnSend.setAlpha(1f);
            btnSend.setClickable(true);
            btnSend.setText("Login");
            if(umsg.getStatus() == UpdateMessage.LOGIN_OK){
                loggedIn();
            }else if(umsg.getStatus() == UpdateMessage.LOGIN_FAILED){
                txtHello.setText(umsg.getUsername());
            }else if(umsg.getStatus() == UpdateMessage.CREATE_ACCOUNT_OK){
                txtHello.setText("Account created");
            }else if(umsg.getStatus() == UpdateMessage.CREATE_ACCOUNT_FAILED){
                txtHello.setText(umsg.getUsername());
            }
        }
    }

    private class LoginTask extends AsyncTask<LoginMessage, Integer, Boolean> {
        protected Boolean doInBackground(LoginMessage... msg) {
            try {
                int count = 0;
                while(CommunicationIntentService.getSocket() == null){
                    Thread.sleep(1);
                    count++;
                    if (count > 10000){
                        btnCreate.setAlpha(1f);
                        btnCreate.setClickable(true);
                        btnCreate.setText("Create");
                        btnSend.setAlpha(1f);
                        btnSend.setClickable(true);
                        btnSend.setText("Login");
                        Log.i("MainActivity.LoginTask", "Wait for connection timed out");
                        return false;
                    }
                }
                MessageOutputStream mos = new MessageOutputStream(
                        CommunicationIntentService.getSocket().getOutputStream());
                mos.writeMessage(msg[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if(!result){
                txtHello.setText("Wait for connection timed out");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnCreate.setAlpha(.5f);
            btnCreate.setClickable(false);
            btnCreate.setText("Waiting..");
            btnSend.setAlpha(.5f);
            btnSend.setClickable(false);
            btnSend.setText("Waiting..");
        }
    }

}
