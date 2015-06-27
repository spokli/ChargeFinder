package dhbw.de.chargefinder;

import android.content.res.Resources;
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
 * Created by Marco on 24.06.2015.
 */
public class SettingsViewReader {

    View settingsView;

    public SettingsViewReader(View v) {
        this.settingsView = v;
    }

    public HashMap<String, Object> getSettings() {
        ViewGroup root = (ViewGroup) settingsView.findViewById(R.id.searchFilterRoot);
        HashMap<String, Object> settings = getChildren(root, null);
        return settings;
    }

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
            } else {
                childId = child.getResources().getResourceEntryName(child.getId());

                if (child instanceof EditText) {

                    EditText editText = (EditText) child;

                    switch (editText.getInputType()) {
                        case InputType.TYPE_CLASS_TEXT:
                            val = editText.getText().toString();
                            break;
                        case InputType.TYPE_CLASS_NUMBER:
                            if (editText.getText().toString().equals("")) {
                                val = Integer.parseInt("0");
                            } else {
                                val = Integer.parseInt(editText.getText().toString());
                            }
                            break;
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

                } else if (child instanceof Spinner) {
                    Spinner spinner = (Spinner) child;
                    val = spinner.getSelectedItemPosition();

                    settings.put(childId, val);

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
