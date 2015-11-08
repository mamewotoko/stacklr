package com.mamewo.stacklr;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

public class StacklrPreference
	extends PreferenceActivity
			//implements OnPreferenceClickListener
			   //View.OnClickListener,
			   //OnSharedPreferenceChangeListener
{
	static final
	public String PREFKEY_WIFI_ONLY = "wify_only";
	static final
	public String PREFKEY_USE_GOOGLE_TASKS = "use_google_tasks";
	static final
	public String PREFKEY_USE_GOOGLE_CALENDAR = "use_google_calendar";
	private SharedPreferences pref_;
	private Preference version_;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pref_ = PreferenceManager.getDefaultSharedPreferences(this);
		addPreferencesFromResource(R.xml.preference);
		version_ = findPreference("version");
		PackageInfo pi;
		try {
			pi = getPackageManager().getPackageInfo(StacklrExpActivity.PACKAGE_NAME, 0);
			version_.setSummary(pi.versionName);
		}
		catch (NameNotFoundException e) {
			version_.setSummary("unknown");
		}
	}
}

