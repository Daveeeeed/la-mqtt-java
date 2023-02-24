package org.davide.lamqtt.backend;

import org.davide.lamqtt.backend.model.Geofence;
import org.davide.lamqtt.backend.model.Subscription;

import java.awt.geom.Point2D;
import java.util.concurrent.ExecutionException;

public class GeoProcessor {

    private final IPersister persister;
    private final SpatialMQTTBackEnd callback;

    GeoProcessor(IPersister persister, SpatialMQTTBackEnd callback) {
        this.persister = persister;
        this.callback = callback;
    }

    public static double computeDistanceGPS(double lat1, double lon1, double lat2, double lon2) {
        return Point2D.distance(lat1, lon1, lat2, lon2);
    }

    public void processUpdate(String clientId, double latitude, double longitude) {
        Iterable<Subscription> subList = this.persister.getAllSubscriptions(clientId);
        subList.forEach(subscription -> {
            Iterable<Geofence> geofenceList = this.persister.getGeofenceInfo(subscription.getTopic());
            geofenceList.forEach(geofence -> {
                boolean inGeofence = this.checkWithinGeofence(geofence, latitude, longitude);
                if (inGeofence) {
                    String msgJSON = this.buildMessageJSON(geofence);
                    try {
                        this.callback.advertiseClient(geofence.getId(), geofence.getTopic(), clientId, msgJSON);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }

    private String buildMessageJSON(Geofence geofence) {
        return "{ 'message': '" + geofence.getMessage() + "', 'latitude': " + geofence.getLatitude() + ", 'longitude': " + geofence.getLongitude() + "}";
    }

    private boolean checkWithinGeofence(Geofence geofence, double latitude, double longitude) {
        double distance = GeoProcessor.computeDistanceGPS(geofence.getLatitude(), geofence.getLongitude(), latitude, longitude);
        // System.out.println("DISTANCE to gf:"+geofence.getId()+" D: "+distance);
        return distance < geofence.getRadius();
    }
}
