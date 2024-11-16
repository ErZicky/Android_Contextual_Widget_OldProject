package com.zetadev.locationwidget;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {


    private List<ResolveInfo> applist;
    private AppAdapter.OnAppSelectionListener  listener;
    private List<ResolveInfo> selectedApps = new ArrayList<>(); // Lista delle app selezionate
    private Context context;

    public AppAdapter(List<ResolveInfo> apps,Context context_ ,AppAdapter.OnAppSelectionListener listener) {
        this.applist = apps;
        this.listener = listener;
        context = context_;

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName;
        ImageView appIcon;
        LinearLayout selectedBackground;
        public ViewHolder(View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            appIcon = itemView.findViewById(R.id.appIcon);
            selectedBackground = itemView.findViewById(R.id.selectedBackground);
        }
    }

    @Override
    public AppAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_app_icon, parent, false);
        return new AppAdapter.ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull AppAdapter.ViewHolder holder, int position) {
        ResolveInfo app = applist.get(position);

        String appLabel= app.loadLabel(context.getPackageManager()).toString();

        if (appLabel != null && !appLabel.isEmpty()) {
            holder.appName.setText(appLabel);
        } else {
            holder.appName.setText("Unknown app");
        }


        holder.appIcon.setImageDrawable(app.loadIcon(context.getPackageManager()));



        if (selectedApps.contains(app)) {
            holder.selectedBackground.setBackgroundResource(R.drawable.rounded_square);
        } else {
            holder.selectedBackground.setBackgroundColor(Color.TRANSPARENT); // Reset background
        }



        // Gestione della selezione dell'app
        holder.itemView.setOnClickListener(v -> {
            if (selectedApps.contains(app)) {
                selectedApps.remove(app); // Deseleziona l'app
            } else {
                if (selectedApps.size() < 3) {
                    selectedApps.add(app); // Seleziona l'app solo se non ci sono più di 3 selezionate

                }
            }

            // Notifica all'activity le app selezionate
            listener.onAppSelected(selectedApps);

            // Notifica l'adapter per aggiornare la UI
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return applist.size();
    }

    // Interfaccia per restituire le app selezionate all'Activity
    public interface OnAppSelectionListener {
        void onAppSelected(List<ResolveInfo> selectedApps);
    }


    public List<ResolveInfo> getSelectedApps() {
        return selectedApps;
    }





}
