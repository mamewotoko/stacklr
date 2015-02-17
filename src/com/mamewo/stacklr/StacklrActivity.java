package com.mamewo.stacklr;

import java.util.LinkedList;
import java.util.List;
import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;

public class StacklrActivity
extends Activity
implements TextView.OnEditorActionListener
{
	static final
	protected String TAG = "stacklr";
	static final
	private int SPEECH_RECOGNITION_REQUEST_CODE = 2222;
	static final
	private String STACK_FILENAME = "stack.txt";
	static final
	private String HISTORY_FILENAME = "history.txt";
	private ListView stackListView_;
	private ListView historyListView_;
	private EditText targetEditText_;
	private StackAdapter stackAdapter_;
	private HistoryAdapter historyAdapter_;
	private Intent speechIntent_;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		PackageManager m = getPackageManager();
		String s = getPackageName();
		String datadir = null;
		try {
			PackageInfo p = m.getPackageInfo(s, 0);
			datadir = p.applicationInfo.dataDir;
		} catch (NameNotFoundException e) {
			Log.w(TAG, "Error Package name not found ", e);
		}
		targetEditText_ = (EditText) findViewById(R.id.target_text_view);
		targetEditText_.setOnEditorActionListener(this);
		targetEditText_.setOnTouchListener(new MicClickListener(targetEditText_));
		File stackfile = new File(datadir, STACK_FILENAME);
		stackAdapter_ = new StackAdapter(new CSVItemStorage(stackfile));

		stackListView_ = (ListView) findViewById(R.id.stack_list);
		stackListView_.setAdapter(stackAdapter_);
		StackClickListener stackListener = new StackClickListener();
		stackListView_.setOnItemClickListener(stackListener);
		stackListView_.setOnItemLongClickListener(stackListener);
		Button pushButton = (Button) findViewById(R.id.push_button);
		pushButton.setOnClickListener(new PushButtonListener());

		File historyfile = new File(datadir, HISTORY_FILENAME);
		historyAdapter_ = new HistoryAdapter(new CSVItemStorage(historyfile));
		historyListView_ = (ListView) findViewById(R.id.history_list);
		historyListView_.setAdapter(historyAdapter_);
		HistoryListItemClickListener historyListener = new HistoryListItemClickListener();
		historyListView_.setOnItemClickListener(historyListener);
		historyListView_.setOnItemLongClickListener(historyListener);

		speechIntent_ = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speechIntent_.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		historyAdapter_.save();
		stackAdapter_.save();
	}

	public class PushButtonListener
	implements View.OnClickListener
	{
		@Override
		public void onClick(View v) {
			String itemname = targetEditText_.getText().toString();
			//TODO: search
			if(itemname.isEmpty()){
				return;
			}
			stackAdapter_.push(new Item(itemname, System.currentTimeMillis()));
			targetEditText_.setText("");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SPEECH_RECOGNITION_REQUEST_CODE) {
			if(resultCode != RESULT_OK) {
				return;
			}
			//TODO: select good one or display list dialog 
			List<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if(matches.isEmpty()){
				return;
			}
			targetEditText_.setText(matches.get(0));
		}
	}

	public class StackAdapter
	extends BaseAdapter
	{
		List<Item> stack_;
		ItemStorage storage_;

		public StackAdapter(ItemStorage storage) {
			storage_ = storage;
			stack_ = storage_.load();
		}

		public void push(Item item) {
			String itemName = item.getName();
			for(int i = 0; i < stack_.size(); i++) {
				Item existing = stack_.get(i);
				if(itemName.equals(existing.getName())){
					stack_.remove(i);
					break;
				}
			}
			stack_.add(0, item);
			notifyDataSetChanged();
		}

		public void push(int pos){
			Item item = stack_.remove(pos);
			stack_.add(0, item);
			notifyDataSetChanged();
		}

		public Item remove(int pos){
			Item item = stack_.remove(pos);
			notifyDataSetChanged();
			return item;
		}

		public void save(){
			storage_.save(stack_);
		}

		@Override
		public int getCount() {
			return stack_.size();
		}

		@Override
		public Object getItem(int position) {
			return stack_.get(position);
		}

		@Override
		public long getItemId(int position) {
			return stack_.get(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = View.inflate(StacklrActivity.this, R.layout.stackitem, null);
			}
			Item item = stack_.get(position);
			TextView labelview = (TextView) convertView.findViewById(R.id.itemname);
			labelview.setText(item.getName());
			return convertView;
		}
	}

	private class StackClickListener 
	implements AdapterView.OnItemClickListener,
	OnItemLongClickListener
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//TODO: manage state...
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			//move to history
			Item item = stackAdapter_.remove(position);
			item.setLastTouchedTime(System.currentTimeMillis());
			historyAdapter_.add(item);
			return true;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		Log.d(TAG, "onAction " + event + " " + Integer.toHexString(actionId));
		if(actionId == EditorInfo.IME_ACTION_DONE){
			String item = v.getText().toString();
			if(item.length() > 0) {
				stackAdapter_.push(new Item(item, System.currentTimeMillis()));
				v.setText("");
				return true;
			}
		}
		return false;
	}

	private
	class MicClickListener
	extends RightDrawableOnTouchListener
	{
		public MicClickListener(TextView view) {
			super(view);
		}

		@Override
		public boolean onDrawableTouch(MotionEvent event) {
			//enter by speech
			startActivityForResult(speechIntent_, SPEECH_RECOGNITION_REQUEST_CODE);
			return true;
		}
	}

	private
	class HistoryListItemClickListener
	implements AdapterView.OnItemClickListener,
	AdapterView.OnItemLongClickListener
	{
		//TODO: change to double tap (gesture detector)
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Item item = historyAdapter_.remove(position);
			item.setLastTouchedTime(System.currentTimeMillis());
			stackAdapter_.push(item);
		}
		//swipe?

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			//show property?
			return true;
		}
	}

	private
	class HistoryAdapter
	extends BaseAdapter
	{
		private List<Item> historyList_;
		private ItemStorage storage_;

		public HistoryAdapter(ItemStorage storage){
			storage_ = storage;
			historyList_ = storage_.load();
		}

		public Item remove(int pos){
			Item item = historyList_.remove(pos);
			notifyDataSetChanged();
			return item;
		}

		public void add(Item item){
			Log.d(TAG, "added To History");
			historyList_.add(item);
			notifyDataSetChanged();
		}

		public void save(){
			storage_.save(historyList_);
		}

		@Override
		public int getCount() {
			return historyList_.size();
		}

		@Override
		public Object getItem(int position) {
			return historyList_.get(position);
		}

		@Override
		public long getItemId(int position) {
			return historyList_.get(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = View.inflate(StacklrActivity.this, R.layout.historyitem, null);
			}
			String name = historyList_.get(position).getName();
			TextView labelview = (TextView) convertView.findViewById(R.id.history_itemname);
			labelview.setText(name);
			return convertView;
		}
	}
}
