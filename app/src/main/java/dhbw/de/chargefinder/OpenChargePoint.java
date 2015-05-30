package dhbw.de.chargefinder;

import com.google.api.client.util.DateTime;

import java.util.ArrayList;

/**
 * Created by Marco on 30.05.2015.
 */
public class OpenChargePoint {

    private int openChargeId;
    private String title; // operatorsReference in JSON
    private String operatorTitle;
    private boolean status; // StatusType->IsOperational in JSON
    private DateTime dateLastStatusUpdate; //Format "2015-01-09T06:03:00Z"
    private ArrayList<ChargeConnection> connections;

    // Address components
    private String street; // AddressLine1 in JSON
    private String street2; // AddressLine2 in JSON
    private String town;
    private String stateOrProvince;
    private String postCode;
    private String country; // country->Title in JSON

    // Contact components
    private String telephone;
    private String telephone2;
    private String eMail;
    private String url;

    // Geo Components
    private double latitude;
    private double longitude;
    private double distance;        // Filterable

}
