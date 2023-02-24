package org.davide.lamqtt.backend.dto;

public interface ILogWatcher {
    void start(ILogCallback callback);
    void stop();
}
