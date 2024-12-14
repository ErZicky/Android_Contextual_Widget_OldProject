package com.zetadev.locationwidget;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

public class LocationUpdateWorker extends Worker {

   // 43.93664512277054, 12.446574624653948 palazzo di san marino
    //45.92518516278744, 12.739332176145107
  // private static final int PERMISSION_REQUEST_LOCATION = 100;

    //private static final double TARGET_LATITUDE = 75.92518516278744;  // Latitudine target
    //private static final double TARGET_LONGITUDE = 12.739332176145107; // Longitudine target
    //private static final float RADIUS_IN_MT = 100.0f;  // Raggio in metri
   private FusedLocationProviderClient fusedLocationClient;

    public LocationUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Agg", "I miss permissions");
            return Result.failure();
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double currentLatitude = location.getLatitude();
                    double currentLongitude = location.getLongitude();

                    // Leggi il file JSON e ottieni le posizioni
                    JSONObject locationData = loadLocationData();
                    if (locationData != null) {
                        try {
                            // Itera attraverso le coordinate dal file JSON
                            Iterator<String> keys = locationData.keys();
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

                                Log.d("agg", "Distanza da " + key + ": " + distanceInMeters + " mt");

                                if (distanceInMeters <= rangeInMeters) {
                                    Log.d("agg", "Entro il range di " + key);

                                    // Ottieni le app associate a questa posizione
                                    ArrayList<String> associatedAppsList = new ArrayList<>();
                                    for (int i = 0; i < locationData.getJSONObject(key).getJSONArray("apps").length(); i++) {
                                        associatedAppsList.add(locationData.getJSONObject(key).getJSONArray("apps").getString(i));
                                    }

                                    // Converti l'ArrayList in un array
                                    String[] associatedApps = new String[associatedAppsList.size()];
                                    associatedApps = associatedAppsList.toArray(associatedApps);

                                    // Aggiorna il widget
                                    Intent updateIntent = new Intent(getApplicationContext(), CustomWidget.class);
                                    updateIntent.setAction("com.zetadev.locationwidget.ACTION_LOCATION_UPDATE");
                                    updateIntent.putExtra("apps", associatedApps); // Aggiungi le app all'intento
                                    getApplicationContext().sendBroadcast(updateIntent);
                                    showNotification(getApplicationContext(), "Controllo effettuato posizione valida trovata");
                                    break; // Una posizione valida trovata, non è necessario continuare
                                }else {
                                    Log.d("agg", "Fuori dal range di " + key);
                                    showNotification(getApplicationContext(), "Controllo effettuato posizione non valida");
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("agg", "Errore durante il parsing del JSON: " + e.getMessage());
                            showNotification(getApplicationContext(), "Controllo effettuato errore con il json");
                        }
                    }
                }
            }
        });


        return Result.success();
    }

    /**
     * Carica i dati di posizione dal file JSON.
     */
    private JSONObject loadLocationData() {
        try {
            // Specifica il percorso del file nella memoria interna
            File file = new File(getApplicationContext().getFilesDir(), "triggers.json");

            if (!file.exists()) {
                Log.e("agg", "File triggers.json non trovato nella memoria interna.");
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
            Log.e("agg", "Errore durante il caricamento del file JSON: " + e.getMessage());
            return null;
        }
    }


    @SuppressLint("MissingPermission")
    private void showNotification(Context context, String Desc) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "TEST_NOT")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Location Check")
                .setContentText(Desc)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(Desc))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel_name";
            String description = "HO appena controllato la posizione";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("TEST_NOT", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(100, mBuilder.build());
    }
}

