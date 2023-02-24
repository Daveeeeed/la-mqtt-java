package org.davide.lamqtt.backend.model;

import org.bson.Document;

public class User extends Document {
    String id;
    double latitude;
    double longitude;

    public User(String id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "User{" + "id='" + id + '\'' + ", latitude=" + latitude + ", longitude=" + longitude + '}';
    }
}
