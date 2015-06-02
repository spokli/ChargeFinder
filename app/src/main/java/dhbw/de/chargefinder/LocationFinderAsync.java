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

/**
 * Created by Marco on 02.06.2015.
 */
public class LocationFinderAsync extends AsyncTask<Void, Void, Location> {

    Location location; // location
    ProgressDialog progDialog;
    LocationManager locationManager;
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

    @Override
    protected void onPreExecute() {
        // Get the location manager
        locationManager =
                (LocationManager) callback.getContext().
                        getSystemService(callback.getContext().LOCATION_SERVICE);

        listener = new MyLocationListener();

//        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String locationProvider = locationManager.getBestProvider(criteria, true);

        if (locationProvider != null) {
            locationManager.requestLocationUpdates(locationProvider, 0, 0, listener);
        } else {
            // Keine Verbindung. Lese letzten bekannten Standort
            Location lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location lastGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastNetworkLocation == null && lastGpsLocation == null) {
                //TODO kein Standort verfügbar, User benachrichtigen
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
        progDialog.setMessage("Suche Standort durch " + locationProvider + "-Verbindung...");
        progDialog.setIndeterminate(true);
        progDialog.setCancelable(true);
        progDialog.setCanceledOnTouchOutside(false);
        progDialog.show();
    }

    @Override
    protected void onCancelled() {
        System.out.println("Cancelled by user!");
        progDialog.dismiss();
        locationManager.removeUpdates(listener);
    }

    @Override
    protected Location doInBackground(Void[] params) {
        while (this.location == null) {

        }
        float test = location.getAccuracy();
        return location;

//        Location location = locationManager.getLastKnownLocation(provider); // Wenn kein GPS
//        locationManager.removeUpdates(locationListener);
//        return location;

    }

    @Override
    protected void onPostExecute(Location result) {
        progDialog.dismiss();
        callback.receiveLocation(result.getLatitude(), result.getLongitude());
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




