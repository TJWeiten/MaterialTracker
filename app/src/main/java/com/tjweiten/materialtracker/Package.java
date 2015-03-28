package com.tjweiten.materialtracker;

/**
 * Created by TJ on 3/27/2015.
 */
public class Package {

    /* database variables */
    int _id;
    String _name;
    String _tracking;
    int _carrier_id;
    String _xml_response;
    int _active;

    public Package() {

    }

    public Package(int id, String name, String tracking, int carrier_id, String xml_response, int active) {
        this._id = id;
        this._name = name;
        this._tracking = tracking;
        this._carrier_id = carrier_id;
        this._xml_response = xml_response;
        this._active = active;
    }

    public Package(String name, String tracking, int carrier_id, String xml_response, int active) {
        this._name = name;
        this._tracking = tracking;
        this._carrier_id = carrier_id;
        this._xml_response = xml_response;
        this._active = active;
    }

    public int getID() {
        return this._id;
    }

    public void setID(int id) {
        this._id = id;
    }

    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getTracking() {
        return this._tracking;
    }

    public void setTracking(String tracking) {
        this._tracking = tracking;
    }

    public int getCarrierID() {
        return this._carrier_id;
    }

    public void setCarrierID(int carrier_id) {
        this._carrier_id = carrier_id;
    }

    public String getXML() {
        return this._xml_response;
    }

    public void setXML(String xml_response) {
        this._xml_response = xml_response;
    }

    public int getActive() {
        return this._active;
    }

    public void setActive(int active) {
        this._active = active;
    }

}
