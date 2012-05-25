package whutcs.viky.viq;

import static whutcs.viky.viq.ViqCommonUtilities.EXTRA_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_LICENCE;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Base class handling common list related tasks.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public abstract class ViqBaseShakeableListActivity extends
		ViqShakeableListActicity {
	private static final String TAG = "ViqBaseShakeableListActivity";

	// view settings
	protected int mLayoutResID = R.layout.vehicle_item_list;
	protected int mListItemId;
	protected String mDefaultTitle;

	// database constants
	protected String mViewName;
	protected String mTableName;
	protected String[] mFrom;
	protected int[] mTo;
	protected String mSelection;
	protected String mOrderBy;
	protected int mColumnLicence;

	// views
	protected ListView mListView;

	protected Button mForwardButton;

	protected Button mQuickSearchButton;
	protected Button mNewQueryButton;
	protected EditText mFilterText;
	protected TextView mMatchesView;

	protected ViqSQLiteOpenHelper mHelper;

	protected boolean mQuickSearchEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(mLayoutResID);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.viq_title);

		mHelper = new ViqSQLiteOpenHelper(this);

		mListView = getListView();

		mForwardButton = (Button) findViewById(R.id.forward);
		mQuickSearchButton = (Button) findViewById(R.id.quick_search);
		mNewQueryButton = (Button) findViewById(R.id.new_query);
		mFilterText = (EditText) findViewById(R.id.filter);
		mMatchesView = (TextView) findViewById(R.id.matches);

		mFilterText.setText(mDefaultTitle);

		registerForContextMenu(mListView);

		mListView.setOnTouchListener(new OnListViewTouchListener());

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

		refreshNewQueryButtonText();
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

		// close the binding cursor
		Cursor cursor = null;
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) mListView
				.getAdapter();
		if (adapter != null) {
			cursor = adapter.getCursor();
		}
		if (cursor != null) {
			cursor.close();
		}

		mHelper.close();
	}

	protected class OnQuickSearchButtonClickListener implements
			android.view.View.OnClickListener {

		public void onClick(View v) {
			toggleQuickSearchMode();
		}
	}

	protected class OnNewQueryButtonClickListener implements
			android.view.View.OnClickListener {
		public void onClick(View v) {
			startActivity(new Intent(ViqBaseShakeableListActivity.this,
					VehicleLicenceInputActivity.class));
		}
	}

	protected class FilterTextWatcher implements TextWatcher {

		protected String mPreviousText;

		public void afterTextChanged(Editable s) {
			String currentText = s.toString();
			// if text changed just because quick search mode is toggled on or
			// off, do not refresh
			if (mPreviousText.equals(mDefaultTitle)
					&& currentText.length() == 0 || mPreviousText.length() == 0
					&& currentText.equals(mDefaultTitle)) {
				return;
			}
			refreshListView();
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			mPreviousText = s.toString();
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

	}

	protected class OnListViewTouchListener implements OnTouchListener {

		public boolean onTouch(View v, MotionEvent event) {
			// toggle off quick search mode if opened.
			if (mQuickSearchEnabled) {
				toggleQuickSearchMode();
			}
			return false;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		Log.v(TAG, "onCreateOptionsMenu");

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.viq_base_list_options_menu, menu);

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
	 * Display the clicked item.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "onListItemClick");

		// Retrieve the cursor (row) that defines the clicked item.
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		// Get the licence field of the clicked item
		String licence = cursor.getString(mColumnLicence);

		startActivity(new Intent(this, VehicleItemViewActivity.class).putExtra(
				EXTRA_LICENCE, licence));
	}

	/**
	 * Refresh the text of the new query button according the preference
	 * "query method".
	 */
	protected void refreshNewQueryButtonText() {
		String queryMethod = ViqPreferenceActivity.getQueryMethod(this);
		mNewQueryButton.setText(queryMethod);

	}

	protected void toggleQuickSearchMode() {
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

	/**
	 * Get the filter text from mFilterText.
	 * 
	 * @return the text of mFilterText; "" if mFilterText equals mDefaultTitle.
	 */
	protected String getFilter() {
		String filter = "";
		if (mFilterText.getText() != null) {
			filter = mFilterText.getText().toString().toUpperCase();
		}
		if (filter.equals(mDefaultTitle)) {
			filter = "";
		}
		return filter;
	}

	protected void deleteItem(final long id, String name) {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.delete_confirm) + " " + name)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								ViqSQLiteOpenHelper helper = new ViqSQLiteOpenHelper(
										ViqBaseShakeableListActivity.this);
								SQLiteDatabase database = helper
										.getWritableDatabase();
								int deleted = database.delete(mTableName,
										"_id=?",
										new String[] { Long.toString(id) });
								Log.v(TAG, deleted + " deleted!");
								database.close();
								helper.close();
								refreshListView();
							}

						}).setNegativeButton(R.string.cancel, null).show();

	}

	/**
	 * Load or Reload the list view with the latest filtered data from the
	 * database. Update mMatchesView by the way.
	 */
	protected void refreshListView() {

	}

}
