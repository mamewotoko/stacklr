package com.mamewo.stacklr;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;

import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
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
	static final public boolean ASCENDING = false;

	//order of groups
	static private final int TO_BUY = 0;
	static private final int STOCK = 1;
	static private final int HISTORY = 2;
	static private final int ARCHIVE = 3;
	//static private final int REMOVE = -1;

	private final int[] NEXT_GROUP = new int[]{
		STOCK, //from to buy
		TO_BUY, //from stock, to buy(click) or history list(long)
		TO_BUY, //from history
		HISTORY //from archive
	};

	private final int[] RADIO_ID = new int[]{
		R.id.radio_to_buy,
		R.id.radio_stock,
		R.id.radio_history,
		R.id.radio_archive
	};

	private final int[] LONG_NEXT_GROUP = new int[]{
		HISTORY,
		HISTORY,
		ARCHIVE,
		ARCHIVE
	};

	private ExpandableListView listView_;
	private EditText targetEditText_;
	private ExpandableAdapter adapter_;
	private Intent speechIntent_;
	private File datadir_;

	private String[] getGroups(){
		return getResources().getStringArray(R.array.groups);
	}

	static String groupNameToFilename(String groupName){
		return groupName.replaceAll(" ", "_")+".txt";
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_expandable);
		// line based
		targetEditText_ = (EditText) findViewById(R.id.target_text_view);
		targetEditText_.setOnEditorActionListener(this);
		targetEditText_.setOnTouchListener(new MicClickListener(targetEditText_));

		Button pushButton = (Button) findViewById(R.id.push_button);
		pushButton.setOnClickListener(new PushButtonListener());

		PackageManager m = getPackageManager();
		String s = getPackageName();
		try {
			PackageInfo p = m.getPackageInfo(s, 0);
			datadir_ = new File(p.applicationInfo.dataDir);
		} catch (NameNotFoundException e) {
			Log.w(TAG, "Error Package name not found ", e);
		}
		String[] groups = getGroups();
		adapter_ = new ExpandableAdapter(groups);
		//TODO: show load toast?
		listView_ = (ExpandableListView) findViewById(R.id.expandableListView1);
		ItemClickListener listener = new ItemClickListener();
		listView_.setOnChildClickListener(listener);
		listView_.setOnItemLongClickListener(listener);
		listView_.setAdapter(adapter_);
		speechIntent_ = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speechIntent_.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							   RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		for(int i = 0; i < groups.length; i++){
			listView_.expandGroup(i);
		}
	}

	@Override
	protected void onStart(){
		super.onStart();
		listView_.requestFocus();
	}

	@Override
	protected void onStop() {
		adapter_.save();
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		adapter_.save();
		super.onDestroy();
	}

	public class PushButtonListener
		implements View.OnClickListener
	{
		@Override
		public void onClick(View v) {
			String itemname = targetEditText_.getText().toString();
			// TODO: search
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
			break;
		default:
			break;
		}
		return handled;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SPEECH_RECOGNITION_REQUEST_CODE) {
			if (resultCode != RESULT_OK) {
				return;
			}
			// TODO: select good one or display list dialog
			List<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (matches.isEmpty()) {
				return;
			}
			targetEditText_.setText(matches.get(0));
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
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
	
	static
	private void debugList(List<String> lst){
		Log.d(TAG, "size: "+lst.size());
		for(String item : lst){
			Log.d(TAG, " "+item);
		}
	}

	private class ItemClickListener
		implements ExpandableListView.OnChildClickListener,
		OnItemLongClickListener
	{
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			boolean handled = false;
			Log.d(TAG, "childClicked " + groupPosition + " " + childPosition);
			adapter_.moveToNextGroup(groupPosition, childPosition);
			return handled;
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
				TextView itemnameView = (TextView)contentView.findViewById(R.id.item_name);
				itemnameView.setText(itemname);

				final Spinner spinner = (Spinner)contentView.findViewById(R.id.item_type);
				ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(StacklrExpActivity.this,
																					 R.array.item_type, android.R.layout.simple_spinner_item);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapter);
				spinner.setSelection(item.getType());

				//int initRadioButtonId = RADIO_ID[LONG_NEXT_GROUP[groupPosition]];
				int initRadioButtonId = RADIO_ID[groupPosition];
				
				RadioGroup radioGroup = (RadioGroup)contentView.findViewById(R.id.radio_group);
				radioGroup.check(initRadioButtonId);

				//TODO: set positive, negative button action
				//use string resource
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which){
							dialog.cancel();
						}
					});
				//set type ...
				// builder.setNeutoralButton("Stay", new new DialogInterface.OnClickListener(){
				// 		@Override
				// 		public void onClick(DialogInterface dialog, int which){
				// 			dialog.dismiss();
				// 		}
				// 	});
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
				builder.create().show();


				//TODO: display context menu
				//Log.d(TAG, "onItemLongClick id: "+ Long.toHexString(id));
				handled = true;
			}
			return handled;
		}
	}

	private class ExpandableAdapter
		extends BaseExpandableListAdapter
	{
		// TODO:Customize?
		private List<List<Item>> children_;
		private List<ItemStorage> storageList_;
		private String[] groups_;

		//long touch -> history or remove

		public ExpandableAdapter(String[] groups){
			groups_ = getGroups();
			children_ = new LinkedList<List<Item>>();
			storageList_ = new LinkedList<ItemStorage>();
			for (int i = 0; i < groups.length; i++) {
				String filename = groupNameToFilename(groups[i]);
				storageList_.add(new CSVItemStorage(new File(datadir_, filename)));
				children_.add(storageList_.get(i).load());
				//modify group name
			}
		}

		public void moveToNextGroup(int groupPosition, int childPosition){
			int nextGroupPosition = NEXT_GROUP[groupPosition];
			Item item = children_.get(groupPosition).remove(childPosition);
			item.setLastTouchedTime(System.currentTimeMillis());
			//children_.get(nextGroupPosition).add(0, item);
			List<Item> lst = children_.get(nextGroupPosition);
			Util.insertItem(lst, item, ASCENDING);
			notifyDataSetChanged();
		}

		public void moveToGroup(int groupPosition, int childPosition, int nextGroupPosition){
			Item item = children_.get(groupPosition).remove(childPosition);
			item.setLastTouchedTime(System.currentTimeMillis());
			//children_.get(nextGroupPosition).add(0, item);
			List<Item> lst = children_.get(nextGroupPosition);
			Util.insertItem(lst, item, ASCENDING);
			notifyDataSetChanged();
		}

		public Item search(String itemname){
			for(List<Item> itemlist: children_){
				for(int i = 0; i < itemlist.size(); i++){
					if(itemname.equals(itemlist.get(i).getName())){
						return itemlist.remove(i);
					}
				}
			}
			return null;
		}

		public void pushToBuy(Item item) {
			children_.get(TO_BUY).remove(item);
			//XXXX
			children_.get(STOCK).remove(item);
			children_.get(HISTORY).remove(item);
			//children_.get(TO_BUY).add(0, item);
			List<Item> lst = children_.get(TO_BUY);
			Util.insertItem(lst, item, ASCENDING);

			notifyDataSetChanged();
		}

		public void pushToBuyList(String items) {
			BufferedReader br = new BufferedReader(new StringReader(items));
			String itemname;
			try {
				while ((itemname = br.readLine()) != null) {
					itemname = itemname.trim();
					if(itemname.length() == 0){
						continue;
					}
					//TODO: find existing item
					//TODO: if entered from text box
					
					//TODO: date
					pushToBuy(new Item(itemname, System.currentTimeMillis()));
				}
			} catch (IOException e) {
				Log.d(TAG, "IOException", e);
			}
		}

		public Item remove(int group, int pos) {
			///debugList(children_.get(HISTORY));
			Item item = children_.get(group).remove(pos);
			notifyDataSetChanged();
			return item;
		}

		public Item get(int group, int pos){
			return children_.get(group).get(pos);
		}

		public void moveToHistory(int groupPosition, int childPosition){
			Item item = children_.get(groupPosition).remove(childPosition);
			//children_.get(HISTORY).add(0, item);
			List<Item> lst = children_.get(HISTORY);
			Util.insertItem(lst, item, ASCENDING);
			notifyDataSetChanged();
		}

		public void clearGroup(int groupPos){
			children_.get(groupPos).clear();
		}

		public void save() {
			for (int i = 0; i < storageList_.size(); i++) {
				storageList_.get(i).save(children_.get(i));
			}
		}

		@Override
		public int getGroupCount() {
			return children_.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return children_.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groups_[groupPosition];
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return children_.get(groupPosition).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView == null) {
 				convertView = View.inflate(StacklrExpActivity.this,
 						android.R.layout.simple_expandable_list_item_1, null);
			}
			TextView text = (TextView) convertView.findViewById(android.R.id.text1);
			text.setText(groups_[groupPosition]);
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView == null) {
				//convertView = View.inflate(StacklrExpActivity.this,
				//R.layout.exp_item, null);
 				convertView = View.inflate(StacklrExpActivity.this,
 						android.R.layout.simple_expandable_list_item_2, null);
			}
			// TextView text = (TextView)
			// convertView.findViewById(R.id.item_name);
			TextView text = (TextView) convertView.findViewById(android.R.id.text1);
			//TextView text = (TextView) convertView.findViewById(R.id.item_name);
			Item item = children_.get(groupPosition).get(childPosition);
			String time = "";
			String date = item.lastTouchedDateStr();
			if(date.length() > 0){
				time = " : " + date + String.format(" (%dd)", item.elapsedDays());
			}
			text.setText(item.getName() + time);
			int color = 0;
			switch(item.getType()){
			case Item.ITEM_TYPE_TOP:
				color = Color.rgb(250, 175, 186);
				break;
			case Item.ITEM_TYPE_ARTICLE:
				color = Color.rgb(65, 163, 23);
				break;
			default:
				color = Color.rgb(255, 255, 255);
				break;
			}
			text.setTextColor(color);
			//TextView time = (TextView) convertView.findViewById(R.id.item_time);
			//text.setText(children_.get(groupPosition).get(childPosition).getLastTouchedTimeStr());
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
}
