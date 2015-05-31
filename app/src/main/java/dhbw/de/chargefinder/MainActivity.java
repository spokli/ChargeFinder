package dhbw.de.chargefinder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;


// TODO zur Deprecation: http://stackoverflow.com/questions/29877692/why-was-actionbaractivity-deprecated
public class MainActivity extends ActionBarActivity implements Search.AsyncListener {

    protected EditText _editText_search = null;
    protected ImageButton _btn_search = null;
    protected ImageButton _btn_searchSettings = null;
    protected ImageButton _btn_searchPosition = null;
    protected ListView _listView_searchResults = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _editText_search = (EditText) findViewById(R.id.editText_search);
        _btn_search = (ImageButton) findViewById(R.id.btn_search);
        _btn_searchPosition = (ImageButton) findViewById(R.id.btn_searchPosition);
        _btn_searchSettings = (ImageButton) findViewById(R.id.btn_searchSettings);
        _listView_searchResults = (ListView) findViewById(R.id.listView_searchResults);


        _btn_search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Check connectivity
                ConnectivityManager conMan = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState(); // Mobile Daten
                NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();   // Wifi

                if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING
                        || wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING)
                {
                    // Prepare parameter for Search callback
                    // TODO das ist noch nich schoen...

                    Object[] param = new Object[3];
                    param[0] = _listView_searchResults;
                    param[1] = this;
                    param[2] = _editText_search.getText().toString();
                    Search s = new Search(MainActivity.this);
                    s.execute(param);
                }
            }
        });

    }

    @Override
    public void updateListView(ArrayList<OpenChargePoint> points) {
        ArrayAdapter<OpenChargePoint> arrayAdapter = new ArrayAdapter<OpenChargePoint>(
                this,
                android.R.layout.simple_list_item_1,
                points);

        _listView_searchResults.setAdapter(arrayAdapter);
    }
}