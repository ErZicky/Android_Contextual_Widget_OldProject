package com.zetadev.locationwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BootMonitor extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("agg", "Device booted");
            // Avvia il servizio o attività desiderata

            //WriteLog(context);
            //check what receiver are needed and start them
            Map<String, Object> triggerData = readTriggerFromFile(context);




            if (triggerData != null ) {

                List<String> listenersNeeded = new ArrayList<>();

                if(triggerData.containsKey("charging"))
                {
                    listenersNeeded.add("charging");
                }

                if(triggerData.containsKey("location"))
                {
                    //TODO location start worker
                }
                //TODO eventualmente clock
                if(triggerData.containsKey("WI-FI"))
                {
                    listenersNeeded.add("WI-FI");

                }
                Intent serviceIntent = new Intent(context, ReceiverRegistrationService.class);
                String[] stringArray = listenersNeeded.toArray(new String[0]);
                serviceIntent.putExtra("tostart", stringArray);
                context.startService(serviceIntent);


            } else {
                Log.d("agg", "No charing trigger found");
            }



        }
    }



    private Map<String, Object> readTriggerFromFile(Context context) {
        try (FileInputStream fis = context.openFileInput("triggers.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            Gson gson = new Gson();
            return gson.fromJson(stringBuilder.toString(), Map.class);
        } catch (Exception e) {
            Log.e("BluethoothReceiver", "Error reading trigger file", e);
            return null;
        }
    }

    private void WriteLog(Context context) {


        String fileName = "log.txt";
        String data = "Boot Complete\n";

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
