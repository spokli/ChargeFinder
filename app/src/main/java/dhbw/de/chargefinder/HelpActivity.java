package dhbw.de.chargefinder;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Activity-Klasse für Hilfeseite
 */
public class HelpActivity extends Activity {

    protected WebView _helpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);


        _helpView = (WebView) findViewById(R.id.help_view);

        try {
            // Auslesen der HTML-Datei für Hilfeseite
            BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.help)));
            String line;
            String htmlContent = "";
            while ((line = br.readLine()) != null) {
                htmlContent += line;
            }

            // Anzeigen der HTML-Datei auf UI
            _helpView.loadData(htmlContent, "text/html", null);

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
