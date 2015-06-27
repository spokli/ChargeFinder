package dhbw.de.chargefinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.location.Address;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Asynchroner Task zum Erhalt von Positionsdaten aus Suchbegriff
 */
public class SearchAsync extends AsyncTask<Object, String, ArrayList<OpenChargePoint>> {

    /**
     * Callback Interface
     */
    public interface SearchAsyncListener {
        void updateListView(ArrayList<OpenChargePoint> points);

        Context getContext();
    }

    private SearchAsyncListener callback;
    private ProgressDialog progDialog;
    private Resources res;

    /**
     * Konstruktor
     *
     * @param callback Activity-Klasse
     */
    public SearchAsync(SearchAsyncListener callback) {
        this.callback = callback;

        res = callback.getContext().getResources();
    }

    /**
     * Asynchrone Hintergrundverarbeitung: Sucht Ladestationen anhand von Koordinaten
     *
     * @param input Koordinaten und Filter, die Web-API uebergeben werden sollen
     * @return Ermittelte Ladestationen in der Naehe
     */
    @Override
    protected ArrayList<OpenChargePoint> doInBackground(Object[] input) {

        Address[] addresses = (Address[]) input[0];
        HashMap<String, Object> settings = (HashMap<String, Object>) input[1];

        // Zeige beschaeftigt-Meldung
        publishProgress(res.getString(R.string.searching));

        String lat = String.valueOf(addresses[0].getLatitude());
        String lon = String.valueOf(addresses[0].getLongitude());

        try {
            // Webserviceaufruf-Vorbereitungen
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();

            // Baue URL auf
            GenericUrl chargeUrl =
                    new GenericUrl("http://api.openchargemap.io/v2/poi");
            chargeUrl.put("output", "json");
            chargeUrl.put("distanceunit", "km");
            chargeUrl.put("latitude", lat);
            chargeUrl.put("longitude", lon);

            // Lese Filter aus
            applyFilters(chargeUrl, settings);

            HttpRequest chargeRequest = requestFactory.buildGetRequest(chargeUrl);

            // Setze Header fuer UTF-8-Kodierung
            chargeRequest.setResponseHeaders(chargeRequest.getResponseHeaders().
                    setContentType("application/json; charset=utf-8").
                    setContentEncoding("UTF-8"));

            HttpResponse chargeResponse = chargeRequest.execute();

            // Dekomprimiere gzip-Ergebnisdaten fuer UTF-8-Darstellung
            String chargeOutput = decompress(chargeResponse.getContent());

            // Speichere gefundene Ladestationen in JSON-Array
            JSONArray chargeWholeArray = new JSONArray(chargeOutput);

            // Erzeuge leere Liste von Ladestationen, die im folgenden befuellt wird
            ArrayList<OpenChargePoint> points = new ArrayList<>();

            // Verarbeite JSON-Ladestationen zu Objekten
            for (int i = 0; i < chargeWholeArray.length(); i++) {
                JSONObject o = chargeWholeArray.getJSONObject(i);

                OpenChargePoint p = new OpenChargePoint();
                p.setOpenChargeId(saveIntRead(o, "ID"));
                p.setTitle(saveStringRead(o, "OperatorsReference"));
                p.setOperatorTitle(saveStringRead(o.getJSONObject("OperatorInfo"), "Title"));
                p.setOperational(saveBooleanRead(o.getJSONObject("StatusType"), "IsOperational"));
                p.setDateLastStatusUpdate(DateTime.parseRfc3339(saveStringRead(o, "DateLastStatusUpdate")));

                // Verarbeite Verbindungen
                JSONArray connectionWholeArray = o.getJSONArray("Connections");
                ArrayList<ChargeConnection> connections = new ArrayList<>();
                for (int j = 0; j < connectionWholeArray.length(); j++) {

                    ChargeConnection con = new ChargeConnection();
                    JSONObject jsonCon = connectionWholeArray.getJSONObject(j);

                    con.setTitle(saveStringRead(jsonCon.getJSONObject("ConnectionType"), "Title"));
                    con.setFormalName(saveStringRead(jsonCon.getJSONObject("ConnectionType"), "FormalName"));
                    con.setLevelId(saveIntRead(jsonCon, "LevelID"));
                    con.setLevelTitle(saveStringRead(jsonCon.getJSONObject("Level"), "Title"));
                    con.setFastCharge(saveBooleanRead(jsonCon.getJSONObject("Level"), "IsFastChargeCapable"));

                    con.setAmps(saveDoubleRead(jsonCon, "Amps"));
                    con.setVoltage(saveDoubleRead(jsonCon, "Voltage"));
                    con.setPowerKW(saveDoubleRead(jsonCon, "PowerKW"));

                    // Hinzufuegen zur Connection-Liste die am Ende dem OpenChargePoint uebergeben wird
                    connections.add(con);

                    con = null;
                    jsonCon = null;
                }

                p.setConnections(connections);

                JSONObject a = o.getJSONObject("AddressInfo");

                p.setStreet(saveStringRead(a, "AddressLine1"));
                p.setStreet2(saveStringRead(a, "AddressLine2"));
                p.setTown(saveStringRead(a, "Town"));
                p.setStateOrProvince(saveStringRead(a, "StateOrProvince"));
                p.setPostcode(saveStringRead(a, "Postcode"));
                p.setCountry(saveStringRead(a.getJSONObject("Country"), "Title"));

                p.setTelephone(saveStringRead(a, "ContactTelephone1"));
                p.setTelephone2(saveStringRead(a, "ContactTelephone2"));
                p.seteMail(saveStringRead(a, "ContactEmail"));
                p.setUrl(saveStringRead(a, "RelatedURL"));

                p.setLatitude(saveDoubleRead(a, "Latitude"));
                p.setLongitude(saveDoubleRead(a, "Longitude"));
                p.setDistance(saveDoubleRead(a, "Distance"));

                // OpenChargePoint zu ArrayList hinzufuegen, um diese spaeter in onPostExecute an die UI
                // zu uebergeben
                points.add(p);

                p = null;
                a = null;
            }

            return points;

        } catch (MalformedURLException mue) {
            System.out.println("Fehler in Webservice-URL");
            mue.printStackTrace();
            return null;
        } catch (IOException ioe) {
            System.out.println("Fehler in Webservice-IO");
            ioe.printStackTrace();
            return null;
        } catch (JSONException je) {
            System.out.println("Fehler in JSON-Parsing");
            je.printStackTrace();
            return null;
        }
    }

