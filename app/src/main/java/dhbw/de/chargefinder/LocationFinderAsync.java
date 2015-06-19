package dhbw.de.chargefinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Asynchroner Task zum Erhalt von Positionsdaten aus Suchbegriff
 */
public class LocationFinderAsync extends AsyncTask<Void, String, Location> {

    Location location; // location
    ProgressDialog progDialog;
    LocationManager locationManager;
    String locationProvider;
    MyLocationListener listener;
    Resources res;

    /**
     * Callback Interface
     */
    public interface LocationFinderAsyncListener {
        void receiveLocation(double lat, double lon);
        Context getContext();
    }

    private LocationFinderAsyncListener callback;

    /**
     * Konstruktor
     *
     * @param callback Activity-Klasse
     */
    public LocationFinderAsync(LocationFinderAsyncListener callback) {
        this.callback = callback;
        this.locationProvider = null;
        this.location = null;
        this.progDialog = null;
        this.locationManager = null;
        this.listener = null;

        res = callback.getContext().getResources();
    }

    /**
     * Konstruktor mit vordefinierter Geodaten-Quelle
     *
     * @param callback         Activity-Klasse
     * @param locationProvider Geodaten-Quelle
     */
    public LocationFinderAsync(LocationFinderAsyncListener callback, String locationProvider) {

        this.callback = callback;
        this.locationProvider = locationProvider;
        this.location = null;
        this.progDialog = null;
        this.locationManager = null;
        this.listener = null;

        res = callback.getContext().getResources();
    }

    /**
     * Auswahl der Geodaten-Quelle vor eigentlicher Hintergrundverarbeitung
     */
    @Override
    protected void onPreExecute() {
        // Location Manager erhalten
        locationManager =
                (LocationManager) callback.getContext().
                        getSystemService(callback.getContext().LOCATION_SERVICE);

        listener = new MyLocationListener();

        if (locationProvider == null) {
            // Falls keine Quelle vordefiniert ist, ermittle sie

//            String locationProvider = locationManager.getBestProvider(new Criteria(), true);
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // Falls GPS aktiviert ist, nutze das. Andernfalls nutze das Internet
            if (gpsEnabled) {
                locationProvider = LocationManager.GPS_PROVIDER;
            } else if (networkEnabled) {
                locationProvider = LocationManager.NETWORK_PROVIDER;
            } else {
                // Keine Verbindung. Lese letzten bekannten Standort
                Location lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Location lastGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                // Falls kein letzter bekannter Standort verfuegbar, Abbruch
                if (lastNetworkLocation == null && lastGpsLocation == null) {
                    this.cancel(true);
                    return;
                }

                // Nutze letzten bekannten Standort
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
        // Abboniere Geodaten-Informationen
        locationManager.requestLocationUpdates(locationProvider, 0, 0, listener);

        // Zeige beschaeftigt-Meldung
        publishProgress();
    }

    /**
     * Schliesse beschaeftigt-Meldung bei Abbruch durch Benutzer
     */
    @Override
    protected void onCancelled() {
        if (progDialog != null && progDialog.isShowing()) {
            progDialog.dismiss();
        }


        Toast.makeText(callback.getContext(), R.string.noPositionResults, Toast.LENGTH_SHORT).show();
        locationManager.removeUpdates(listener);
    }

    /**
     * Asynchrone Hintergrundverarbeitung: Ermittelt aktuellen Standort
     * @param params Ungenutzt
     * @return Ermittelter Standort
     */
    @Override
    protected Location doInBackground(Void[] params) {
        // Bestimme aktuelle Uhrzeit fuer Timeout-Rechnung
        long timeBefore = Calendar.getInstance().getTime().getTime();

        // Definiere Timeout fuer Standortsuche (in ms)
        long maxTime = timeBefore + 30 * 1000;

        // Solange kein Standort gefunden wurde und Timeout nicht erreicht, warte auf Standort
        while (this.location == null && Calendar.getInstance().getTime().getTime() < maxTime) {
            //Warte
        }

        if (location == null) {
            // Falls kein Standort gefunden wurde (also Timeout erreicht), versuche letzten
            // bekannten Standort zu finden
            Location location = locationManager.getLastKnownLocation(locationProvider);
            locationManager.removeUpdates(listener);
        }
        return location;
    }

    /**
     * Zeige beschaeftigt-Meldung
     * @param step Anzuzeigende Nachricht
     */
    @Override
    protected void onProgressUpdate(String[] step) {
        String locationProviderName;

        // Setze Name der Geodaten-Quelle fuer beschaeftigt-Nachricht
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

    /**
     * Schliesse beschaeftigt-Meldung und uebergebe Ergebnisse der Hintergrundverarbeitung an UI-Thread
     * @param result Standort
     */
    @Override
    protected void onPostExecute(Location result) {
        // Schliesse beschaeftigt-Meldung
        if (progDialog != null && progDialog.isShowing()) {
            progDialog.dismiss();
        }

        if (result != null) {
            // Gebe Laengen- und Breitengrad des ermittelten Standorts an UI-Thread zurueck
            callback.receiveLocation(result.getLatitude(), result.getLongitude());
        } else {
            Toast.makeText(callback.getContext(), R.string.noPositionResults, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Methoden fuer Geodaten-Listener
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




