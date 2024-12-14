package com.zetadev.locationwidget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TriggerCreationActivity extends AppCompatActivity implements AppAdapter.OnAppSelectionListener {

    RecyclerView appRecyclerView;
    Button buttonCreation;
    AppAdapter ap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trigger_creation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView textView = findViewById(R.id.TriggerText);
        appRecyclerView = findViewById(R.id.appsRecyclerView);
        buttonCreation = findViewById(R.id.buttonCreateTrigger);
        ImageView  typeic = findViewById(R.id.TriggerTypeIcon);





        // Ottieni il valore String e int dai rispettivi extra
        String TriggerType = getIntent().getStringExtra("STRING_TYPE");
        int  radius = getIntent().getIntExtra("RADIUS_VALUE", 50);
        String DeviceName = getIntent().getStringExtra("STRING_DEVICE"); //o wifiname
         typeic.setImageResource(DecideIcon(TriggerType));
        AnimateImageview(typeic);
        textView.setText(TriggerType +DeviceName);




        PopulateAppList();

        buttonCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ResolveInfo> selectedApps = ap.getSelectedApps();


                if(TriggerType.equals("bluetooth"))
                {
                    saveBluethoothTriggerToFile(DeviceName, selectedApps);
                }
                else if(TriggerType.equals("WI-FI"))
                {
                    saveWifiTriggerToFile(DeviceName, selectedApps);
                }
                else if(TriggerType.equals("charging"))
                {
                    saveChargingTriggerToFile(selectedApps);
                }
                else if(TriggerType.equals("incall"))
                {
                    saveCallTriggerToFile(selectedApps);
                }
                else if(TriggerType.equals("CLOCK"))
                {
                    saveClockTriggerToFile(DeviceName, selectedApps);
                }
                else if(TriggerType.equals("location"))
                {

                    String[] cords = DeviceName.split(",");

                    LatLng location = new LatLng(Double.parseDouble(cords[0]), Double.parseDouble(cords[1]));



                    saveLocationTriggerToFile(location, radius ,selectedApps);
                }


            }
        });
    }



    private void AnimateImageview(View ic)
    {
        // Animazioni di ingrandimento e rimpicciolimento
        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(ic, "scaleX", 1f, 1.8f);
        scaleXUp.setDuration(300);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(ic, "scaleY", 1f, 1.8f);
        scaleYUp.setDuration(300);

        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(ic, "scaleX", 1.8f, 1f);
        scaleXDown.setDuration(200);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(ic, "scaleY", 1.8f, 1f);
        scaleYDown.setDuration(200);

        // Rimbalzi
        ObjectAnimator bounceX = ObjectAnimator.ofFloat(ic, "scaleX", 1f, 1.1f, 0.9f, 1f);
        bounceX.setDuration(400);
        ObjectAnimator bounceY = ObjectAnimator.ofFloat(ic, "scaleY", 1f, 1.1f, 0.9f, 1f);
        bounceY.setDuration(400);

        // Creiamo un AnimatorSet per gestire le sequenze
        AnimatorSet pulseAnimation = new AnimatorSet();
        pulseAnimation.play(scaleXUp).with(scaleYUp); // Primo step di ingrandimento
        pulseAnimation.play(scaleXDown).with(scaleYDown).after(scaleXUp); // Torna alla dimensione originale
        pulseAnimation.play(bounceX).with(bounceY).after(scaleXDown); // Aggiungi i rimbalzi dopo il ritorno alla dimensione originale

        // Avvia l'animazione all'apertura dell'activity
        pulseAnimation.start();
    }

    public int DecideIcon(String triggertype) //TODO aggiungere clock
    {
        if (triggertype != null) {
            switch (triggertype) {

                case "charging":
                    return R.drawable.bolt_48px;
                case "incall":
                    return R.drawable.call_48px;
                case "bluetooth":
                    return R.drawable.bluetooth_48px;
                case "WI-FI":
                    return R.drawable.wifi_48px;
                case"location":
                    return R.drawable.location_on_48px;
                default:

                    return R.drawable.android_48px;
            }
        } else {
            return R.drawable.android_48px;
        }
    }



    public List<ResolveInfo> getInstalledApps() {

        // Ottieni la lista delle app installate
        PackageManager pm = this.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedApps = pm.queryIntentActivities(mainIntent, 0);


        return installedApps;
    }


    private void PopulateAppList()
    {
        appRecyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 colonne

        // Ottieni la lista delle app installate
        List<ResolveInfo> installedApps = getInstalledApps();

        // Crea l'adapter e impostalo nella RecyclerView
        ap = new AppAdapter(installedApps, this, this);
        appRecyclerView.setAdapter(ap);
    }

    @Override
    public void onAppSelected(List<ResolveInfo> selectedApps) {
        // Questo metodo viene chiamato quando l'utente seleziona o deseleziona un'app
        if (selectedApps.size() > 0) {

            buttonCreation.setVisibility(Button.VISIBLE);
        }
        else
        {
            buttonCreation.setVisibility(Button.GONE);
        }
    }





    private void saveLocationTriggerToFile( LatLng location, int range ,List<ResolveInfo> selectedApps) {
        Map<String, Object> triggerData;

        // Prova a leggere il file esistente
        try (FileInputStream fis = openFileInput("triggers.json")) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            String jsonStr = new String(buffer);

            // Deserializza il contenuto del file JSON
            Gson gson = new Gson();
            triggerData = gson.fromJson(jsonStr, Map.class);

            if (triggerData == null) {
                triggerData = new HashMap<>();
            }

            // Assicurati che ci sia una chiave "bluetooth"
            if (!triggerData.containsKey("location")) {
                triggerData.put("location", new HashMap<String, Object>());
            }
        } catch (IOException e) {
            // Se il file non esiste o non può essere letto, crea una nuova mappa
            triggerData = new HashMap<>();
            triggerData.put("location", new HashMap<String, Object>());
        }

        // Ottieni la mappa per i dispositivi Bluetooth
        Map<String, Object> locationData = (Map<String, Object>) triggerData.get("location");

        // Crea i dati per il nuovo trigger
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("apps", selectedApps.stream().map(app -> app.activityInfo.packageName).toArray());
        String rangeToPut = String.valueOf(range);
     //   rangeToPut.replace(".0","");
        deviceData.put("range", range);
        // Aggiungi o sovrascrivi il trigger esistente per il deviceName
        locationData.put(location.latitude +", " + location.longitude, deviceData);

        // Serializza l'oggetto JSON aggiornato
        Gson gson = new Gson();
        String json = gson.toJson(triggerData);

        // Salva il file JSON nello storage interno
        try (FileOutputStream fos = openFileOutput("triggers.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
           // StartMonitoringLocation();
            startLocationService();
            Toast.makeText(this, "Trigger saved", Toast.LENGTH_SHORT).show();
            FinishActivity();
        } catch (IOException e) {
            Log.e("TriggerCreationActivity", "Error saving trigger", e);
            Toast.makeText(this, "Error saving trigger", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveWifiTriggerToFile(String network, List<ResolveInfo> selectedApps) {
        Map<String, Object> triggerData;

        // Prova a leggere il file esistente
        try (FileInputStream fis = openFileInput("triggers.json")) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            String jsonStr = new String(buffer);

            // Deserializza il contenuto del file JSON
            Gson gson = new Gson();
            triggerData = gson.fromJson(jsonStr, Map.class);

            if (triggerData == null) {
                triggerData = new HashMap<>();
            }

            // Assicurati che ci sia una chiave "bluetooth"
            if (!triggerData.containsKey("WI-FI")) {
                triggerData.put("WI-FI", new HashMap<String, Object>());
            }
        } catch (IOException e) {
            // Se il file non esiste o non può essere letto, crea una nuova mappa
            triggerData = new HashMap<>();
            triggerData.put("WI-FI", new HashMap<String, Object>());
        }

        // Ottieni la mappa per i dispositivi Bluetooth
        Map<String, Object> bluetoothData = (Map<String, Object>) triggerData.get("WI-FI");

        // Crea i dati per il nuovo trigger
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("apps", selectedApps.stream().map(app -> app.activityInfo.packageName).toArray());

        // Aggiungi o sovrascrivi il trigger esistente per il deviceName
        bluetoothData.put(network, deviceData);

        // Serializza l'oggetto JSON aggiornato
        Gson gson = new Gson();
        String json = gson.toJson(triggerData);

        // Salva il file JSON nello storage interno
        try (FileOutputStream fos = openFileOutput("triggers.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
            Toast.makeText(this, "Trigger saved", Toast.LENGTH_SHORT).show();
            StartMonitoringWifi();
            FinishActivity();
        } catch (IOException e) {
            Log.e("TriggerCreationActivity", "Error saving trigger", e);
            Toast.makeText(this, "Error saving trigger", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveChargingTriggerToFile(List<ResolveInfo> selectedApps) {
        Map<String, Object> triggerData;

        // Prova a leggere il file esistente
        try (FileInputStream fis = openFileInput("triggers.json")) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            String jsonStr = new String(buffer);

            // Deserializza il contenuto del file JSON
            Gson gson = new Gson();
            triggerData = gson.fromJson(jsonStr, Map.class);

            if (triggerData == null) {
                triggerData = new HashMap<>();
            }

            // Assicurati che ci sia una chiave "charging"
            if (!triggerData.containsKey("charging")) {
                triggerData.put("charging", new HashMap<String, Object>());
            }
        } catch (IOException e) {
            // Se il file non esiste o non può essere letto, crea una nuova mappa
            triggerData = new HashMap<>();
            triggerData.put("charging", new HashMap<String, Object>());
        }

        // Ottieni la mappa per "charging"
        Map<String, Object> chargingData = (Map<String, Object>) triggerData.get("charging");

        // Crea i dati per il trigger di charging
        Map<String, Object> triggerInfo = new HashMap<>();
        triggerInfo.put("apps", selectedApps.stream().map(app -> app.activityInfo.packageName).toArray());

        // Aggiungi o sovrascrivi i dati sotto "charging"
        triggerData.put("charging", triggerInfo); // Aggiungi direttamente sotto "charging"

        // Serializza l'oggetto JSON aggiornato
        Gson gson = new Gson();
        String json = gson.toJson(triggerData);

        // Salva il file JSON nello storage interno
        try (FileOutputStream fos = openFileOutput("triggers.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
            Toast.makeText(this, "Charging trigger saved", Toast.LENGTH_SHORT).show();


            // Inizializza e registra il receiver per lo stato della batteria
        //    ChargingMonitor  cm= new ChargingMonitor();
          //  IntentFilter filter = new IntentFilter();
         //   filter.addAction(Intent.ACTION_POWER_CONNECTED);
         //   filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        //    registerReceiver(cm, filter);




            Intent serviceIntent = new Intent(this, ChargingStartingService.class);
            ContextCompat.startForegroundService(this, serviceIntent);

          FinishActivity();


        } catch (IOException e) {
            Log.e("TriggerCreationActivity", "Error saving charging trigger", e);
            Toast.makeText(this, "Error saving charging trigger", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCallTriggerToFile(List<ResolveInfo> selectedApps) {
        Map<String, Object> triggerData;

        // Prova a leggere il file esistente
        try (FileInputStream fis = openFileInput("triggers.json")) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            String jsonStr = new String(buffer);

            // Deserializza il contenuto del file JSON
            Gson gson = new Gson();
            triggerData = gson.fromJson(jsonStr, Map.class);

            if (triggerData == null) {
                triggerData = new HashMap<>();
            }

            // Assicurati che ci sia una chiave "charging"
            if (!triggerData.containsKey("incall")) {
                triggerData.put("incall", new HashMap<String, Object>());
            }
        } catch (IOException e) {
            // Se il file non esiste o non può essere letto, crea una nuova mappa
            triggerData = new HashMap<>();
            triggerData.put("incall", new HashMap<String, Object>());
        }

        // Ottieni la mappa per "charging"
        Map<String, Object> chargingData = (Map<String, Object>) triggerData.get("charging");

        // Crea i dati per il trigger di charging
        Map<String, Object> triggerInfo = new HashMap<>();
        triggerInfo.put("apps", selectedApps.stream().map(app -> app.activityInfo.packageName).toArray());

        // Aggiungi o sovrascrivi i dati sotto "charging"
        triggerData.put("incall", triggerInfo); // Aggiungi direttamente sotto "charging"

        // Serializza l'oggetto JSON aggiornato
        Gson gson = new Gson();
        String json = gson.toJson(triggerData);

        // Salva il file JSON nello storage interno
        try (FileOutputStream fos = openFileOutput("triggers.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
            Toast.makeText(this, "Calling  trigger saved", Toast.LENGTH_SHORT).show();

            FinishActivity();

        } catch (IOException e) {

            Toast.makeText(this, "Error saving calling trigger", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveBluethoothTriggerToFile(String deviceName, List<ResolveInfo> selectedApps) {
        Map<String, Object> triggerData;

        // Prova a leggere il file esistente
        try (FileInputStream fis = openFileInput("triggers.json")) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            String jsonStr = new String(buffer);

            // Deserializza il contenuto del file JSON
            Gson gson = new Gson();
            triggerData = gson.fromJson(jsonStr, Map.class);

            if (triggerData == null) {
                triggerData = new HashMap<>();
            }

            // Assicurati che ci sia una chiave "bluetooth"
            if (!triggerData.containsKey("bluetooth")) {
                triggerData.put("bluetooth", new HashMap<String, Object>());
            }
        } catch (IOException e) {
            // Se il file non esiste o non può essere letto, crea una nuova mappa
            triggerData = new HashMap<>();
            triggerData.put("bluetooth", new HashMap<String, Object>());
        }

        // Ottieni la mappa per i dispositivi Bluetooth
        Map<String, Object> bluetoothData = (Map<String, Object>) triggerData.get("bluetooth");

        // Crea i dati per il nuovo trigger
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("apps", selectedApps.stream().map(app -> app.activityInfo.packageName).toArray());

        // Aggiungi o sovrascrivi il trigger esistente per il deviceName
        bluetoothData.put(deviceName, deviceData);

        // Serializza l'oggetto JSON aggiornato
        Gson gson = new Gson();
        String json = gson.toJson(triggerData);

        // Salva il file JSON nello storage interno
        try (FileOutputStream fos = openFileOutput("triggers.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
            Toast.makeText(this, "Trigger saved", Toast.LENGTH_SHORT).show();
            FinishActivity();
        } catch (IOException e) {
            Log.e("TriggerCreationActivity", "Error saving trigger", e);
            Toast.makeText(this, "Error saving trigger", Toast.LENGTH_SHORT).show();
        }
    }



    private void saveClockTriggerToFile(String Time, List<ResolveInfo> selectedApps) {
        Map<String, Object> triggerData;

        // Prova a leggere il file esistente
        try (FileInputStream fis = openFileInput("triggers.json")) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            String jsonStr = new String(buffer);

            // Deserializza il contenuto del file JSON
            Gson gson = new Gson();
            triggerData = gson.fromJson(jsonStr, Map.class);

            if (triggerData == null) {
                triggerData = new HashMap<>();
            }

            // Assicurati che ci sia una chiave "bluetooth"
            if (!triggerData.containsKey("CLOCK")) {
                triggerData.put("CLOCK", new HashMap<String, Object>());
            }
        } catch (IOException e) {
            // Se il file non esiste o non può essere letto, crea una nuova mappa
            triggerData = new HashMap<>();
            triggerData.put("CLOCK", new HashMap<String, Object>());
        }

        // Ottieni la mappa per i dispositivi Bluetooth
        Map<String, Object> bluetoothData = (Map<String, Object>) triggerData.get("CLOCK");

        // Crea i dati per il nuovo trigger
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("apps", selectedApps.stream().map(app -> app.activityInfo.packageName).toArray());

        // Aggiungi o sovrascrivi il trigger esistente per il deviceName
        bluetoothData.put(Time, deviceData);

        // Serializza l'oggetto JSON aggiornato
        Gson gson = new Gson();
        String json = gson.toJson(triggerData);

        // Salva il file JSON nello storage interno
        try (FileOutputStream fos = openFileOutput("triggers.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
            Toast.makeText(this, "Trigger saved", Toast.LENGTH_SHORT).show();
            FinishActivity();
        } catch (IOException e) {
            Log.e("TriggerCreationActivity", "Error saving trigger", e);
            Toast.makeText(this, "Error saving trigger", Toast.LENGTH_SHORT).show();
        }
    }




    private void FinishActivity()
    {
        //this.finish();

        Intent intent = new Intent(TriggerCreationActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        //FLAG_ACTIVITY_CLEAR_TOP: Rimuove tutte le attività sopra MainActivity.
        //FLAG_ACTIVITY_NEW_TASK: Garantisce che l'attività Main sia avviata in una nuova task se necessario.
        startActivity(intent);
        finish();


    }


    public void StartMonitoringWifi()
    {

     /*   Log.d("agg","start monitoringwifi");
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


    public void StartMonitoringLocation()
    {

        Log.d("agg","start monitoring location");
        // Pianifica il Worker per aggiornare la posizione periodicamente
        PeriodicWorkRequest wifiWorkRequest = new PeriodicWorkRequest.Builder(
                LocationUpdateWorker.class,
                15,
                TimeUnit.MINUTES)
                .build();


        WorkManager.getInstance(this).enqueue(wifiWorkRequest);
    }

    public void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationForegroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }




}