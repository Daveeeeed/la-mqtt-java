package org.davide.lamqtt.client.privacy;

import org.davide.lamqtt.common.Direction;
import org.davide.lamqtt.common.Position;
import org.json.JSONObject;

import java.util.ArrayList;

public class EntropyMetrics implements IPrivacyMetrics {

    private static final double DEFAULT_VALUE = 0;
    private static final double DEFAULT_VARIANCE = 500;
    private static final double MAX_SPEED = 5;
    private static final double MIN_SPEED = 1;

    private PrivacySet historian;
    private double interval;
    private double mean;
    private double variance;

    public EntropyMetrics() {
        this.historian = null;
        this.mean = 1;
    }

    @Override
    public void setParameters(JSONObject parameters) {
        this.interval = parameters.getDouble("interval") * parameters.getDouble("numdummy");
        this.mean = ((EntropyMetrics.MAX_SPEED + EntropyMetrics.MIN_SPEED) / 2) * this.interval;
        this.variance = Math.pow(this.interval, 2) * (EntropyMetrics.MAX_SPEED - EntropyMetrics.MIN_SPEED) / 12;
    }

    @Override
    public double compute(PrivacySet ps) {
        ArrayList<Double> prob = new ArrayList<>();
        double tot = 0;
        double entropy = 0;
        if (this.historian == null) {
            return EntropyMetrics.DEFAULT_VALUE;
        }
        for (int i = 0; i < ps.dummySet.size(); i++) {
            //console.log("Sample "+ i);
            //console.log("------------");
            prob.add(i, this.computeMaxProbability(ps.dummySet.get(i), this.historian));
            tot += prob.get(i);
        }
        for (int i = 0; i < ps.dummySet.size(); i++) {
            prob.set(i, prob.get(i) / tot);
            if (prob.get(i) > 0) entropy += ((Math.log(prob.get(i)) / Math.log(2)) * prob.get(i));
        }
        entropy *= entropy * -1;
        return entropy;
    }

    @Override
    public void update(PrivacySet ps) {
        this.historian = ps;
    }

    private double computeAvgProbability(Position pos1, PrivacySet ps) {
        double avg = 0;
        for (int i = 0; i < ps.dummySet.size(); i++) {
            avg += this.computeLikelihood(pos1, ps.dummySet.get(i));
        }
        avg = avg / ps.dummySet.size();
        //console.log("AVG: "+avg+" "+ps.dummySet.length);
        return avg;
    }

    private Double computeMaxProbability(Position pos1, PrivacySet ps) {
        Double max = null;
        for (int i = 0; i < ps.dummySet.size(); i++) {
            double val = this.computeLikelihood(pos1, ps.dummySet.get(i));
            if ((max == null) || (val > max)) max = val;
        }
        //console.log("MAX: "+max+" "+ps.dummySet.length);
        return max;
    }

    private double computeLikelihood(Position pos1, Position pos2) {
        double distance = Direction.computeDistanceGPS(pos1.latitude, pos1.longitude, pos2.latitude, pos2.longitude);
        double gle = (1 / (Math.sqrt(2 * Math.PI * EntropyMetrics.DEFAULT_VARIANCE))) * Math.exp(Math.pow(distance - this.mean, 2) / (-2 * this.variance));
        boolean ok = distance > this.interval * EntropyMetrics.MAX_SPEED;
        //console.log("DIST: "+distance+" "+gle+" ok: "+ok+" bound: "+this.interval* EntropyMetrics.MAX_SPEED);
        return gle;
    }
}
