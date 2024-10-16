package de.mimuc.senseeverything.sensor.implementation;

import android.content.Context;
import android.view.View;

import de.mimuc.senseeverything.sensor.AbstractSensor;

public class InteractionLogSensor extends AbstractSensor {
    public InteractionLogSensor(Context applicationContext) {
        super(applicationContext);
        TAG = getClass().getName();
        SENSOR_NAME = "Interaction Log";
    }

    @Override
    public View getSettingsView(Context context) {
        return null;
    }

    @Override
    public boolean isAvailable(Context context) {
        return true;
    }

    @Override
    public boolean availableForPeriodicSampling() {
        return false;
    }

    @Override
    public void stop() {
        m_IsRunning = false;
        closeDataSource();
    }

    @Override
    public void start(Context context) {
        super.start(context);
        m_IsRunning = true;
    }

    public void logInteractionStart() {
        onLogDataItem(System.currentTimeMillis(), "start");
    }

    public void logContinued() {
        onLogDataItem(System.currentTimeMillis(), "confirm");
    }

    public void logInteractionEnd() {
        onLogDataItem(System.currentTimeMillis(), "end");
    }

    public void logNoInteraction() {
        onLogDataItem(System.currentTimeMillis(), "noInteraction");
    }

    public void logInteractionAsked() {
        onLogDataItem(System.currentTimeMillis(), "asked");
    }
}