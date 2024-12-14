package com.zetadev.locationwidget;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ChargingStartingService extends Service {

    private static final String CHANNEL_ID = "CHARGING_MONITOR_CHANNEL";
    private static final long RECHECK_INTERVAL = 240000; // 4 minuto
    private ChargingMonitor chargingMonitor;
    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ChargingStartingService", "Service created");

        // Crea il canale per le notifiche
        createNotificationChannel();

        // Inizializza e registra il ChargingMonitor
        chargingMonitor = new ChargingMonitor();
        registerChargingMonitor();

        // Avvia il controllo periodico
        if(android.os.Build.VERSION.SDK_INT >= 34)
        {
            startForeground(1, getNotification("Monitoraggio stato batteria attivo"), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        }
        else
        {
            startForeground(1, getNotification("Monitoraggio stato batteria attivo"));
        }

        startPeriodicMonitor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ChargingStartingService", "Service destroyed");

        // Annulla la registrazione del ChargingMonitor
        unregisterReceiver(chargingMonitor);

        // Ferma i callback del handler
        handler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Non supporta il binding
    }

    private void registerChargingMonitor() {
        Log.d("ChargingStartingService", "Registering ChargingMonitor");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(chargingMonitor, filter);
    }

    private void startPeriodicMonitor() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("ChargingStartingService", "Periodic monitor triggered");
                // Rimuovi e registra nuovamente il ChargingMonitor per assicurare che sia attivo
                try {
                    unregisterReceiver(chargingMonitor);
                } catch (IllegalArgumentException e) {
                    // Ignora errori, il receiver potrebbe non essere già registrato
                }
                registerChargingMonitor();

                // Ripeti dopo l'intervallo definito
                handler.postDelayed(this, RECHECK_INTERVAL);
            }
        }, RECHECK_INTERVAL);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Monitoraggio stato di carica",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Servizio per monitorare lo stato di carica del dispositivo");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private NotificationCompat.Builder getNotificationBuilder(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Charging Monitor")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true); // La notifica è persistente
    }

    private android.app.Notification getNotification(String contentText) {
        return getNotificationBuilder(contentText).build();
    }


    private void showNotification(String desc) {
        NotificationCompat.Builder builder = getNotificationBuilder(desc);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(2, builder.build());
        }
    }
}
