package com.example.sourabh.wifi_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Join_Contest extends Activity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join__contest);
    }

    public void join_contest(View v){
        intent = new Intent(this,Participant_details.class);
        startActivity(intent);
    }
}
