package com.example.partymaker.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import java.util.Locale;

public class MapUtilities {

  private static final String TAG = "MapUtilities";
  private static final float DEFAULT_ZOOM = 15f;

  /** Encodes a LatLng into a simple "latitude,longitude" string. */
  public static String encodeCoordinatesToStringLocation(LatLng latLng) {
    if (latLng == null) return "";
    return String.format(java.util.Locale.US, "%.6f,%.6f", latLng.latitude, latLng.longitude);
  }

  /** Takes a "lat,lng" string and returns a LatLng object. */
  public static LatLng decodeStringLocationToCoordinates(String locationString) {
    if (locationString == null || locationString.trim().isEmpty()) {
      return null;
    }
    String[] parts = locationString.split(",");
    if (parts.length != 2) return null;

    try {
      double lat = Double.parseDouble(parts[0]);
      double lng = Double.parseDouble(parts[1]);
      return new LatLng(lat, lng);
    } catch (NumberFormatException e) {
      Log.e("MapUtilities", "Failed to parse coordinates: " + locationString, e);
      return null;
    }
  }

  public static void showGroupLocationOnGoogleMaps(String groupLocation, Context ctx) {
    // 1) Decode your saved string into a LatLng:
    LatLng latLng = MapUtilities.decodeStringLocationToCoordinates(groupLocation);

    if (latLng != null) {
      // 2) Build a geo: URI. The format "geo:lat,lng?q=lat,lng(label)"
      //    will open Google Maps centered on (lat,lng) with a pin (label).
      //
      //    You can omit "?q=…" or tweak it however you like.
      //    Here we'll use the coordinates and give a generic query.
      String uriString =
          String.format(
              Locale.ENGLISH,
              "geo:%f,%f?q=%f,%f(%s)",
              latLng.latitude,
              latLng.longitude,
              latLng.latitude,
              latLng.longitude,
              Uri.encode("Party Here"));
      Uri mapUri = Uri.parse(uriString);

      // 3) Fire an Intent to open Google Maps (if installed).
      Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
      mapIntent.setPackage("com.google.android.apps.maps");

      // If Google Maps app is not installed, you can let Android choose any maps-capable app:
      if (mapIntent.resolveActivity(ctx.getPackageManager()) != null) {
        ctx.startActivity(mapIntent);
      } else {
        // Fallback: open in a browser if Google Maps isn't installed
        Intent browserIntent =
            new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://www.google.com/maps/search/?api=1&query="
                        + latLng.latitude
                        + ","
                        + latLng.longitude));
        ctx.startActivity(browserIntent);
      }
    } else {
      Toast.makeText(ctx, "Invalid location data", Toast.LENGTH_SHORT).show();
    }
  }

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

  /**
   * Request location permission and get the user's current location
   * @param activity The activity requesting permission
   * @param map The GoogleMap instance
   * @param locationClient The FusedLocationProviderClient
   * @param permissionCode The permission request code
   */
  public static void requestLocationPermission(Activity activity, GoogleMap map, 
                                             FusedLocationProviderClient locationClient, 
                                             int permissionCode) {
    try {
      if (ContextCompat.checkSelfPermission(activity, 
              android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        
        // Permission already granted, enable location on map
        if (map != null) {
      map.setMyLocationEnabled(true);

          // Get current location
          if (locationClient != null) {
            locationClient.getLastLocation().addOnSuccessListener(activity, location -> {
                if (location != null) {
                centerMapOnUserLocation(map, location);
              } else {
                Log.w(TAG, "Location is null");
              }
            });
          }
        }
      } else {
        // Request permission
        ActivityCompat.requestPermissions(activity,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                permissionCode);
      }
    } catch (SecurityException e) {
      Log.e(TAG, "Error requesting location permission", e);
    } catch (Exception e) {
      Log.e(TAG, "Unexpected error in location permission handling", e);
    }
  }

  /**
   * Handle permission request results
   * @param activity The activity that requested permissions
   * @param requestCode The request code
   * @param grantResults The permission grant results
   * @param permissionCode The expected permission code
   * @param map The GoogleMap instance
   * @param locationClient The FusedLocationProviderClient
   */
  public static void handlePermissionsResult(Activity activity, int requestCode, 
                                             int[] grantResults, int permissionCode,
                                             GoogleMap map, FusedLocationProviderClient locationClient) {
    if (requestCode == permissionCode) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // Permission granted
        requestLocationPermission(activity, map, locationClient, permissionCode);
      } else {
        Log.w(TAG, "Location permission denied");
      }
    }
  }
}
