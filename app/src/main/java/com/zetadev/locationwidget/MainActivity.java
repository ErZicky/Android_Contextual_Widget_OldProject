package com.zetadev.locationwidget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.utilities.DynamicColor;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    private static final int PERMISSION_REQUEST_LOCATION = 100;
    private static final int PERMISSION_REQUEST_BLUETHOOTH = 100;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    RecyclerView carouselRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View bluebutton = findViewById(R.id.bluethtrigg);
        View wifibutton = findViewById(R.id.wifitrigg);
        View chargingbutton = findViewById(R.id.chargtrigg);
        View callbutton = findViewById(R.id.calltrigg);
        View locationbutton = findViewById(R.id.loctrigg);
        carouselRecyclerView = findViewById(R.id.carousel_recycler_view);


     //   WorkManager.getInstance(this).cancelAllWork();


        // Controlla e richiedi i permessi
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            checkLocationPermission();
            checkBackgroundLocationPermission();


        } else {
            //         StartMonitoring(); //position monitoring
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                CheckBluethootPermissions();
            }

        }







        bluebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndEnableBluetooth();
            }
        });

        wifibutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndEnableWifi();
            }
        });


        chargingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, TriggerCreationActivity.class);
                String type = "charging";
                String deviceName = "";
                i.putExtra("STRING_TYPE", type);
                i.putExtra("STRING_DEVICE", deviceName);
                startActivity(i);

            }
        });

        callbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, TriggerCreationActivity.class);
                String type = "incall";
                String deviceName = "";
                i.putExtra("STRING_TYPE", type);
                i.putExtra("STRING_DEVICE", deviceName);
                startActivity(i);

            }
        });

        locationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // CreateLocationDialog();

                Intent i = new Intent(MainActivity.this, PositionTriggerCreation.class);
                startActivity(i);

            }
        });



      checkPhoneStatePermission();



