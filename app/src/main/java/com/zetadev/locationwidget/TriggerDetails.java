package com.zetadev.locationwidget;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TriggerDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView imageviewType;
    TextView textViewType;
    TextView SecondaryLabel;
    TextView secondarytrigger;
    TextView editLabel;
    TextView rangeLabel;
    TextView rangeValue;


    String TriggerType;
    String SubTrigger;
    String[] assApps;
    List<ResolveInfo> selectedApps_;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ImageView deleteIm;
    String rangeFetched;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trigger_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



         TriggerType = getIntent().getStringExtra("triggerType");
         SubTrigger = getIntent().getStringExtra("secondValue");


        recyclerView = findViewById(R.id.TriggerAppsRec);
        imageviewType = findViewById(R.id.imageViewType);
        textViewType = findViewById(R.id.textView3);
        SecondaryLabel = findViewById(R.id.textviewsecondary);
        secondarytrigger = findViewById(R.id.LabelSecondary);
        editLabel = findViewById(R.id.attachsub);
        deleteIm = findViewById(R.id.imageDelete);
        rangeLabel = findViewById(R.id.textviewRange);
        rangeValue = findViewById(R.id.LabelRange);


        assApps = FetchAppsForTrigger(TriggerType, SubTrigger);
        CreateAppList(assApps);
        imageviewType.setImageResource(DecideIcon(TriggerType));
        textViewType.setText(DecideString(TriggerType));
        ManageSubTrigger(SecondaryLabel, secondarytrigger, TriggerType, SubTrigger);
        editLabel.setPaintFlags(editLabel.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);






        secondarytrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DecidePopupToOpen();
            }
        });

        editLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAppPopUp();

            }
        });

        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAppPopUp();
            }
        });

        deleteIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteTriggerFromFile();
            }
        });

        rangeValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               CreateRangeDialog();
            }
        });

        AnimateImageview(imageviewType);

    }



    private void DecidePopupToOpen()
    {

            switch (TriggerType) {


                case "bluetooth":

                    CheckBluethootPermissions();
                    checkAndEnableBluetooth();



                    break;
                case "WI-FI":
                    break;
                case "location": //todo
                  break;


            }

    }


    private void CreateAppList(String[] associatedApps)
    {


        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        // Crea l'adapter e impostalo nella RecyclerView
        AppAdapterDetails  ap = new AppAdapterDetails(Arrays.asList(associatedApps), this, null);
        recyclerView.setAdapter(ap);
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
                case "location":
                        return  R.drawable.location_on_48px;
                case "null":
                    return R.drawable.person_cancel_48px;

                default:

                    return R.drawable.android_48px;
            }
        } else {
            return R.drawable.android_48px;
        }
    }


    public String DecideString(String triggertype) //TODO aggiungere clock
    {
        if (triggertype != null) {
            switch (triggertype) {

                case "charging":
                    return "Charging trigger";
                case "incall":
                    return "Phone call trigger";
                case "bluetooth":
                    return "Bluetooth trigger";
                case "WI-FI":
                    return "WI-FI trigger";
                case "location":
                    return  "Location trigger";
                case "null":
                    return "Currently there are no active triggers";
                default:

                    return "Trigger";
            }
        } else {
            return "Trigger";
        }
    }

    public void ManageSubTrigger(TextView SecondLabel, TextView SubTrigger, String triggertype, String secondkey)
    {

        if (triggertype != null) {
            switch (triggertype) {

                case "charging":
                    SecondLabel.setVisibility(View.GONE);
                    SubTrigger.setVisibility(View.GONE);
                    break;
                case "incall":
                    SecondLabel.setVisibility(View.GONE);
                    SubTrigger.setVisibility(View.GONE);
                    break;
                case "bluetooth":
                    SecondLabel.setText("Device:");
                    SubTrigger.setText(secondkey.toString());
                    SubTrigger.setPaintFlags(SubTrigger.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

                    break;
                case "WI-FI":
                    SecondLabel.setText("Network:");
                    SubTrigger.setText(secondkey.toString());
                    SubTrigger.setPaintFlags(SubTrigger.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
                    break;
                case "location":
                    SecondLabel.setText("Location:");

                    String[] cords = secondkey.split(",");

                    String convertedcoordinates =  AddressHelper.getAddressFromCoordinates(this, Double.parseDouble(cords[0]), Double.parseDouble(cords[1]));

                    SubTrigger.setText(convertedcoordinates);
                    SubTrigger.setPaintFlags(SubTrigger.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
                    rangeValue.setVisibility(View.VISIBLE);
                    rangeLabel.setVisibility(View.VISIBLE);
                    rangeFetched = FetchRangeForLocationTrigger(TriggerType, secondkey);
                    rangeValue.setText(rangeFetched+ " mt");
                    rangeValue.setPaintFlags(rangeValue.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

                    break;

                default:

                   break;
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
            Log.e("Agg", "Error reading trigger file", e);
            return null;
        }
    }



    private String[] FetchAppsForTrigger(String primary, String Secondary)
    {
            Map<String, Object> triggerData = readTriggerFromFile(this);
            if(Secondary != null)
            {


                // Leggi il file JSON

                if (triggerData != null && triggerData.containsKey(primary)) {
                    Map<String, Object> bluetoothTriggers = (Map<String, Object>) triggerData.get(primary);

                    // Controlla se il dispositivo connesso è presente nel file
                    if (bluetoothTriggers.containsKey(Secondary)) {
                        Log.d("agg", "Trigger found for subtrigger: " + Secondary);
                        Map<String, Object> deviceData = (Map<String, Object>) bluetoothTriggers.get(Secondary);

                        // Passa le app associate al widget (esempio)
                        List<String> associatedAppsList = (List<String>) deviceData.get("apps");
                        String[] associatedApps = new String[associatedAppsList.size()];
                        associatedApps = associatedAppsList.toArray(associatedApps); // Converte l'ArrayList in un array di stringhe
                        return  associatedApps;
                    } else {
                        Log.d("agg", "No trigger found for subtrigger: " + secondarytrigger);
                        return null;
                    }
                }
                else
                {
                    return null;
                }
            }
            else
            {
                if (triggerData != null && triggerData.containsKey(primary)) {
                    Map<String, Object> Triggers = (Map<String, Object>) triggerData.get(primary);





                    List<String> associatedAppsList = (List<String>) Triggers.get("apps");
                    String[] associatedApps = new String[associatedAppsList.size()];
                    associatedApps = associatedAppsList.toArray(associatedApps); // Converte l'ArrayList in un array di stringhe
                    return  associatedApps;




                }
                else {
                    return null;
                }
            }
    }


    private String FetchRangeForLocationTrigger(String primary, String Secondary)
    {
        Map<String, Object> triggerData = readTriggerFromFile(this);
        if(Secondary != null)
        {


            // Leggi il file JSON

            if (triggerData != null && triggerData.containsKey(primary)) {
                Map<String, Object> bluetoothTriggers = (Map<String, Object>) triggerData.get(primary);

                // Controlla se il dispositivo connesso è presente nel file
                if (bluetoothTriggers.containsKey(Secondary)) {
                    Log.d("agg", "Trigger found for subtrigger: " + Secondary);
                    Map<String, Object> deviceData = (Map<String, Object>) bluetoothTriggers.get(Secondary);

                    // Passa le app associate al widget (esempio)
                    String range = deviceData.get("range").toString();
                    return  range.replace(".0", "");
                } else {
                    Log.d("agg", "No trigger found for subtrigger: " + secondarytrigger);
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }




    //dialogs
    @SuppressLint("MissingPermission")
    private void CreateBlueDialog(String currentkey, String[] apps)
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
            String deviceName = device.getName();

            saveBluetoothTriggerToFile( currentkey,deviceName, apps);



        });
        recyclerView.setAdapter(adapter);



    }

   /* private void CreateWifiDialog()
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



    }*/


    private void CreateRangeDialog()
    {




        // Crea il dialog utilizzando il Material 3 design
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        // Infla il layout personalizzato della dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.rangedialog, null);
        builder.setView(dialogView);


        TextView rangeLabel =  (TextView) dialogView.findViewById(R.id.radiuslabel);
        SeekBar rangeSlider = (SeekBar) dialogView.findViewById(R.id.radiusSlider);



        rangeSlider.setProgress(Integer.parseInt(rangeFetched));
        rangeLabel.setText("Change minumum distance to: " + rangeFetched +"mt");

        rangeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int radius = Math.max(progress, 20);  // Assicura un raggio minimo di 20 metri
                rangeLabel.setText("Change minumum distance to: " + radius +"mt");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Non necessario
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Non necessario
            }
        });


        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                rangeFetched =   String.valueOf(rangeSlider.getProgress());


                String[] cords = SubTrigger.split(",");

                LatLng location = new LatLng(Double.parseDouble(cords[0]), Double.parseDouble(cords[1]));

                saveLocationTriggerToFile(location, location, rangeSlider.getProgress(), assApps);

                dialog.dismiss();
            }
        });

        // Mostra il dialog
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void CreateAppPopUp()
    {
        List<ResolveInfo> installedApps = getInstalledApps();




        // Crea il dialog utilizzando il Material 3 design
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        // Infla il layout personalizzato della dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.appsdialog, null);
        builder.setView(dialogView);

        // Inizializza la RecyclerView e l'Adapter
        RecyclerView recyclerView = dialogView.findViewById(R.id.SelectAppsRecycle);

        Button confirmBtn = dialogView.findViewById(R.id.OkayButton);


        // Imposta il GridLayoutManager per avere 2 dispositivi per riga
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);


        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        final AlertDialog create =  builder.show();;
        AppAdapter adapter = new AppAdapter(installedApps, getApplicationContext(), selectedApps -> {
            // Aggiungi la tua logica quando un dispositivo viene selezionato
            //  Toast.makeText(this, "Dispositivo selezionato: " + device.getName(), Toast.LENGTH_SHORT).show();

            selectedApps_ = selectedApps;
            if (selectedApps.size() > 0) {

                confirmBtn.setVisibility(Button.VISIBLE);
            }
            else
            {
                confirmBtn.setVisibility(Button.GONE);
            }







        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                DecideTriggerToSave();
                CreateAppList(assApps);
                create.dismiss();

            }
        });

        recyclerView.setAdapter(adapter);


    }




    private void DecideTriggerToSave()
    {

        switch (TriggerType) {


            case "bluetooth":

                saveBluetoothTriggerToFile(SubTrigger, SubTrigger, ConvertResolveListToArray(selectedApps_));
                break;
            case "WI-FI":
           //     saveWIFITriggerToFile(SubTrigger, SubTrigger, ConvertResolveListToArray(selectedApps_));
                break;
            case "location":
                String[] cords = SubTrigger.split(",");

                LatLng location = new LatLng(Double.parseDouble(cords[0]), Double.parseDouble(cords[1]));
                saveLocationTriggerToFile(location, location, Integer.parseInt(rangeFetched), ConvertResolveListToArray(selectedApps_));
                break;


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



    private String[] ConvertResolveListToArray(List<ResolveInfo> list)
    {
        String[] packageNames = new String[list.size()];

        // Iteriamo sulla lista e estraiamo il package name di ogni ResolveInfo
        for (int i = 0; i < list.size(); i++) {
            packageNames[i] = list.get(i).activityInfo.packageName;
        }
        assApps = packageNames;
        return packageNames;
    }


    //manipulate json

    private void saveBluetoothTriggerToFile(String deviceName, String newDeviceName, String[] selectedApps) {
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

        // Se la chiave deviceName esiste, sposta i dati sul newDeviceName ed elimina la vecchia chiave
        if (bluetoothData.containsKey(deviceName)) {
         //   Map<String, Object> existingDeviceData = (Map<String, Object>) bluetoothData.remove(deviceName);
        //  bluetoothData.put(newDeviceName, existingDeviceData);
            bluetoothData.remove(deviceName);
            Map<String, Object> deviceData = new HashMap<>();
            deviceData.put("apps", selectedApps);
            bluetoothData.put(newDeviceName, deviceData);
        } else {
            // Crea i dati per il nuovo trigger se deviceName non esiste
            Map<String, Object> deviceData = new HashMap<>();
            deviceData.put("apps", selectedApps);
            bluetoothData.put(newDeviceName, deviceData);
        }

        // Serializza l'oggetto JSON aggiornato
        Gson gson = new Gson();
        String json = gson.toJson(triggerData);

        // Salva il file JSON nello storage interno
        try (FileOutputStream fos = openFileOutput("triggers.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
            secondarytrigger.setText(newDeviceName);
            SubTrigger = newDeviceName;
            Toast.makeText(this, "Trigger edited with success", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error editing trigger", Toast.LENGTH_SHORT).show();
        }
    }




    private void saveLocationTriggerToFile( LatLng location, LatLng newlocation , int range ,String[] selectedApps) {
        Map<String, Object> triggerData;
        String stringedRange = String.valueOf(range);
        stringedRange.replace(".0","");
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

            if (!triggerData.containsKey("location")) {
                triggerData.put("location", new HashMap<String, Object>());
            }
        } catch (IOException e) {
            // Se il file non esiste o non può essere letto, crea una nuova mappa
            triggerData = new HashMap<>();
            triggerData.put("location", new HashMap<String, Object>());
        }


        Map<String, Object> locationData = (Map<String, Object>) triggerData.get("location");


        // Se le coordinate esistono già, sposta i dati sul newlocationed elimina la vecchia chiave
        if (locationData.containsKey(location.latitude +", " + location.longitude)) {

            locationData.remove(location.latitude +", " + location.longitude);
            Map<String, Object> cordsData = new HashMap<>();
            cordsData.put("apps", selectedApps);
            cordsData.put("range", stringedRange);
            locationData.put(newlocation.latitude +", " + newlocation.longitude, cordsData);
        } else {
            // Crea i dati per il nuovo trigger se deviceName non esiste
            Map<String, Object> cordsData = new HashMap<>();
            cordsData.put("apps", selectedApps);
            cordsData.put("range", stringedRange);
            locationData.put(newlocation.latitude +", " + newlocation.longitude, cordsData);
        }


        // Serializza l'oggetto JSON aggiornato
        Gson gson = new Gson();
        String json = gson.toJson(triggerData);

        // Salva il file JSON nello storage interno
        try (FileOutputStream fos = openFileOutput("triggers.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
            Toast.makeText(this, "Trigger edited with success", Toast.LENGTH_SHORT).show();
            rangeValue.setText(stringedRange);
            //todo start monitoring position
        } catch (IOException e) {
            Log.e("TriggerCreationActivity", "Error editing trigger", e);
            Toast.makeText(this, "Error editing trigger", Toast.LENGTH_SHORT).show();
        }
    }




    private void DeleteTriggerFromFile() {
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

            if (triggerData == null || !triggerData.containsKey(TriggerType)) {
                Toast.makeText(this, "Trigger of type"  + TriggerType +" not found", Toast.LENGTH_SHORT).show();
                return; // Esci se non ci sono dati o non esiste la sezione "bluetooth"
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error reading triggers", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ottieni la mappa per i dispositivi Bluetooth
        Map<String, Object> primaryData = (Map<String, Object>) triggerData.get(TriggerType);

        // Controlla se esiste il dispositivo specificato
        if (SubTrigger != null && primaryData.containsKey(SubTrigger)) {
            Map<String, Object> deviceData = (Map<String, Object>) primaryData.get(SubTrigger);

            // Verifica se le app associate corrispondono a selectedApps
            List<String> existingApps = (List<String>) deviceData.get("apps");
            List<String> selectedAppsList = Arrays.asList(assApps);

            if (existingApps != null && existingApps.equals(selectedAppsList)) {
                // Rimuovi il dispositivo se le app corrispondono
                primaryData.remove(SubTrigger);
                Toast.makeText(this, "Trigger deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "No matching apps found for this device", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if(SubTrigger == null)
        {
            // Verifica se le app associate corrispondono a selectedApps
            List<String> existingApps = (List<String>) primaryData.get("apps");
            List<String> selectedAppsList = Arrays.asList(assApps);

            if (existingApps != null && existingApps.equals(selectedAppsList)) {
                // Rimuovi il dispositivo se le app corrispondono
                triggerData.remove(TriggerType);
                Toast.makeText(this, "Trigger deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "No matching apps found for this trigger", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            Toast.makeText(this, "Device not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Serializza l'oggetto JSON aggiornato
        Gson gson = new Gson();
        String json = gson.toJson(triggerData);

        // Salva il file JSON aggiornato nello storage interno
        try (FileOutputStream fos = openFileOutput("triggers.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
        } catch (IOException e) {
            Toast.makeText(this, "Error saving updated triggers", Toast.LENGTH_SHORT).show();
        }
    }





    //permissions

    private void CheckBluethootPermissions()
    {
        // Controlla il permesso di ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {

            // Se non è stato concesso, richiedi il permesso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
        } else {
            // Il permesso è già stato concesso, puoi iniziare a monitorare la posizione

        }
    }



    //enable stuff
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
            CreateBlueDialog(SubTrigger, assApps);
        }
    }

    //risultato attivazione bluethooth
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {


                    CreateBlueDialog(SubTrigger, assApps);
                }
                else
                {
                    Log.d("agg","I need blue");
                }
            });


    // Gestisci la richiesta di permesso
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
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


    //animazione

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

}