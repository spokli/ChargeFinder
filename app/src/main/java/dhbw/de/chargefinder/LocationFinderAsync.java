package dhbw.de.chargefinder;

import android.content.Context;
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

    public interface LocationFinderAsyncListener {
        public void receiveLocation(double lat, double lon);

        public Context getContext();
    }

    private LocationFinderAsyncListener callback;

    public LocationFinderAsync(LocationFinderAsyncListener callback) {
        this.callback = callback;
    }

    @Override
    protected Location doInBackground(Void[] params) {

        // Get the location manager
        LocationManager locationManager =
                (LocationManager) callback.getContext().
                        getSystemService(callback.getContext().LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Hier per Callback location wieder in Methode ziehen
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
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

//        Location location = locationManager.getLastKnownLocation(provider); // Wenn kein GPS
//        locationManager.removeUpdates(locationListener);
//        return location;
        return null;
    }

    @Override
    protected void onPostExecute(Location result) {
        callback.receiveLocation(result.getLatitude(), result.getLongitude());
    }
}




