package com.mamewo.stacklr;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceManager;

public class StacklrPreference
	extends PreferenceActivity
	implements OnSharedPreferenceChangeListener
{
	//TODO: move to strings.xml to assign id?
	static final
	public String PREFKEY_WIFI_ONLY = "wify_only";
	static final
	public String PREFKEY_USE_GOOGLE_TASKS = "use_google_tasks";
	static final
	public String PREFKEY_REMOVE_COMPLETED_TASK = "remove_completed_gtask";
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

	@Override
	protected void onResume(){
		super.onResume();
		pref_.registerOnSharedPreferenceChangeListener(this);
		updateSummary(pref_);
	}

	@Override
	protected void onPause(){
		super.onPause();
		pref_.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	private void updateSummary(SharedPreferences sharedPreferences){
		findPreference(PREFKEY_LOAD_CALENDAR_NAME).setSummary(sharedPreferences.getString(PREFKEY_LOAD_CALENDAR_NAME,
																						  DEFAULT_LOAD_CALENDAR_NAME));
        findPreference(PREFKEY_LOG_CALENDAR_NAME).setSummary(sharedPreferences.getString(PREFKEY_LOG_CALENDAR_NAME,
																						 DEFAULT_LOG_CALENDAR_NAME));
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateSummary(sharedPreferences);
    }  
}
