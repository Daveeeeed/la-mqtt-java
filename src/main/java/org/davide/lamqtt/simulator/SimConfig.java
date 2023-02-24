package org.davide.lamqtt.simulator;

import org.davide.lamqtt.client.privacy.PrivacyModel;
import org.davide.lamqtt.common.Position;

public class SimConfig {
    public Position scenarioLeftCorner;
    public Position scenarioRightCorner;
    public double simSlotLength;
    public double simDuration;
    public double numUsers;
    public double minSpeed;
    public double maxSpeed;
    public double maxPauseTime;
    public double frequencyGPSPublish;
    public double numGeofences;
    public double radiusGeofence;
    public double frequencyAdvPublish;
    public double seed;
    public double numTopics;
    public PrivacyModel privacyModel;
    public String privacyParameters;

    public SimConfig() {

    }
}
