package whutcs.viky.viq;

import static whutcs.viky.viq.ViqCommonUtilities.*;

import java.io.File;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
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
 * Update or create a mVehicle info record.
 * 
 * @author xyxzfj@gmail.com
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
	private RadioButton mGenderMaleButton, mGenderFemaleButton;
	private EditText mBirthText;
	private EditText mDrivingLicenceText;
	private EditText mNoteText;

	private Long mID;

	private String mVehicle;
	private String mLicence;
	private String mType;
	private String mVin;
	private String mName;
	private String mPhone;
	private String mGender;
	private String mBirth;
	private String mDrivingLicence;
	private String mNote;

	private boolean mSaveOnFinish = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mID = intent.getLongExtra(EXTRA_ID, 0);
		Log.v(TAG, "_id got:" + mID);

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

		if (mID != 0) {
			SQLiteOpenHelper helper = new ViqSQLiteOpenHelper(this);
			SQLiteDatabase database = helper.getReadableDatabase();
			Cursor cursor = database.query(TABLE_INFO, TABLE_INFO_COLUMNS,
					"_id=?", new String[] { Long.toString(mID) }, null, null,
					null);
			Log.v(TAG, "cursor count: " + cursor.getCount());
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				mVehicle = cursor.getString(TABLE_INFO_COLUMN_PHOTO);
				mLicence = cursor.getString(TABLE_INFO_COLUMN_LICENCE);
				mType = cursor.getString(TABLE_INFO_COLUMN_TYPE);
				mVin = cursor.getString(TABLE_INFO_COLUMN_VIN);
				mName = cursor.getString(TABLE_INFO_COLUMN_NAME);
				mPhone = cursor.getString(TABLE_INFO_COLUMN_PHONE);
				mGender = cursor.getString(TABLE_INFO_COLUMN_GENDER);
				mBirth = cursor.getString(TABLE_INFO_COLUMN_BIRTH);
				mDrivingLicence = cursor
						.getString(TABLE_INFO_COLUMN_DRIVING_LICENCE);
				mNote = cursor.getString(TABLE_INFO_COLUMN_NOTE);
			}
			cursor.close();
			database.close();
			helper.close();
		} else {
			mVehicle = intent.getStringExtra(EXTRA_VEHICLE);
			Log.v(TAG, "mVehicle: " + mVehicle);
			mLicence = intent.getStringExtra(EXTRA_LICENCE);
			Log.v(TAG, "mLicence: " + mLicence);
			mType = null;
			mVehicle = null;
			mName = null;
			mPhone = null;
			mGender = null;
			mBirth = null;
			mDrivingLicence = null;
			mNote = null;
		}

		setViews();

		mImageView.setOnClickListener(new OnImageViewClickListener());
	}

	class OnImageViewClickListener implements OnClickListener {

		public void onClick(View v) {
			selectVehicleImage();
		}

	}

	/**
	 * Get a vehicle image by selecting a image file in local mStorage.
	 */
	private void selectVehicleImage() {
		Intent selectVehicleImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
		selectVehicleImageIntent.setType("image/**");
		startActivityForResult(selectVehicleImageIntent, CODE_SELECT_PHOTO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CODE_SELECT_PHOTO:
				Uri vehicleImageUri = data.getData();
				String selectImageName = uriToImagePath(this, vehicleImageUri);
				File srcFile = new File(selectImageName);
				File vehicleImageFile = getNewImageFile();
				fileCopy(srcFile, vehicleImageFile);
				mVehicle = vehicleImageFile.getName();
				setImageView();
				break;
			default:
				break;
			}
		}
	}

	private void setViews() {
		setImageView();
		mLicenceText.setText(mLicence);
		mTypeText.setText(mType);
		mVinText.setText(mVin);
		mNameText.setText(mName);
		mPhoneText.setText(mPhone);
		if (mGender != null) {
			boolean genderMale = mGender.equals(getString(R.string.male));
			mGenderMaleButton.setChecked(genderMale);
			mGenderFemaleButton.setChecked(!genderMale);
		}
		mBirthText.setText(mBirth);
		mDrivingLicenceText.setText(mDrivingLicence);
		mNoteText.setText(mNote);
	}

	private void setImageView() {
		Bitmap bitmap = getBitmapByName(mVehicle);
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
		case R.id.menu_cancel:
			mSaveOnFinish = false;
			finish();
			break;
		case R.id.menu_save:
			mSaveOnFinish = true;
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

		if (mSaveOnFinish) {
			// Save to database.
			// If save on stop, VehicleInfoListActivity's onResume is called
			// before
			// this's onStop, the info list won't be updated.

			ContentValues values = new ContentValues();
			values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_PHOTO], mVehicle);
			if (mLicenceText.getText() != null) {
				values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_LICENCE],
						mLicenceText.getText().toString());
			}
			if (mTypeText.getText() != null) {
				values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_TYPE],
						mTypeText.getText().toString());
			}
			if (mVinText.getText() != null) {
				values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_VIN], mVinText
						.getText().toString());
			}
			if (mNameText.getText() != null) {
				values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_NAME],
						mNameText.getText().toString());
			}
			if (mPhoneText.getText() != null) {
				values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_PHONE],
						mPhoneText.getText().toString());
			}
			values.put(
					TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_GENDER],
					mGenderMaleButton.isChecked() ? getString(R.string.male)
							: mGenderFemaleButton.isChecked() ? getString(R.string.female)
									: null);
			if (mBirthText.getText() != null) {
				values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_BIRTH],
						mBirthText.getText().toString());
			}
			if (mDrivingLicenceText.getText() != null) {
				values.put(
						TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_DRIVING_LICENCE],
						mDrivingLicenceText.getText().toString());
			}
			if (mNoteText.getText() != null) {
				values.put(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_NOTE],
						mNoteText.getText().toString());
			}

			SQLiteOpenHelper helper = new ViqSQLiteOpenHelper(this);
			SQLiteDatabase database = helper.getWritableDatabase();

			long result;
			if (mID != 0) {
				result = database.update(TABLE_INFO, values, "_id=?",
						new String[] { Long.toString(mID) });
				Log.v(TAG, "rows updated: " + result);

			} else {
				mID = database.insert(TABLE_INFO, null, values);
				Log.v(TAG, "row id inserted: " + mID);

			}
			database.close();
			helper.close();

			Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT)
					.show();
		}

	}
}
