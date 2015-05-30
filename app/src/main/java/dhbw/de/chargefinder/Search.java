package dhbw.de.chargefinder;

import android.os.AsyncTask;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by Marco on 30.05.2015.
 */
public class Search extends AsyncTask {

    @Override
    protected String doInBackground(Object[] params) {

//        publishProgress();

        String query = "";

        if(params.length == 1){
            query = "" + params[0];
            query = query.replaceAll(" ", "+");
            //TODO: Prevent injection
        }

        try {
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();

            GenericUrl coordUrl =
                    new GenericUrl("http://nominatim.openstreetmap.org/search");
            coordUrl.put("format", "json");
            coordUrl.put("q", query);

            HttpRequest coordRequest = requestFactory.buildGetRequest(coordUrl);
            HttpResponse coordResponse = coordRequest.execute();

            String coordOutput = coordResponse.parseAsString();

            JSONArray coordWholeArray = new JSONArray(coordOutput);
            JSONObject coordFirstObject = (JSONObject) coordWholeArray.get(0);

            String lat = coordFirstObject.getString("lat");
            String lon = coordFirstObject.getString("lon");


            // Now use the coordinates to address the opencharge webservice
            GenericUrl chargeUrl =
                    new GenericUrl("http://api.openchargemap.io/v2/poi");
            chargeUrl.put("output", "json");
            chargeUrl.put("distanceunit", "km");
            chargeUrl.put("latitude", lat);
            chargeUrl.put("longitude", lon);

            HttpRequest chargeRequest = requestFactory.buildGetRequest(chargeUrl);
            HttpResponse chargeResponse = chargeRequest.execute();

            String chargeOutput = chargeResponse.parseAsString();

            JSONArray chargeWholeArray = new JSONArray(chargeOutput);
            JSONObject chargeFirstObject = (JSONObject) chargeWholeArray.get(0);

        } catch (MalformedURLException mue) {
            System.out.println("Fehler in Webservice-URL");
            return null;
        } catch (IOException ioe) {
            System.out.println("Fehler in Webservice-IO");
            return null;
        } catch (JSONException je) {
            System.out.println("Fehler in JSON-Parsing");
            return null;
        }
        return null;
    }

    protected void onPostExecute(String result) {
        System.out.println(result);
    }
}
