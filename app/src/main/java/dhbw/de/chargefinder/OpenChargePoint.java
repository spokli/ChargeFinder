package dhbw.de.chargefinder;


import com.google.api.client.util.DateTime;

import java.util.ArrayList;

/**
 * Repraesentiert eine Ladestation
 * Created by Marco on 30.05.2015.
 */
public class OpenChargePoint {

    private int openChargeId;
    private String title; // operatorsReference in JSON
    private String operatorTitle; // operator -> title in JSON
    private boolean isOperational; // StatusType->IsOperational in JSON
    private DateTime dateLastStatusUpdate; //Format "2015-01-09T06:03:00Z"
    private ArrayList<ChargeConnection> connections;

    // Address components
    private String street; // AddressLine1 in JSON
    private String street2; // AddressLine2 in JSON
    private String town;
    private String stateOrProvince;
    private String postcode;
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

    public String toString(){
        return this.getTitle() + " | " + ("" + this.getDistance()).substring(0,4) + " km entfernt";
    }

    //----------------------------------Getter & Setter---------------------------------

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getOpenChargeId() {
        return openChargeId;
    }

    public void setOpenChargeId(int openChargeId) {
        this.openChargeId = openChargeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOperatorTitle() {
        return operatorTitle;
    }

    public void setOperatorTitle(String operatorTitle) {
        this.operatorTitle = operatorTitle;
    }

    public boolean isOperational() {
        return isOperational;
    }

    public void setOperational(boolean operational) {
        this.isOperational = operational;
    }

    public DateTime getDateLastStatusUpdate() {
        return dateLastStatusUpdate;
    }

    public void setDateLastStatusUpdate(DateTime dateLastStatusUpdate) {
        this.dateLastStatusUpdate = dateLastStatusUpdate;
    }

    public ArrayList<ChargeConnection> getConnections() {
        return connections;
    }

    public void setConnections(ArrayList<ChargeConnection> connections) {
        this.connections = connections;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getStateOrProvince() {
        return stateOrProvince;
    }

    public void setStateOrProvince(String stateOrProvince) {
        this.stateOrProvince = stateOrProvince;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postCode) {
        this.postcode = postCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getTelephone2() {
        return telephone2;
    }

    public void setTelephone2(String telephone2) {
        this.telephone2 = telephone2;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }



}
