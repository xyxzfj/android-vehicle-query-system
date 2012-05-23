package whutcs.viky.viq;

import static whutcs.viky.viq.ViqCommonUtility.*;
import static whutcs.viky.viq.ViqCommonUtility.EXTRA_VEHICLE;
import static whutcs.viky.viq.ViqCommonUtility.getDataTimeString;
import static whutcs.viky.viq.ViqCommonUtility.getGpsString;
import static whutcs.viky.viq.ViqCommonUtility.getLoacalBitmap;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.SPECIAL_COLUMN_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMNS;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_BIRTH;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_DRIVING_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_GENDER;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NAME;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NOTE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_PHONE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_PHOTO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_TYPE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_VIN;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMNS;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_PHOTO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_PLACE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_TIME;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Query the database with the given licence number and show its vehicle's and
 * owner's basic information and its historical queries. If none, redirect to
 * VehicleInfoEditActivity to create one.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class VehicleItemViewActivity extends ViqShakeableListActicity {
	private static final String TAG = "VehicleItemViewActivity";

	private String mLicence;
	private String mVehicle;
	String mPhone;

	ViqSQLiteOpenHelper mHelper;

	private final String mTableName = TABLE_QUERY;

	private final String[] mFrom = TABLE_QUERY_COLUMNS;

	private final String mOrderBy = "_id DESC";

	private final int mListItemId = R.layout.vehicle_item_query_list_item;

	private final int[] mTo = new int[] { R.id.rowid, R.id.time, R.id.place,
			R.id.note, R.drawable.vehicle };

	protected int mColumnPhoto = VIEW_QUERY_INFO_COLUMN_PHOTO;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mLicence = intent.getStringExtra(EXTRA_LICENCE);
		mVehicle = intent.getStringExtra(EXTRA_VEHICLE);
		Log.v(TAG, "licence:" + mLicence);
		Log.v(TAG, "vehicle: " + mVehicle);

		if (mLicence == null) {
			startActivity(new Intent(this, VehicleInfoListActivity.class));
			finish();
			return;
		}

		if (mVehicle != null) {
			createQueryRecord();
		}

		mHelper = new ViqSQLiteOpenHelper(this);
		SQLiteDatabase database = mHelper.getReadableDatabase();

		Cursor infoCursor = database.query(TABLE_INFO, TABLE_INFO_COLUMNS,
				"licence=?", new String[] { mLicence }, null, null, null);

		setContentView(R.layout.vehicle_item_view);

		infoCursor.moveToFirst();
		mPhone = infoCursor.getString(TABLE_INFO_COLUMN_PHONE);

		((TextView) findViewById(R.id.licence)).setText(mLicence);
		((TextView) findViewById(R.id.type)).setText(infoCursor
				.getString(TABLE_INFO_COLUMN_TYPE));
		((TextView) findViewById(R.id.vin)).setText(infoCursor
				.getString(TABLE_INFO_COLUMN_VIN));
		((TextView) findViewById(R.id.name)).setText(infoCursor
				.getString(TABLE_INFO_COLUMN_NAME));
		((TextView) findViewById(R.id.phone)).setText(mPhone);
		((TextView) findViewById(R.id.gender)).setText(infoCursor
				.getString(TABLE_INFO_COLUMN_GENDER));
		((TextView) findViewById(R.id.birth)).setText(infoCursor
				.getString(TABLE_INFO_COLUMN_BIRTH));
		((TextView) findViewById(R.id.dirving_licence)).setText(infoCursor
				.getString(TABLE_INFO_COLUMN_DRIVING_LICENCE));
		((TextView) findViewById(R.id.note)).setText(infoCursor
				.getString(TABLE_INFO_COLUMN_NOTE));
		String imageName = infoCursor.getString(TABLE_INFO_COLUMN_PHOTO);
		Bitmap bitmap = getLoacalBitmap(imageName);
		if (bitmap != null) {
			((ImageView) findViewById(R.id.vehicle)).setImageBitmap(bitmap);
		}

		registerForContextMenu(getListView());
	}

	@Override
	protected void onResume() {
		super.onResume();

		refreshListView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");

		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListView()
				.getAdapter();
		Cursor cursor = adapter.getCursor();
		cursor.close();
		mHelper.close();
	}

	private void createQueryRecord() {
		String datetime = getDataTimeString();
		String gps = getGpsString(this);

		ContentValues values = new ContentValues();
		values.put(TABLE_QUERY_COLUMNS[TABLE_QUERY_COLUMN_PHOTO], mVehicle);
		values.put(TABLE_QUERY_COLUMNS[TABLE_QUERY_COLUMN_TIME],
				getDataTimeString());
		values.put(TABLE_QUERY_COLUMNS[TABLE_QUERY_COLUMN_PLACE], gps);
		values.put(SPECIAL_COLUMN_LICENCE, mLicence);

		ViqSQLiteOpenHelper helper = new ViqSQLiteOpenHelper(this);
		SQLiteDatabase database = helper.getWritableDatabase();
		long rowid = database.insert(TABLE_QUERY, null, values);
		Log.v(TAG, "rowid: " + rowid);
		database.close();
		helper.close();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_item_query_list_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = super.onContextItemSelected(item);

		AdapterView.AdapterContextMenuInfo info;
		info = (AdapterContextMenuInfo) item.getMenuInfo();

		long id = info.id;
		int position = info.position;

		// Retrieve the cursor (row) that defines this item.
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		String time = cursor.getString(TABLE_QUERY_COLUMN_TIME);

		switch (item.getItemId()) {
		case R.id.menu_edit:
			startActivity(new Intent(this, VehicleQueryEditActivity.class)
					.putExtra("id", id));
			break;
		case R.id.menu_delete:
			deleteQuery(id, time);
			break;
		default:
			break;
		}

		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_item_view_options_menu, menu);

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.menu_setup:
			startActivity(new Intent(this, ViqPreferenceActivity.class));
			break;
		case R.id.menu_call_owner:
			if (mPhone == null) {
				Toast.makeText(this, getString(R.string.no_phone_found),
						Toast.LENGTH_SHORT).show();
			} else {
				startActivity(new Intent(Intent.ACTION_CALL).setData(Uri
						.parse("tel:" + mPhone)));
			}
			break;
		case R.id.menu_add_to_vehicle_info_list:
			startActivity(new Intent(this, VehicleInfoEditActivity.class)
					.putExtra(EXTRA_LICENCE, mLicence).putExtra(EXTRA_VEHICLE,
							mVehicle));
			break;
		default:
			break;
		}

		return result;
	}

	/**
	 * Prompt the user whether to delete the query item.
	 * 
	 * @param id
	 *            the row id of the item.
	 * @param licence
	 *            the name to distinguish of the item.
	 */
	private void deleteQuery(final long id, String name) {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.delete_confirm) + " " + name)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						SQLiteDatabase database = mHelper.getWritableDatabase();
						int deleted = database.delete(TABLE_QUERY, "_id=?",
								new String[] { Long.toString(id) });
						Log.v(TAG, deleted + " deleted!");
						database.close();
						refreshListView();
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}

	/**
	 * Load or Reload the list view with the latest filtered data from the
	 * database. Update mMatchesView by the way.
	 */
	protected void refreshListView() {
		// Should never be closed explicitly until onDestroy().
		Cursor cursor;

		SQLiteDatabase database = mHelper.getReadableDatabase();
		// Get the cursor.
		cursor = database.query(mTableName, mFrom, null, null, null, null,
				mOrderBy);
		// Bind or rebind the cursor to the list adapter.
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
		if (adapter == null) {
			adapter = new SimpleCursorAdapter(this, mListItemId, cursor, mFrom,
					mTo);
			adapter.setViewBinder(new ViewBinder() {
				public boolean setViewValue(View view, Cursor cursor,
						int columnIndex) {
					boolean result = false;
					if (columnIndex == mColumnPhoto) {
						final ImageView imageView = (ImageView) view;
						final String imageName = cursor.getString(columnIndex);
						if (imageName != null) {
							// set image in new thread
							ViqImageFetchCacher fetchCacher = new ViqImageFetchCacher(
									imageName, imageView);
							fetchCacher.run();
							result = true;
						} else {
							imageView.setImageResource(R.drawable.vehicle);
						}
					}
					return result;
				}
			});

			setListAdapter(adapter);
		} else {
			// Will close the previous cursor.
			adapter.changeCursor(cursor);
			adapter.notifyDataSetChanged();
		}
		database.close();
	}
}
