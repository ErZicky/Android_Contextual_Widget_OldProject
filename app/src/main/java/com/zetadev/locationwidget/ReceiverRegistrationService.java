package com.zetadev.locationwidget;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ReceiverRegistrationService extends Service {

    ChargingMonitor cm;
    @Override
    public void onCreate() {
        super.onCreate();







    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //recuperiamo lista di service da avviare
        String[] listenersNeeded = intent.getStringArrayExtra("tostart");


        if(listenersNeeded != null)
        {
            ManageExtra(listenersNeeded);
        }


        //  fermiamo il servizio dopo aver registrato il receiver
      //  stopSelf();

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        // Assicurati di deregistrare il receiver per evitare perdite di memoria
        if (cm != null) {
            unregisterReceiver(cm);
        }
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private void ManageExtra(String[] extra)
    {
        for(int i = 0; i < extra.length; i++)
        {
            if(extra[i].equals("charging"))
            {
                // Crea il nuovo receiver
             /*   cm = new ChargingMonitor();

                // Inizializza e registra il receiver per lo stato della batteria
                IntentFilter filter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
                registerReceiver(cm, filter);

                WriteLog(getApplicationContext(), "Charging receiver registed\n");*/


                Intent serviceIntent = new Intent(this, ChargingStartingService.class);
                ContextCompat.startForegroundService(this, serviceIntent);

            }
            else if(extra[i].equals("location"))
            {
               /* WriteLog(getApplicationContext(), "start monitoring location\n");
                // Pianifica il Worker per aggiornare la posizione periodicamente
                PeriodicWorkRequest wifiWorkRequest = new PeriodicWorkRequest.Builder(
                        LocationUpdateWorker.class,
                        15,
                        TimeUnit.MINUTES)
                        .build();


                WorkManager.getInstance(this).enqueue(wifiWorkRequest);*/

                Intent serviceIntent = new Intent(this, LocationForegroundService.class);
                ContextCompat.startForegroundService(this, serviceIntent);
            }
            else if(extra[i].equals("WI-FI"))
            {

            /*    WriteLog(getApplicationContext(), "start monitoringwifi\n");
                // Pianifica il Worker per aggiornare la posizione periodicamente
                PeriodicWorkRequest wifiWorkRequest = new PeriodicWorkRequest.Builder(
                        WifiMonitor.class,
                        15,
                        TimeUnit.MINUTES)
                        .build();


                WorkManager.getInstance(this).enqueue(wifiWorkRequest);*/

                Intent serviceIntent = new Intent(this, WifiForegroundService.class);
                ContextCompat.startForegroundService(this, serviceIntent);
            }
        }
    }



    private void WriteLog(Context context, String message) {


        String fileName = "log.txt";
        String data =   message;

        FileOutputStream fos = null;
        try {
            // Apri il file in modalità append (MODE_APPEND)
            fos = context.openFileOutput(fileName, Context.MODE_APPEND);
            fos.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
