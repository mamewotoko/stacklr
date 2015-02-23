package com.mamewo.stacklr;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import static com.mamewo.stacklr.Constant.*;

public class StacklrExpActivity
	extends Activity
	implements TextView.OnEditorActionListener
{
	static final private int SPEECH_RECOGNITION_REQUEST_CODE = 2222;
	static final private String GROUPS[] = new String[]{
		"To buy list", "Stock", "History"
	};
	
	//order of groups
	static private final int TO_BUY = 0;
	static private final int STOCK = 1;
	static private final int HISTORY = 2;

	private final int[] NEXT_GROUP = new int[]{
		STOCK, //from to buy
		TO_BUY, //from stock, to buy(click) or history list(long)
		TO_BUY, //from history
	};

	private ExpandableListView listView_;
	private EditText targetEditText_;
	private ExpandableAdapter adapter_;
	private Intent speechIntent_;
	private File datadir_;

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
		adapter_ = new ExpandableAdapter(GROUPS);
		listView_ = (ExpandableListView) findViewById(R.id.expandableListView1);
		ItemClickListener listener = new ItemClickListener();
		listView_.setOnChildClickListener(listener);
		listView_.setOnItemLongClickListener(listener);
		listView_.setAdapter(adapter_);
		speechIntent_ = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speechIntent_.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							   RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		for(int i = 0; i < GROUPS.length; i++){
			listView_.expandGroup(i);
		}
	}

	// TODO: load data in onStart method
	@Override
	protected void onStart(){
		super.onStart();
		listView_.requestFocus();
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
			// TODO: pushitem to TODO group
			// stackAdapter_.push(itemname);
			// targetEditText_.setText("");
			// hide keyboard?
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
		case R.id.clear_menu:
			for(int i = 0; i < GROUPS.length; i++){
				adapter_.clearGroup(i);
			}
			handled = true;
			break;
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
			List<String> matches = data
			.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (matches.isEmpty()) {
				return;
			}
			targetEditText_.setText(matches.get(0));
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		Log.d(TAG, "onAction " + event + " " + Integer.toHexString(actionId));
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			String item = v.getText().toString();
			if (item.length() > 0) {
				adapter_.pushToBuyList(item);
				v.setText("");
				return true;
			}
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
				int groupPosition = ExpandableListView.getPackedPositionGroup(id);
				int childPosition = ExpandableListView.getPackedPositionChild(id);
				//TODO: display context menu
				Log.d(TAG, "onItemLongClick id: "+ Long.toHexString(id));
				adapter_.moveToHistory(groupPosition, childPosition);
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
			groups_ = groups;
			children_ = new ArrayList<List<Item>>();
			storageList_ = new ArrayList<ItemStorage>();
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
			children_.get(nextGroupPosition).add(item);
			notifyDataSetChanged();
		}

		public void pushToBuy(Item item) {
			children_.get(TO_BUY).remove(item);
			//XXXX
			children_.get(STOCK).remove(item);
			children_.get(HISTORY).remove(item);
			children_.get(TO_BUY).add(0, item);
			notifyDataSetChanged();
		}

		public void pushToBuyList(String items) {
			BufferedReader br = new BufferedReader(new StringReader(items));
			String item;
			try {
				while ((item = br.readLine()) != null) {
					item = item.trim();
					if(item.length() == 0){
						continue;
					}
					//TODO: date
					pushToBuy(new Item(item));
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

		public void moveToHistory(int groupPosition, int childPosition){
			Item item = children_.get(groupPosition).remove(childPosition);
			children_.get(HISTORY).add(item);
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
			if(item.getLastTouchedTimeStr().length() > 0){
				time = " : " + item.getLastTouchedTimeStr() + String.format(" (%dd)", item.elapsedDays());
			}
			text.setText(item.getName() + time);
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
