package com.zetadev.locationwidget;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BluethdialogAdapter extends RecyclerView.Adapter<BluethdialogAdapter.ViewHolder> {

    private List<BluetoothDevice> deviceList;
    private OnDeviceClickListener listener;
    private  Context context;

    public BluethdialogAdapter(List<BluetoothDevice> devices,Context context_, OnDeviceClickListener listener) {
        this.deviceList = devices;
        this.listener = listener;
        context = context_;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        ImageView deviceIcon;
        public ViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.DeviceName);
            deviceIcon = itemView.findViewById(R.id.DeviceIcon);
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
        BluetoothDevice device = deviceList.get(position);

        if (device.getName() != null) {
            holder.deviceName.setText(device.getName());
        } else {
            holder.deviceName.setText("Unknown Device");
        }


    holder.deviceIcon.setImageResource(DecideIcon(device.getBluetoothClass(), device));




        holder.itemView.setOnClickListener(v -> listener.onDeviceClick(device));
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }




 
    public int DecideIcon(BluetoothClass deviceClass, BluetoothDevice device)
    {
        if (deviceClass != null) {
            switch (deviceClass.getDeviceClass()) {
                case BluetoothClass.Device.PHONE_SMART:
                case BluetoothClass.Device.PHONE_UNCATEGORIZED:
                case BluetoothClass.Device.PHONE_CELLULAR:
                case BluetoothClass.Device.PHONE_CORDLESS:
                case BluetoothClass.Device.PHONE_ISDN:
                case BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY:
                    return R.drawable.smartphone_48px;
                case BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES:
                case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
                case BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED:
                case BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO:
                    return R.drawable.headphones_48px;
                case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                    return R.drawable.bluetooth_drive_48px;
                case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER:
                case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER:
                case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CONFERENCING:
                    return R.drawable.speaker_48px;
                case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR:
                case BluetoothClass.Device.COMPUTER_DESKTOP:
                case BluetoothClass.Device.COMPUTER_LAPTOP:
                case BluetoothClass.Device.COMPUTER_UNCATEGORIZED:
                case BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA:
                    return R.drawable.desktop_windows_48px;
                case BluetoothClass.Device.HEALTH_BLOOD_PRESSURE:
                case BluetoothClass.Device.HEALTH_UNCATEGORIZED:
                case BluetoothClass.Device.HEALTH_PULSE_RATE:
                    return R.drawable.ecg_heart_48px;
                case BluetoothClass.Device.PERIPHERAL_KEYBOARD:
                    return R.drawable.keyboard_48px;
                case BluetoothClass.Device.TOY_CONTROLLER:
                case BluetoothClass.Device.TOY_GAME:
                case BluetoothClass.Device.TOY_UNCATEGORIZED:
                    return R.drawable.stadia_controller_48px;
                case BluetoothClass.Device.WEARABLE_GLASSES:
                case BluetoothClass.Device.WEARABLE_UNCATEGORIZED:
                case BluetoothClass.Device.WEARABLE_WRIST_WATCH:
                case BluetoothClass.Device.WEARABLE_HELMET:
                case BluetoothClass.Device.WEARABLE_PAGER:
                case BluetoothClass.Device.COMPUTER_WEARABLE:
                    return R.drawable.watch_48px;
                default:

                    return R.drawable.bluetooth_48px;
            }
        } else {
          return R.drawable.bluetooth_48px;
        }
    }
}