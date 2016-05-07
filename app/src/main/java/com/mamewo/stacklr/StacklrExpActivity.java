package com.mamewo.stacklr;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.text.DateFormat;

import java.io.File;

import android.app.Dialog;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import android.preference.PreferenceManager;
import android.accounts.AccountManager;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
import java.util.Collections;
import java.util.Set;

//import android.os.Debug;
import android.os.Handler;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.DatePicker;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import static com.mamewo.stacklr.Constant.*;

import android.graphics.Color;

public class StacklrExpActivity
	extends Activity
	implements TextView.OnEditorActionListener
{
	static final private int SPEECH_RECOGNITION_REQUEST_CODE = 2222;
	static final private long LOAD_MIN_INTERVAL = 180*1000;
	final static
	public String PACKAGE_NAME = StacklrExpActivity.class.getPackage().getName();

	static public
	final String CALENDAR_NAME = "stacklr";

	//pref
	private static final String PREF_ACCOUNT_NAME = "accountName";
	private static final String PREF_LAST_LOAD_TIME = "lastLoadTime";

	final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
	List<Group> groups_;
	public int numAsyncTasks;
	private long lastLoadTime_;
	public SharedPreferences pref_;

	//private Handler handler_;
	//private Runnable fileSaver_;
	private boolean groupLoaded_ = false;
	//end of tasks

	private final int[] RADIO_ID = new int[]{
		R.id.radio_to_buy,
		R.id.radio_stock,
		R.id.radio_shelf,
		R.id.radio_history,
		R.id.radio_later
	};

	private ExpandableListView listView_;
	private EditText targetEditText_;
	public ExpandableAdapter adapter_;
	private Intent speechIntent_;
	private File datadir_;
	private GoogleAccountCredential credential_;
	//TODO: move to expandable adapter
	public com.google.api.services.tasks.Tasks service_;
	public com.google.api.services.calendar.Calendar calendarService_;
	public boolean accountPickerCanceled_;

	private List<Group> getGroups(){
		return groups_;
	}

	/** Check that Google Play services APK is installed and up to date. */
	private boolean checkGooglePlayServicesAvailable() {
		final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
			showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
			//TODO: toast?
			return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            return false;
		}
		return true;
	}

	public boolean isWifiAvaiable(){
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		if(activeInfo != null
		   && activeInfo.getType() == ConnectivityManager.TYPE_WIFI){
			return true;
		}
		return false;
	}

	private void refreshTasks(boolean loadGroups, boolean force) {
		if(credential_.getSelectedAccountName() == null){
			return;
		}
		setTitle("stacklr "+credential_.getSelectedAccountName());
		adapter_.stackLater();

		boolean wifiOnly = pref_.getBoolean(StacklrPreference.PREFKEY_WIFI_ONLY, false);

		if(wifiOnly && !isWifiAvaiable()){
			showMessage(getString(R.string.wifi_is_not_available));
			return;
		}
		//TODO: use string resource for title
		long now = System.currentTimeMillis();
		if((!force) && now-lastLoadTime_ < LOAD_MIN_INTERVAL){
			Log.d(TAG, "short interval return " + (now-lastLoadTime_));
			return;
		}
		//TODO: set lastLoadTime_ only if load successed
		lastLoadTime_ = now;
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		//TODO: latest item time in gtask
		editor.putLong(PREF_LAST_LOAD_TIME, lastLoadTime_);
		editor.commit();

		boolean useTasks = pref_.getBoolean(StacklrPreference.PREFKEY_USE_GOOGLE_TASKS, true);			
		if(useTasks){
			if(loadGroups){
				AsyncLoadGroupTask.run(this);
			}
			else {
				startLoadTask(false);
			}
		}
		boolean useCalendar = pref_.getBoolean(StacklrPreference.PREFKEY_USE_GOOGLE_CALENDAR, true);
		if(useCalendar){
			//TODO: config calendar name
			AsyncLoadGoogleCalendarListTask.run(this,
												CALENDAR_NAME,
												new AsyncLoadGoogleCalendarListTask.CalendarIdRunnable() {
													@Override
													public void run(String calendarName, String calendarId){
														AsyncLoadGoogleCalendarTask.run(StacklrExpActivity.this, calendarId);
													}
												});
		}
	}

	void refreshView() {
		adapter_.notifyDataSetChanged();
	}

	private void chooseAccount() {
		accountPickerCanceled_ = false;
		startActivityForResult(credential_.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}

	void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
		runOnUiThread(new Runnable(){
				@Override
				public void run(){
					Dialog dialog =
						GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, StacklrExpActivity.this,
															  REQUEST_GOOGLE_PLAY_SERVICES);
					dialog.show();
				}
			});
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//		Debug.startMethodTracing("stacklr");
		long t1 = System.nanoTime();
		super.onCreate(savedInstanceState);

		//load default preferences from xml
		PreferenceManager.setDefaultValues(this, R.xml.preference, false);
		long t2 = System.nanoTime();

		accountPickerCanceled_ = false;
		pref_ = PreferenceManager.getDefaultSharedPreferences(this);
		//trace is saved as /sdcard/stacklr.trace

		//TODO: load from file or savedInstanceState
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		lastLoadTime_ = settings.getLong(PREF_LAST_LOAD_TIME, 0);

		long t3 = System.nanoTime();
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main_expandable);
		long t4 = System.nanoTime();

		PackageManager m = getPackageManager();
		String s = getPackageName();
		try {
			PackageInfo p = m.getPackageInfo(s, 0);
			datadir_ = new File(p.applicationInfo.dataDir);
		}
		catch (NameNotFoundException e) {
			Log.w(TAG, "Error Package name not found ", e);
		}
		long t5 = System.nanoTime();
		//---------------
		//gtasks & google calendar
		credential_ =
		 	GoogleAccountCredential.usingOAuth2(this, Arrays.asList(TasksScopes.TASKS, CalendarScopes.CALENDAR_READONLY));
		
		credential_.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
		//TODO: rename
		service_ =
			new com.google.api.services.tasks.Tasks.Builder(httpTransport, jsonFactory, credential_)
            .setApplicationName("Stacklr/0.2").build();
		calendarService_ =
			new com.google.api.services.calendar.Calendar.Builder(httpTransport, jsonFactory, credential_)
			.setApplicationName("Stacklr/0.2").build();

		long t6 = System.nanoTime();
		groups_ = Group.load(datadir_);
		if(groups_ == null){
			String[] groupNames = getResources().getStringArray(R.array.groups);
			groups_ = new ArrayList<Group>();
			for(String name: groupNames){
				groups_.add(new Group(name, null));
			}
		}
		if(! "Shelf".equals(groups_.get(2).getName())){
			//add is not supported?
			groups_.add(2, new Group("Shelf", ""));
		}
		if(! "Later".equals(groups_.get(groups_.size()-1).getName())){
			groups_.add(new Group("Later", ""));
		}
		//TODO: sync group(upload)
		//---------------
		// line based
		targetEditText_ = (EditText) findViewById(R.id.target_text_view);
		targetEditText_.setOnEditorActionListener(this);
		targetEditText_.setOnTouchListener(new MicClickListener(targetEditText_));
		Button pushButton = (Button) findViewById(R.id.push_button);
		pushButton.setOnClickListener(new PushButtonListener());

		long t7 = System.nanoTime();

		adapter_ = new ExpandableAdapter(this, groups_);
		long t8 = System.nanoTime();

		//TODO: show load toast?
		listView_ = (ExpandableListView) findViewById(R.id.expandableListView1);
		ItemClickListener listener = new ItemClickListener();
		listView_.setOnChildClickListener(listener);
		listView_.setOnItemLongClickListener(listener);
		listView_.setAdapter(adapter_);
		speechIntent_ = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speechIntent_.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							   RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		long t9 = System.nanoTime();

		//TODO: add option
		for(int i = 0; i < groups_.size(); i++){
			if(Constant.DEFAULT_GROUP_OPEN[i]){
				listView_.expandGroup(i);
			}
			else {
				listView_.collapseGroup(i);
			}
		}
		long t10 = System.nanoTime();
		Intent intent = getIntent();
		Set<String> categories = intent.getCategories();
		Log.d(TAG, "onCreate check intent: "+intent+" "+categories);
		//TODO: filtered by the 2nd filter
		//TODO: just save, do not move stacklr UI to front
		if("com.google.android.gm.action.AUTO_SEND".equals(intent.getAction())){
			adapter_.pushToBuyAsText(intent.getStringExtra("android.intent.extra.TEXT"));
		}
		//Debug.stopMethodTracing();
		
		Log.d(TAG, "perf,1,"+t1);
		Log.d(TAG, "perf,2,"+t2);
		Log.d(TAG, "perf,3,"+t3);
		Log.d(TAG, "perf,4,"+t4);
		Log.d(TAG, "perf,5,"+t5);
		Log.d(TAG, "perf,6,"+t6);
		Log.d(TAG, "perf,7,"+t7);
		Log.d(TAG, "perf,8,"+t8);
		Log.d(TAG, "perf,9,"+t9);
		Log.d(TAG, "perf,10,"+t10);
		//t3-t4: 50%
		//t7-t8: 30%
		
		//handler_ = new Handler();
		// fileSaver_ = new Runnable(){
		// 		@Override
		// 		public void run(){
		// 			Log.d(TAG, "saver run");
		// 			long now = System.currentTimeMillis();
		// 			//nearly idle 
		// 			//count modified items and large enough
		// 			long lastModifiedTime = adapter_.lastModifiedTime();
		// 			long lastSavedTime = adapter_.lastSavedTime();
		// 			if(lastSavedTime < lastModifiedTime && now-adapter_.lastModifiedTime() >= 3000){
		// 				Log.d(TAG, "save data to file");
		// 				adapter_.save();
		// 			}
		// 			handler_.postDelayed(fileSaver_, 3000);
		// 		}
		// 	};
		// handler_.postDelayed(fileSaver_, 8000);
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean useTasks = pref_.getBoolean(StacklrPreference.PREFKEY_USE_GOOGLE_TASKS, false);
		boolean useCalendar = pref_.getBoolean(StacklrPreference.PREFKEY_USE_GOOGLE_CALENDAR, false);
	
		if ((useTasks || useCalendar) && checkGooglePlayServicesAvailable()) {
			// check if there is already an account selected
			if (credential_.getSelectedAccountName() == null && (!accountPickerCanceled_)) {
				// ask user to choose account
				chooseAccount();
			}
			else {
				boolean loadGroups = false;
				for(Group group: groups_){
					String gid = group.getGtaskListId();
					if(gid == null || "".equals(gid)){
						loadGroups = true;
						break;
					}
				}
				refreshTasks(loadGroups, loadGroups);
			}
		}
	}

	@Override
	protected void onStop() {
		long lastModifiedTime = adapter_.lastModifiedTime();
		long lastSavedTime = adapter_.lastSavedTime();
		Log.d(TAG, "onStop: " + (lastModifiedTime - lastSavedTime));
		if(lastSavedTime < lastModifiedTime){
			Log.d(TAG, "onStop: saved");
			adapter_.save();
		}
		//TODO: if group changed?
		Group.save(datadir_, groups_);
		super.onStop();
	}
	
	public class PushButtonListener
		implements View.OnClickListener
	{
		@Override
		public void onClick(View v) {
			String itemname = targetEditText_.getText().toString();
			if (itemname.isEmpty()) {
				return;
			}
			adapter_.pushToBuyAsText(itemname);
			targetEditText_.setText("");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = false;
		switch (item.getItemId()) {
		case R.id.save_menu:
			adapter_.save();
			handled = true;
			break;
		case R.id.reload_menu:
			//TODO: display force load dialog
			refreshTasks(true, true);
			handled = true;
			break;
		case R.id.preference_menu:
			startActivity(new Intent(this, StacklrPreference.class));
			break;
		default:
			break;
		}
		return handled;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_GOOGLE_PLAY_SERVICES:
			if(resultCode != RESULT_OK){
				checkGooglePlayServicesAvailable();
			}
			break;
		case REQUEST_AUTHORIZATION:
			if(resultCode != RESULT_OK) {
				chooseAccount();
			}
			break;
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
				String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					credential_.setSelectedAccountName(accountName);
					SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(PREF_ACCOUNT_NAME, accountName);
					editor.commit();
				}
			}
			else if(resultCode == Activity.RESULT_CANCELED){
				accountPickerCanceled_ = true;
			}
			break;
		case SPEECH_RECOGNITION_REQUEST_CODE:
			if (resultCode != RESULT_OK) {
				break;
			}
			// TODO: select good one or display list dialog
			List<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (matches.isEmpty()) {
				break;
			}
			targetEditText_.setText(matches.get(0));
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE
			|| actionId == EditorInfo.IME_NULL) {
			String itemname = v.getText().toString();
			if(itemname.length() == 0){
				return true;
			}
			adapter_.pushToBuyAsText(itemname);
			v.setText("");
			return true;
		}
		return false;
	}

	private class MicClickListener
		extends RightDrawableOnTouchListener
	{
		public MicClickListener(TextView view) {
			super(view);
		}

		@Override
		public boolean onDrawableTouch(MotionEvent event) {
			// enter by speech
			startActivityForResult(speechIntent_,
								   SPEECH_RECOGNITION_REQUEST_CODE);
			return true;
		}
	}
	
	private void setLaterTime(long time, TextView datetext, TextView datelong){
		DateFormat df = DateFormat.getDateInstance();
		Date newdate = new Date(time);
		datetext.setText(df.format(newdate));
		datelong.setText(Long.toString(time));
	}
	
	private void showCalendarDialog(final TextView datetext, final TextView datelong){
		AlertDialog.Builder builder = new AlertDialog.Builder(StacklrExpActivity.this);
		View contentView = View.inflate(StacklrExpActivity.this, R.layout.calendar_dialog, null);
		//TODO; show item name as title

		builder.setView(contentView);
		DatePicker picker = (DatePicker)contentView.findViewById(R.id.date_picker);

		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTimeInMillis(Long.valueOf(datelong.getText().toString()));
		int year = cal.get(java.util.Calendar.YEAR);
		int month = cal.get(java.util.Calendar.MONTH);
		int date = cal.get(java.util.Calendar.DATE);
		//TODO: show diff date
		picker.updateDate(year, month, date);

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					DateFormat df = DateFormat.getDateInstance();
					DatePicker picker = (DatePicker)((AlertDialog)dialog).findViewById(R.id.date_picker);

					GregorianCalendar cal = new GregorianCalendar();
					cal.set(picker.getYear(), picker.getMonth(), picker.getDayOfMonth(), 0, 0, 0);
					long time = cal.getTimeInMillis();
					setLaterTime(time, datetext, datelong);
					dialog.dismiss();
				}
			});
		builder.create().show();
	}

	private class ItemClickListener
		implements ExpandableListView.OnChildClickListener,
				   OnItemLongClickListener
	{
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
									int groupPosition, int childPosition, long id) {
			Log.d(TAG, "childClicked " + groupPosition + " " + childPosition);
			adapter_.moveToNextGroup(groupPosition, childPosition);
			return true;
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent,
									   View view,
									   int position,
									   long id)
		{
			//final DateFormat
			final DateFormat df = DateFormat.getDateInstance();
			
			if (ExpandableListView.getPackedPositionType(id) != ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				return false;
			}
			final int groupPosition = ExpandableListView.getPackedPositionGroup(id);
			final int childPosition = ExpandableListView.getPackedPositionChild(id);
			final Item item = (Item)adapter_.getChild(groupPosition, childPosition);

			AlertDialog.Builder builder = new AlertDialog.Builder(StacklrExpActivity.this);
			View contentView = View.inflate(StacklrExpActivity.this, R.layout.item, null);
			builder.setView(contentView);
			//set default radio
			String itemname = item.getName();
			//set item name
			//TODO: change id
			TextView itemnameView = (TextView)contentView.findViewById(R.id.item_dialog_name);
			itemnameView.setText(itemname);

			final Spinner spinner = (Spinner)contentView.findViewById(R.id.item_dialog_type);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(StacklrExpActivity.this,
																				 R.array.item_type, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinner.setSelection(item.getType());

			int initRadioButtonId = RADIO_ID[groupPosition];
				
			final RadioGroup radioGroup = (RadioGroup)contentView.findViewById(R.id.radio_group);
			radioGroup.check(initRadioButtonId);

			final TextView datetext = (TextView)contentView.findViewById(R.id.item_later_date);
			final TextView datelong = (TextView)contentView.findViewById(R.id.item_later_date_long);

			long now = System.currentTimeMillis();
			setLaterTime(now, datetext, datelong);

			radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() 
				{
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch(checkedId){
						case R.id.radio_later:
							long later = System.currentTimeMillis();
							later += 3*24*60*60*1000;
							setLaterTime(later, datetext, datelong);
							break;
						default:
							break;
						}
					}
				});

			//xxx
			Button datebutton = (Button)contentView.findViewById(R.id.item_later_date_button);
			datebutton.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v){
						radioGroup.check(R.id.radio_later);
						showCalendarDialog(datetext, datelong);
					}
				});

			//TODO: use string resource
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						dialog.cancel();
					}
				});
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						//TODO: move to specified group
						int nextGroupId = -1;
						//TODO: loop....
						RadioGroup nextGroup = (RadioGroup)((AlertDialog)dialog).findViewById(R.id.radio_group);
						long updatedTime = System.currentTimeMillis();
						switch(nextGroup.getCheckedRadioButtonId()){
						case R.id.radio_to_buy:
							nextGroupId = TO_BUY;
							break;
						case R.id.radio_stock:
							nextGroupId = STOCK;
							break;
						case R.id.radio_shelf:
							nextGroupId = SHELF;
							break;
						case R.id.radio_history:
							nextGroupId = HISTORY;
							break;
						case R.id.radio_later:
							nextGroupId = LATER;
							//3 days later in default
							//updatedTime += 3*24*60*60*1000;
							updatedTime = Long.valueOf(datelong.getText().toString());
							break;
						default:
							break;
						}
						//
						item.setType(spinner.getSelectedItemPosition());
						//TODO: if moved
						adapter_.moveToGroup(groupPosition, childPosition, nextGroupId, updatedTime);
						dialog.dismiss();
					}
				});
			builder.setNeutralButton(R.string.remove, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						//TODO: show confirm dialog?
						adapter_.remove(groupPosition, childPosition);
						dialog.dismiss();
					}
				});
			builder.create().show();
			//TODO: display context menu
			//Log.d(TAG, "onItemLongClick id: "+ Long.toHexString(id));
			return true;
		}
	}

	//call: POST load
	public void uploadTasks(){
		Map<Group, List<Item>> local = adapter_.getLocalTasks();
		Log.d(TAG, "uploadTasks: " + local.size());
		if(local.size() > 0){
			AsyncAddTask.run(this, local);
		}
	}

	//group is already loaded
	public void startLoadTask(boolean force){
		boolean wifiOnly = pref_.getBoolean(StacklrPreference.PREFKEY_WIFI_ONLY, false);
		if(wifiOnly && !isWifiAvaiable()){
			showMessage(getString(R.string.wifi_is_not_available));
			return;
		}

		List<String> gidList = new ArrayList<String>();
		for(Group group: groups_){
			String gid = group.getGtaskListId();
			if(gid == null){
				gid = "";
			}
			gidList.add(gid);
		}
		long lastTaskLoadTime = -1;
		AsyncLoadTask.run(this, gidList, lastTaskLoadTime);
	}
	
	public void showLoadingIcon(){
		setProgressBarIndeterminateVisibility(true);
	}
	public void hideLoadingIcon(){
		setProgressBarIndeterminateVisibility(false);
	}
	
	public com.google.api.services.tasks.Tasks getTasksService(){
		return service_;
	}

	public File getDataDir(){
		return datadir_;
	}

	public void showMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
}
