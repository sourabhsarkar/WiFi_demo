package com.example.sourabh.wifi_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WifiLandingPage extends Activity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_landing_page);
        intent = new Intent(this, NsdChatActivity.class);
    }

    //direct the user to the server activity
    public void sv_btn_click(View v) {
        intent.putExtra("flag","server");
        startActivity(intent);
    }

    //direct the user to the client page
    public void cl_btn_click(View v) {
        intent.putExtra("flag","client");
        startActivity(intent);
    }
}
