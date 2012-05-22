package whutcs.viky.viq;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

/**
 * Update or create a vehicle info record.
 * 
 * @author Administrator
 * 
 */
public class VehicleInfoEditActivity extends Activity {
	private static final String TAG = "VehicleInfoEditActivity";

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

	private Long mID;

	// Values from the database.
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

		mImageView = (ImageView) findViewById(R.id.vehicle_image);

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

		mID = getIntent().getLongExtra("_id", 0);
		Log.v(TAG, "_id got:" + mID);

		if (mID != 0) {
			SQLiteOpenHelper helper = new ViqSQLiteOpenHelper(this);
			SQLiteDatabase database = helper.getReadableDatabase();
			Cursor cursor = database.query(ViqSQLiteOpenHelper.TABLE_INFO,
					ViqSQLiteOpenHelper.TABLE_INFO_COLUMNS_CONCERNED, "_id=?",
					new String[] { Long.toString(mID) }, null, null, null);
			Log.v(TAG, "cursor count: " + cursor.getCount());
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();

				licence = cursor
						.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_LICENCE);
				type = cursor
						.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_TYPE);
				vin = cursor
						.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_VIN);
				name = cursor
						.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NAME);
				phone = cursor
						.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_PHONE);
				gender = cursor
						.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_GENDER);
				birth = cursor
						.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_BIRTH);
				drivingLicence = cursor
						.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_DRIVING_LICENCE);
				note = cursor
						.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NOTE);

				setTexts();
			}
			cursor.close();
			database.close();
			helper.close();

		}

		mImageView.setOnClickListener(new OnImageViewClickListener());
	}

	class OnImageViewClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * Set the edit texts' text to values from the database.
	 */
	private void setTexts() {
		mLicenceText.setText(licence);
		mTypeText.setText(type);
		mVinText.setText(vin);
		mNameText.setText(name);
		mPhoneText.setText(phone);
		mGenderMaleButton.setChecked(gender.equals(getString(R.string.male)));
		mGenderFemaleButton.setChecked(gender
				.equals(getString(R.string.female)));
		mBirthText.setText(birth);
		mDrivingLicenceText.setText(drivingLicence);
		mNoteText.setText(note);
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
		case R.id.menu_revert:
			setTexts();
			finish();
			Log.v(TAG, "finished");
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
		values.put("licence", mLicenceText.getText().toString());
		values.put("type", mTypeText.getText().toString());
		values.put("vin", mVinText.getText().toString());
		values.put("name", mNameText.getText().toString());
		values.put("phone", mPhoneText.getText().toString());
		values.put(
				"gender",
				mGenderMaleButton.isChecked() ? getString(R.string.male)
						: mGenderFemaleButton.isChecked() ? getString(R.string.female)
								: null);
		values.put("birth", mBirthText.getText().toString());
		values.put("driving_licence", mDrivingLicenceText.getText().toString());
		values.put("note", mNoteText.getText().toString());

		SQLiteOpenHelper helper = new ViqSQLiteOpenHelper(this);
		SQLiteDatabase database = helper.getWritableDatabase();

		long result;
		if (mID != 0) {
			result = database.update(ViqSQLiteOpenHelper.TABLE_INFO, values,
					"_id=?", new String[] { Long.toString(mID) });
			Log.v(TAG, "rows updated: " + result);

		} else {
			result = database.insert(ViqSQLiteOpenHelper.TABLE_INFO, null,
					values);
			Log.v(TAG, "row id inserted: " + result);

		}
		database.close();
		helper.close();
	}
}
