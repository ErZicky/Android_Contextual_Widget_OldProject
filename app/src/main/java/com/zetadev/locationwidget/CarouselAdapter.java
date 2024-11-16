package com.zetadev.locationwidget;


import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    Context context;
    ArrayList<Map<String, String>> arrayList;
    OnItemClickListener onItemClickListener;


    public CarouselAdapter(Context context,  ArrayList<Map<String, String>> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView triggerText;
        ImageView triggerIcon;
        public ViewHolder(View itemView) {
            super(itemView);
            triggerText = itemView.findViewById(R.id.textView2);
            triggerIcon = itemView.findViewById(R.id.carousel_image_view);
        }
    }


    @Override
    public CarouselAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.carousel_item_trigger, parent, false);
        return new CarouselAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselAdapter.ViewHolder holder, int position) {



        String triggerType = arrayList.get(position).get("triggerType");
        String secondValue = arrayList.get(position).get("condition");



        holder.triggerIcon.setImageResource(DecideIcon(triggerType));

        holder.triggerText.setText(DecideString(triggerType, secondValue));


        holder.itemView.setOnClickListener(view -> onItemClickListener.onClick(triggerType, secondValue));
    }

    @Override
    public int getItemCount() {
     return  arrayList.size();
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick( String triggerType, String secondValue);
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
                    return R.drawable.location_on_48px;
                case "null":
                    return R.drawable.person_cancel_48px;

                default:

                    return R.drawable.android_48px;
            }
        } else {
            return R.drawable.android_48px;
        }
    }


    public String DecideString(String triggertype, String secondkey) //TODO aggiungere  clock
    {
        if (triggertype != null) {
            switch (triggertype) {

                case "charging":
                    return "When charging";
                case "incall":
                    return "When in phone call";
                case "bluetooth":
                    return "When connected via bluetooth to " + secondkey;
                case "WI-FI":
                    return "When connected to WI-FI network: " + secondkey;
                case "location":

                    String[] cords = secondkey.split(",");

                    String convertedcoordinates =  AddressHelper.getShortAddressFromCoordinates(context, Double.parseDouble(cords[0]), Double.parseDouble(cords[1]));



                    return "when near: " + convertedcoordinates;
                case "null":
                    return "Currently there are no active triggers";
                default:

                    return "Trigger";
            }
        } else {
            return "Trigger";
        }
    }




}
