package lab.wifiscanner;

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
    public View view;
    public LocationManager locationManager;
    double longitude;
    double latitude;

    public LocationFinder(View view, LocationManager manager) {
        this.view = view;
        this.locationManager = manager;
        checkGpsStatus();
    }

    public void checkGpsStatus() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Snackbar.make(view, "GPS is not available", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent settingGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    v.getContext().startActivity(settingGPS);
                }
            }).show();
        } else {
            getLocation();
        }
    }

    public void getLocation() {
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            onLocationChanged(location);
        } else {
            Snackbar.make(view, "Location is null", Snackbar.LENGTH_LONG).show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0.1f, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        String lng = String.format(Locale.getDefault(), "%.3f", longitude);
        String ltd = String.format(Locale.getDefault(), "%.3f", latitude);
        Snackbar.make(view, "Lng: " + lng + " <> Ltd: " + ltd, Snackbar.LENGTH_LONG).show();
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
    public double getlongitude(){
        return longitude;

    }
    public double getlatitude(){
        return  latitude;

    }



}
