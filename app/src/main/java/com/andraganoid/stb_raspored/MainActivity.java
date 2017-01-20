package com.andraganoid.stb_raspored;

import android.content.Intent;
import android.net.Uri;
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
    }

    public void mailToMe(View v) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "andraganoid@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "StB raspored");
        startActivity(Intent.createChooser(emailIntent, "Izaberi..."));
    }
}

