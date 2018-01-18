package com.mlaf.hu.implementationexample.sensoragent;

import com.mlaf.hu.models.Measurement;
import com.mlaf.hu.models.Measurements;

import java.util.concurrent.ThreadLocalRandom;

public class Sensor extends com.mlaf.hu.sensoragent.Sensor {
    private String sensorID;

    public Sensor(String idPostfix) {
        this.sensorID = "ImplementationSensor" + idPostfix;
    }

    @Override
    public String getSensorID() {
        return this.sensorID;
    }

    @Override
    public Measurements getMeasurements() {
        Measurements measurements = new Measurements();
        measurements.addMeasurement(new Measurement("val1", getRandomNum()));
        measurements.addMeasurement(new Measurement("val2", getRandomNum()));
        return measurements;
    }

    private int getRandomNum() {
        int min = 0;
        int max = 100;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}