package org.davide.lamqtt.common;

public interface MQTTReceiver {
    void messageRecv(MQTTMessage message);
}