    /**
     * Zeige beschaeftigt-Meldung
     *
     * @param step Anzuzeigende Nachricht
     */
    @Override
    protected void onProgressUpdate(String[] step) {
        progDialog = new ProgressDialog(callback.getContext());
        progDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                SearchAsync.this.cancel(true);
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
     *
     * @param results Gefundene Ladestationen
     */
    @Override
    protected void onPostExecute(ArrayList<OpenChargePoint> results) {
        // Schliesse beschaeftigt-Meldung
        if (progDialog != null && progDialog.isShowing()) {
            progDialog.dismiss();
        }

        // OpenChargePoints an UI uebergeben
        if (results != null) {
            if (results.size() == 0) {
                Toast.makeText(callback.getContext(), "Keine Ergebnisse gefunden", Toast.LENGTH_SHORT).show();
            } else {
                callback.updateListView(results);
            }
        }
    }

    /**
     * Extraktion von Zeichen aus komprimiertem gzip-Format in UTF-8-Zeichensatz
     *
     * @param is Gzip-Zeichen als Stream
     * @return Unkomprimierte Zeichen
     */
    private String decompress(InputStream is) {
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            String outStr = "";
            while ((line = bf.readLine()) != null) {
                outStr += line;
            }
            return outStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void applyFilters(GenericUrl url, HashMap<String, Object> filters) {

        if ((int) filters.get("operator") != 0)
            url.put("operatorId", filters.get("operator"));

        if ((int) filters.get("connection") != 0)
            url.put("connectiontypeid", filters.get("connection"));

        if ((int) filters.get("level") != 0)
            url.put("levelid", filters.get("level"));

        Double kw = (Double) filters.get("kw_from");
        url.put("minpowerkw", kw);

        Integer maxresults = (Integer) filters.get("maxresults");
        if (maxresults > 0)
            url.put("maxresults", maxresults);
    }

    //---------------------Private Methoden fuer Exception-sicheres JSON-Parsing-------

    private String saveStringRead(JSONObject origin, String param) {
        try {
            return origin.getString(param);
        } catch (JSONException e) {
            return "N/A";
        }
    }

    private double saveDoubleRead(JSONObject origin, String param) {
        try {
            return origin.getDouble(param);
        } catch (JSONException e) {
            return 0.0;
        }
    }

    private int saveIntRead(JSONObject origin, String param) {
        try {
            return origin.getInt(param);
        } catch (JSONException e) {
            return 0;
        }
    }

    private boolean saveBooleanRead(JSONObject origin, String param) {
        try {
            return origin.getBoolean(param);
        } catch (JSONException e) {
            return false;
        }
    }


}
