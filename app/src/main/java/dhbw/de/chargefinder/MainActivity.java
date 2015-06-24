package dhbw.de.chargefinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Hauptklasse mit UI-Thread
 */
public class MainActivity extends Activity implements SearchAsync.SearchAsyncListener,
        GeocoderAsync.GeocoderAsyncListener, LocationFinderAsync.LocationFinderAsyncListener {

    public static final String CHARGE_POINT_ID = "ChargePointId";
    protected EditText _editText_search = null;
    protected ImageButton _btn_search = null;
    protected ImageButton _btn_searchSettings = null;
    protected ImageButton _btn_searchPosition = null;
    protected ListView _listView_searchResults = null;
    private Resources _res = null;
    private ShareActionProvider mShareActionProvider = null;
    private ArrayList<OpenChargePoint> points = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Erhalte Referenzen auf View-Elemente
        _editText_search = (EditText) findViewById(R.id.editText_search);
        _btn_search = (ImageButton) findViewById(R.id.btn_search);
        _btn_searchPosition = (ImageButton) findViewById(R.id.btn_searchPosition);
        _btn_searchSettings = (ImageButton) findViewById(R.id.btn_searchSettings);
        _listView_searchResults = (ListView) findViewById(R.id.listView_searchResults);
        _res = getContext().getResources();

        // Setze Listener fuer Klick auf Entertaste in SoftKeyboard
        _editText_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            // Fuehre Suche aus wenn Enter geklickt wurde
                            performSearch();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        // Setze Listener fuer Suchen-Button
        _btn_search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Suche durchfuehren
                performSearch();
            }
        });

        // Setze Listener fuer Positionsermittlung-Button
        _btn_searchPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pruefe ob GPS aktiv, frage User ob er aktivieren will und starte Task fuer
                // asynchrone Positionsbestimmung
                toggleGPSadapter();
            }
        });

        // Setze Listener fuer langen Klick auf Positionsermittlung-Button
        _btn_searchPosition.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // Fuelle Array mit anzuzeigenden Auswahloptionen fuer Geodaten-Quelle
                String[] sources = new String[]{
                        getResources().getString(R.string.geoSource_gps),
                        getResources().getString(R.string.geoSource_net),
                        getResources().getString(R.string.geoSource_pas)};

                // Erzeuge Dialog fuer Auswahl der Geodatenquelle
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getString(R.string.geoSource));
                builder.setItems(sources, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                // Pruefe ob GPS aktiv, frage User ob er aktivieren will und starte
                                // Task fuer asynchrone Positionsbestimmung  per GPS
                                toggleGPSadapter();
                                new LocationFinderAsync(MainActivity.this, LocationManager.GPS_PROVIDER).execute();
                                break;
                            case 1:
                                // Starte Task fuer asynchrone Positionsbestimmung per Netzwerk
                                new LocationFinderAsync(MainActivity.this, LocationManager.NETWORK_PROVIDER).execute();
                                break;
                            case 2:
                                // Starte Task fuer asynchrone Positionsbestimmung per passiven Provider
                                new LocationFinderAsync(MainActivity.this, LocationManager.PASSIVE_PROVIDER).execute();
                                break;
                        }
                    }
                });
                // Erzeuge und zeige Auswahldialog
                AlertDialog d = builder.create();
                d.show();
                return true;
            }

        });
    }

    /**
     * Suche durchfuehren
     * Ausgelagert, da von mehreren Eventhandler genutzt
     */
    private void performSearch() {

        // Ergebnisliste leeren
        _listView_searchResults.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<OpenChargePoint>()));

