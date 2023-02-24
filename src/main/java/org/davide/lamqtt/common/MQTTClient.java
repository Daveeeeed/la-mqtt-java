package org.davide.lamqtt.common;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MQTTClient {
    private static final String DEFAULT_CLIENT_NAME = "tester";
    protected BrokerConf brokerConf;
    protected BrokerConnector mconnector;
    protected ArrayList<MQTTReceiver> mcallback;
    protected String clientId;

    public MQTTClient(String username, String password, String host, int port, String id) {
        if (id == null) {
            this.clientId = MQTTClient.DEFAULT_CLIENT_NAME;
        } else {
            this.clientId = id;
            this.brokerConf = new BrokerConf(username, password, host, port, this.clientId);
        }
        this.mconnector = new PahoConnector();
        this.mcallback = new ArrayList<>();
    }

    public void setCallback(MQTTReceiver mcallback) {
        this.mcallback.add(mcallback);
    }

    public boolean connect() throws ExecutionException, InterruptedException {
        return this.mconnector.connect(this.brokerConf).get();
    }

    public void publish(String topic, String message) throws ExecutionException, InterruptedException {
        this.mconnector.publish(topic, message).get();
    }

    public void subscribe(String topic) throws ExecutionException, InterruptedException {
        this.mconnector.subscribe(topic, this).get();
    }

    public void disconnect() throws ExecutionException, InterruptedException {
        this.mconnector.disconnect().get();
    }

    public void msgRecv(MQTTMessage msg) {
        for (MQTTReceiver mqttReceiver : this.mcallback) {
            mqttReceiver.messageRecv(msg);
        }
    }
}
