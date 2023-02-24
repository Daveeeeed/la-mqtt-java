package org.davide.lamqtt.client.privacy;

import org.json.JSONObject;

public interface IPrivacyMetrics {
    double compute(PrivacySet ps);
    void update(PrivacySet ps);
    void setParameters(JSONObject parameters);
}
