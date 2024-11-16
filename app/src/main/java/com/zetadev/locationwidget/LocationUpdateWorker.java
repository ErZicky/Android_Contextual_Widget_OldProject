package com.zetadev.locationwidget;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationUpdateWorker extends Worker {

   // 43.93664512277054, 12.446574624653948 palazzo di san marino
    //45.92518516278744, 12.739332176145107
   private static final int PERMISSION_REQUEST_LOCATION = 100;

    private static final double TARGET_LATITUDE = 75.92518516278744;  // Latitudine target
    private static final double TARGET_LONGITUDE = 12.739332176145107; // Longitudine target
    private static final float RADIUS_IN_MT = 100.0f;  // Raggio in metri
    private FusedLocationProviderClient fusedLocationClient;

    public LocationUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        // Ottieni la posizione dell'utente
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


         //   Toast.makeText(getApplicationContext(), "I miss permissions", Toast.LENGTH_SHORT).show();
            Log.d("Agg", "I miss permissions");


        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double currentLatitude = location.getLatitude();
                    double currentLongitude = location.getLongitude();

                    // Calcola la distanza in km dalla posizione target
                    float[] results = new float[1];
                    Location.distanceBetween(currentLatitude, currentLongitude, TARGET_LATITUDE, TARGET_LONGITUDE, results);
                    float distanceInMt = results[0];

                    Log.d("agg", "Distanza dalla posizione target: " + distanceInMt + " mt " +  currentLatitude +" " + currentLongitude);

                    // Se l'utente è entro il raggio, aggiorna il widget
                    if (distanceInMt <= RADIUS_IN_MT) {
                        Intent refreshIntent = new Intent(getApplicationContext(), CustomWidget.class);
                        refreshIntent.setAction("com.zetadev.locationwidget.ACTION_LOCATION_UPDATE");
                        getApplicationContext().sendBroadcast(refreshIntent);
                    }
                    else
                    {
                        Log.d("Agg", "too far");
                    }
                }
            }
        });

        return Result.success(); // Indica che il lavoro è stato completato correttamente
    }





}
