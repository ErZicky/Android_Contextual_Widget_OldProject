package com.zetadev.locationwidget;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class LocationForegroundService extends Service {

    private static final String CHANNEL_ID = "LOCATION_SERVICE_CHANNEL";
    private static final long INTERVAL_MS = TimeUnit.MINUTES.toMillis(10); // tempo in minuti
    private Handler handler;
    private Runnable runnable;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                checkLocation();
                handler.postDelayed(this, INTERVAL_MS);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();
        startForeground(1, getNotification("I've started Monitoring Position for trigger"), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);

        // Inizia il ciclo di controllo della posizione
        handler.post(runnable);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Rimuovi i callback quando il servizio viene distrutto
        handler.removeCallbacks(runnable);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null; // Non supporta il binding
    }

    private NotificationCompat.Builder getNotificationBuilder(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Widget")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);
    }

    private android.app.Notification getNotification(String contentText) {
        return getNotificationBuilder(contentText).build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Location Service Channel";
            String description = "Canale per il servizio di monitoraggio posizione";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void checkLocation() {

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();

                // Leggi il file JSON e ottieni le posizioni
                JSONObject locationData = loadLocationData();
                if (locationData != null) {
                    try {
                        // Itera attraverso le coordinate dal file JSON
                        Iterator<String> keys = locationData.keys();
                        boolean withinRange = false;
                        ArrayList<String> associatedAppsList = new ArrayList<>();

                        while (keys.hasNext()) {
                            String key = keys.next();
                            String[] coords = key.split(", ");
                            double targetLatitude = Double.parseDouble(coords[0]);
                            double targetLongitude = Double.parseDouble(coords[1]);
                            double rangeInMeters = locationData.getJSONObject(key).getDouble("range");

                            // Calcola la distanza tra la posizione corrente e quella del JSON
                            float[] results = new float[1];
                            Location.distanceBetween(currentLatitude, currentLongitude, targetLatitude, targetLongitude, results);
                            float distanceInMeters = results[0];

                            Log.d("LocationService", "Distanza da " + key + ": " + distanceInMeters + " mt");

                            if (distanceInMeters <= rangeInMeters) {
                                Log.d("LocationService", "Entro il range di " + key);

                                // Ottieni le app associate a questa posizione
                                for (int i = 0; i < locationData.getJSONObject(key).getJSONArray("apps").length(); i++) {
                                    associatedAppsList.add(locationData.getJSONObject(key).getJSONArray("apps").getString(i));
                                }

                                withinRange = true;
                                break; // Una posizione valida trovata, non è necessario continuare
                            } else {
                                Log.d("LocationService", "Fuori dal range di " + key);
                            }
                        }

                        if (withinRange) {
                            // Converti l'ArrayList in un array
                            String[] associatedApps = new String[associatedAppsList.size()];
                            associatedApps = associatedAppsList.toArray(associatedApps);

                            // Aggiorna il widget
                            Intent updateIntent = new Intent(this, CustomWidget.class);
                            updateIntent.setAction("com.zetadev.locationwidget.ACTION_LOCATION_UPDATE");
                            updateIntent.putExtra("apps", associatedApps); // Aggiungi le app all'intento
                            sendBroadcast(updateIntent);
                            showNotification(" Foreground Controllo effettuato: posizione valida trovata");
                        } else {
                            showNotification(" Foreground Controllo effettuato: posizione non valida");
                        }

                    } catch (JSONException e) {
                        Log.e("LocationService", "Errore durante il parsing del JSON: " + e.getMessage());
                        showNotification("Foreground Controllo effettuato: errore con il JSON");
                    }
                }
            } else {
                Log.d("LocationService", "Nessuna posizione disponibile");
                showNotification("Foreground Controllo effettuato: nessuna posizione disponibile");
            }
        }).addOnFailureListener(e -> {
            Log.e("LocationService", "Errore nel recupero della posizione: " + e.getMessage());
            showNotification("Foreground Controllo effettuato: errore nel recupero della posizione");
        });
    }

    /**
     * Carica i dati di posizione dal file JSON.
     */
    private JSONObject loadLocationData() {
        try {
            // Specifica il percorso del file nella memoria interna
            File file = new File(getFilesDir(), "triggers.json");

            if (!file.exists()) {
                Log.e("LocationService", "File triggers.json non trovato nella memoria interna.");
                return null;
            }

            // Legge il contenuto del file
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();

            // Converte la stringa JSON in un oggetto JSONObject
            JSONObject jsonObject = new JSONObject(sb.toString());
            return jsonObject.getJSONObject("location"); // Ottieni solo la sezione delle location
        } catch (Exception e) {
            Log.e("LocationService", "Errore durante il caricamento del file JSON: " + e.getMessage());
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
}

