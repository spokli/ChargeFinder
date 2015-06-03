package dhbw.de.chargefinder;

import android.location.Address;
import android.os.AsyncTask;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpEncoding;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.StreamingContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Created by Marco on 30.05.2015.
 */
public class SearchAsync extends AsyncTask<Address, Integer, ArrayList<OpenChargePoint>> {

    public interface SearchAsyncListener {
        public void updateListView(ArrayList<OpenChargePoint> points);
    }

    private SearchAsyncListener callback;

    public SearchAsync(SearchAsyncListener callback) {
        this.callback = callback;
    }

    @Override
    protected ArrayList<OpenChargePoint> doInBackground(Address[] addresses) {

//        publishProgress();

//        //TODO: Prevent injection

        String lat = String.valueOf(addresses[0].getLatitude());
        String lon = String.valueOf(addresses[0].getLongitude());

        try {
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
/*
            Eigener Webserviceaufruf durch Nutzung von Geocoder nicht mehr notwendig
 */
//            GenericUrl coordUrl =
//                    new GenericUrl("http://nominatim.openstreetmap.org/search");
//            coordUrl.put("format", "json");
//            coordUrl.put("q", query);
//
//            HttpRequest coordRequest = requestFactory.buildGetRequest(coordUrl);
//            HttpResponse coordResponse = coordRequest.execute();
//
//            String coordOutput = coordResponse.parseAsString();
//
//            JSONArray coordWholeArray = new JSONArray(coordOutput);
//            JSONObject coordFirstObject = (JSONObject) coordWholeArray.get(0);
//
//            String lat = saveStringRead(coordFirstObject, "lat");
//            String lon = saveStringRead(coordFirstObject, "lon");

            // Now use the coordinates to address the opencharge webservice
            GenericUrl chargeUrl =
                    new GenericUrl("http://api.openchargemap.io/v2/poi");
            chargeUrl.put("output", "json");
            chargeUrl.put("distanceunit", "km");
            chargeUrl.put("latitude", lat);
            chargeUrl.put("longitude", lon);
            chargeUrl.put("maxresults", "20");

            HttpRequest chargeRequest = requestFactory.buildGetRequest(chargeUrl);

            chargeRequest.setResponseHeaders(chargeRequest.getResponseHeaders().
                    setContentType("application/json; charset=utf-8").
                    setContentEncoding("UTF-8"));

            HttpResponse chargeResponse = chargeRequest.execute();

            String chargeOutput = decompress(chargeResponse.getContent());

            JSONArray chargeWholeArray = new JSONArray(chargeOutput);
            ArrayList<OpenChargePoint> points = new ArrayList<OpenChargePoint>();

            for (int i = 0; i < chargeWholeArray.length(); i++) {
                JSONObject o = chargeWholeArray.getJSONObject(i);

                // Convert JSONObject to OpenChargePoint-Object

                OpenChargePoint p = new OpenChargePoint();
                p.setTitle(saveStringRead(o, "OperatorsReference"));
                p.setOperatorTitle(saveStringRead(o.getJSONObject("OperatorInfo"), "Title"));
                p.setOperational(saveBooleanRead(o.getJSONObject("StatusType"), "IsOperational"));
                p.setDateLastStatusUpdate(DateTime.parseRfc3339(saveStringRead(o, "DateLastStatusUpdate")));

                // Connections
                JSONArray connectionWholeArray = o.getJSONArray("Connections");
                ArrayList<ChargeConnection> connections = new ArrayList<ChargeConnection>();
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

                    // Hinzufügen zur Connection-Liste die am Ende dem OpenChargePoint übergeben wird
                    connections.add(con);

                    con = null; // Destroy con
                    jsonCon = null; // Destroy jsonCon
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

                // OpenChargePoint zu ArrayList hinzufügen, um diese später in onPostExecute an die UI
                // zu übergeben
                points.add(p);

                p = null; // Destroy p
                a = null; // Destroy address information
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

    @Override
    protected void onPostExecute(ArrayList<OpenChargePoint> results) {
        // OpenChargePoints an UI übergeben
        if (results != null) {
            callback.updateListView(results);
        }
    }

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

    //---------------------Private Methoden für Exception-sicheres JSON-Parsing-------

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
