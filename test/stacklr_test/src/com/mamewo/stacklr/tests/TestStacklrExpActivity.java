package com.mamewo.stacklr.tests;

import java.io.File;
import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;
import com.robotium.solo.Solo.Config;
import com.robotium.solo.Solo.Config.ScreenshotFileType;
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

	@Override
	public void setUp(){
		Log.d(TAG, "setup started");

		Config config = new Config();
		config.screenshotFileType = ScreenshotFileType.PNG;
		config.screenshotSavePath = new File(Environment.getExternalStorageDirectory(), "Robotium-Screenshots").getPath();
		config.shouldScroll = false;
		solo_ = new Solo(getInstrumentation(), config, getActivity());
	}
	
	@Override
	public void tearDown(){
		try{
			solo_.finalize();
			getActivity().finish();
			super.tearDown();
			Log.d(TAG, "tearDown finished");
		}
		catch(Throwable t){
			t.printStackTrace();
		}
	}

	// @Smoke
	// public void test000InitGoogleService(){
		
	// }

	@Smoke
	public void testAddNewItem(){
		String egg = "Egg";
		solo_.enterText(0, egg);
		solo_.clickOnButton(0);
		solo_.sleep(500);
		Log.d(TAG, "0");

		ExpandableListView list = (ExpandableListView)solo_.getView(R.id.expandableListView1);
		
		//0: group
		//1: first item
		View v = list.getChildAt(1);
		Log.d(TAG, "1");
		TextView text = (TextView)v.findViewById(R.id.item_name);
		String label = text.getText().toString();
		Log.d(TAG, "2");
		assertTrue("item name", label.startsWith(egg+" "));
		String afterText = solo_.getEditText(0).getText().toString();
		assertTrue("after text", "".equals(afterText));
		Log.d(TAG, "takeScreenshot: testAddNewItem");
		solo_.takeScreenshot("testAddNewItem");
		Log.d(TAG, "takeScreenshot: last sleep");
	}

	@Smoke
	public void testClickFirstChild(){
		Log.d(TAG, "testClickFirstChild");
		solo_.clickInList(2);
		solo_.sleep(500);
		//TODO: assert that first item goto "Stock" group
		solo_.takeScreenshot("testClickFirstChild");
	}

	@Smoke
	public void testLongClickFirstChild(){
		Log.d(TAG, "testLongClickFirstChild");
		solo_.clickLongInList(2);
		solo_.sleep(1000);
		//TODO: assert that first item goto "Stock" group
		assertTrue("dilogOpen", solo_.waitForDialogToOpen(1000));
		//TODO:assert
		solo_.takeScreenshot("testLongClickFirstChild");
	}
}