//check workers

  /*      PeriodicWorkRequest wifiWorkRequest = new PeriodicWorkRequest.Builder(
                WifiMonitor.class,
                15,
                TimeUnit.MINUTES)
                .build();


        WorkManager.getInstance(this).enqueue(wifiWorkRequest);*/



    }



    private void CreateCarousel()
    {
        /**carousel adapter*/


        ArrayList<Map<String, String>> arrayList = null;
        try {
            arrayList = FetchCurrentTriggers();
        } catch (JSONException e) {
            Toast.makeText(this, "Error in fetching current triggers", Toast.LENGTH_SHORT).show();
        }




        if(arrayList != null && !arrayList.isEmpty())
        {
            CarouselLayoutManager gridLayoutManager = new CarouselLayoutManager();
            CarouselAdapter adapter = new CarouselAdapter(MainActivity.this, arrayList);
            carouselRecyclerView.setLayoutManager(gridLayoutManager);

            LinearSnapHelper snapHelper = new LinearSnapHelper();

            if (carouselRecyclerView.getOnFlingListener() != null) {
                carouselRecyclerView.setOnFlingListener(null);
            }

            snapHelper.attachToRecyclerView(carouselRecyclerView);

            carouselRecyclerView.setAdapter(adapter);


            adapter.setOnItemClickListener(new CarouselAdapter.OnItemClickListener() {
                @Override
                public void onClick(String type, String secondary) {



                    Intent i = new Intent(MainActivity.this, TriggerDetails.class);
                    i.putExtra("triggerType", type);
                    i.putExtra("secondValue", secondary);
                    startActivity(i);
                }
            });
        }
        else
        {
            arrayList = new ArrayList<>();
            Map<String, String> map = new HashMap<>();
            map.put("triggerType", "null");
            map.put("secondValue", "null");
            arrayList.add(map);
            CarouselLayoutManager gridLayoutManager = new CarouselLayoutManager();
            CarouselAdapter adapter = new CarouselAdapter(MainActivity.this, arrayList);
            carouselRecyclerView.setLayoutManager(gridLayoutManager);


            LinearSnapHelper snapHelper = new LinearSnapHelper();

            if (carouselRecyclerView.getOnFlingListener() != null) {
                carouselRecyclerView.setOnFlingListener(null);
            }

            snapHelper.attachToRecyclerView(carouselRecyclerView);

            carouselRecyclerView.setAdapter(adapter);
        }





        /****/

    }


    private void CheckBluethootPermissions()
    {
        // Controlla il permesso di ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {

            // Se non è stato concesso, richiedi il permesso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_LOCATION);
        } else {
            // Il permesso è già stato concesso, puoi iniziare a monitorare la posizione

        }
    }




    private void checkLocationPermission() {
        // Controlla il permesso di ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Se non è stato concesso, richiedi il permesso
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        } else {
            // Il permesso è già stato concesso, puoi iniziare a monitorare la posizione
          //  StartMonitoring();
        }
    }


    private void checkPhoneStatePermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Se non è stato concesso, richiedi il permesso
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_PHONE_STATE}, 100);
        } else {
            // Il permesso è già stato concesso, puoi iniziare a monitorare la posizione
            //  StartMonitoring();
        }
    }

    private void checkBackgroundLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Richiedi il permesso di ACCESS_BACKGROUND_LOCATION
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_LOCATION);
        } else {
            // Il permesso di background è già stato concesso
         //   StartMonitoring();
        }
    }


    // Gestisci la richiesta di permesso
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permesso concesso
                //StartMonitoring();
            } else {
                // Permesso negato
                // Mostra un messaggio all'utente o disabilita le funzionalità che dipendono dalla posizione
                Toast.makeText(this, "I need this permission", Toast.LENGTH_SHORT).show();
            }
        }
    }



    public void StartMonitoring() //posizione
    {

        Log.d("agg","start monitoring");
        // Pianifica il Worker per aggiornare la posizione periodicamente
        PeriodicWorkRequest locationWorkRequest = new PeriodicWorkRequest.Builder(
                LocationUpdateWorker.class,
                15,
                TimeUnit.MINUTES)
                .build();


        WorkManager.getInstance(this).enqueue(locationWorkRequest);
    }




    private void checkAndEnableBluetooth() {
        // Se il Bluetooth è disabilitato, chiedi all'utente di attivarlo
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth non supportato", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activityResultLauncher.launch(intent);
        } else {
            // Se il Bluetooth è già abilitato, mostra il dialog
           CreateBlueDialog();
        }
    }


    private void checkAndEnableWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);

        if (wifiManager == null) {
            Toast.makeText(this, "Wi-Fi non supportato", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!wifiManager.isWifiEnabled()) {
            // Abilita il Wi-Fi
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);
        } else {
           checkAndEnableGps();
        }
    }

    private void checkAndEnableGps() {


        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager == null) {
            Toast.makeText(this, "Servizi di localizzazione non supportati", Toast.LENGTH_SHORT).show();
            return;
        }

        // Controlla se i servizi di localizzazione sono abilitati
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled && !isNetworkEnabled) {
            // Abilita la geolocalizzazione
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else {
            // Se la geolocalizzazione è abilitata, chiama il metodo per mostrare le reti Wi-Fi
            CreateWifiDialog();
        }

    }



    @SuppressLint("MissingPermission")
    private void CreateBlueDialog()
    {

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> deviceList = new ArrayList<>(pairedDevices);





        // Crea il dialog utilizzando il Material 3 design
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        // Infla il layout personalizzato della dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.bluethdialog, null);
        builder.setView(dialogView);

        // Inizializza la RecyclerView e l'Adapter
        RecyclerView recyclerView = dialogView.findViewById(R.id.bluetoothRecyclerView);


        // Imposta il GridLayoutManager per avere 2 dispositivi per riga
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);


        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        final AlertDialog create =  builder.show();;
        BluethdialogAdapter adapter = new BluethdialogAdapter(deviceList, getApplicationContext(), device -> {
            // Aggiungi la tua logica quando un dispositivo viene selezionato
          //  Toast.makeText(this, "Dispositivo selezionato: " + device.getName(), Toast.LENGTH_SHORT).show();

            create.dismiss();
            Intent i = new Intent(MainActivity.this, TriggerCreationActivity.class);
            String type = "bluetooth";
            String deviceName = device.getName();
            i.putExtra("STRING_TYPE", type);
            i.putExtra("STRING_DEVICE", deviceName);
            startActivity(i);


        });
        recyclerView.setAdapter(adapter);



    }

    @SuppressLint("MissingPermission")
    private void CreateWifiDialog()
    {

        // Inizializza il WifiManager
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);


        // Effettua una scansione delle reti Wi-Fi disponibili
        List<ScanResult> wifiList = wifiManager.getScanResults();





        // Crea il dialog utilizzando il Material 3 design
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        // Infla il layout personalizzato della dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.wifidialog, null);
        builder.setView(dialogView);

        // Inizializza la RecyclerView e l'Adapter
        RecyclerView recyclerView = dialogView.findViewById(R.id.bluetoothRecyclerView);


        // Imposta il GridLayoutManager per avere 2 dispositivi per riga
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);


        // Mostra il dialog
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog_ = builder.show();
        WifidialogAdapter adapter = new WifidialogAdapter(wifiList, getApplicationContext(), device -> {
            // Aggiungi la tua logica quando un dispositivo viene selezionato
            //  Toast.makeText(this, "Dispositivo selezionato: " + device.getName(), Toast.LENGTH_SHORT).show();
            dialog_.dismiss();
            Intent i = new Intent(MainActivity.this, TriggerCreationActivity.class);
            String type = "WI-FI";
            String deviceName = device.SSID;
            i.putExtra("STRING_TYPE", type);
            i.putExtra("STRING_DEVICE", deviceName);
            startActivity(i);


        });
        recyclerView.setAdapter(adapter);



    }



    private void CreateLocationDialog()
    {




        // Crea il dialog utilizzando il Material 3 design
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        // Infla il layout personalizzato della dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.locationdialog, null);
        builder.setView(dialogView);


        EditText addresstext =  (EditText) dialogView.findViewById(R.id.editTextTextPostalAddress);





        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {



               LatLng convertedaddress =  AddressHelper.getCoordinatesFromAddress(MainActivity.this, addresstext.getText().toString());

                Log.d("agg", convertedaddress.latitude + " " + convertedaddress.longitude);
                dialog.dismiss();
            }
        });

        // Mostra il dialog
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }




    public  ArrayList<Map<String, String>> FetchCurrentTriggers() throws JSONException {
        // Convertiamo il JSON in un oggetto JSONObject

        String jsonstring = readJsonFile(this);

        if(jsonstring != null)
        {
            // Convertiamo il JSON in un oggetto JSONObject
            JSONObject jsonObject = new JSONObject(jsonstring);
            ArrayList<Map<String, String>> resultList = new ArrayList<>();

            // Iteriamo sulle chiavi dell'oggetto JSON
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();

                // Se il trigger è "charging" o "incall", aggiungiamo direttamente con "null"
                if (key.equals("charging") || key.equals("incall")) {
                    Map<String, String> map = new HashMap<>();
                    map.put("triggerType", key);
                    map.put("secondValue", "null");
                    resultList.add(map);
                }
                // Per "bluetooth", "wifi", o altri casi simili, processiamo i dispositivi
                else if (key.equals("bluetooth") || key.equals("WI-FI")|| key.equals("location")) {
                    JSONObject innerObject = jsonObject.getJSONObject(key);

                    // Iteriamo sulle chiavi interne (es. "E_Buds Pro", "Pixel 4a", ecc.)
                    Iterator<String> innerKeys = innerObject.keys();
                    while (innerKeys.hasNext()) {
                        String innerKey = innerKeys.next();
                        Map<String, String> map = new HashMap<>();
                        map.put("triggerType", key);
                        map.put("condition", innerKey);  // Adatta il formato del nome del dispositivo
                        resultList.add(map);
                    }
                }
            }

            return resultList;
        }
        else {
            return null;
        }


    }



    // Funzione per leggere il file JSON dalla memoria interna

    private String readJsonFile(Context context) {
        try (FileInputStream fis = context.openFileInput("triggers.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            return stringBuilder.toString();
        } catch (Exception e) {
            Log.e("BluethoothReceiver", "Error reading trigger file", e);
            return null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
       CreateCarousel();

    }



    /**results**/


    //risultato attivazione bluethooth
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.e("Activity result","OK");
                    // There are no request codes
                    CreateBlueDialog();
                }
                else
                {
                    Log.d("agg","I need blue");
                }
            });

}
