package com.zetadev.locationwidget;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class WifiForegroundService  extends Service {

    private static final String CHANNEL_ID = "WIFI_MONITOR_CHANNEL";
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("WifiMonitorService", "Service created");
        createNotificationChannel();

        startForeground(1, getNotification("Monitoraggio Wi-Fi in corso"), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        // Avvia il monitoraggio del Wi-Fi
        registerNetworkCallback(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("WifiMonitorService", "Service destroyed");

        // Rimuovi la callback della rete
        unregisterNetworkCallback(getApplicationContext());
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null; // Non supporta il binding
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Monitor Wi-Fi",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Servizio di monitoraggio delle reti Wi-Fi");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private android.app.Notification getNotification(String contentText) {
        return getNotificationBuilder(contentText).build();
    }

    public void registerNetworkCallback(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI) // Monitoraggio solo per reti Wi-Fi
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);

                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                if (wifiInfo != null) {
                    Log.d("WifiMonitorService", "Connected to WiFi network: " + wifiInfo.getSSID());
                    String networkName = wifiInfo.getSSID().replace("\"", ""); // Rimuove eventuali virgolette

                    // Leggi il file JSON
                    Map<String, Object> triggerData = readTriggerFromFile(context);

                    if (triggerData != null && triggerData.containsKey("WI-FI")) {
                        Map<String, Object> wifiTriggers = (Map<String, Object>) triggerData.get("WI-FI");

                        // Controlla se il Wi-Fi connesso è presente nel file
                        if (wifiTriggers.containsKey(networkName)) {
                            Log.d("WifiMonitorService", "Trigger found for network: " + networkName);
                            Map<String, Object> deviceData = (Map<String, Object>) wifiTriggers.get(networkName);

                            // Passa le app associate al widget
                            List<String> associatedAppsList = (List<String>) deviceData.get("apps");
                            String[] associatedApps = new String[associatedAppsList.size()];
                            associatedApps = associatedAppsList.toArray(associatedApps); // Converte l'ArrayList in un array

                            // Aggiorna il widget
                            Intent updateIntent = new Intent(context, CustomWidget.class);
                            updateIntent.setAction("com.zetadev.locationwidget.ACTION_WIFI_UPDATE");
                            updateIntent.putExtra("apps", associatedApps);
                            context.sendBroadcast(updateIntent);
                            showNotification(" Foreground Controllo effettuato: wifi valido trovato");
                        } else {
                            Log.d("WifiMonitorService", "No trigger found for network: " + networkName);
                            showNotification(" Foreground Controllo effettuato: nessun trigger trovato per wifi: " + networkName);
                        }
                    } else {
                        showNotification(" Foreground Controllo effettuato: stop self wifi");
                        stopSelf();
                    }
                } else {
                    Log.d("WifiMonitorService", "Connected to WiFi network: null");
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.d("WifiMonitorService", "WiFi network lost.");
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
            Log.e("WifiMonitorService", "Error reading trigger file", e);
            return null;
        }
    }

    private void showNotification(String desc) {
        NotificationCompat.Builder builder = getNotificationBuilder(desc);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(2, builder.build());
        }
    }

    private NotificationCompat.Builder getNotificationBuilder(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Wifi Widget")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);
    }

}