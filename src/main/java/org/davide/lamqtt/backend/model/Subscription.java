package org.davide.lamqtt.backend.model;

import org.bson.Document;

public class Subscription extends Document {
    String clientId;
    String topic;

    public Subscription(String clientId, String topic) {
        this.clientId = clientId;
        this.topic = topic;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "Subscription{" + "clientId='" + clientId + '\'' + ", topic='" + topic + '\'' + '}';
    }
}
