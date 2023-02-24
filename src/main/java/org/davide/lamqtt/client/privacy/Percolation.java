package org.davide.lamqtt.client.privacy;

import org.davide.lamqtt.common.Direction;
import org.davide.lamqtt.common.Position;
import org.davide.lamqtt.simulator.RNG;
import org.json.JSONObject;

import java.util.ArrayList;

public class Percolation implements IPrivacyManager {
    private static final double MAX_SPEED = 5;
    private static final double MIN_SPEED = 2;
    private static final int MAX_TRAJECTORIES = 12;
    private double numUpdates;
    private final GeoPerturbation perturbation;
    private final RNG rng;
    private final ArrayList<Position> trajectories;
    private final ArrayList<Direction> directions;
    private final ArrayList<Double> speed;
    private int sequenceNo;
    private double interval;
    private final IPrivacyMetrics metric;
    private PrivacySet ps;
    private double metricValue;
    private boolean initialized;
    private Position dest;
    private double cPerturbationIndex;

    public Percolation(RNG rng) {
        this.perturbation = new GeoPerturbation(rng);
        this.rng = rng;
        this.trajectories = new ArrayList<>();
        this.directions = new ArrayList<>();
        this.speed = new ArrayList<>();
        this.metric = new DistanceMetrics();
        this.sequenceNo = 0;
        this.metricValue = PrivacySet.NO_METRIC_VALUE;
        this.ps = new PrivacySet();
        this.initialized = false;
    }


    @Override
    public Position transform(Position cPosition) {
        Position prev = this.trajectories.get(this.sequenceNo);
        this.updatePosition(cPosition);
        if (this.sequenceNo == 0) {
            this.sequenceNo += 1;
            this.ps.add(cPosition);
            this.ps.realPosition = cPosition;
            if (this.sequenceNo >= this.numUpdates) {
                this.sequenceNo = 0;
                this.metricValue = this.metric.compute(this.ps);
                this.metric.update(this.ps);
                this.ps = new PrivacySet();
            }
            return cPosition;
        } else {
            Position retPos = this.trajectories.get(this.sequenceNo);
            this.ps.add(retPos);
            this.sequenceNo += 1;
            if (this.sequenceNo >= this.numUpdates) {
                this.sequenceNo = 0;
                this.metricValue = this.metric.compute(this.ps);
                // console.log(this.metricValue);
                this.metric.update(this.ps);
                this.ps = new PrivacySet();
            }
            return retPos;
        }
    }

    @Override
    public void setParameters(JSONObject parameters) {
        this.perturbation.setParameters(new JSONObject("{\"digit\":" + parameters.getDouble("digit") + "}"));
        this.metric.setParameters(parameters);
        this.interval = parameters.getDouble("interval");
        if (parameters.getDouble("numDummy") < Percolation.MAX_TRAJECTORIES)
            this.numUpdates = parameters.getDouble("numDummy");
        else this.numUpdates = Percolation.MAX_TRAJECTORIES;
        if (!this.initialized) {
            for (int i = 0; i < (Percolation.MAX_TRAJECTORIES); i++) {
                this.trajectories.set(i, null);
                this.speed.set(i, 0.0);
            }
        } else if (parameters.getDouble("digit") != this.cPerturbationIndex) {
            for (int i = 0; i < (Percolation.MAX_TRAJECTORIES); i++) {
                this.trajectories.set(i, this.perturbation.transform(this.trajectories.get(i)));
                this.directions.set(i, new Direction(this.trajectories.get(i), this.perturbation.transform(this.dest), this.speed.get(i)));
            }
        }
        this.cPerturbationIndex = parameters.getDouble("digit");
    }

    @Override
    public void setTrajectory(Position cPosition, Position dPosition) {
        this.trajectories.set(0, new Position(cPosition.latitude, cPosition.longitude));
        for (int i = 1; i < (Percolation.MAX_TRAJECTORIES); i++) {
            if (this.trajectories.get(i) == null) this.trajectories.set(i, this.perturbation.transform(cPosition));
            this.speed.set(i, (this.rng.nextDouble() * (Percolation.MAX_SPEED - Percolation.MIN_SPEED) + Percolation.MIN_SPEED));
            this.directions.set(i, new Direction(this.trajectories.get(i), this.perturbation.transform(dPosition), this.speed.get(i)));
        }
        this.dest = dPosition;
        this.initialized = true;
    }


    public void updatePosition(Position cPosition) {
        this.trajectories.set(0, cPosition);
        for (int i = 1; i < Percolation.MAX_TRAJECTORIES; i++) {
            this.trajectories.set(i, this.directions.get(i).computeAdvance(this.speed.get(i), this.interval));
            if (this.directions.get(i).isDestinationReached()) {
                this.speed.set(i,(this.rng.nextDouble() * Percolation.MAX_SPEED + Percolation.MIN_SPEED));
                this.directions.set(i, new Direction(this.trajectories.get(i), this.perturbation.transform(cPosition), this.speed.get(i)));
            }
        }

    }

    @Override
    public double getPrivacyMetricValue() {
        if (this.sequenceNo == 0) {
            return this.metricValue;
        } else return PrivacySet.NO_METRIC_VALUE;

    }
}
