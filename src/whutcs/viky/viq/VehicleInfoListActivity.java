package whutcs.viky.viq;

import static whutcs.viky.viq.ViqCommonUtilities.EXTRA_ID;
import static whutcs.viky.viq.ViqCommonUtilities.EXTRA_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMNS;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_BIRTH;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_DRIVING_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_GENDER;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NAME;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NOTE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_PHONE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_PHOTO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_TYPE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_VIN;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_INFO_SELECTION;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.getSelectiionArgs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Show a list of vehicle info (info of the vehicle itself and its owner) items,
 * including the vehicle's image, licence, type, vin, the owner's name, phone,
 * gender, birth, driving licence, and the note.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class VehicleInfoListActivity extends ViqBaseShakeableListActivity {
	private static final String TAG = "VehicleInfoListActivity";

	private AdapterContextMenuInfo mContextMenuInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mDefaultTitle = getString(R.string.vehicle_info_list);
		mForwardButtonText = getString(R.string.vehicle_query_list);
		mForwardClass = VehicleQueryListActivity.class;
		mWriteableTableName = TABLE_INFO;

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		mContextMenuInfo = (AdapterContextMenuInfo) menuInfo;

		int position = mContextMenuInfo.position;
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		String licence = cursor.getString(TABLE_INFO_COLUMN_LICENCE);

		menu.setHeaderTitle(licence);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_info_list_context_menu, menu);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = super.onContextItemSelected(item);

		if (item.getItemId() == R.id.menu_copy) {
			// Let its sub menu's menu items to handle.
			return result;
		}

		// AdapterView.AdapterContextMenuInfo mContextMenuInfo;
		// mContextMenuInfo = (AdapterContextMenuInfo) item.getMenuInfo();

		long id = mContextMenuInfo.id;
		int position = mContextMenuInfo.position;

		// Retrieve the cursor (row) that defines this item.
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		String _id = cursor.getString(cursor.getColumnIndex(EXTRA_ID));
		Log.v(TAG, "_id at position " + position + ", id " + id + " is " + _id);
		String licence = cursor.getString(TABLE_INFO_COLUMN_LICENCE);
		String type = cursor.getString(TABLE_INFO_COLUMN_TYPE);
		String vin = cursor.getString(TABLE_INFO_COLUMN_VIN);
		String name = cursor.getString(TABLE_INFO_COLUMN_NAME);
		String phone = cursor.getString(TABLE_INFO_COLUMN_PHONE);
		String gender = cursor.getString(TABLE_INFO_COLUMN_GENDER);
		String birth = cursor.getString(TABLE_INFO_COLUMN_BIRTH);
		String drivingLicence = cursor
				.getString(TABLE_INFO_COLUMN_DRIVING_LICENCE);
		String note = cursor.getString(TABLE_INFO_COLUMN_NOTE);

		StringBuilder builder = new StringBuilder();
		String comma = getString(R.string.comma) + " ";
		builder.append(licence).append(comma).append(type).append(comma)
				.append(vin).append(comma).append(name).append(comma)
				.append(phone).append(comma).append(gender).append(comma)
				.append(birth).append(comma).append(drivingLicence)
				.append(comma).append(note);
		String all = builder.toString();

		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		switch (item.getItemId()) {
		case R.id.menu_view:
			startActivity(new Intent(this, VehicleItemViewActivity.class)
					.putExtra(EXTRA_ID, id));
			break;
		case R.id.menu_edit:
			startActivity(new Intent(this, VehicleInfoEditActivity.class)
					.putExtra(EXTRA_ID, id));
			break;
		case R.id.menu_delete:
			deleteItem(id, licence);
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
		case R.id.menu_fast_check:
			startActivity(new Intent(this, VehicleQueryEditActivity.class)
					.putExtra(EXTRA_LICENCE, licence));
			break;

		// Menu item menu_copy's sub menu's menu items:
		case R.id.menu_copy_licence_number:
			clipboard.setText(licence);
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

		Log.v(TAG, clipboard.getText().toString());
		return result;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		startActivity(new Intent(this, VehicleItemViewActivity.class).putExtra(
				EXTRA_ID, id));
	}

	@Override
	protected void refreshListView() {
		super.refreshListView();

		String filter = getFilter();
		// Should never be closed explicitly until onDestroy().
		Cursor cursor;

		SQLiteDatabase database = mHelper.getReadableDatabase();
		// Get the cursor.
		if (filter.length() == 0) {
			cursor = database.query(TABLE_INFO, TABLE_INFO_COLUMNS, null, null,
					null, null, TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_LICENCE]);
		} else {
			String[] selectionArgs = getSelectiionArgs(getFilter(),
					TABLE_INFO_COLUMNS);
			cursor = database.query(TABLE_INFO, TABLE_INFO_COLUMNS,
					TABLE_INFO_SELECTION, selectionArgs, null, null,
					TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_LICENCE]);
		}
		// Bind or rebind the cursor to the list adapter.
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
		if (adapter == null) {
			adapter = new SimpleCursorAdapter(this,
					R.layout.vehicle_info_list_item, cursor,
					TABLE_INFO_COLUMNS, new int[] { R.id.rowid, R.id.licence,
							R.id.type, R.id.vin, R.id.name, R.id.phone,
							R.id.gender, R.id.birth, R.id.dirving_licence,
							R.id.note, R.id.vehicle });
			adapter.setViewBinder(new ViewBinder() {
				public boolean setViewValue(View view, Cursor cursor,
						int columnIndex) {
					boolean result = false;
					if (columnIndex == TABLE_INFO_COLUMN_PHOTO) {
						final ImageView imageView = (ImageView) view;
						final String imageName = cursor.getString(columnIndex);
						if (imageName != null) {
							// set image in new thread
							ViqCachedImageFetcher fetchCacher = new ViqCachedImageFetcher(
									imageName, imageView);
							fetchCacher.run();
							result = true;
						}
					} else if (columnIndex == TABLE_INFO_COLUMN_BIRTH) {
						TextView textView = (TextView) view;
						String age = null;
						String birth = cursor.getString(columnIndex);
						SimpleDateFormat format = new SimpleDateFormat("yyyy-M");
						Date date = null;
						try {
							date = format.parse(birth);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						if (date != null) {
							age = Long.toString((System.currentTimeMillis() - date
									.getTime()) / DateUtils.YEAR_IN_MILLIS);
						}
						if (age == null) {
							age = birth;
						}
						textView.setText(age);
						result = true;
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

		// Update mMatchesView to show the latest record count.
		mMatchesView.setText("" + cursor.getCount());

	}
}
