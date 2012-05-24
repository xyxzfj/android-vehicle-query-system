package whutcs.viky.viq;

import static whutcs.viky.viq.ViqCommonUtilities.*;
import static whutcs.viky.viq.ViqCommonUtilities.EXTRA_VEHICLE;
import static whutcs.viky.viq.ViqCommonUtilities.getBitmapByName;
import static whutcs.viky.viq.ViqCommonUtilities.getDataTimeString;
import static whutcs.viky.viq.ViqCommonUtilities.getGpsString;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.SPECIAL_COLUMN_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMNS;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_BIRTH;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_DRIVING_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_GENDER;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NAME;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NOTE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_PHONE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_TYPE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_VIN;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMNS;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_PHOTO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_PLACE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_TIME;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_NOTE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_PLACE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_TIME;
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
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Query the database with the given row id of a vehicle Info record or licence
 * number, show the vehicle's and owner's basic information and historical
 * queries.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class VehicleItemViewActivity extends ViqShakeableListActicity {
	private static final String TAG = "VehicleItemViewActivity";

	// these fields are from the calling intent
	private Long mId;
	private String mLicence;

	// private String licence;
	private String type;
	private String vin;
	private String name;
	private String phone;
	private String gender;
	private String birth;
	private String drivingLicence;
	private String note;
	private String vehicle;

	private ViqSQLiteOpenHelper mHelper;

	private AdapterContextMenuInfo mContextMenuInfo;

	private MenuItem mAddToVehicleInfoListMenuItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mId = intent.getLongExtra(EXTRA_ID, 0L);
		mLicence = intent.getStringExtra(EXTRA_LICENCE);
		Log.v(TAG, "mId: " + mId);
		Log.v(TAG, "mLicence: " + mLicence);

		if (mId == null && mLicence == null) {
			finish();
			return;
		}

		setContentView(R.layout.vehicle_item_view);

		mHelper = new ViqSQLiteOpenHelper(this);

		registerForContextMenu(getListView());
	}

	@Override
	protected void onResume() {
		super.onResume();

		refreshItemView();
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_item_query_list_context_menu, menu);

		mContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = super.onContextItemSelected(item);

		if (item.getItemId() == R.id.menu_copy) {
			return result;
		}

		long id = mContextMenuInfo.id;
		int position = mContextMenuInfo.position;

		// Retrieve the cursor (row) that defines this item.
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		String time = cursor.getString(TABLE_QUERY_COLUMN_TIME);
		String place = cursor.getString(TABLE_QUERY_COLUMN_PLACE);
		String note = cursor.getString(TABLE_QUERY_COLUMN_NOTE);

		StringBuilder builder = new StringBuilder();
		String comma = getString(R.string.comma) + " ";
		builder.append(time).append(comma).append(place).append(comma)
				.append(note);
		String all = builder.toString();

		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		switch (item.getItemId()) {
		case R.id.menu_edit:
			startActivity(new Intent(this, VehicleQueryEditActivity.class)
					.putExtra("_id", id));
			break;
		case R.id.menu_delete:
			deleteQuery(id, time);
			break;
		case R.id.menu_copy_time:
			clipboard.setText(time);
			break;
		case R.id.menu_copy_place:
			clipboard.setText(place);
			break;
		case R.id.menu_copy_note:
			clipboard.setText(note);
			break;
		case R.id.menu_copy_all:
			clipboard.setText(all);
			break;
		case R.id.menu_sms_all:
			startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
					.putExtra("sms_body", all));
			break;
		default:
			break;
		}

		return result;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onPrepareOptionsMenu(menu);
		Log.v(TAG, "onPrepareOptionsMenu");

		// if this vehicle is not in the database, add menu
		// mAddToVehicleInfoListMenuItem
		mAddToVehicleInfoListMenuItem.setVisible(mId == 0);
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		Log.v(TAG, "onCreateOptionsMenu");

		mAddToVehicleInfoListMenuItem = menu
				.add(R.string.add_to_vehicle_info_list);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_item_view_options_menu, menu);

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);

		if (item.getItemId() == R.id.menu_copy) {
			// Let its sub menu's menu items to handle.
			return result;
		}

		if (item == mAddToVehicleInfoListMenuItem) {
			startActivity(new Intent(this, VehicleInfoEditActivity.class)
					.putExtra(EXTRA_LICENCE, mLicence));
			return result;
		}

		StringBuilder builder = new StringBuilder();
		String comma = getString(R.string.comma) + " ";
		builder.append(mLicence).append(comma).append(type).append(comma)
				.append(vin).append(comma).append(name).append(comma)
				.append(phone).append(comma).append(gender).append(comma)
				.append(birth).append(comma).append(drivingLicence)
				.append(comma).append(note);
		String all = builder.toString();

		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		switch (item.getItemId()) {
		case R.id.menu_setup:
			startActivity(new Intent(this, ViqPreferenceActivity.class));
			break;
		case R.id.menu_edit:
			startActivity(new Intent(this, VehicleInfoEditActivity.class)
					.putExtra("_id", mId));
			break;
		case R.id.menu_call_owner:
			if (phone == null || phone.length() == 0) {
				Toast.makeText(this, getString(R.string.no_phone_found),
						Toast.LENGTH_SHORT).show();
			} else {
				startActivity(new Intent(Intent.ACTION_CALL).setData(Uri
						.parse("tel:" + phone)));
			}
			break;
		case R.id.menu_sms_all:
			startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
					.putExtra("sms_body", all));
			break;

		// Menu item menu_copy's sub menu's menu items:
		case R.id.menu_copy_licence_number:
			clipboard.setText(mLicence);
			break;
		case R.id.menu_copy_vin:
			clipboard.setText(vin);
			break;
		case R.id.menu_copy_owner_name:
			clipboard.setText(name);
			break;
		case R.id.menu_copy_owner_phone_number:
			clipboard.setText(phone);
			break;
		case R.id.menu_copy_owner_gender_birth:
			clipboard.setText(gender + comma + birth);
			break;
		case R.id.menu_copy_owner_driving_licence:
			clipboard.setText(drivingLicence);
			break;
		case R.id.menu_copy_note:
			clipboard.setText(note);
			break;
		case R.id.menu_copy_all:
			clipboard.setText(all);
			break;

		default:
			break;
		}

		return result;
	}

	/**
	 * Prompt the user whether to delete the query item.
	 * 
	 * @param mId
	 *            the row mId of the item.
	 * @param licence
	 *            the name to distinguish of the item.
	 */
	private void deleteQuery(final long mId, String name) {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.delete_confirm) + " " + name)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						SQLiteDatabase database = mHelper.getWritableDatabase();
						int deleted = database.delete(TABLE_QUERY, "_id=?",
								new String[] { Long.toString(mId) });
						Log.v(TAG, deleted + " deleted!");
						database.close();
						refreshListView();
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}

	private void refreshItemView() {
		SQLiteDatabase database = mHelper.getReadableDatabase();

		Cursor infoCursor;
		if (mId != 0) {
			infoCursor = database.query(TABLE_INFO, TABLE_INFO_COLUMNS,
					"_id=?", new String[] { Long.toString(mId) }, null, null,
					null);
		} else {
			infoCursor = database.query(TABLE_INFO, TABLE_INFO_COLUMNS,
					"licence=?", new String[] { mLicence }, null, null, null);
		}
		if (infoCursor.getCount() > 0) {
			infoCursor.moveToFirst();
			mId = infoCursor.getLong(0);
			mLicence = infoCursor.getString(TABLE_INFO_COLUMN_LICENCE);
			type = infoCursor.getString(TABLE_INFO_COLUMN_TYPE);
			vin = infoCursor.getString(TABLE_INFO_COLUMN_VIN);
			name = infoCursor.getString(TABLE_INFO_COLUMN_NAME);
			phone = infoCursor.getString(TABLE_INFO_COLUMN_PHONE);
			gender = infoCursor.getString(TABLE_INFO_COLUMN_GENDER);
			birth = infoCursor.getString(TABLE_INFO_COLUMN_BIRTH);
			drivingLicence = infoCursor
					.getString(TABLE_INFO_COLUMN_DRIVING_LICENCE);
			note = infoCursor.getString(TABLE_INFO_COLUMN_NOTE);
			vehicle = infoCursor.getString(TABLE_INFO_COLUMN_PHOTO);

			((TextView) findViewById(R.id.licence)).setText(mLicence);
			((TextView) findViewById(R.id.type)).setText(type);
			((TextView) findViewById(R.id.vin)).setText(vin);
			((TextView) findViewById(R.id.name)).setText(name);
			((TextView) findViewById(R.id.phone)).setText(phone);
			((TextView) findViewById(R.id.gender)).setText(gender);
			((TextView) findViewById(R.id.birth)).setText(birth);
			((TextView) findViewById(R.id.dirving_licence))
					.setText(drivingLicence);
			((TextView) findViewById(R.id.note)).setText(note);
			Bitmap bitmap = getBitmapByName(vehicle);
			if (bitmap != null) {
				((ImageView) findViewById(R.id.vehicle)).setImageBitmap(bitmap);
			}
		}
		infoCursor.close();
		database.close();
	}

	/**
	 * Load or Reload the list view with the latest filtered data from the
	 * database. Update mMatchesView by the way.
	 */
	private void refreshListView() {
		// Should never be closed explicitly until onDestroy().
		Cursor cursor;

		SQLiteDatabase database = mHelper.getReadableDatabase();
		// Get the cursor.
		cursor = database.query(TABLE_QUERY, TABLE_QUERY_COLUMNS, "_licence=?",
				new String[] { mLicence }, null, null, "_id DESC");
		// Bind or rebind the cursor to the list adapter.
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
		if (adapter == null) {
			adapter = new SimpleCursorAdapter(this,
					R.layout.vehicle_item_query_list_item, cursor,
					TABLE_QUERY_COLUMNS, new int[] { R.id.rowid, R.id.time,
							R.id.place, R.id.note, R.id.vehicle });
			adapter.setViewBinder(new ViewBinder() {
				public boolean setViewValue(View view, Cursor cursor,
						int columnIndex) {
					boolean result = false;
					if (columnIndex == TABLE_QUERY_COLUMN_PHOTO) {
						final ImageView imageView = (ImageView) view;
						final String imageName = cursor.getString(columnIndex);
						if (imageName != null) {
							// set image in new thread
							ViqCachedImageFetcher fetchCacher = new ViqCachedImageFetcher(
									imageName, imageView);
							fetchCacher.run();
							result = true;
						} else {
							imageView.setImageResource(R.drawable.vehicle);
						}
					} else if (columnIndex == TABLE_QUERY_COLUMN_TIME) {
						TextView textView = (TextView) view;
						String relativeTime = null;
						String time = cursor.getString(columnIndex);
						if (time != null) {
							relativeTime = getRelativeTime(
									VehicleItemViewActivity.this, time);
						}
						if (relativeTime == null) {
							relativeTime = time;
						}
						textView.setText(relativeTime);
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
