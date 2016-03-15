package com.example.sourabh.wifi_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Contest_Creation extends Activity {

    Intent intent;

    public static final String TAG = "Contest_Creation";

    TextView con_name;
    TextView con_password;
    TextView con_details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest__creation);
        con_name= (TextView)findViewById(R.id.con_name);
        con_password= (TextView)findViewById(R.id.con_password);
        con_details= (TextView)findViewById(R.id.con_details);
    }

    public void con_create_click(View v){

        if(con_name.getText().toString().equals("") || con_password.getText().toString().equals("")) {
            Toast.makeText(this,"One or more fields is empty",Toast.LENGTH_LONG).show();
        }
        else {
            intent = new Intent(this, Contest_Upload.class);
            startActivity(intent);
        }
    }
}
