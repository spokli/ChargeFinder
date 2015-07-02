package dhbw.de.chargefinder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

/**
 * Die folgende Klasse enthaelt den einzelnen OpenChargePoint.
 * Mit den vorhandenen Buttons laesst sich zum Punkt navigieren und dieser kann geteilt werden.
 */
public class SingleItemActivity extends Activity {

    protected TextView _textView_Title = null;
    protected Button _btn_Navigation = null;
    protected Button _btn_Share = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_item);

        //Erhalte Referenz auf UI-Elemente
        _textView_Title = (TextView) findViewById(R.id.textView);
        _btn_Navigation = (Button) findViewById(R.id.btn_Navigation);
        _btn_Share = (Button) findViewById(R.id.btn_share);

        //Hole den Parameter, der von der MainActivity mit uebergeben wurde
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        final OpenChargePoint openChargePoint = (OpenChargePoint) bundle.getSerializable(MainActivity.CHARGE_POINT);

        String textView = "Name: " + openChargePoint.getTitle() + "\n"
                + "Betreiber: " + openChargePoint.getOperatorTitle() + "\n"
                + "Entfernung: " + openChargePoint.getDistance() + " km \n"
                + "Zuletzt Aktualisiert: " + openChargePoint.getDateLastStatusUpdate() + "\n"
                + "Adresse: " + openChargePoint.getStreet() + ", " + openChargePoint.getPostcode() + " " + openChargePoint.getTown() + "\n"
                + openChargePoint.getCountry() + "\n\n"
                + "Kontakt:\n"
                + "Telefon: " + (openChargePoint.getTelephone().equals("null") ? "-" : openChargePoint.getTelephone())  + "\n"
                + "Telefon2: " + (openChargePoint.getTelephone2().equals("null") ? "-" : openChargePoint.getTelephone2())  + "\n"
                + "Email: " + (openChargePoint.geteMail().equals("null") ? "-" : openChargePoint.geteMail()) + "\n"
                + "Webseite: " + (openChargePoint.getUrl().equals("null") ? "-" : openChargePoint.getUrl()) + "\n\n";

        for(int i = 0, j = 1; i < openChargePoint.getConnections().size(); i++, j++) {
            textView += "Anschluss" + j + ": \n"
                     + "Name: " + openChargePoint.getConnections().get(i).getTitle() + "\n"
                     + openChargePoint.getConnections().get(i).getLevelTitle() + "\n"
                     + "Formaler Name: " + openChargePoint.getConnections().get(i).getFormalName() + "\n"
                     + "Spannung: " + openChargePoint.getConnections().get(i).getVoltage() + " V \n"
                     + "Stromsträrke: " + openChargePoint.getConnections().get(i).getAmps() + " A \n"
                     + "Leistung: " + openChargePoint.getConnections().get(i).getPowerKW() + " kW \n"
                     + "Schnellladen: " + (openChargePoint.getConnections().get(i).isFastCharge() ? "Ja" : "Nein");
        }

        _textView_Title.setText(textView);

        //Beim Klick auf den Navigations-Button wird ein Intent erzeugt, dass mit den
        // Geokoordinaten bestueckt an die Methode startActivity uebergeben wird
        _btn_Navigation.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                String uri = "google.navigation:q=%f, %f";
                Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String
                        .format(Locale.US, uri, openChargePoint.getLatitude(), openChargePoint.getLongitude())));
                startActivity(navIntent);
            }
        });

        //Beim Klick auf den Teilen-Button wird ein Intent erzeugt, dass eine String als Payload
        //enthaelt und ueber die startActivity wird eine Menue mit moegliche Share-Apps aufgerufen
        _btn_Share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = SingleItemActivity.this.getResources().getString(R.string.share_msg)
                        + "http://openchargemap.org/site/poi/details/" + openChargePoint.getOpenChargeId();
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, SingleItemActivity.this.getResources().getString(R.string.share_via)));
            }
        });

    }
}
