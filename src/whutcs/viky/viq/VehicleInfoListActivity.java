package whutcs.viky.viq;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

/**
 * Show a list of all vehicles' information, the basic information of the
 * vehicle and the owner, including the vehicle's licence, type,vin, the owner's
 * name, phone, gender, birth, driving licence, and the note.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class VehicleInfoListActivity extends VehicleBaseListActivity {
	private static final String TAG = "VehicleInfoListActivity";

	boolean mQuickSearchEnabled = false;

	private String mDefaultTitle;

	private ListView mListView;
	private Button mViewVehicleQueryButton;
	private Button mQuickSearchButton;
	private Button mNewQueryButton;
	private EditText mFilterText;
	private TextView mMatchesView;

	private AdapterContextMenuInfo mContextMenuInfo;

	private ViqSQLiteOpenHelper mHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.vehicle_info_list);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.viq_title);

		mDefaultTitle = getString(R.string.vehicle_info_list);

		mHelper = new ViqSQLiteOpenHelper(this);

		mListView = getListView();
		mViewVehicleQueryButton = (Button) findViewById(R.id.view_vehicle_query_list);
		mQuickSearchButton = (Button) findViewById(R.id.quick_search);
		mNewQueryButton = (Button) findViewById(R.id.new_query);
		mFilterText = (EditText) findViewById(R.id.filter);
		mMatchesView = (TextView) findViewById(R.id.matches);

		mFilterText.setText(mDefaultTitle);

		registerForContextMenu(mListView);

		mListView.setOnTouchListener(new OnListViewTouchListener());
		mViewVehicleQueryButton
				.setOnClickListener(new OnViewVehicleQueryButtonClickListener());
		mQuickSearchButton
				.setOnClickListener(new OnQuickSearchButtonClickListener());
		mNewQueryButton.setOnClickListener(new OnNewQueryButtonClickListener());
		mFilterText.addTextChangedListener(new FilterTextWatcher());
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.v(TAG, "onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.v(TAG, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");

		refreshListView();

		// Set the text of the new query button according the preference
		// "query method".
		String queryMethod = ViqPreferenceActivity.getQueryMethod(this);
		mNewQueryButton.setText(queryMethod);

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.v(TAG, "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.v(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");

		SimpleCursorAdapter adapter = (SimpleCursorAdapter) mListView
				.getAdapter();
		Cursor cursor = adapter.getCursor();
		cursor.close();
		mHelper.close();
	}

	class OnViewVehicleQueryButtonClickListener implements
			android.view.View.OnClickListener {
		public void onClick(View v) {
			startActivity(new Intent(VehicleInfoListActivity.this,
					VehicleQueryListActivity.class));
			// Switch rather than move from one activity to another.
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
			startActivity(new Intent(VehicleInfoListActivity.this,
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
			// Close the quick search function if opened.
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
		String licence = cursor
				.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_LICENCE);
		String type = cursor
				.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_TYPE);
		String vin = cursor
				.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_VIN);
		String name = cursor
				.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NAME);
		String phone = cursor
				.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_PHONE);
		String gender = cursor
				.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_GENDER);
		String birth = cursor
				.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_BIRTH);
		String drivingLicence = cursor
				.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_DRIVING_LICENCE);
		String note = cursor
				.getString(ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_NOTE);

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
					.putExtra("licence", licence));
			break;
		case R.id.menu_edit:
			startActivity(new Intent(this, VehicleInfoEditActivity.class)
					.putExtra("_id", id));
			break;
		case R.id.menu_delete:
			deleteInfo(id, licence);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		Log.v(TAG, "onCreateOptionsMenu");

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_info_list_options_menu, menu);

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		Log.v(TAG, "onOptionsItemSelected");

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
		Log.v(TAG, "onListItemClick");

		// Retrieve the cursor (row) that defines the clicked item.
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		// Get the licence field of the clicked item
		String licence = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_LICENCE);

		startActivity(new Intent(this, VehicleItemViewActivity.class).putExtra(
				"licence", licence));
	}

	/**
	 * Prompt the user whether to delete the info item.
	 * 
	 * @param id
	 *            the row id of the item.
	 * @param name
	 *            the name to distinguish of the item.
	 */
	private void deleteInfo(final long id, String name) {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.delete_confirm) + " " + name)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						SQLiteDatabase database = mHelper.getWritableDatabase();
						int deleted = database.delete(
								ViqSQLiteOpenHelper.TABLE_INFO, "_id=?",
								new String[] { Long.toString(id) });
						Log.v(TAG, deleted + " deleted!");
						database.close();
						refreshListView();
					}

				}).setNegativeButton(R.string.cancel, null).show();
	}

	/**
	 * Get the filter text from mFilterText.
	 * 
	 * @return the filter text from mFilterText; "" if mFilterText equals
	 *         mDefaultTitle.
	 */
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
		Log.v(TAG, "refreshListView");

		String filter = getFilter();
		// Should never be closed explicitly until onDestroy().
		Cursor cursor;

		SQLiteDatabase database = mHelper.getReadableDatabase();
		// Get the cursor.
		if (filter.length() == 0) {
			cursor = database.query(ViqSQLiteOpenHelper.TABLE_INFO,
					ViqSQLiteOpenHelper.TABLE_INFO_COLUMNS_CONCERNED, null,
					null, null, null, "licence");
		} else {
			String selection = ViqSQLiteOpenHelper.TABLE_INFO_SELECTION;
			String[] selectionArgs = ViqSQLiteOpenHelper
					.getTableInfoSelectionArgs(filter);
			cursor = database.query(ViqSQLiteOpenHelper.TABLE_INFO,
					ViqSQLiteOpenHelper.TABLE_INFO_COLUMNS_CONCERNED,
					selection, selectionArgs, null, null, "licence");
		}
		// Bind or rebind the cursor to the list adapter.
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
		if (adapter == null) {
			adapter = new SimpleCursorAdapter(this,
					R.layout.vehicle_info_list_item, cursor,
					ViqSQLiteOpenHelper.TABLE_INFO_COLUMNS_CONCERNED,
					new int[] { R.id.rowid, R.id.licence, R.id.type, R.id.vin,
							R.id.name, R.id.phone, R.id.gender, R.id.birth,
							R.id.dirving_licence, R.id.note, R.id.vehicle });
			adapter.setViewBinder(new ViewBinder() {
				public boolean setViewValue(View view, Cursor cursor,
						int columnIndex) {
					boolean result = false;
					if (columnIndex == ViqSQLiteOpenHelper.TABLE_INFO_COLUMN_PHOTO) {
						ImageView imageView = (ImageView) view;
						String imagePath = cursor.getString(columnIndex);
						if (imagePath != null) {
							BitmapDrawable drawable = new BitmapDrawable(
									imagePath);
							imageView.setImageDrawable(drawable);
							result = true;
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

		// Update mMatchesView to show the latest record count.
		mMatchesView.setText("" + cursor.getCount());
	}

}
