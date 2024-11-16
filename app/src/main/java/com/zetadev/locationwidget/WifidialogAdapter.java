package com.zetadev.locationwidget;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WifidialogAdapter extends RecyclerView.Adapter<WifidialogAdapter.ViewHolder> {

    private List<ScanResult> wifis;
    private OnWifiClickListener  listener;
    private  Context context;

    public WifidialogAdapter(List<ScanResult> wifiList, Context context_, OnWifiClickListener  listener) {
        this.wifis =  new ArrayList<>();
        this.listener = listener;
        context = context_;


        for (ScanResult network : wifiList) {
            if (network.SSID != null && !network.SSID.isEmpty()) {
                wifis.add(network);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView networkName;
        ImageView networkIcon;
        public ViewHolder(View itemView) {
            super(itemView);
            networkName = itemView.findViewById(R.id.DeviceName);
            networkIcon = itemView.findViewById(R.id.DeviceIcon);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_blue_device, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ScanResult network = wifis.get(position);


            holder.networkName.setText(network.SSID);



    holder.networkIcon.setImageResource(DecideIcon(network));




        holder.itemView.setOnClickListener(v -> listener.OnWifiClick(network));
    }

    @Override
    public int getItemCount() {
        return wifis.size();
    }

    public interface OnWifiClickListener {
        void OnWifiClick(ScanResult network);
    }


    private int DecideIcon(ScanResult network) {
        int level = WifiManager.calculateSignalLevel(network.level, 5);
        if (level == 4) {
            return R.drawable.signal_wifi_4_bar_48px;
        } else if (level == 3) {
            return R.drawable.network_wifi_3_bar_48px;
        } else if (level == 2) {
            return R.drawable.network_wifi_2_bar_48px;
        } else {
            return R.drawable.signal_wifi_statusbar_null_48px;
        }
    }




}