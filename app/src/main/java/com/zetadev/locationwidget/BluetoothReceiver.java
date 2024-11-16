package com.zetadev.locationwidget;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Network;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class BluetoothReceiver extends BroadcastReceiver  {



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();


        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Agg", "I miss permissions");
                return;
            }
            if (device != null) {
                String deviceName = device.getName();
                Log.d("agg", "Device connected: " + deviceName);

                // Leggi il file JSON
                Map<String, Object> triggerData = readTriggerFromFile(context);
                if (triggerData != null && triggerData.containsKey("bluetooth")) {
                    Map<String, Object> bluetoothTriggers = (Map<String, Object>) triggerData.get("bluetooth");

                    // Controlla se il dispositivo connesso è presente nel file
                    if (bluetoothTriggers.containsKey(deviceName)) {
                        Log.d("agg", "Trigger found for device: " + deviceName);
                        Map<String, Object> deviceData = (Map<String, Object>) bluetoothTriggers.get(deviceName);

                        // Passa le app associate al widget (esempio)
                        List<String> associatedAppsList = (List<String>) deviceData.get("apps");
                        String[] associatedApps = new String[associatedAppsList.size()];
                        associatedApps = associatedAppsList.toArray(associatedApps); // Converte l'ArrayList in un array di stringhe

                        // Aggiorna il widget
                        Intent updateIntent = new Intent(context, CustomWidget.class);
                        updateIntent.setAction("com.zetadev.locationwidget.ACTION_BLUETOOTH_UPDATE");
                        updateIntent.putExtra("apps", associatedApps); // Aggiungi le app all'intento
                        context.sendBroadcast(updateIntent);
                    } else {
                        Log.d("agg", "No trigger found for device: " + deviceName);
                    }
                }
            }
            else
            {
                Log.d("agg", "Device connected: null");
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
            Log.e("BluethoothReceiver", "Error reading trigger file", e);
            return null;
        }
    }
}
