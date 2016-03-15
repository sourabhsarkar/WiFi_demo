package com.example.sourabh.wifi_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Contest_Creation extends Activity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest__creation);
    }

    public void con_create_click(View v){
        intent = new Intent(this,Contest_Upload.class);
        startActivity(intent);
    }
}
