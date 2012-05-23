package whutcs.viky.viq;

import static whutcs.viky.viq.ViqCommonUtility.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

/**
 * Show a list of all vehicles' information, the basic information of the
 * vehicle and the owner, including the vehicle's licence, type,vin, the owner's
 * name, phone, gender, birth, driving licence, and the note.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class VehicleInfoListActivity extends ViqBaseShakeableListActivity {
	private static final String TAG = "VehicleInfoListActivity";
	private AdapterContextMenuInfo mContextMenuInfo;

	@Override
	protected void setDefaultTitle() {
		setDefaultTitle(getString(R.string.vehicle_info_list));
	}

	@Override
	protected void setTableName() {
		setTableName(TABLE_INFO);
	}

	@Override
	protected void setFrom() {
		setFrom(TABLE_INFO_COLUMNS);
	}

	@Override
	protected void setTo() {
		setTo(new int[] { R.id.rowid, R.id.licence, R.id.type, R.id.vin,
				R.id.name, R.id.phone, R.id.gender, R.id.birth,
				R.id.dirving_licence, R.id.note, R.id.vehicle });
	}

	@Override
	protected void setSelection() {
		setSelection(TABLE_INFO_SELECTION);
	}

	@Override
	protected void setSelectionArgs() {
		setSelectionArgs(getTableInfoSelectionArgs(getFilter()));
	}

	@Override
	protected void setOrderBy() {
		setOrderBy(TABLE_INFO_COLUMNS[TABLE_INFO_COLUMN_LICENCE]);
	}

	@Override
	protected void setColumnPhoto() {
		setColumnPhoto(TABLE_INFO_COLUMN_PHOTO);
	}

	@Override
	protected void setListItemId() {
		setListItemId(R.layout.vehicle_info_list_item);
	}

	@Override
	protected void setForwardButtonListener() {
		setForwardButtonListener(new OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(VehicleInfoListActivity.this,
						VehicleQueryListActivity.class));
				// Switch rather than move from one activity to another.
				finish();
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Log.v(TAG, "onCreateContextMenu");

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_info_list_context_menu, menu);

		mContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = super.onContextItemSelected(item);
		Log.v(TAG, "onContextItemSelected");

		if (item.getItemId() == R.id.menu_copy) {
			// Let its sub menu's menu items to handle.
			return result;
		}

		// AdapterView.AdapterContextMenuInfo mContextMenuInfo;
		// mContextMenuInfo = (AdapterContextMenuInfo) item.getMenuInfo();

		long id = mContextMenuInfo.id;
		int position = mContextMenuInfo.position;

		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		String _id = cursor.getString(cursor.getColumnIndex("_id"));
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
					.putExtra(EXTRA_LICENCE, licence));
			break;
		case R.id.menu_edit:
			startActivity(new Intent(this, VehicleInfoEditActivity.class)
					.putExtra("_id", id));
			break;
		case R.id.menu_delete:
			deleteItem(id, licence);
			break;
		case R.id.menu_call_owner:
			startActivity(new Intent(Intent.ACTION_CALL).setData(Uri
					.parse("tel:" + phone)));
			break;
		case R.id.menu_sms_vehicle_info:
			startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
					.putExtra("sms_body", all));
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
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		String licence = cursor.getString(TABLE_INFO_COLUMN_LICENCE);

		startActivity(new Intent(this, VehicleItemViewActivity.class).putExtra(
				EXTRA_LICENCE, licence));
	}

	@Override
	protected void deleteItem(final long id, String name) {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.delete_confirm) + " " + name)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								ViqSQLiteOpenHelper helper = new ViqSQLiteOpenHelper(
										VehicleInfoListActivity.this);
								SQLiteDatabase database = helper
										.getWritableDatabase();
								int deleted = database.delete(TABLE_INFO,
										"_id=?",
										new String[] { Long.toString(id) });
								Log.v(TAG, deleted + " deleted!");
								database.close();
								helper.close();
								refreshListView();
							}

						}).setNegativeButton(R.string.cancel, null).show();

	}

}
