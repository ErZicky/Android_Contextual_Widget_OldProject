package com.zetadev.locationwidget;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddressHelper {


    // Method to get LatLng from Address
    public static LatLng getCoordinatesFromAddress(Context context, String address) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (addresses == null || addresses.isEmpty()) {
                Log.e("AddressConverter", "No location found for the given address.");
                return null;
            }

            Address location = addresses.get(0);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            return new LatLng(latitude, longitude);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("AddressConverter", "Geocoding failed: " + e.getMessage());
            return null;
        }
    }



    public static String getAddressFromCoordinates(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        try {
            // Getting the address from coordinates
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses == null || addresses.isEmpty()) {
                Log.e("AddressConverter", "No address found for the given coordinates.");
                return null;
            }

            Address address = addresses.get(0);
            StringBuilder addressString = new StringBuilder();




            // Append address components (e.g., street, city, country)
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {

                addressString.append(address.getAddressLine(i)).append("\n");
            }

            return addressString.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("AddressConverter", "Geocoding failed: " + e.getMessage());
            return null;
        }
    }


    public static String getShortAddressFromCoordinates(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        try {
            // Getting the address from coordinates
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses == null || addresses.isEmpty()) {
                Log.e("AddressConverter", "No address found for the given coordinates.");
                return null;
            }

            Address address = addresses.get(0);
            StringBuilder addressString = new StringBuilder();

            // Append address components (e.g., street, city, country)

                addressString.append(address.getThoroughfare()).append(" ");
                addressString.append(address.getSubThoroughfare()).append(", ");
                addressString.append(address.getLocality()).append(", ");
                addressString.append(address.getCountryName()).append(", ");

            return addressString.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("AddressConverter", "Geocoding failed: " + e.getMessage());
            return null;
        }
    }

}

class LatLng {
    double latitude;
    double longitude;

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Latitude: " + latitude + ", Longitude: " + longitude;
    }
}
