package dhbw.de.chargefinder;

import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;

import java.util.HashMap;

/**
 * Klasse zum Auslesen von Einstellungen aus Suchfilter-View
 */
public class SettingsViewReader {

    View settingsView;

    public SettingsViewReader(View v) {
        this.settingsView = v;
    }

    /**
     * Gibt Inhalte der SettingsView zurück
     * @return Name-Wert-Paare der Filtereinstellungen
     */
    public HashMap<String, Object> getSettings() {
        ViewGroup root = (ViewGroup) settingsView.findViewById(R.id.searchFilterRoot);
        HashMap<String, Object> settings = getChildren(root, null);
        return settings;
    }

    /**
     * Erhalte Filtereinstellungen untergeordneter UI-ELemente
     * @param parent Elternelement der UI
     * @param settings zu ergänzende Filtereinstellungen
     * @return neue Filtereinstellungen
     */
    private HashMap<String, Object> getChildren(View parent, HashMap<String, Object> settings) {
        if (settings == null) {
            settings = new HashMap<>();
        }

        ViewGroup group = (ViewGroup) parent;

        for (int i = 0; i < group.getChildCount(); i++) {

            View child = group.getChildAt(i);
            String childId = "";
            Object val = null;
            boolean isParent;

            // Prüfe, ob View Elternview ist
            if (child instanceof LinearLayout | child instanceof RelativeLayout |
                    child instanceof GridLayout | child instanceof TableLayout |
                    child instanceof FrameLayout) {
                isParent = true;
            } else {
                isParent = false;
            }

            if (isParent) {
                // Rufe Methode rekursiv für alle untergeordneten Layouts auf
                if (((ViewGroup) child).getChildCount() != 0) {
                    settings = getChildren(child, settings);
                }

                // Falls kein Elternelement, lese Filterwerte aus
            } else {
                childId = child.getResources().getResourceEntryName(child.getId());

                // UI-Element ist Eingabefeld
                if (child instanceof EditText) {

                    EditText editText = (EditText) child;

                    switch (editText.getInputType()) {
                        // Text
                        case InputType.TYPE_CLASS_TEXT:
                            val = editText.getText().toString();
                            break;

                        // Ganzzahl
                        case InputType.TYPE_CLASS_NUMBER:
                            if (editText.getText().toString().equals("")) {
                                val = Integer.parseInt("0");
                            } else {
                                val = Integer.parseInt(editText.getText().toString());
                            }
                            break;

                        // Dezimalzahl
                        case (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL):
                            if (editText.getText().toString().equals("")) {
                                val = Double.parseDouble("0.0");
                            } else {
                                val = Double.parseDouble(editText.getText().toString());
                            }
                            break;
                        default:
                            val = null;
                    }

                    settings.put(childId, val);

                    // Viewelement ist Dropdown
                } else if (child instanceof Spinner) {
                    Spinner spinner = (Spinner) child;
                    val = spinner.getSelectedItemPosition();

                    settings.put(childId, val);

                    // Viewelement ist Checkbox
                } else if (child instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) child;
                    val = checkBox.isSelected();

                    settings.put(childId, val);
                }
            }
        }
        return settings;
    }
}
