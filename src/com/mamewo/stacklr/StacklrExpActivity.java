package com.mamewo.stacklr;

import java.util.LinkedList;
import java.util.ArrayList;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class StacklrExpActivity
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
    private ExpandableListView listView_;
    private EditText targetEditText_;
	private ExpandableAdapter adapter_;
    private Intent speechIntent_;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_expandable);
		
        targetEditText_ = (EditText) findViewById(R.id.target_text_view);
        targetEditText_.setOnEditorActionListener(this);
        targetEditText_.setOnTouchListener(new MicClickListener(targetEditText_));

        Button pushButton = (Button) findViewById(R.id.push_button);
        pushButton.setOnClickListener(new PushButtonListener());
		

		PackageManager m = getPackageManager();
		String s = getPackageName();
		String datadir = null;
		try {
			PackageInfo p = m.getPackageInfo(s, 0);
			datadir = p.applicationInfo.dataDir;
		} catch (NameNotFoundException e) {
			Log.w(TAG, "Error Package name not found ", e);
		}
		File stackfile = new File(datadir, STACK_FILENAME);
		File historyfile = new File(datadir, HISTORY_FILENAME);
		adapter_ = new ExpandableAdapter(stackfile.getPath(), historyfile.getPath());
		listView_ = (ExpandableListView) findViewById(R.id.expandableListView1);
		listView_.setOnChildClickListener(new ItemClickListener());
		listView_.setAdapter(adapter_);
		speechIntent_ = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent_.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        listView_.expandGroup(ExpandableAdapter.TODO);
        listView_.expandGroup(ExpandableAdapter.HISTORY);
    }
    
	//TODO: load data in onStart method

	@Override
    protected void onDestroy(){
		adapter_.save();
		super.onDestroy();
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
			//TODO: pushitem to TODO group
            //stackAdapter_.push(itemname);
            //targetEditText_.setText("");
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

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d(TAG, "onAction " + event + " " + Integer.toHexString(actionId));
        if(actionId == EditorInfo.IME_ACTION_DONE){
            String item = v.getText().toString();
            if(item.length() > 0) {
				adapter_.pushTODO(item);
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
    class ItemClickListener
        implements ExpandableListView.OnChildClickListener
    {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v,
                int groupPosition, int childPosition, long id) {
            boolean handled = false;
            Log.d(TAG, "childClicked " + groupPosition + " " + childPosition);
            if(groupPosition == ExpandableAdapter.TODO){
                String item = adapter_.remove(ExpandableAdapter.TODO, childPosition);
                adapter_.addHistory(item);
            }
            else if(groupPosition == ExpandableAdapter.HISTORY){
                String item = adapter_.remove(ExpandableAdapter.HISTORY, childPosition);
                adapter_.pushTODO(item);
            }
            return handled;
        }
    }
    
	private class ExpandableAdapter
		extends BaseExpandableListAdapter
	{
		//TODO:Customize?
		final static
		public int TODO = 0;
		final static
		public int HISTORY = 1;
		final
		private String groups_[] = { "TODO", "History" };
		//child
		private List<List<String>> children_;
		private List<ItemStorage> storageList_;

		public ExpandableAdapter(String stackpath, String historypath){
			children_ = new ArrayList<List<String>>();
			storageList_ = new ArrayList<ItemStorage>();
			storageList_.add(new FileItemStorage(stackpath));
			storageList_.add(new FileItemStorage(historypath));
			for(int i = 0; i < groups_.length; i++){
				children_.add(storageList_.get(i).load());
			}
			//TODO: debug
            children_.get(HISTORY).add("卵");
            children_.get(HISTORY).add("牛乳");
            children_.get(HISTORY).add("納豆");
            children_.get(HISTORY).add("豆腐");
            children_.get(HISTORY).add("わかめ");
            children_.get(TODO).add("みかん");
		}

		public void pushTODO(String item){
			children_.get(TODO).add(0, item);
            notifyDataSetChanged();
		}

		public void addHistory(String item){
			children_.get(HISTORY).add(item);
			notifyDataSetChanged();
		}
		
		public String remove(int group, int pos){
			String item = children_.get(group).remove(pos);
            notifyDataSetChanged();
            return item;
		}

		public void save(){
			for(int i = 0; i < storageList_.size(); i++){
				//get each group
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
            return getChild(groupPosition, childPosition).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
        
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            if(convertView == null){
                //convertView = View.inflate(StacklrExpActivity.this, R.layout.exp_group, null);
                convertView = View.inflate(StacklrExpActivity.this, android.R.layout.simple_expandable_list_item_1, null);
                convertView.setBackgroundColor(0x888888);
            }
            //TextView text = (TextView) convertView.findViewById(R.id.text);
            TextView text = (TextView) convertView.findViewById(android.R.id.text1);
            text.setText(groups_[groupPosition]);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
            if(convertView == null){
//                convertView = View.inflate(StacklrExpActivity.this, R.layout.exp_item, null);
                convertView = View.inflate(StacklrExpActivity.this, android.R.layout.simple_expandable_list_item_2, null);
            }
//          TextView text = (TextView) convertView.findViewById(R.id.item_name);
            TextView text = (TextView) convertView.findViewById(android.R.id.text1);
            text.setText(children_.get(groupPosition).get(childPosition));
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
	}
}
