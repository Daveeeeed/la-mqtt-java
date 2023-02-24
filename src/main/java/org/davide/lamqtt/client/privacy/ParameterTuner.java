package org.davide.lamqtt.client.privacy;

import org.davide.lamqtt.client.MQTTClientMeasurer;
import org.davide.lamqtt.common.Position;
import org.davide.lamqtt.simulator.RNG;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParameterTuner {

    private static final double MAX_TEMPERATURE = 10;
    private static final double MIN_TEMPERATURE = 0.1;
    private static final double SLOT_DURATION = 540;
    private double EXP_FOR_STATES = 2;
    private final Configuration cConf;
    private double cAction;
    private double cReward;
    private final QTable qTable;
    private final IPrivacyManager pManager;
    private final MQTTClientMeasurer mqttMeasurer;
    private final double alpha;
    private double temperature;
    private final double deltaTemperature;
    private final double frequency;
    private double slotTime;
    private double slotNumber;

    public ParameterTuner(ArrayList<Double> dummyUpdateValues, ArrayList<Double> perturbationValues, double alpha, IPrivacyManager pm, MQTTClientMeasurer measurer, RNG rng, double frequency, double exploration) {
        this.cConf = new Configuration(dummyUpdateValues, perturbationValues);
        this.pManager = pm;
        this.alpha = alpha;
        this.mqttMeasurer = measurer;
        this.qTable = new QTable(this.cConf, dummyUpdateValues, perturbationValues, rng);
        this.temperature = ParameterTuner.MAX_TEMPERATURE;
        this.cAction = this.cConf.getConfigNumber();
        this.frequency = frequency;
        this.implementAction();
        this.slotTime = 0;
        this.slotNumber = 0;
        this.EXP_FOR_STATES = exploration;
        this.deltaTemperature = (ParameterTuner.MAX_TEMPERATURE - ParameterTuner.MIN_TEMPERATURE) / (this.qTable.getNumEntries(this.cConf) * this.EXP_FOR_STATES);
        System.out.println("EXPLORATION " + this.EXP_FOR_STATES + " Delta " + this.deltaTemperature);

    }

    public double getReward() {
        this.cReward = this.mqttMeasurer.getPrivacyPerSlot() * this.alpha + (1 - this.alpha) * this.mqttMeasurer.getSpatialAccuracyPerSlot();
        System.out.println("[REW] TIME: " + this.slotNumber + " REWARD: " + this.cReward + " " + " P: " + this.mqttMeasurer.getPrivacyPerSlot() + " SA: " + this.mqttMeasurer.getSpatialAccuracyPerSlot() + " DU: " + this.cConf.cDummyValue + "  PE: " + this.cConf.cPerturbationValue + " NN: " + this.mqttMeasurer.getNumberNotificationRecvPerSlot());
        return this.cReward;
    }


    public void update(Position cPosition) {
        this.slotTime += this.frequency;
        if ((this.mqttMeasurer.getPrivacyPerSlot() != PrivacySet.NO_METRIC_VALUE) && (this.slotTime >= ParameterTuner.SLOT_DURATION)) {
            this.qTable.update(this.cConf, this.cAction, cPosition, this.getReward());
            this.cAction = this.qTable.getBestAction(cPosition, this.cConf, this.temperature);
            this.implementAction();
            this.adjustTemperature();
            this.mqttMeasurer.resetLatest();
            this.slotTime = 0;

        }
        this.slotNumber += 1;
    }


    private void adjustTemperature() {
        if (this.temperature > ParameterTuner.MIN_TEMPERATURE) this.temperature -= this.deltaTemperature;
    }

    private void implementAction() {
        this.cConf.setConfiguration(this.cAction);
        String privacyParameters = "{\"digit\":" + this.cConf.cPerturbationValue + ", \"numDummy\":" + this.cConf.cDummyValue + ", \"interval\":" + this.frequency + " }";
        System.out.println(privacyParameters);
        this.pManager.setParameters(new JSONObject(privacyParameters));
    }
}
