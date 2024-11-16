package com.zetadev.locationwidget;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class WifiMonitor extends Worker {

    private ConnectivityManager.NetworkCallback networkCallback;

    public WifiMonitor(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Avvia il monitoraggio del Wi-Fi
        registerNetworkCallback(getApplicationContext());
        return Result.success();
    }

    public void registerNetworkCallback(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)  // Monitoraggio solo per reti Wi-Fi
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);

                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                if (wifiInfo != null) {
                    Log.d("agg", "Connected to specific WiFi network: " + wifiInfo.getSSID());



                    String networkName = wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length() - 1);

                    // Leggi il file JSON
                    Map<String, Object> triggerData   = readTriggerFromFile(context);


                    if (triggerData != null && triggerData.containsKey("WI-FI")) {
                        Map<String, Object> wifiTriggers = (Map<String, Object>) triggerData.get("WI-FI");

                        // Controlla se il Wi-Fi connesso è presente nel file
                        if (wifiTriggers.containsKey(networkName)) {
                            Log.d("agg", "Trigger found for network: " + networkName);
                            Map<String, Object> deviceData = (Map<String, Object>) wifiTriggers.get(networkName);

                            // Passa le app associate al widget (esempio)
                            List<String> associatedAppsList = (List<String>) deviceData.get("apps");
                            String[] associatedApps = new String[associatedAppsList.size()];
                            associatedApps = associatedAppsList.toArray(associatedApps); // Converte l'ArrayList in un array di stringhe

                            // Aggiorna il widget
                            Intent updateIntent = new Intent(context, CustomWidget.class);
                            updateIntent.setAction("com.zetadev.locationwidget.ACTION_WIFI_UPDATE");
                            updateIntent.putExtra("apps", associatedApps); // Aggiungi le app all'intento
                            context.sendBroadcast(updateIntent);
                        } else {
                            Log.d("agg", "No trigger found for network: " + wifiInfo.getSSID());
                        }
                    }
                    else
                    {
                        Log.d("agg", "No WIFI triggers presents or file do not exist terminating worker");
                        WorkManager.getInstance(getApplicationContext()).cancelWorkById(getId());

                    }
                } else {
                    Log.d("agg", "Connected to WiFi network: null");
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.d("agg", "WiFi network lost.");
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    public void unregisterNetworkCallback(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
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
            Log.e("BluethoothReceiver", "Error reading trigger file, File do not exist");
            return null;
        }
    }


    //&& SPECIFIC_SSID.equals(wifiInfo.getSSID())



}
