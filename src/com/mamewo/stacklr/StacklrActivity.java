package com.mamewo.stacklr;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;

public class StacklrActivity
    extends Activity
    implements AdapterView.OnItemClickListener,
    TextView.OnEditorActionListener
{
    private ListView stackListView_;
    private EditText targetEditText_;
    private StackAdapter stackAdapter_;
    static private
    final String TAG = "stacklr";
    static private
    final int SPEECH_RECOGNITION_REQUEST_CODE = 2222;
    private Intent speechIntent_;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        stackListView_ = (ListView) findViewById(R.id.stacklist);
        targetEditText_ = (EditText) findViewById(R.id.target_text_view);
        targetEditText_.setOnEditorActionListener(this);
        targetEditText_.setOnTouchListener(new MicClickListener(targetEditText_));
        stackAdapter_ = new StackAdapter();
        stackListView_.setAdapter(stackAdapter_);
        stackListView_.setOnItemClickListener(this);
        Button pushButton = (Button) findViewById(R.id.push_button);
        pushButton.setOnClickListener(new PushButtonListener());
        
        speechIntent_ = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent_.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    }

    public class PushButtonListener
        implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            String itemname = targetEditText_.getText().toString();
            //TODO: search
            if(itemname.length() == 0){
                return;
            }
            stackAdapter_.push(itemname);
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
        List<String> stack_;

        public StackAdapter() {
            stack_ = new LinkedList<String>();
        }

        public void push(String item) {
            for(int i = 0; i < stack_.size(); i++) {
                String existing = stack_.get(i);
                if(existing.equals(item)){
                    stack_.remove(i);
                    break;
                }
            }
            stack_.add(0, item);
            notifyDataSetChanged();
        }
        
        public void push(int pos){
            String itemtext = stack_.remove(pos);
            stack_.add(0, itemtext);
            notifyDataSetChanged();
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
            String name = stack_.get(position);
            TextView labelview = (TextView) convertView.findViewById(R.id.itemname);
            labelview.setText(name);
            return convertView;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        stackAdapter_.push(position);
        targetEditText_.setText("");
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d(TAG, "onAction " + event + " " + Integer.toHexString(actionId));
        if(actionId == EditorInfo.IME_ACTION_DONE){
            String item = v.getText().toString();
            if(item.length() > 0) {
                stackAdapter_.push(item);
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
}