//        // Tastatur schliessen
//        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        // Pruefe ob Netzwerkverbindung aktiv, frage Benutzer nach Aktivierung
        // und starte Task fuer asynchrone Suche
        toggleNetworkAdapter();
    }

    /**
     * Erhalte moegliche Adressen als Ergebnis von asynchronem Suchtask
     * @param addresses Gefundene Adressen aus Suchbegriff
     */
    @Override
    public void receiveAddresses(List<Address> addresses) {

        // TODO: "Meinten Sie..." einbauen. Momentan wird nur die erste Addresse genutzt

        if (addresses.size() != 0) {
            // Fuehre Suche nach Ladestationen bei Adresse aus
            new SearchAsync(this).
                    execute(addresses.toArray(new Address[addresses.size()]));
        } else {
            Toast.makeText(getContext(), R.string.noSearchResults, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Aktualisiere Ergebnisanzeige in UI mit OpenChargePoints aus asynchronem Suchtask
     * @param points Ladestationen, die angezeigt werden sollen
     */
    @Override
    public void updateListView(ArrayList<OpenChargePoint> points) {

        // Erzeuge Adapter als Visitor der OpenChargePoint-Liste
        ArrayAdapter<OpenChargePoint> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                points);
        // Setze Adapter fuer Liste. Elemente werden per toString angezeigt
        _listView_searchResults.setAdapter(arrayAdapter);
        // Fokus auf Liste setzen, damit Cursor nicht mehr in Suchfeld ist
        _listView_searchResults.requestFocus();

        // Übergebe OpenChargePoints an Activity-Klasse
        this.points = points;

        _listView_searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), SingleItemActivity.class);


                //intent.putExtra(CHARGE_POINT_ID, );
                startActivity(intent);
                //Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    /**
     * Erhalte Koordinaten aus asynchronem Geodaten-Task
     * @param lat Breitengrad
     * @param lon Laengengrad
     */
    @Override
    public void receiveLocation(double lat, double lon) {
        // Erzeuge Dummyadresse und setze manuell Koordinaten
        Address a = new Address(Locale.getDefault());
        a.setLatitude(lat);
        a.setLongitude(lon);

        // Rufe Methode zur Suche von Ladestationen auf
        List<Address> addressList = new ArrayList<>();
        addressList.add(a);
        receiveAddresses(addressList);
    }

    /**
     * Gibt aktuellen Context zurueck fuer korrekte Interface-Implementierung aus AsyncTask-Interfaces
     * @return aktueller Context
     */
    @Override
    public Context getContext() {
        return this;
    }

    /**
     * Pruefe ob Geo-Quelle aktiv ist und frage Benutzer nach Aktivierung.
     * Starte asynchronen Task zur Geodatenbestimmung
     */
    private void toggleGPSadapter() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Geodaten-Quelle ist aktiv, starte asynchronen Task zur Positionsbestimmung
            new LocationFinderAsync(MainActivity.this).execute();

        } else {
            // Geodaten-Quelle ist nicht aktiv, frage Benutzer ob er aktivieren will
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(getResources().getString(R.string.geoAktivieren))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ja), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            // Benutzer will aktivieren, zeige per Intent die Einstellungen an
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.nein), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            // Benutzer will nicht aktivieren, starte Positionsbestimmung trotzdem
                            dialog.cancel();
                            new LocationFinderAsync(MainActivity.this).execute();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    /**
     * Pruefe ob Internet-Quelle aktiv ist und frage Benutzer nach Aktivierung.
     * Starte asynchronen Task zur Suche nach Ladestationen
     */
    private void toggleNetworkAdapter() {

        // Pruefe ob Internetverbindung besteht
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState(); // Mobile Daten
        NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();   // WLAN

        if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING
                || wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
            // Internetverbindung besteht, starte Suche
            new GeocoderAsync(MainActivity.this).execute(_editText_search.getText().toString());

        } else {
            // Internetverbindung besteht nicht, frage Benutzer nach Aktivierung
            String[] sources = new String[]{
                    getResources().getString(R.string.netSource_roaming),
                    getResources().getString(R.string.netSource_wifi),
                    getResources().getString(R.string.cancel)};

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getResources().getString(R.string.netSource));
            builder.setItems(sources, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            // Starte Einstellungen fuer mobile Daten
                            startActivityForResult(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS), 2);
                            break;
                        case 1:
                            // Starte Einstellungen fuer WLAN
                            startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 2);
                            break;
                        case 2:
                            // Benutzer bricht ab
                            Toast.makeText(getContext(), R.string.noSearchResults, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
            AlertDialog d = builder.create();
            d.show();
        }
    }

    /**
     * Erhalte Bestaetigung, dass Einstellungsaktivitaeten geschlossen wurden
     * @param requestCode Aufrufcode des Intents
     * @param resultCode Ergebniscode des Intents
     * @param data Aufrufintent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 1:
                //GPS-Einstellungen
                new LocationFinderAsync(MainActivity.this).execute();
                break;
            case 2:
                // Wifi oder mobile Daten
                new GeocoderAsync(MainActivity.this).execute(_editText_search.getText().toString());
                break;
            default:
                break;
        }
    }
}