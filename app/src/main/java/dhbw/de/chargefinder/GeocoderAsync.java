package dhbw.de.chargefinder;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Marco on 02.06.2015.
 */
public class GeocoderAsync extends AsyncTask<String, Void, List<Address>> {

    public interface GeocoderAsyncListener {
        public void receiveAddresses (List<Address> addresses);
        public Context getContext();
    }

    private GeocoderAsyncListener callback;

    public GeocoderAsync(GeocoderAsyncListener callback){
        this.callback = callback;
    }

    @Override
    protected List<Address> doInBackground(String[] params) {

        try {
            Geocoder geocoder = new Geocoder(callback.getContext(), Locale.getDefault());
            return geocoder.getFromLocationName(params[0], 5);

        } catch(IOException ioe){
            ioe.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(List<Address> results){
        callback.receiveAddresses(results);
    }
}
