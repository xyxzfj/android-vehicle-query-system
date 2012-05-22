package whutcs.viky.viq;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ViqPreferenceActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.viq_preference);

	}

	public static String getQueryMethod(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(context.getString(R.string.query_method),
						context.getString(R.string.take_licence_image));
	}

}
