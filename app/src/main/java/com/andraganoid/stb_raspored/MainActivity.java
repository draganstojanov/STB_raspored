package com.andraganoid.stb_raspored;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void readScheduleFile(View v) {
        Intent i = new Intent(this, ReadTable.class);
        startActivity(i);
        finish();
    }


}

