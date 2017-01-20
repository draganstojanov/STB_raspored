package com.andraganoid.stb_raspored;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Send extends AppCompatActivity {

    String name;
    int day;
    String shift;
    String sector;
    String tel;
    String msg;
    ArrayList<String> schedule;
    HashMap<String, ArrayList<Object>> sms = new HashMap<String, ArrayList<Object>>();
    ArrayList<Object> sendList = new ArrayList<Object>();
    ArrayList<String> sendTel = new ArrayList<>();
    ArrayList<String> sendMsg = new ArrayList<>();
    String s;
    String firstDay;
    String[] dani = {"pon", "uto", "sre", "cet", "pet", "sub", "ned"};
    Calendar calendar = Calendar.getInstance();
    StringBuilder sb;
    EditText et;
    HashMap<String, String> people;

    public Send() {
    }

    public Send(String sector, int day, String shift) {
        this.sector = sector;
        this.day = day;
        this.shift = shift;
    }

    public Send(String tel, String msg) {
        this.tel = tel;
        this.msg = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        schedule = getIntent().getStringArrayListExtra("schedule");
        people = (HashMap) getIntent().getSerializableExtra("people");
        et = (EditText) findViewById(R.id.tekst);
        for (String g : people.keySet()) {
            sms.put(g, new ArrayList<>());
        }

        firstDay = schedule.get(0);
        schedule.remove(0);
        Iterator<String> sIter = schedule.iterator();
        while (sIter.hasNext()) {
            s = sIter.next();
            if (s.contains("sektor")) {
                sector = sIter.next();
                for (int i = 0; i < 7; i++) {
                    sIter.next();
                }
                while (true) {
                    shift = sIter.next();
                    if (shift.contains("stop")) {
                        break;
                    }
                    for (int j = 0; j < 7; j++) {
                        setDate(firstDay, j);
                        name = (sIter.next()).trim();
                        day = calendar.get(Calendar.DAY_OF_WEEK) - 2;
                        if (day == -1) {
                            day = 6;
                        }
                        if (name.equals("x")) {
                            continue;
                        }
                        Send snd = new Send(sector, day, shift);
                        if (sms.get(name) == null) {
                            sms.put(name, new ArrayList<>());
                        }
                        sms.get(name).add(snd);
                    }
                }
            }
        }
        for (String g : sms.keySet()) {
            sendList = sms.get(g);
            ArrayList<Object> al = new ArrayList<Object>();
            for (int i = 0; i < 7; i++) {
                for (Object o : sendList) {
                    if (((Send) o).day == i) {
                        al.add(o);
                    }
                }
            }
            sms.put(g, al);
        }
    }

    void setDate(String d, int j) {
        try {
            calendar.setTime(new SimpleDateFormat("dd.MM.yyyy").parse(d));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.DAY_OF_YEAR, j);
    }

    public void sendsms(View v) {
        String txt = et.getText().toString();
        sendTel.clear();
        sendMsg.clear();
        for (Map.Entry<String, ArrayList<Object>> entry : sms.entrySet()) {
            String ime = entry.getKey();
            sendList = entry.getValue();
            String tel = people.get(ime);
            sb = new StringBuilder();
            setDate(firstDay, 0);
            sb.append("Vas raspored:\n" + new SimpleDateFormat("dd.MM.").format(calendar.getTime()));
            setDate(firstDay, 6);
            sb.append("-" + new SimpleDateFormat("dd.MM.").format(calendar.getTime()) + "\n");
            for (Object o : sendList) {
                Send snd = (Send) o;
                sb.append(dani[snd.day] + " ");
                setDate(firstDay, snd.day);
                sb.append(snd.shift + " ");
                sb.append(snd.sector + " \n");
            }
            sb.append(txt);
            if (tel != null) {
                sendTel.add(tel);
                sendMsg.add(sb.toString());
            }
        }

        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setMessage("Poruka za slanje: " + sendTel.size());
        ad.setPositiveButton("Pošalji", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getApplicationContext(), SendingMsgs.class);
                i.putExtra("sendTel", sendTel);
                i.putExtra("sendMsg", sendMsg);
                startActivity(i);
                finish();
            }
        });

        ad.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Send.this, MainActivity.class));
            }
        });
        ad.create().show();
    }
}
