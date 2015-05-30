package dhbw.de.chargefinder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;


public class MainActivity extends ActionBarActivity {

    protected EditText _editText_search = null;
    protected ImageButton _btn_search = null;
    protected ImageButton _btn_searchSettings = null;
    protected ImageButton _btn_searchPosition = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _editText_search = (EditText) findViewById(R.id.editText_search);
        _btn_search = (ImageButton) findViewById(R.id.btn_search);
        _btn_searchPosition = (ImageButton) findViewById(R.id.btn_searchPosition);
        _btn_searchSettings = (ImageButton) findViewById(R.id.btn_searchSettings);

        _btn_search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Check connectivity
                ConnectivityManager conMan = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);

                //mobile
                NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();

                //wifi
                NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();

                if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING
                        || wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING)
                {
                    new Search().execute(_editText_search.getText().toString());
                }
            }
        });

    }
}