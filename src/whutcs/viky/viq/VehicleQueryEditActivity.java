package whutcs.viky.viq;

import static whutcs.viky.viq.ViqCommonUtilities.*;
import static whutcs.viky.viq.ViqCommonUtilities.EXTRA_LICENCE;
import static whutcs.viky.viq.ViqCommonUtilities.EXTRA_VEHICLE;
import static whutcs.viky.viq.ViqCommonUtilities.getBitmapByName;
import static whutcs.viky.viq.ViqCommonUtilities.getDataTimeString;
import static whutcs.viky.viq.ViqCommonUtilities.getGpsString;
import static whutcs.viky.viq.ViqCommonUtilities.getNewImageFile;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.SPECIAL_COLUMN_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NOTE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMNS;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_NOTE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_PHOTO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_PLACE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_TIME;

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
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Update or create a mVehicle query record.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class VehicleQueryEditActivity extends Activity {
	private static final String TAG = "VehicleQueryEditActivity";

	private ImageView mImageView;
	private EditText mLicenceText;
	private EditText mTimeText;
	private EditText mPlaceText;
	private EditText mNoteText;

	private Long mID;

	private String mVehicle;
	private String mLicence;
	private String mTime;
	private String mPlace;
	private String mNote;

	private boolean mSaveOnFinish = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vehicle_query_edit);

		mImageView = (ImageView) findViewById(R.id.vehicle);
		mLicenceText = (EditText) findViewById(R.id.licence);
		mTimeText = (EditText) findViewById(R.id.time);
		mPlaceText = (EditText) findViewById(R.id.place);
		mNoteText = (EditText) findViewById(R.id.note);

		Intent intent = getIntent();
		mID = intent.getLongExtra(EXTRA_ID, 0);
		Log.v(TAG, "_id got:" + mID);

		if (mID != 0) {
			SQLiteOpenHelper helper = new ViqSQLiteOpenHelper(this);
			SQLiteDatabase database = helper.getReadableDatabase();
			Cursor cursor = database.query(TABLE_QUERY, null, "_id=?",
					new String[] { Long.toString(mID) }, null, null, null);
			Log.v(TAG, "cursor count: " + cursor.getCount());
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				mVehicle = cursor.getString(TABLE_QUERY_COLUMN_PHOTO);
				mLicence = cursor.getString(TABLE_QUERY_COLUMN_LICENCE);
				mTime = cursor.getString(TABLE_QUERY_COLUMN_TIME);
				mPlace = cursor.getString(TABLE_QUERY_COLUMN_PLACE);
				mNote = cursor.getString(TABLE_QUERY_COLUMN_NOTE);
			}
			cursor.close();
			database.close();
			helper.close();
		} else {
			mVehicle = intent.getStringExtra(EXTRA_VEHICLE);
			Log.v(TAG, "mVehicle: " + mVehicle);
			mLicence = intent.getStringExtra(EXTRA_LICENCE);
			Log.v(TAG, "mLicence: " + mLicence);

			mTime = getDataTimeString();
			mPlace = getGpsString(this);
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
		mTimeText.setText(mTime);
		mPlaceText.setText(mPlace);
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
		inflater.inflate(R.menu.vehicle_query_edit_options_menu, menu);

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
			ContentValues values = new ContentValues();
			values.put(TABLE_QUERY_COLUMNS[TABLE_QUERY_COLUMN_PHOTO], mVehicle);
			if (mLicenceText.getText() != null) {
				values.put(SPECIAL_COLUMN_LICENCE, mLicenceText.getText()
						.toString());
			}
			if (mTimeText.getText() != null) {
				values.put(TABLE_QUERY_COLUMNS[TABLE_QUERY_COLUMN_TIME],
						mTimeText.getText().toString());
			}
			if (mPlaceText.getText() != null) {
				values.put(TABLE_QUERY_COLUMNS[TABLE_QUERY_COLUMN_PLACE],
						mPlaceText.getText().toString());
			}
			if (mNoteText.getText() != null) {
				values.put(TABLE_QUERY_COLUMNS[TABLE_QUERY_COLUMN_NOTE],
						mNoteText.getText().toString());
			}

			SQLiteOpenHelper helper = new ViqSQLiteOpenHelper(this);
			SQLiteDatabase database = helper.getWritableDatabase();

			long result;
			if (mID != 0) {
				result = database.update(TABLE_QUERY, values, "_id=?",
						new String[] { Long.toString(mID) });
				Log.v(TAG, "rows updated: " + result);

			} else {
				result = database.insert(TABLE_QUERY, null, values);
				Log.v(TAG, "row id inserted: " + result);

			}
			database.close();
			helper.close();

			Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT)
					.show();
		}

	}
}
