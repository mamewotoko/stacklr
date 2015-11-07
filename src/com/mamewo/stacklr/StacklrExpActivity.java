package com.mamewo.stacklr;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

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

import android.accounts.AccountManager;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
import java.util.Collections;

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
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;

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

	//pref
	private static final String PREF_ACCOUNT_NAME = "accountName";
	private static final String PREF_LAST_LOADTASK_TIME = "lastLoadTaskTime";

	final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
	List<Group> groups_;
	public int numAsyncTasks;
	private long lastLoadTime_;

	//private Handler handler_;
	//private Runnable fileSaver_;
	private boolean groupLoaded_ = false;
	//end of tasks

	private final int[] RADIO_ID = new int[]{
		R.id.radio_to_buy,
		R.id.radio_stock,
		R.id.radio_history,
		R.id.radio_archive
	};

	private ExpandableListView listView_;
	private EditText targetEditText_;
	private View loadingIcon_;

	public ExpandableAdapter adapter_;
	private Intent speechIntent_;
	private File datadir_;
	private GoogleAccountCredential credential_;
	//TODO: move to expandable adapter
	public com.google.api.services.tasks.Tasks service_;
	public com.google.api.services.calendar.Calendar calendarService_;

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

	private void refreshTasks() {
		// check if there is already an account selected
		if (credential_.getSelectedAccountName() == null) {
			// ask user to choose account
			chooseAccount();
		}
		else {
			//TODO: use string resource for title
			setTitle("stacklr "+credential_.getSelectedAccountName());
			AsyncLoadGroupTask.run(this);
		}
	}

	void refreshView() {
		adapter_.notifyDataSetChanged();
	}

	private void chooseAccount() {
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
		super.onCreate(savedInstanceState);
		//trace is saved as /sdcard/stacklr.trace
		//		Debug.startMethodTracing("stacklr");

		//TODO: load from file or savedInstanceState
		lastLoadTime_ = 0;
		setContentView(R.layout.main_expandable);
		PackageManager m = getPackageManager();
		String s = getPackageName();
		try {
			PackageInfo p = m.getPackageInfo(s, 0);
			datadir_ = new File(p.applicationInfo.dataDir);
		}
		catch (NameNotFoundException e) {
			Log.w(TAG, "Error Package name not found ", e);
		}
		//---------------
		//gtasks & google calendar
		credential_ =
		 	GoogleAccountCredential.usingOAuth2(this, Arrays.asList(TasksScopes.TASKS, CalendarScopes.CALENDAR_READONLY));
		
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		credential_.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
		//TODO: rename
		service_ =
			new com.google.api.services.tasks.Tasks.Builder(httpTransport, jsonFactory, credential_)
            .setApplicationName("Stacklr/0.01").build();
		calendarService_ =
			new com.google.api.services.calendar.Calendar.Builder(httpTransport, jsonFactory, credential_)
			.setApplicationName("Stacklr/0.01").build();
		groups_ = Group.load(datadir_);
		if(groups_ == null){
			String[] groupNames = getResources().getStringArray(R.array.groups);
			groups_ = new ArrayList<Group>();
			for(String name: groupNames){
				groups_.add(new Group(name, null));
			}
		}
		//TODO: sync group(upload)
		//---------------
		// line based
		targetEditText_ = (EditText) findViewById(R.id.target_text_view);
		targetEditText_.setOnEditorActionListener(this);
		targetEditText_.setOnTouchListener(new MicClickListener(targetEditText_));
		loadingIcon_ = findViewById(R.id.loading_icon);

		Button pushButton = (Button) findViewById(R.id.push_button);
		pushButton.setOnClickListener(new PushButtonListener());

		adapter_ = new ExpandableAdapter(this, groups_);
		//TODO: show load toast?
		listView_ = (ExpandableListView) findViewById(R.id.expandableListView1);
		ItemClickListener listener = new ItemClickListener();
		listView_.setOnChildClickListener(listener);
		listView_.setOnItemLongClickListener(listener);
		listView_.setAdapter(adapter_);
		speechIntent_ = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speechIntent_.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							   RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		//TODO: add option
		for(int i = 0; i < groups_.size(); i++){
			if(Constant.DEFAULT_GROUP_OPEN[i]){
				listView_.expandGroup(i);
			}
			else {
				listView_.collapseGroup(i);
			}
		}
		// handler_ = new Handler();
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
	protected void onStart(){
		super.onStart();
		listView_.requestFocus();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Debug.stopMethodTracing();
		if (checkGooglePlayServicesAvailable()) {
			refreshTasks();
		}
	}

	@Override
	protected void onStop() {
		long lastModifiedTime = adapter_.lastModifiedTime();
		long lastSavedTime = adapter_.lastSavedTime();
		if(lastSavedTime < lastModifiedTime){
			adapter_.save();
		}
		//TODO: if group changed?
		Group.save(datadir_, groups_);
		super.onStop();
	}
	
	// @Override
	// protected void onDestroy() {
	// 	super.onDestroy();
	// }

	public class PushButtonListener
		implements View.OnClickListener
	{
		@Override
		public void onClick(View v) {
			String itemname = targetEditText_.getText().toString();
			if (itemname.isEmpty()) {
				return;
			}
			Item existing = adapter_.search(itemname);
			if(existing != null){
				existing.setLastTouchedTime(System.currentTimeMillis());
				adapter_.pushToBuy(existing);
			}
			else {
				adapter_.pushToBuyList(itemname);
			}
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
			AsyncLoadGroupTask.run(this);
			handled = true;
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
		default:
			break;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE
			|| actionId == EditorInfo.IME_NULL) {
			String item = v.getText().toString();
			//search
			if(item.length() == 0){
				return true;
			}
			Item existing = adapter_.search(item);
			if(existing != null){
				existing.setLastTouchedTime(System.currentTimeMillis());
				adapter_.pushToBuy(existing);
			}
			else {
				adapter_.pushToBuyList(item);
			}
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
		public boolean onItemLongClick(AdapterView<?> parent, View view,
									   int position, long id) {
			boolean handled = false;
			if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				final int groupPosition = ExpandableListView.getPackedPositionGroup(id);
				final int childPosition = ExpandableListView.getPackedPositionChild(id);
				final Item item = adapter_.get(groupPosition, childPosition);

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
				
				RadioGroup radioGroup = (RadioGroup)contentView.findViewById(R.id.radio_group);
				radioGroup.check(initRadioButtonId);

				//TODO: use string resource
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which){
							dialog.cancel();
						}
					});
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which){
							//TODO: move to specified group
							int nextGroupId = -1;
							//TODO: loop....
							RadioGroup nextGroup = (RadioGroup)((AlertDialog)dialog).findViewById(R.id.radio_group);
							switch(nextGroup.getCheckedRadioButtonId()){
							case R.id.radio_to_buy:
								nextGroupId = TO_BUY;
								break;
							case R.id.radio_stock:
								nextGroupId = STOCK;
								break;
							case R.id.radio_history:
								nextGroupId = HISTORY;
								break;
							case R.id.radio_archive:
								nextGroupId = ARCHIVE;
								break;
								//TODO: add remove?
							default:
								break;
							}
							item.setType(spinner.getSelectedItemPosition());
							//TODO: if moved
							adapter_.moveToGroup(groupPosition, childPosition, nextGroupId);
							dialog.dismiss();
						}
					});
				builder.setNeutralButton("Remove", new DialogInterface.OnClickListener(){
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
				handled = true;
			}
			return handled;
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
		//TODO: remove
		if((!force) && System.currentTimeMillis()-lastLoadTime_ < LOAD_MIN_INTERVAL){
			return;
		}
		lastLoadTime_ = System.currentTimeMillis();
		List<String> gidList = new ArrayList<String>();
		for(Group group: groups_){
			String gid = group.getGtaskListId();
			if(gid == null){
				gid = "";
			}
			gidList.add(gid);
		}
		long lastTaskLoadTime = -1;
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		if(!force){
			lastTaskLoadTime = settings.getLong(PREF_LAST_LOADTASK_TIME, -1);
		}
		AsyncLoadTask.run(this, gidList, lastTaskLoadTime);
		AsyncLoadGoogleCalendarListTask.run(this);
		long now = System.currentTimeMillis();
		SharedPreferences.Editor editor = settings.edit();
		//TODO: latest item time in gtask
		editor.putLong(PREF_LAST_LOADTASK_TIME, now);
		editor.commit();
	}
	
	public void showLoadingIcon(){
		loadingIcon_.setVisibility(View.VISIBLE);
	}
	public void hideLoadingIcon(){
		loadingIcon_.setVisibility(View.INVISIBLE);
	}
	
	public com.google.api.services.tasks.Tasks getTasksService(){
		return service_;
	}

	public File getDataDir(){
		return datadir_;
	}
}
