package whutcs.viky.viq;

import static whutcs.viky.viq.ViqCommonUtilities.*;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Update or create a vehicle info record.
 * 
 * @author Administrator
 * 
 */
public class VehicleInfoEditActivity extends Activity {
	private static final String TAG = "VehicleInfoEditActivity";

	// views in the UI:

	private ImageView mImageView;

	private EditText mLicenceText;
	private EditText mTypeText;
	private EditText mVinText;
	private EditText mNameText;
	private EditText mPhoneText;
	private RadioButton mGenderMaleButton;
	private RadioButton mGenderFemaleButton;
	private EditText mBirthText;
	private EditText mDrivingLicenceText;
	private EditText mNoteText;

	/**
	 * row id of the current Info record from the calling intent
	 */
	private Long mID;

	// values from the database:

	private String vehicle;
	private String licence;

	private String type;
	private String vin;
	private String name;
	private String phone;
	private String gender;
	private String birth;
	private String drivingLicence;
	private String note;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vehicle_info_edit);

		mImageView = (ImageView) findViewById(R.id.vehicle);

		mLicenceText = (EditText) findViewById(R.id.licence);
		mTypeText = (EditText) findViewById(R.id.type);
		mVinText = (EditText) findViewById(R.id.vin);
		mNameText = (EditText) findViewById(R.id.name);
		mPhoneText = (EditText) findViewById(R.id.phone);
		mGenderFemaleButton = (RadioButton) findViewById(R.id.gender);
		mGenderMaleButton = (RadioButton) findViewById(R.id.gender_male);
		mGenderFemaleButton = (RadioButton) findViewById(R.id.gender_female);
		mBirthText = (EditText) findViewById(R.id.birth);
		mDrivingLicenceText = (EditText) findViewById(R.id.driving_licence);
		mNoteText = (EditText) findViewById(R.id.note);

		Intent intent = getIntent();
		mID = intent.getLongExtra("_id", 0);
		Log.v(TAG, "_id got:" + mID);

		if (mID != 0) {
			SQLiteOpenHelper helper = new ViqSQLiteOpenHelper(this);
			SQLiteDatabase database = helper.getReadableDatabase();
			Cursor cursor = database.query(TABLE_INFO, TABLE_INFO_COLUMNS,
					"_id=?", new String[] { Long.toString(mID) }, null, null,
					null);
			Log.v(TAG, "cursor count: " + cursor.getCount());
			cursor.moveToFirst();

			licence = cursor.getString(TABLE_INFO_COLUMN_LICENCE);
			type = cursor.getString(TABLE_INFO_COLUMN_TYPE);
			vin = cursor.getString(TABLE_INFO_COLUMN_VIN);
			name = cursor.getString(TABLE_INFO_COLUMN_NAME);
			phone = cursor.getString(TABLE_INFO_COLUMN_PHONE);
			gender = cursor.getString(TABLE_INFO_COLUMN_GENDER);
			birth = cursor.getString(TABLE_INFO_COLUMN_BIRTH);
			drivingLicence = cursor
					.getString(TABLE_INFO_COLUMN_DRIVING_LICENCE);
			note = cursor.getString(TABLE_INFO_COLUMN_NOTE);
			vehicle = cursor.getString(TABLE_INFO_COLUMN_PHOTO);
			cursor.close();
			database.close();
			helper.close();

		} else {
			vehicle = intent.getStringExtra(EXTRA_VEHICLE);
			Log.v(TAG, "vehicle: " + vehicle);
			licence = intent.getStringExtra(EXTRA_LICENCE);
			Log.v(TAG, "licence: " + licence);

			type = "";
			vin = "";
			name = "";
			phone = "";
			gender = "";
			birth = "";
			drivingLicence = "";
			note = "";
		}

		setViews();

		mImageView.setOnClickListener(new OnImageViewClickListener());
	}

	class OnImageViewClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO: replace the vehicle image of the Info record
		}

	}

	private void setViews() {
		mLicenceText.setText(licence);
		mTypeText.setText(type);
		mVinText.setText(vin);
		mNameText.setText(name);
		mPhoneText.setText(phone);
		if (gender != null) {
			boolean genderMale = gender.equals(getString(R.string.male));
			mGenderMaleButton.setChecked(genderMale);
			mGenderFemaleButton.setChecked(!genderMale);
		}
		mBirthText.setText(birth);
		mDrivingLicenceText.setText(drivingLicence);
		mNoteText.setText(note);
		Bitmap bitmap = getBitmapByName(vehicle);
		if (bitmap != null) {
			mImageView.setImageBitmap(bitmap);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_info_edit_options_menu, menu);

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.menu_save:
			finish();
			break;
		case R.id.menu_revert:
			setViews();
			break;

		default:
			break;
		}

		return result;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.v(TAG, "onPause");
		// Save to database.
		// If save on stop, VehicleInfoListActivity's onResume is called before
		// this's onStop, the info list won't be updated.

		ContentValues values = new ContentValues();
		values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_LICENCE], mLicenceText
				.getText().toString());
		values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_TYPE], mTypeText
				.getText().toString());
		values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_VIN], mVinText
				.getText().toString());
		values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_NAME], mNameText
				.getText().toString());
		values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_PHONE], mPhoneText
				.getText().toString());
		values.put(
				TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_GENDER],
				mGenderMaleButton.isChecked() ? getString(R.string.male)
						: mGenderFemaleButton.isChecked() ? getString(R.string.female)
								: null);
		values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_BIRTH], mBirthText
				.getText().toString());
		values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_DRIVING_LICENCE],
				mDrivingLicenceText.getText().toString());
		values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_NOTE], mNoteText
				.getText().toString());
		values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_PHOTO], vehicle);

		SQLiteOpenHelper helper = new ViqSQLiteOpenHelper(this);
		SQLiteDatabase database = helper.getWritableDatabase();

		long result;
		if (mID != 0) {
			result = database.update(TABLE_INFO, values, "_id=?",
					new String[] { Long.toString(mID) });
			Log.v(TAG, "rows updated: " + result);

		} else {
			result = database.insert(TABLE_INFO, null, values);
			Log.v(TAG, "row id inserted: " + result);

		}
		database.close();
		helper.close();

		Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT)
				.show();
	}
}
