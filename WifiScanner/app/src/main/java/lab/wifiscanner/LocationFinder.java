package lab.wifiscanner;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.Locale;

/**
 * Created by namtr on 27/07/2016.
 */
@SuppressWarnings("MissingPermission")
public class LocationFinder implements LocationListener {
    private View view;
    private LocationManager locationManager;
    private double longitude;
    private double latitude;
    private static final long TWO_MINUTES = 1000 * 60 * 2;

    public LocationFinder(View view, LocationManager locationManager) {
        this.view = view;
        this.locationManager = locationManager;
        updateLocation();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    private void updateLocation() {
        Location location = getLocation();
        if (location != null) {
            onLocationChanged(location);
        } else {
            Snackbar.make(view, "Location is null", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            }).show();
        }
    }

    private Location getLocation() {
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGpsEnabled && isNetworkEnabled) {
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            return getBetterLocation(gpsLocation, networkLocation);
        } else if (isGpsEnabled) {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if (isNetworkEnabled) {
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        Snackbar.make(view, "Location service is not available", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                v.getContext().startActivity(settingGPS);
            }
        }).show();
        return null;
    }

    private Location getBetterLocation(Location newLocation, Location currentBestLocation) {
        if (newLocation == null && currentBestLocation == null) {
            return null;
        } else if (currentBestLocation == null) {
            return newLocation;
        } else if (newLocation == null) {
            return currentBestLocation;
        }
        // Kiểm tra xem vị trí cố định mới là mới hơn hay cũ hơn
        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;
        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return newLocation;
        } else if (isSignificantlyOlder) { // If the new location is more than two minutes older, it must be worse
            return currentBestLocation;
        }
        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), currentBestLocation.getProvider());
        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return newLocation;
        } else if (isNewer && !isLessAccurate) {
            return newLocation;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return newLocation;
        }
        return currentBestLocation;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        String lng = String.format(Locale.getDefault(), "%.3f", longitude);
        String ltd = String.format(Locale.getDefault(), "%.3f", latitude);
        Snackbar.make(view, "Long: " + lng + " ~ Lat: " + ltd, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        }).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public double getlongitude() {
        return longitude;

    }

    public double getlatitude() {
        return latitude;

    }

    public void removeUpdate() {
        locationManager.removeUpdates(this);
    }
}
