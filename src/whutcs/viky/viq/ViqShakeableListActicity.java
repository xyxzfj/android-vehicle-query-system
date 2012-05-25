package whutcs.viky.viq;

import android.app.ListActivity;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;

/**
 * A list activity responding to shaking movements.
 * 
 * @author xyxzfj@gmail.com
 * 
 */

public abstract class ViqShakeableListActicity extends ListActivity implements
		SensorEventListener {
	// private static final String TAG = "VehicleBaseListActicity";

	private SensorManager mSensorManager;
	private Vibrator mVibrator;
	private static int SHAKE_THRESHOULD = 16;
	/**
	 * to stop from multiple responding
	 */
	private boolean shakeAlreadyResponded;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		shakeAlreadyResponded = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		if (ViqPreferenceActivity.isQuickQueryEnabled(this)) {
			int sensorType = event.sensor.getType();

			float[] values = event.values;

			if (sensorType == Sensor.TYPE_ACCELEROMETER) {
				if ((Math.abs(values[0]) > SHAKE_THRESHOULD
						|| Math.abs(values[1]) > SHAKE_THRESHOULD || Math
						.abs(values[2]) > SHAKE_THRESHOULD)) {
					if (!shakeAlreadyResponded) {
						mVibrator.vibrate(500);
						startActivity(new Intent(this,
								VehicleLicenceInputActivity.class));
						shakeAlreadyResponded = true;
					}
				}
			}

		}
	}

}
