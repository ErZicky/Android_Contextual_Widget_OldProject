package com.zetadev.locationwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class ChargingMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_POWER_CONNECTED.equals(action)) {

                Log.d("agg", "Device is charging");



                // Leggi il file JSON
                Map<String, Object> triggerData = readTriggerFromFile(context);
                if (triggerData != null && triggerData.containsKey("charging")) {
                    Map<String, Object> Triggers = (Map<String, Object>) triggerData.get("charging");




                        // Passa le app associate al widget (esempio)
                        List<String> associatedAppsList = (List<String>) Triggers.get("apps");
                        String[] associatedApps = new String[associatedAppsList.size()];
                        associatedApps = associatedAppsList.toArray(associatedApps); // Converte l'ArrayList in un array di stringhe



                        // Aggiorna il widget
                        Intent updateIntent = new Intent(context, CustomWidget.class);
                        updateIntent.setAction("com.zetadev.locationwidget.ACTION_CHARGING_UPDATE");
                        updateIntent.putExtra("apps", associatedApps); // Aggiungi le app all'intento
                        context.sendBroadcast(updateIntent);

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

            return null;
        }
    }
}
