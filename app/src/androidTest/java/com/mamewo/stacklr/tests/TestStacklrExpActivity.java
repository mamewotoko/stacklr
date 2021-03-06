package com.mamewo.stacklr.tests;

import java.io.File;
import android.test.ActivityInstrumentationTestCase2;

import android.content.Intent;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import com.squareup.spoon.Spoon;

import com.robotium.solo.Solo;
import com.robotium.solo.Solo.Config;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.Smoke;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ExpandableListView;
import static junit.framework.Assert.*;
import com.mamewo.stacklr.*;
import com.mamewo.stacklr.R;
/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.mamewo.stacklr.tests.StacklrExpActivityTest \
 * com.mamewo.stacklr.tests/android.test.InstrumentationTestRunner
 */

//UI test with Robotium 
public class TestStacklrExpActivity
	extends ActivityInstrumentationTestCase2<StacklrExpActivity>
{
	static final private String TAG = "stacklr_test";
	private Solo solo_;

    public TestStacklrExpActivity() {
        super("com.mamewo.stacklr", StacklrExpActivity.class);
    }

	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// 	super.onCraete(savedInstanceState);
	// 	//select default account
	// 	Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null);
	// 	//Utils.PICK_ACCOUNT_REQUEST
	// 	startActivityForResult(googlePicker, 1234);
	// 	//wait?
	// }
	
	@Override
	public void setUp(){
		Log.d(TAG, "setup started");
		
		Config config = new Config();
		solo_ = new Solo(getInstrumentation(), config, getActivity());
	}
	
	@Override
	public void tearDown(){
		try{
			solo_.finalize();
			getActivity().finish();
			solo_.waitForEmptyActivityStack(1000);
			super.tearDown();
			Log.d(TAG, "tearDown finished");
		}
		catch(Throwable t){
			t.printStackTrace();
		}
	}

	@Smoke
	public void testAddNewItem(){
		String egg = "Egg";
		solo_.enterText(0, egg);
		solo_.clickOnButton(0);
		solo_.sleep(500);

		ExpandableListView list = (ExpandableListView)solo_.getView(R.id.expandableListView1);
		
		//0: group
		//1: first item
		View v = list.getChildAt(1);
		TextView text = (TextView)v.findViewById(R.id.item_name);
		String label = text.getText().toString();
		assertTrue("item name", label.startsWith(egg+" "));
		String afterText = solo_.getEditText(0).getText().toString();
		assertTrue("after text", "".equals(afterText));
		Log.d(TAG, "screenshot: testAddNewItem");
		Spoon.screenshot(getActivity(), "add_new_item");
	}

	@Smoke
	public void testClickFirstChild(){
		String egg = "Egg";
		solo_.enterText(0, egg);
		solo_.clickOnButton(0);
		solo_.sleep(500);

		solo_.clickInList(2);
		solo_.sleep(500);
		ExpandableListView list = (ExpandableListView)solo_.getView(R.id.expandableListView1);

		int i;
		for(i = 0; i < list.getChildCount(); i++){
			//0: group
			//1: first item
			View v = list.getChildAt(i);
			TextView text = (TextView)v.findViewById(R.id.item_name);
			//XXXX
			if(text == null){
				text = (TextView)v.findViewById(android.R.id.text1);
			}
			String label = text.getText().toString();
			Log.d(TAG,"testClickFirstChild: child " + i + " " + label);

			if(label.equals("Stock")){
				break;
			}
		}
		i++;
		View v = list.getChildAt(i);
		TextView text = (TextView)v.findViewById(R.id.item_name);
		String label = text.getText().toString();
		Log.d(TAG,"testClickFirstChild: child " + label);
		assertTrue("item name", label.startsWith(egg+" "));
		String afterText = solo_.getEditText(0).getText().toString();

		assertTrue("after text", "".equals(afterText));
		Log.d(TAG, "screenshoth: testAddNewItem");
		Spoon.screenshot(getActivity(), "click_first_item");
	}

	@Smoke
	public void testLongClickFirstChild(){
		String egg = "Egg";
		solo_.enterText(0, egg);
		solo_.clickOnButton(0);
		solo_.sleep(500);

		solo_.clickLongInList(2);
		solo_.sleep(1000);
		//TODO: assert that first item goto "Stock" group
		assertTrue("dilogOpen", solo_.waitForDialogToOpen(1000));
		//TODO:assert
		Spoon.screenshot(getActivity(), "long_click_first_item");
	}
}
