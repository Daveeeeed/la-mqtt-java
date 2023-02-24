package org.davide.lamqtt.backend.dto;

public interface ILogCallback {
    void newSubscribeEvent(String clientId, String topic);
}
