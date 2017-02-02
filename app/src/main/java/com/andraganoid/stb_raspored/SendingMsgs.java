package com.andraganoid.stb_raspored;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SendingMsgs extends AppCompatActivity {
    BroadcastReceiver br;
    int msgForSending;
    int msgError;
    int msgTotal;
    long tt;
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    ArrayList<String> sendTel = new ArrayList<>();
    ArrayList<String> sendMsg = new ArrayList<>();
    ArrayList smslist = new ArrayList();
    PendingIntent sentPI;
    PendingIntent deliveredPI;
    SmsManager smsm;
    String num;
    String txt;
    int msgNo;
    int smsNo;
    int msg = 0;
    int sms = 0;
    SendingMsgs sm;
    int sent = 0;
    int progress = 0;
    TextView bartext;
    ProgressBar bar;

    public SendingMsgs() {
    }

    public SendingMsgs(String num, String txt, int msgNo, int smsNo) {
        this.num = num;
        this.txt = txt;
        this.msgNo = msgNo;
        this.smsNo = smsNo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_msgs);
        bartext = (TextView) findViewById(R.id.bartext);
        bar = (ProgressBar) findViewById(R.id.bar);
        br = new sms_receiver();
        smsm = SmsManager.getDefault();
        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
        sendTel = getIntent().getStringArrayListExtra("sendTel");
        sendMsg = getIntent().getStringArrayListExtra("sendMsg");
        sending();
    }

    void sending() {
        ((Button) findViewById(R.id.finish)).setVisibility(View.INVISIBLE);
        ((Button) findViewById(R.id.again)).setVisibility(View.INVISIBLE);
        msgError = 0;
        smslist.clear();
        msg = 0;
        sms = 0;
        while (msg < sendMsg.size()) {
            Log.i("probaaa", sendMsg.get(msg));
            String[] w = (sendMsg.get(msg)).split("#");
            for (String m : w) {
                if (m.startsWith("#")) {
                    m = m.substring(1);
                }
                Log.i("proba", m);
                smslist.add(new SendingMsgs(sendTel.get(msg), m, msg, sms));
                sms++;
            }
            msg++;
        }
        sendTel.clear();
        sendMsg.clear();
        registerReceiver(br, new IntentFilter(SENT));

     /*   registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if (getResultCode() != Activity.RESULT_OK) {
                }
            }
        }, new IntentFilter(DELIVERED));*/

        sendmsg();
    }

    void sendmsg() {
        if (!smslist.isEmpty()) {
            sm = (SendingMsgs) smslist.get(0);
            msgForSending = sm.msgNo;
            msgTotal = sm.smsNo;
            tt = 1000;
            if (sent % 29 == 0 && sent > 28) {
                tt = 180000;
            }
            smsm.sendTextMessage(sm.num, null, sm.txt, sentPI, deliveredPI);
            smslist.remove(0);
            sent++;
        } else {
            unregisterReceiver(br);
            ((Button) findViewById(R.id.finish)).setVisibility(View.VISIBLE);
            if (msgError > 0) {
                ((Button) findViewById(R.id.again)).setVisibility(View.VISIBLE);
            }
        }
    }

    public void again(View V) {
        sending();
    }

    public void kraj(View V) {
        finish();
    }


    void waiting() {
        bartext.setVisibility(View.VISIBLE);
        bar.setVisibility((View.VISIBLE));
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                progress = 0;
                while (progress < 180) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progress++;
                    bar.setProgress(progress);
                }
            }
        });
        thread.start();
    }

    class sms_receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            {
                if (!(getResultCode() == Activity.RESULT_OK)) {
                    msgError++;
                    sendTel.add(sm.num);
                    sendMsg.add(sm.txt);
                }
                final Handler handler = new Handler();
                final Timer timer = new Timer(false);
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) findViewById(R.id.success)).setText("Poruka poslato:     " + (msgForSending + 1) + " od " + msg);
                                ((TextView) findViewById(R.id.total)).setText("SMS-ova poslato: " + (msgTotal + 1) + " od " + sms);
                                ((TextView) findViewById(R.id.errors)).setText("Grešaka: " + msgError);
                                sendmsg();
                            }
                        });
                    }
                };
                if (tt == 180000) {
                    waiting();
                } else {
                    bartext.setVisibility(View.GONE);
                    bar.setVisibility((View.GONE));
                }
                timer.schedule(timerTask, tt);
            }
        }
    }
}
