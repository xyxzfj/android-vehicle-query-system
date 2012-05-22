/**
 * 
 */
package whutcs.viky.viq;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Show a list of historical queries, including the vehicle image taken or
 * selected, the licence , the owner's name and phone if can be retrieved from
 * the database, and the time and place the query happens.</br> The implemention
 * of this class is similar to VehicleInfoListActivity.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class VehicleQueryListActivity extends ListActivity {
	private static final String TAG = "VehicleQueryListActivity";

	boolean mQuickSearchEnabled = false;

	private String mDefaultTitle;

	private ListView mListView;
	private Button mViewVehicleInfoButton;
	private Button mQuickSearchButton;
	private Button mNewQueryButton;
	private EditText mFilterText;
	private TextView mMatchesView;

	private AdapterContextMenuInfo mContextMenuInfo;

	private ViqSQLiteOpenHelper mHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.vehicle_query_list);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.viq_title);

		mDefaultTitle = getString(R.string.vehicle_query_list);

		mHelper = new ViqSQLiteOpenHelper(this);

		mListView = getListView();
		mViewVehicleInfoButton = (Button) findViewById(R.id.view_vehicle_info_list);
		mQuickSearchButton = (Button) findViewById(R.id.quick_search);
		mNewQueryButton = (Button) findViewById(R.id.new_query);
		mFilterText = (EditText) findViewById(R.id.filter);
		mMatchesView = (TextView) findViewById(R.id.matches);

		mFilterText.setText(mDefaultTitle);

		registerForContextMenu(mListView);

		mListView.setOnTouchListener(new OnListViewTouchListener());
		mViewVehicleInfoButton
				.setOnClickListener(new OnViewVehicleInfoButtonClickListener());
		mQuickSearchButton
				.setOnClickListener(new OnQuickSearchButtonClickListener());
		mNewQueryButton.setOnClickListener(new OnNewQueryButtonClickListener());
		mFilterText.addTextChangedListener(new FilterTextWatcher());
	}

	@Override
	protected void onResume() {
		super.onResume();

		refreshListView();

		// Set the text of the new query button according the preference
		// "query method".
		String queryMethod = ViqPreferenceActivity.getQueryMethod(this);
		mNewQueryButton.setText(queryMethod);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		SimpleCursorAdapter adapter = (SimpleCursorAdapter) mListView
				.getAdapter();
		Cursor cursor = adapter.getCursor();
		cursor.close();
		mHelper.close();
	}

	class OnViewVehicleInfoButtonClickListener implements
			android.view.View.OnClickListener {
		public void onClick(View v) {
			startActivity(new Intent(VehicleQueryListActivity.this,
					VehicleInfoListActivity.class));
			finish();
		}
	}

	class OnQuickSearchButtonClickListener implements
			android.view.View.OnClickListener {

		public void onClick(View v) {
			switchQuickSearchMood();
		}
	}

	class OnNewQueryButtonClickListener implements
			android.view.View.OnClickListener {
		public void onClick(View v) {
			startActivity(new Intent(VehicleQueryListActivity.this,
					VehicleLicenceInputActivity.class));
		}
	}

	class FilterTextWatcher implements TextWatcher {

		public void afterTextChanged(Editable s) {

		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			refreshListView();
		}

	}

	class OnListViewTouchListener implements OnTouchListener {

		public boolean onTouch(View v, MotionEvent event) {
			if (mQuickSearchEnabled) {
				switchQuickSearchMood();
			}
			return false;
		}

	}

	private void switchQuickSearchMood() {
		mQuickSearchEnabled = !mQuickSearchEnabled;
		mQuickSearchButton.setTextColor(mQuickSearchEnabled ? Color.GREEN
				: Color.WHITE);

		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		if (mQuickSearchEnabled) {
			if (mFilterText.getText().toString().equals(mDefaultTitle)) {
				mFilterText.setText("");
			}
			// mFilterText.setFocusable(true);
			mFilterText.setFocusableInTouchMode(true);
			mFilterText.requestFocus();

			inputMethodManager.showSoftInput(mFilterText, 0);
		} else {
			if (mFilterText.getText().toString().length() == 0) {
				mFilterText.setText(mDefaultTitle);
			}
			mFilterText.setFocusable(false);
			// mFilterText.setFocusableInTouchMode(false);

			inputMethodManager.hideSoftInputFromWindow(
					mFilterText.getWindowToken(), 0);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_query_list_context_menu, menu);

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
		String licence = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_LICENCE);
		String name = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_NAME);
		String phone = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_PHONE);
		String time = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_TIME);
		String place = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_PLACE);
		String note = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_NOTE);

		StringBuilder builder = new StringBuilder();
		String comma = getString(R.string.comma) + " ";
		builder.append(licence).append(comma).append(name).append(comma)
				.append(phone).append(comma).append(time).append(comma)
				.append(place).append(comma).append(note);
		String all = builder.toString();

		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		switch (item.getItemId()) {
		case R.id.menu_view:
			startActivity(new Intent(this, VehicleItemViewActivity.class)
					.putExtra("licence", licence));
			break;
		case R.id.menu_edit:
			startActivity(new Intent(this, VehicleQueryEditActivity.class)
					.putExtra("_id", id));
			break;
		case R.id.menu_delete:
			deleteQuery(id, licence);
			break;
		case R.id.menu_call_owner:
			startActivity(new Intent(Intent.ACTION_CALL).setData(Uri
					.parse("tel:" + phone)));
			break;

		// Sub menu items of menu item menu_copy:
		case R.id.menu_copy_licence_number:
			clipboard.setText(licence);
			break;
		case R.id.menu_copy_owner_name:
			clipboard.setText(name);
			break;
		case R.id.menu_copy_owner_phone_number:
			clipboard.setText(phone);
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
		case R.id.menu_sms_vehicle_query:
			startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
					.putExtra("sms_body", all));
			break;
		default:
			break;
		}

		Log.v(TAG, clipboard.getText().toString());
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_query_list_options_menu, menu);

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.menu_setup:
			startActivity(new Intent(this, ViqPreferenceActivity.class));
			break;
		default:
			break;
		}

		return result;
	}

	/**
	 * Display the clicked query.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// Retrieve the cursor (row) that defines the clicked item.
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		// Get the licence field of the clicked item
		String licence = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_LICENCE);

		startActivity(new Intent(this, VehicleItemViewActivity.class).putExtra(
				"licence", licence));
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

	private String getFilter() {
		String filter = mFilterText.getText().toString().toUpperCase();
		if (filter.equals(mDefaultTitle)) {
			filter = "";
		}
		return filter;
	}

	/**
	 * Load or Reload the list view with the latest filtered data from the
	 * database. Update mMatchesView by the way.
	 */
	private void refreshListView() {
		String filter = getFilter();
		Cursor cursor;

		SQLiteDatabase database = mHelper.getReadableDatabase();
		if (filter.length() == 0) {
			cursor = database.query(ViqSQLiteOpenHelper.VIEW_QUERY_INFO,
					ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMNS, null, null,
					null, null, "_id DESC");
		} else {
			// Find records which matches the filter.
			String selection = ViqSQLiteOpenHelper.VIEW_QUERY_INFO_SELECTION;
			String[] selectionArgs = ViqSQLiteOpenHelper
					.getViewQueryInfoSelectionArgs(filter);
			cursor = database.query(ViqSQLiteOpenHelper.VIEW_QUERY_INFO,
					ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMNS, selection,
					selectionArgs, null, null, "licence");
		}
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
		if (adapter == null) {
			// TODO: R.id.vehicle_image
			adapter = new SimpleCursorAdapter(this,
					R.layout.vehicle_query_list_item, cursor,
					ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMNS, new int[] {
							R.id.rowid, R.id.licence, R.id.name, R.id.phone,
							R.id.time, R.id.place, R.id.note,
							R.drawable.vehicle });
			setListAdapter(adapter);
		} else {
			adapter.changeCursor(cursor);
			adapter.notifyDataSetChanged();
		}
		database.close();

		mMatchesView.setText("" + cursor.getCount());
	}
}
