package org.davide.lamqtt.client.privacy;

import org.davide.lamqtt.common.Position;

import java.util.ArrayList;

public class PrivacySet {
    public Position realPosition;
    public ArrayList<Position> dummySet;
    public static double NO_METRIC_VALUE = -1;

    public PrivacySet(){
        this.dummySet = new ArrayList<>();
    }

    public void add(Position pos){
        this.dummySet.add(pos);
    }
}
