package de.mimuc.senseeverything.sensor.implementation;

import java.io.IOException;

import de.mimuc.senseeverything.activity.CONST;
import de.mimuc.senseeverything.sensor.AbstractSensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
public class OrientationSensor extends AbstractSensor implements SensorEventListener {

	private static final long serialVersionUID = 1L;
	
	private SensorManager sensorManager;

	public OrientationSensor(Context applicationContext) {
		super(applicationContext);
		TAG = getClass().getName();
		m_IsRunning = false;
		SENSOR_NAME = "Orientation Sensor";
		FILE_NAME = "orientation.csv";
		m_FileHeader = "TimeUnix,X,Y,Z,Reliable";

	}
	
	@Override
	public View getSettingsView(Context context) {
		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setPadding(20, 10, 20, 10);		
		TextView textView = new TextView(context);		
		textView.setText("Sensor delay");
		textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);		
		RadioGroup radioGroup = new RadioGroup(context);		
		RadioButton r1 = new RadioButton(context);
		r1.setText("Normal");
		radioGroup.addView(r1);		
		RadioButton r2 = new RadioButton(context);
		r2.setText("Game");
		radioGroup.addView(r2);	
		RadioButton r3 = new RadioButton(context);
		r3.setText("Fastest");
		radioGroup.addView(r3);		
		r3.setChecked(true);
		linearLayout.addView(textView);
		linearLayout.addView(radioGroup);		
		return linearLayout;
	}
	
	@Override
	public boolean isAvailable(Context context) {
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		return !(sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) == null);
	}
	@Override
	public void start(Context context) {
		super.start(context);
		if (!m_isSensorAvailable)
			return;

		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR),
				SensorManager.SENSOR_DELAY_NORMAL);		
		m_IsRunning = true;
	}
	@Override
	public void stop() {
		if(m_IsRunning) {
			m_IsRunning = false;
			sensorManager.unregisterListener(this);
			closeDataSource();
		}	
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
        Long t = System.currentTimeMillis();
		if(m_IsRunning) {
			if(event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
				onLogDataItem(t, CONST.numberFormat.format(event.values[0])+ "," +
						CONST.numberFormat.format(event.values[1]) + "," +
						CONST.numberFormat.format(event.values[2]) + ",false\n");
			} else {
				onLogDataItem(t, CONST.numberFormat.format(event.values[0]) + "," +
						CONST.numberFormat.format(event.values[1]) + "," +
						CONST.numberFormat.format(event.values[2]) + ",true\n");
			}
		}
	}
}
