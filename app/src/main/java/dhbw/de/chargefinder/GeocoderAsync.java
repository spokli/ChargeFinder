package dhbw.de.chargefinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Asynchroner Task zum Erhalt von Positionsdaten aus Suchbegriff
 */
public class GeocoderAsync extends AsyncTask<String, String, List<Address>> {

    /**
     * Callback Interface
     */
    public interface GeocoderAsyncListener {
        void receiveAddresses(List<Address> addresses);
        Context getContext();
    }

    private GeocoderAsyncListener callback;
    private ProgressDialog progDialog;
    private Resources res;

    /**
     * Konstruktor
     * @param callback Activity-Klasse
     */
    public GeocoderAsync(GeocoderAsyncListener callback) {
        this.callback = callback;
        res = callback.getContext().getResources();
    }

    /**
     * Asynchrone Hintergrundverarbeitung: Ermittelt Koordinaten aus Suchbegriff
     * @param params Suchbegriff
     * @return Gefundene Adressen
     */
    @Override
    protected List<Address> doInBackground(String[] params) {

        List<Address> addresses = new ArrayList<>();

        publishProgress(res.getString(R.string.getCoordinatesFromSearch));

        try {
            // Suche Adressen mit Android-API. Internetverbindung benoetigt
            Geocoder geocoder = new Geocoder(callback.getContext(), Locale.getDefault());
            addresses = geocoder.getFromLocationName(params[0], 5);
        } catch (IOException ioe) {
            // Keine Internetverbindung . Addresses bleibt leer
        }
        return addresses;

    }

    /**
     * Zeige beschaeftigt-Meldung
     * @param step Anzuzeigende Nachricht
     */
    @Override
    protected void onProgressUpdate(String[] step) {
        progDialog = new ProgressDialog(callback.getContext());
        progDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                GeocoderAsync.this.cancel(true);
            }
        });
        progDialog.setMessage(step[0]);
        progDialog.setIndeterminate(true);
        progDialog.setCancelable(true);
        progDialog.setCanceledOnTouchOutside(false);
        progDialog.show();
    }

    /**
     * Schliesse beschaeftigt-Meldung bei Abbruch durch Benutzer
     */
    @Override
    protected void onCancelled() {
        if (progDialog != null && progDialog.isShowing()) {
            progDialog.dismiss();
        }
        Toast.makeText(callback.getContext(), R.string.searchCancelled, Toast.LENGTH_SHORT).show();
    }

    /**
     * Schliesse beschaeftigt-Meldung und uebergebe Ergebnisse der Hintergrundverarbeitung an UI-Thread
     * @param results Adressen
     */
    @Override
    protected void onPostExecute(List<Address> results) {
        if (progDialog != null && progDialog.isShowing()) {
            progDialog.dismiss();
        }
        callback.receiveAddresses(results);
    }
}
