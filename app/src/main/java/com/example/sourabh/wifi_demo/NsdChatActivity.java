package com.example.sourabh.wifi_demo;

import android.app.Activity;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;

public class NsdChatActivity extends Activity {

    NsdHelper mNsdHelper;
    View v;

    private TextView mStatusView;
    private Handler mUpdateHandler;

    public static final String TAG = "NsdChat";
    public static String mUserChoice;

    ChatConnection mConnection;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating chat activity");
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Checking if the user's role

        mUserChoice =getIntent().getStringExtra("flag");
        if(mUserChoice.equals("server")) {
            v = findViewById(R.id.discover_btn);
            v.setVisibility(View.GONE);
            v = findViewById(R.id.connect_btn);
            v.setVisibility(View.GONE);
        }
        else if(mUserChoice.equals("client")) {
            v = findViewById(R.id.advertise_btn);
            v.setVisibility(View.GONE);
        }

        mStatusView = (TextView) findViewById(R.id.status);
        mStatusView.setMovementMethod(new ScrollingMovementMethod());

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };

    }

    //Initiating service registration
    public void clickAdvertise(View v) {
        // Register service
        if(mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
            Toast.makeText(this, "ServerSocket isn't bound",Toast.LENGTH_SHORT).show();
        }
    }

    //Initiating service discovery
    public void clickDiscover(View v) {
        mNsdHelper.discoverServices();
    }

    //Initializing client connectivity to the service
    public void clickConnect(View v) {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting...");
            Toast.makeText(this, "Connecting...",Toast.LENGTH_SHORT).show();
            Socket s = null;
            mConnection.commonConnection(service.getHost(), service.getPort(), s);
        } else {
            Log.d(TAG, "No service to connect to!");
            Toast.makeText(this, "No service to connect to!",Toast.LENGTH_SHORT).show();
        }
    }

    public void clickSend(View v) {
        EditText messageView = (EditText) this.findViewById(R.id.chatInput);
        if (messageView != null) {
            String messageString = messageView.getText().toString();
            if (!messageString.isEmpty()) {
                mConnection.sendMessage(messageString);
            }
            messageView.setText("");
        }
    }

    public void addChatLine(String line) {
        mStatusView.append("\n" + line);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting.");
        //creating an object of the ChatConnection class
        mConnection = new ChatConnection(mUpdateHandler);
        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();
        super.onStart();
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing.");
        if (mNsdHelper != null) {
            //mNsdHelper.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Resuming.");
        super.onResume();
        if (mNsdHelper != null && !NsdHelper.flag) {
            //mNsdHelper.discoverServices();
        }
    }


    // For KitKat and earlier releases, it is necessary to remove the
    // service registration when the application is stopped.  There's
    // no guarantee that the onDestroy() method will be called (we're
    // killable after onStop() returns) and the NSD service won't remove
    // the registration for us if we're killed.

    // In L and later, NsdService will automatically unregister us when
    // our connection goes away when we're killed, so this step is
    // optional (but recommended).

    @Override
    protected void onStop() {
        Log.d(TAG, "Being stopped.");
        mNsdHelper.tearDown();
        mConnection.tearDown();
        mNsdHelper = null;
        mConnection = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Being destroyed.");
        super.onDestroy();
    }
}
