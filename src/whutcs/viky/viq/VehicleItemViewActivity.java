package whutcs.viky.viq;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Query the database with the given licence number and show its vehicle's and
 * owner's basic information and its historical queries.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class VehicleItemViewActivity extends ListActivity {
	private static final String TAG = "VehicleItemViewActivity";

	String mLicence;
	String mPhone = null;

	ViqSQLiteOpenHelper mHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vehicle_item_view);

		mLicence = getIntent().getStringExtra("licence");
		Log.v(TAG, "licence:" + mLicence);
		if (mLicence == null) {
			Toast.makeText(this,
					"No licence specified! Will goto vehicle database.",
					Toast.LENGTH_SHORT).show();
			startActivity(new Intent(this, VehicleInfoListActivity.class));
			finish();
			return;
		}

		mHelper = new ViqSQLiteOpenHelper(this);
		SQLiteDatabase database = mHelper.getReadableDatabase();

		LinearLayout basicInfoLayout = (LinearLayout) findViewById(R.id.basic_info);
		Cursor infoCursor = database.query(ViqSQLiteOpenHelper.TABLE_INFO,
				ViqSQLiteOpenHelper.TABLE_INFO_COLUMNS_CONCERNED, "licence=?",
				new String[] { mLicence }, null, null, null);
		if (infoCursor.getCount() > 0) {
			basicInfoLayout.setVisibility(View.VISIBLE);
			infoCursor.moveToFirst();
			mPhone = infoCursor
					.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_PHONE);

			((TextView) findViewById(R.id.licence)).setText(mLicence);
			((TextView) findViewById(R.id.type)).setText(infoCursor
					.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_TYPE));
			((TextView) findViewById(R.id.vin)).setText(infoCursor
					.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_VIN));
			((TextView) findViewById(R.id.name)).setText(infoCursor
					.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NAME));
			((TextView) findViewById(R.id.phone)).setText(mPhone);
			((TextView) findViewById(R.id.gender)).setText(infoCursor
					.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_GENDER));
			((TextView) findViewById(R.id.birth)).setText(infoCursor
					.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_BIRTH));
			((TextView) findViewById(R.id.dirving_licence))
					.setText(infoCursor
							.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_DRIVING_LICENCE));
			((TextView) findViewById(R.id.note)).setText(infoCursor
					.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NOTE));
		} else {
			basicInfoLayout.setVisibility(View.GONE);
		}

		Cursor queryCursor = database
				.query(ViqSQLiteOpenHelper.TABLE_QUERY,
						ViqSQLiteOpenHelper.TABLE_QUERY_COLUMNS_CONCERNED,
						"_licence=?", new String[] { mLicence }, null, null,
						"_id DESC");
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.vehicle_item_query_list_item, queryCursor,
				ViqSQLiteOpenHelper.TABLE_QUERY_COLUMNS_CONCERNED, new int[] {
						R.id.rowid, R.id.time, R.id.place, R.id.note,
						R.drawable.vehicle });
		setListAdapter(adapter);
		database.close();

		registerForContextMenu(getListView());
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
		String time = cursor
				.getString(ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_TIME);

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
		inflater.inflate(R.menu.vehicle_item_query_list_options_menu, menu);

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
						int deleted = database.delete(
								ViqSQLiteOpenHelper.TABLE_QUERY, "_id=?",
								new String[] { Long.toString(id) });
						Log.v(TAG, deleted + " deleted!");
						database.close();
						refreshListView();
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}

	/**
	 * Refresh the list view with the latest data from the database.
	 */
	protected void refreshListView() {
		SQLiteDatabase database = mHelper.getReadableDatabase();
		Cursor queryCursor = database
				.query(ViqSQLiteOpenHelper.TABLE_QUERY,
						ViqSQLiteOpenHelper.TABLE_QUERY_COLUMNS_CONCERNED,
						"_licence=?", new String[] { mLicence }, null, null,
						"_id DESC");
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
		adapter.changeCursor(queryCursor);
		adapter.notifyDataSetChanged();
		database.close();
	}
}
