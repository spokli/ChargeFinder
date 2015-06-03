package dhbw.de.chargefinder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.api.client.util.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

/**
 * Created by Marco on 02.06.2015.
 */
public class LocationFinderAsync extends AsyncTask<Void, Void, Location> {

    Location location; // location
    ProgressDialog progDialog;
    LocationManager locationManager;
    String locationProvider;
    MyLocationListener listener;
    Resources res;

    public interface LocationFinderAsyncListener {
        public void receiveLocation(double lat, double lon);

        public Context getContext();
    }

    private LocationFinderAsyncListener callback;

    public LocationFinderAsync(LocationFinderAsyncListener callback) {
        this.callback = callback;
        this.locationProvider = null;
        this.location = null;
        this.progDialog = null;
        this.locationManager = null;
        this.listener = null;

        res = callback.getContext().getResources();
    }

    public LocationFinderAsync(LocationFinderAsyncListener callback, String locationProvider) {

        this.callback = callback;
        this.locationProvider = locationProvider;
        this.location = null;
        this.progDialog = null;
        this.locationManager = null;
        this.listener = null;

        res = callback.getContext().getResources();
    }

    @Override
    protected void onPreExecute() {
        // Get the location manager

        locationManager =
                (LocationManager) callback.getContext().
                        getSystemService(callback.getContext().LOCATION_SERVICE);

        listener = new MyLocationListener();
        String locationProviderName = "";


        if (locationProvider == null) {
//            String locationProvider = locationManager.getBestProvider(new Criteria(), true);
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
            if (gpsEnabled) {
                locationProvider = LocationManager.GPS_PROVIDER;
            } else if (networkEnabled) {
                locationProvider = LocationManager.NETWORK_PROVIDER;
            } else {
                // Keine Verbindung. Lese letzten bekannten Standort
                Location lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Location lastGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastNetworkLocation == null && lastGpsLocation == null) {
                    //TODO kein Standort verfügbar, User benachrichtigen
                    this.cancel(true);
                    return;
                }

                if (lastNetworkLocation == null && lastGpsLocation != null) {
                    location = lastGpsLocation;
                } else if (lastNetworkLocation != null && lastGpsLocation == null) {
                    location = lastNetworkLocation;
                } else {
                    if (lastNetworkLocation.getTime() > lastGpsLocation.getTime()) {
                        location = lastNetworkLocation;
                    } else {
                        location = lastGpsLocation;
                    }
                }
            }
        }
        locationManager.requestLocationUpdates(locationProvider, 0, 0, listener);

        switch (locationProvider) {
            case LocationManager.GPS_PROVIDER:
                locationProviderName = res.getString(R.string.geoSource_gps);
                break;
            case LocationManager.NETWORK_PROVIDER:
                locationProviderName = res.getString(R.string.geoSource_net);
                break;
            case LocationManager.PASSIVE_PROVIDER:
                locationProviderName = res.getString(R.string.geoSource_pas);
                break;
            default:
                locationProviderName = "";
                break;
        }

        progDialog = new ProgressDialog(callback.getContext());
        progDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                LocationFinderAsync.this.cancel(true);
            }
        });

        progDialog.setMessage(res.getString(R.string.geoSearch1) + locationProviderName + res.getString(R.string.geoSearch2));
        progDialog.setIndeterminate(true);
        progDialog.setCancelable(true);
        progDialog.setCanceledOnTouchOutside(false);
        progDialog.show();
    }

    @Override
    protected void onCancelled() {
        if (progDialog != null && progDialog.isShowing()) {
            progDialog.dismiss();
        }


        Toast.makeText(callback.getContext(), res.getString(R.string.geoNoResult), Toast.LENGTH_SHORT).show();
        locationManager.removeUpdates(listener);
    }

    @Override
    protected Location doInBackground(Void[] params) {
        long timeBefore = Calendar.getInstance().getTime().getTime();
        long maxTime = timeBefore + 3 * 10000; // Für Such-Timeout
        while (this.location == null && Calendar.getInstance().getTime().getTime() < maxTime) {
            //Warte
        }
//        return location;
        if (location == null) {
            Location location = locationManager.getLastKnownLocation(locationProvider);
            locationManager.removeUpdates(listener);
        }
        return location;
    }

    @Override
    protected void onPostExecute(Location result) {
        if (progDialog != null && progDialog.isShowing()) {
            progDialog.dismiss();
        }

        if (result != null) {
            callback.receiveLocation(result.getLatitude(), result.getLongitude());
        } else {
            Toast.makeText(callback.getContext(), res.getString(R.string.geoNoResult), Toast.LENGTH_SHORT).show();
        }
    }

    /*
         LocationListener-Methods
     */
    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location newLocation) {
            location = newLocation;
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
    }
}




