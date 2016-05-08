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
	//TODO: move to strings.xml to assign id?
	static final
	public String PREFKEY_WIFI_ONLY = "wify_only";
	static final
	public String PREFKEY_USE_GOOGLE_TASKS = "use_google_tasks";
	static final
	public String PREFKEY_REMOVE_COMPELTED_TASK = "remove_completed_gtask";
	static final
	public String PREFKEY_LOAD_CALENDAR = "load_google_calendar";
	static final
	public boolean DEFAULT_LOAD_CALENDAR = false;
	static final
	public String PREFKEY_LOAD_CALENDAR_NAME = "load_calendar_name";
	static final
	public String DEFAULT_LOAD_CALENDAR_NAME = "stacklr";
	static final
	public String PREFKEY_LOG_CALENDAR = "log_google_calendar";
	static final
	public boolean DEFAULT_LOG_CALENDAR = false;
	static final
	public String PREFKEY_LOG_CALENDAR_NAME = "log_calendar_name";
	static final
	public String DEFAULT_LOG_CALENDAR_NAME = "stacklr_done";

	private SharedPreferences pref_;
	private Preference version_;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pref_ = PreferenceManager.getDefaultSharedPreferences(this);
		//TODO: unrecommended API
		addPreferencesFromResource(R.xml.preference);
		//TODO: unrecommended API
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

