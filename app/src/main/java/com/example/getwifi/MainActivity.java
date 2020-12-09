package com.example.getwifi;

import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Bundle;
import android.os.Build;
import android.Manifest;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;


import java.util.Collections;
import java.util.Date;
import java.util.List;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends ListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Scan 毎に Adapter, WifiManager を生成するのは無駄なので 生成は1度だけにします */
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        setListAdapter(adapter);


        final String FORMAT = "SSID: %s, BSSID: %s, capabilities: %s, level: %d, frequency: %d";
        final WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);

        /* メインスレッド以外から UI を触るための 仲介役です */
        final Handler handler = new Handler();

        /* 一定時間ごとに 実行してほしいことを run() の中に書きます */
        final TimerTask timerTask = new TimerTask(){
            @Override
            public void run() {
                /* 今回やることは AP のスキャンです */
                if (manager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                    // APをスキャン
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

                    manager.startScan();
                    // スキャン結果を取得
                    List<ScanResult> results = manager.getScanResults();
                    Log.d("testLog: ", "APList size : " + results.size());

                    if (!results.isEmpty()) {
                        Date dt = new Date();
                        StringBuffer buffer = new StringBuffer();
                        for (ScanResult result : results) {
                            String line = String.format(FORMAT,
                                    result.SSID.replaceAll(",", "-"),
                                    result.BSSID,
                                    result.capabilities,
                                    result.level,
                                    result.frequency);
                            buffer.append("\t");
                            buffer.append(line);
                        }
                        Intent intent = new Intent();
                        intent.putExtra("data", buffer.toString());
                        Log.d("CSLabLogger", "data=" + buffer.toString());
                    }
                    else{
                        Log.d("testLog:","cccc");
                    }



                    /* スキャンが終わったら表示してあげましょう */
                    /* TimerTask は メインスレッドで実行できませんが UIを触りたいのでHandlerを使います */
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /* リストをきれいさっぱり空にして */
                            //adapter.clear();
                            /* 新しい情報を全部足して */
                            //adapter.addAll(aps);
                            /* 中身変わったからねって教える */
                            //adapter.notifyDataSetChanged();
                            /* テスト用の Toast です */
                            /* このプログラムでは 10秒ごとに 表示されれば 正常に動作しています */
                            Toast.makeText(MainActivity.this, "datasetChanged", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };

        /* やることを書き終えたので 実行してあげましょう */
        /* 前述の通り TimerTaskは UI スレッドで実行できないので 別なスレッドの中で実行します */
        new Thread(new Runnable() {
            @Override
            public void run() {
                /* new Timer の引数 true は 管理を簡単にするおまじないです */
                /* アプリが終了処理に入っても 絶対に中断されたくない処理は false にします*/
                /* 1回目を 0秒後(直後)から 10000ミリ秒(10秒)間隔で実行する */
                new Timer(true).schedule(timerTask, 0, 5000);
            }
        }).start();
    }


}

