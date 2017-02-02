package com.andraganoid.stb_raspored;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    StringBuilder sb, sb2, sbSplit;
    EditText et;
    HashMap<String, String> people;
    SmsManager sm = SmsManager.getDefault();

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
        sbSplit = new StringBuilder();
        String txt = et.getText().toString();
        sendTel.clear();
        sendMsg.clear();
        for (Map.Entry<String, ArrayList<Object>> entry : sms.entrySet()) {
            String ime = entry.getKey();
            sendList = entry.getValue();
            String tel = people.get(ime);
            sb = new StringBuilder();
            sbSplit.delete(0, sbSplit.length());
            setDate(firstDay, 0);
            sb.append("Vas raspored:\n" + new SimpleDateFormat("dd.MM.").format(calendar.getTime()));
            setDate(firstDay, 6);
            sb.append("-" + new SimpleDateFormat("dd.MM.").format(calendar.getTime()) + "\n");
            sbSplit.append(sb);
            sb2 = new StringBuilder();
            for (Object o : sendList) {
                Send snd = (Send) o;

                sbSplit.append(dani[snd.day] + " ");
                sbSplit.append(snd.shift + " ");
                sbSplit.append(snd.sector + " \n");
                List<String> me = sm.divideMessage(sbSplit.toString());
                if (me.size() > 1) {
                    sb2.append("#");
                    sbSplit.delete(0, sbSplit.length());
                    sbSplit.append(dani[snd.day] + " ");
                    sbSplit.append(snd.shift + " ");
                    sbSplit.append(snd.sector + " \n");
                }
                sb2.append(dani[snd.day] + " ");
                sb2.append(snd.shift + " ");
                sb2.append(snd.sector + " \n");
            }
            if (sb2.length() == 0) {
                sb2.append("Ove sedmice niste u rasporedu!");
                sbSplit.append("Ove sedmice niste u rasporedu!");
            }
            sb.append(sb2);
            sbSplit.append((txt));
            List<String> me = sm.divideMessage(sbSplit.toString());
            if (me.size() > 1) {
                sb.append("#");
            }
            sb.append(txt);
            if (tel != null) {
                sendTel.add(tel);
                sendMsg.add(sb.toString());
            }
        }

        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setMessage("Potvrdi slanje");
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
