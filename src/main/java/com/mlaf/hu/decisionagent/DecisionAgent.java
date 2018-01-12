package com.mlaf.hu.decisionagent;

import com.mlaf.hu.decisionagent.behaviour.ReceiveBehaviour;
import com.mlaf.hu.decisionagent.behaviour.RegisterSensorAgentBehaviour;
import com.mlaf.hu.decisionagent.behaviour.UpdateStatusSensorAgentBehaviour;
import com.mlaf.hu.helpers.JadeServices;
import com.mlaf.hu.helpers.XmlParser;
import com.mlaf.hu.helpers.exceptions.ParseException;
import com.mlaf.hu.models.*;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.time.LocalDateTime;
import java.util.HashMap;

public abstract class DecisionAgent extends Agent {
    private static final String SERVICE_NAME = "DECISION-AGENT";
    private static final int MAX_READINGS = 100;
    public static java.util.logging.Logger decisionAgentLogger = Logger.getLogger("DecisionAgentLogger");
    public HashMap<AID, InstructionSet> sensorAgents = new HashMap<>();
    private AID brokerService;

    public DecisionAgent() {
        super();
    }

    @Override
    protected void setup() {
        try {
            JadeServices.registerAsService(SERVICE_NAME, "decision-agent", null, null, this);
            addBehaviour(new RegisterSensorAgentBehaviour(this));
            addBehaviour(new ReceiveBehaviour(this));
            addBehaviour(new UpdateStatusSensorAgentBehaviour(this, 5000L));
        } catch (Exception e) {
            DecisionAgent.decisionAgentLogger.log(Logger.SEVERE, "Could not initialize BrokerAgent", e);
            System.exit(1);
        }
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception ignore) {
        }
    }

    public void registerSensorAgent(AID sensoragent, InstructionSet instructionset) {
        this.sensorAgents.put(sensoragent, instructionset);
    }

    public void unregisterSensorAgent(AID sensoragent) {
        this.sensorAgents.remove(sensoragent);
        unregisterSensorAgentCallback(sensoragent);
    }

    public abstract void unregisterSensorAgentCallback(AID sensoragent);

    public InstructionSet parseInstructionXml(String xml) throws ParseException {
        return XmlParser.parseToObject(InstructionSet.class, xml);
    }

    public SensorReading parseSensorReadingXml(String xml) throws ParseException {
        return XmlParser.parseToObject(SensorReading.class, xml);
    }

    public void handleSensorReading(double value, InstructionSet is, Sensor sensor, String measurementId) {
        try {
            Measurement measurement = sensor.getMeasurements().getMeasurement(measurementId);
            CircularFifoQueue<Double> readings = measurement.getReadings();
            readings.add(value);
            is.setLastReceivedDataPackageAt(LocalDateTime.now());
        } catch (NullPointerException npe) {
            decisionAgentLogger.log(Logger.SEVERE, String.format("No measurement found by that ID: %s", measurementId));
        }
        storeReading(value);
    }

    public abstract void storeReading(double value);

    public void decide(double reading, Measurement measurement) {
        for (Plan plan : measurement.getPlans().getPlans()) {
            if ((measurement.getMax() * plan.getAbove() < reading) || (reading < measurement.getMax() * plan.getBelow())) {
                executePlan(plan);
            }
        }
    }

    private void executePlan(Plan plan) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setContent(plan.getMessage());
        message.addReceiver(JadeServices.getService(plan.getVia(), this));
        message.addUserDefinedParameter("to", plan.getTo());
        this.send(message);
        executePlanCallback(plan);
    }

    public abstract void executePlanCallback(Plan plan);

}