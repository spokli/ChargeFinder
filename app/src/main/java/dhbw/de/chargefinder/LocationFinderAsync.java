package dhbw.de.chargefinder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

    public interface LocationFinderAsyncListener {
        public void receiveLocation(double lat, double lon);

        public Context getContext();
    }

    private LocationFinderAsyncListener callback;

    public LocationFinderAsync(LocationFinderAsyncListener callback) {
        this.callback = callback;
        this.location = null;
    }

    public LocationFinderAsync(LocationFinderAsyncListener callback, String locationProvider) {
        this.callback = callback;
        this.location = null;
        this.locationProvider = locationProvider;
    }

    @Override
    protected void onPreExecute() {
        // Get the location manager
        locationManager =
                (LocationManager) callback.getContext().
                        getSystemService(callback.getContext().LOCATION_SERVICE);

        listener = new MyLocationListener();
        String locationProviderName = "";

//        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//        if (gpsEnabled) {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
//            locationProviderName = "GPS";
//        } else if (networkEnabled) {
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
//            locationProviderName = "Netzwerk";

        if (locationProvider == null) {
            String locationProvider = locationManager.getBestProvider(new Criteria(), true);
        }

        if (locationProvider != null) {
            locationManager.requestLocationUpdates(locationProvider, 0, 0, listener);
            locationProviderName = locationProvider;
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
            return;
        }

        progDialog = new ProgressDialog(callback.getContext());
        progDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                LocationFinderAsync.this.cancel(true);
            }
        });

        progDialog.setMessage("Suche Standort durch " + locationProviderName + "-Verbindung...");
        progDialog.setIndeterminate(true);
        progDialog.setCancelable(true);
        progDialog.setCanceledOnTouchOutside(false);
        progDialog.show();
    }

    @Override
    protected void onCancelled() {
        if (progDialog != null & progDialog.isShowing()) {
            progDialog.dismiss();
        }
        Toast.makeText(callback.getContext(), "Kein Standort gefunden.", Toast.LENGTH_SHORT).show();
        locationManager.removeUpdates(listener);
    }

    @Override
    protected Location doInBackground(Void[] params) {
        long timeBefore = Calendar.getInstance().getTime().getTime();
        long maxTime = timeBefore + 10 * 10000;
        while (this.location == null && Calendar.getInstance().getTime().getTime() < maxTime) {
            //Warte
        }
        return location;

//        Location location = locationManager.getLastKnownLocation(provider); // Wenn kein GPS
//        locationManager.removeUpdates(locationListener);
//        return location;

    }

    @Override
    protected void onPostExecute(Location result) {
        if (progDialog != null & progDialog.isShowing()) {
            progDialog.dismiss();
        }

        if (result != null) {
            callback.receiveLocation(result.getLatitude(), result.getLongitude());
        } else {
            Toast.makeText(callback.getContext(), "Kein Standort gefunden.", Toast.LENGTH_SHORT).show();
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




