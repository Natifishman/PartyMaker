package com.example.partymaker.ui.group;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;

/**
 * Utility class for map-related operations
 */
public class MapUtilities {
    private static final String TAG = "MapUtilities";
    private static final float DEFAULT_ZOOM = 15f;

    /**
     * Centers the map on the chosen place and adds a marker
     * @param map The GoogleMap instance
     * @param place The selected Place
     * @return The LatLng of the selected place
     */
    public static LatLng centerMapOnChosenPlace(@NonNull GoogleMap map, @NonNull Place place) {
        try {
            LatLng latLng = place.getLatLng();
            if (latLng == null) {
                Log.e(TAG, "Place has no location data");
                return null;
            }
            
            // Clear previous markers
            map.clear();
            
            // Add marker at the selected place
            map.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
            
            // Move camera to the selected place
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            
            return latLng;
        } catch (Exception e) {
            Log.e(TAG, "Error centering map on place", e);
            return null;
        }
    }
    
    /**
     * Centers the map on the user's current location
     * @param map The GoogleMap instance
     * @param location The user's current location
     * @return The LatLng of the user's location
     */
    public static LatLng centerMapOnUserLocation(@NonNull GoogleMap map, @NonNull Location location) {
        try {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            
            // Clear previous markers
            map.clear();
            
            // Add marker at the user's location
            map.addMarker(new MarkerOptions().position(latLng).title("Your Location"));
            
            // Move camera to the user's location
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            
            return latLng;
        } catch (Exception e) {
            Log.e(TAG, "Error centering map on user location", e);
            return null;
        }
    }
    
    /**
     * Formats a LatLng object to a string representation
     * @param latLng The LatLng to format
     * @return String representation of the LatLng
     */
    public static String formatLatLng(LatLng latLng) {
        if (latLng == null) return "";
        return latLng.latitude + "," + latLng.longitude;
    }
    
    /**
     * Parses a string representation of coordinates into a LatLng object
     * @param coordinates String in format "latitude,longitude"
     * @return LatLng object or null if parsing fails
     */
    public static LatLng parseLatLng(String coordinates) {
        try {
            if (coordinates == null || coordinates.isEmpty()) return null;
            
            String[] parts = coordinates.split(",");
            if (parts.length != 2) return null;
            
            double latitude = Double.parseDouble(parts[0]);
            double longitude = Double.parseDouble(parts[1]);
            
            return new LatLng(latitude, longitude);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing coordinates: " + coordinates, e);
            return null;
        }
    }
} 