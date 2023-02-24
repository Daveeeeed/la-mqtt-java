package org.davide.lamqtt.client.privacy;

import org.davide.lamqtt.common.Position;
import org.davide.lamqtt.simulator.RNG;
import org.json.JSONObject;

import java.util.Objects;

public class DummyUpdates implements IPrivacyManager {

    private double numUpdates;
    private final GeoPerturbation perturbation;
    private double sequenceNo;
    private double numWithinSequence;
    private final RNG rng;
    private final IPrivacyMetrics metric;
    private PrivacySet ps;
    private double metricValue;

    public DummyUpdates(RNG rng) {
        this.perturbation = new GeoPerturbation(rng);
        this.rng = rng;
        this.numWithinSequence = this.generatePosition();
        this.sequenceNo = 0.0;
        this.metricValue = PrivacySet.NO_METRIC_VALUE;
        this.ps = new PrivacySet();
        this.metric = new EntropyMetrics();
    }

    @Override
    public Position transform(Position cPosition) {
        if ((Objects.equals(this.sequenceNo, this.numWithinSequence))) {
            return getPosition(cPosition);
        } else {
            Position newPos = this.perturbation.transform(cPosition);
            return getPosition(newPos);
        }
    }

    private Position getPosition(Position newPos) {
        this.sequenceNo += 1;
        this.ps.add(newPos);
        if (this.sequenceNo >= this.numUpdates) {
            this.sequenceNo = 0;
            this.numWithinSequence = this.generatePosition();
            this.metricValue = this.metric.compute(this.ps);
            //console.log(this.metricValue);
            this.metric.update(this.ps);
            this.ps = new PrivacySet();
        }
        return newPos;
    }

    @Override
    public void setParameters(JSONObject parameters) {
        this.perturbation.setParameters(new JSONObject("{\"digit\":" + parameters.get("digit") + "}"));
        this.numUpdates = parameters.getDouble("numdummy");
        this.metric.setParameters(parameters);
    }

    @Override
    public void setTrajectory(Position cPosition, Position dPosition) {

    }

    private double generatePosition(){
        return this.rng.nextInt(0.0, this.numUpdates);
    }

    @Override
    public double getPrivacyMetricValue() {
        if (this.sequenceNo == 0){
            return  this.metricValue;
        } else {
            return PrivacySet.NO_METRIC_VALUE;
        }
    }
}
