package com.example.partymaker.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

  /** Encodes a LatLng into a simple "latitude,longitude" string. */
  public static String encodeCoordinatesToStringLocation(LatLng latLng) {
    if (latLng == null) return "";
    // e.g. "32.7940,34.9896"
    return latLng.latitude + "," + latLng.longitude;
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
      // 2) Build a geo: URI. The format “geo:lat,lng?q=lat,lng(label)”
      //    will open Google Maps centered on (lat,lng) with a pin (label).
      //
      //    You can omit “?q=…” or tweak it however you like.
      //    Here we’ll use the coordinates and give a generic query.
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
        // Fallback: open in a browser if Google Maps isn’t installed
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

  public static LatLng centerMapOnChosenPlace(GoogleMap map, Place place) {
    LatLng ll = place.getLatLng();
    if (ll != null && map != null) {
      map.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 15f));
      map.clear();
      map.addMarker(
          new MarkerOptions()
              .position(ll)
              .title(place.getName() != null ? place.getName() : "Selected Place"));
      return ll;
    }
    return null;
  }

  /**
   * Requests ACCESS_FINE_LOCATION from the user if not already granted. If the permission is
   * already granted, calls enableMyLocation(...) immediately.
   */
  public static void requestLocationPermission(
      Activity activity,
      GoogleMap map,
      FusedLocationProviderClient locationClient,
      int finePermissionCode) {
    // 1) Check if ACCESS_FINE_LOCATION is already granted:
    if (ContextCompat.checkSelfPermission(
            activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      // Not yet granted → ask for it now:
      ActivityCompat.requestPermissions(
          activity,
          new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
          finePermissionCode);
    } else {
      // Permission already granted → immediately turn on “My Location”
      enableMyLocation(activity, map, locationClient);
    }
  }

  public static void handlePermissionsResult(
      Context context,
      int requestCode,
      @NonNull int[] grantResults,
      int expectedRequestCode,
      GoogleMap map,
      FusedLocationProviderClient locationClient) {
    if (requestCode == expectedRequestCode
        && grantResults.length > 0
        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      // The user just granted ACCESS_FINE_LOCATION → enable “My Location”
      enableMyLocation(context, map, locationClient);
    } else {
      // Permission was denied or something else happened:
      Toast.makeText(
              context,
              "Location permission denied. Map will remain in default mode.",
              Toast.LENGTH_SHORT)
          .show();
    }
  }

  /**
   * If (and only if) the ACCESS_FINE_LOCATION permission is granted, turn on “My Location” layer on
   * the given GoogleMap, and then move the camera to the user’s last known location.
   *
   * @param context Any Context (e.g. your Activity).
   * @param map The GoogleMap instance to call setMyLocationEnabled(true) on.
   * @param locationClient The FusedLocationProviderClient used to fetch last known location.
   */
  public static void enableMyLocation(
      Context context, GoogleMap map, FusedLocationProviderClient locationClient) {
    if (map == null) return;

    // Double-check that ACCESS_FINE_LOCATION is actually granted before calling
    // setMyLocationEnabled
    if (ActivityCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {

      // Turn on the blue “My Location” dot
      map.setMyLocationEnabled(true);

      // Then move the camera to the last known location (at zoom = 15f)
      locationClient
          .getLastLocation()
          .addOnSuccessListener(
              location -> {
                if (location != null) {
                  LatLng me = new LatLng(location.getLatitude(), location.getLongitude());
                  map.moveCamera(CameraUpdateFactory.newLatLngZoom(me, 15f));
                }
              })
          .addOnFailureListener(
              e -> {
                // In case fetching last location fails, you can optionally log/Toast:
                Toast.makeText(context, "Could not fetch last location.", Toast.LENGTH_SHORT)
                    .show();
              });
    }
  }
}
