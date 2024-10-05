package de.mimuc.senseeverything.sensor.implementation;

import android.content.Context;
import android.view.View;

import de.mimuc.senseeverything.sensor.AbstractSensor;

public class NotificationSensor extends AbstractSensor {
    public NotificationSensor(Context applicationContext) {
        super(applicationContext);
        TAG = getClass().getName();
        SENSOR_NAME = "Notification";
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
    public boolean availableForContinuousSampling() {
        return true;
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
}