package whutcs.viky.viq;

import android.app.ListActivity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Vibrator;

/**
 * A list activity responding to shaking movements.
 * 
 * @author xyxzfj@gmail.com
 * 
 */

public abstract class ViqShakeableListActicity extends ListActivity implements
		SensorEventListener, LocationListener {
	// private static final String TAG = "VehicleBaseListActicity";

	private SensorManager mSensorManager;
	private Vibrator mVibrator;
	private static int SHAKE_THRESHOULD = 16;
	/**
	 * to stop from multiple responding
	 */
	protected boolean shakeAlreadyResponded;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

		updateGpsLocation(this);
	}

	/**
	 * Update GPS location for later getting the latest location.
	 * 
	 * @param context
	 */
	private void updateGpsLocation(Context context) {
		// update location
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		LocationProvider gpsProvider = locationManager
				.getProvider(LocationManager.GPS_PROVIDER);
		if (gpsProvider != null) {
			String providerName = gpsProvider.getName();
			locationManager.requestLocationUpdates(providerName, 1000, 5, this);
			try {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 1000, 5, this);
			} catch (RuntimeException e) {
				// If anything at all goes wrong with getting a cell location do
				// not abort. Cell location is not essential to this app.
			}
		}

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

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
