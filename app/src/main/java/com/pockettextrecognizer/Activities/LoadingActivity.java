package com.pockettextrecognizer.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pockettextrecognizer.R;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Thread startThread = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(3000);
                    // setting intent
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    // launching MainActivity
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        // initializing thread
        startThread.start();
    }
}
