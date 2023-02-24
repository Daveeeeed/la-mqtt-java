package org.davide.lamqtt.client.privacy;

import org.davide.lamqtt.common.Position;
import org.json.JSONObject;

public interface IPrivacyManager {
    Position transform(Position cPosition);
    void setParameters(JSONObject parameters);
    void setTrajectory(Position cPosition, Position dPosition);
    double getPrivacyMetricValue();
}
