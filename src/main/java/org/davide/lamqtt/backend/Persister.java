package org.davide.lamqtt.backend;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.davide.lamqtt.backend.model.Geofence;
import org.davide.lamqtt.backend.model.Subscription;
import org.davide.lamqtt.backend.model.User;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.concurrent.CompletableFuture;

public class Persister implements IPersister {

    private final String dbName;
    MongoDatabase db;
    private boolean connected;

    Persister(String dbName) {
        this.dbName = dbName;
        this.connected = false;
    }

    public void connect() {
        CompletableFuture.supplyAsync(() -> {
            String url = "mongodb://localhost:27017/";
            try (MongoClient mongoClient = MongoClients.create(url)) {
                db = mongoClient.getDatabase(this.dbName);
                this.connected = true;
                System.out.println("MongoDB Connection OK");
                return true;
            } catch (Exception e) {
                System.out.println("MongoDB Connection error");
                return false;
            }
        });
    }

    public void disconnect() {
        db.getCollection("geofence", Geofence.class).deleteMany(Filters.empty());
        db.getCollection("user", User.class).deleteMany(Filters.empty());
        db.getCollection("subscription", Subscription.class).deleteMany(Filters.empty());
    }

    @Override
    public void addUserPosition(String userId, double lat, double lon) {
        if (this.connected) {
            User user = new User(userId, lat, lon);
            MongoCollection<User> collection = db.getCollection("user", User.class);
            Bson filter = Filters.eq("id", userId);
            Document query = collection.find(filter).first();
            if (query == null) {
                collection.insertOne(user);
            } else {
                Bson update = Updates.combine(Updates.set("latitude", lat), Updates.set("longitude", lon));
                collection.updateOne(filter, update);
            }
        }
    }

    @Override
    public void addGeofence(String name, String idG, double lat, double lon, double rad, String msg) {
        if (this.connected) {
            Geofence geofence = new Geofence(name, idG, lat, lon, rad, msg);
            MongoCollection<Geofence> collection = db.getCollection("geofence", Geofence.class);
            Bson filter = Filters.eq("id", idG);
            Document query = collection.find(filter).first();
            if (query == null) {
                collection.insertOne(geofence);
            } else {
                Bson update = Updates.combine(Updates.set("latitude", lat), Updates.set("longitude", lon), Updates.set("radius", rad), Updates.set("message", msg), Updates.set("topic", name));
                collection.updateOne(filter, update);
            }
        }
    }

    @Override
    public void addSubscription(String id, String top) {
        if (this.connected) {
            Subscription geofence = new Subscription(id, top);
            MongoCollection<Subscription> collection = db.getCollection("subscription", Subscription.class);
            Bson filter = Filters.and(Filters.eq("clientId", id), Filters.eq("topic", top));
            Document query = collection.find(filter).first();
            if (query == null) {
                collection.insertOne(geofence);
            }
        }
    }

    @Override
    public Iterable<Subscription> getAllSubscriptions(String id) {
        if (this.connected) {
            MongoCollection<Subscription> collection = db.getCollection("subscription", Subscription.class);
            Bson filter = Filters.eq("clientId", id);
            return collection.find(filter);
        }
        return null;
    }

    @Override
    public Iterable<Geofence> getGeofenceInfo(String top) {
        if (this.connected) {
            MongoCollection<Geofence> collection = db.getCollection("geofence", Geofence.class);
            Bson filter = Filters.eq("topic", top);
            return collection.find(filter);
        }
        return null;
    }

}
