package com.mamewo.stacklr.tests;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;
import com.robotium.solo.Solo.Config;
import com.robotium.solo.Solo.Config.ScreenshotFileType;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.Smoke;
import android.view.View;
import android.widget.TextView;
import android.widget.ExpandableListView;
import junit.framework.Assert;
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
	private Solo solo_;

    public TestStacklrExpActivity() {
        super("com.mamewo.stacklr", StacklrExpActivity.class);
    }

	@Override
	public void setUp(){
		Config config = new Config();
		config.screenshotFileType = ScreenshotFileType.PNG;
		config.screenshotSavePath = "/sdcard/Pictures/robotium/";
		config.shouldScroll = false;
		
		solo_ = new Solo(getInstrumentation(), config, getActivity());
	}
	
	@Override
	public void tearDown(){
		try{
			solo_.finalize();
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
		solo_.startScreenshotSequence("testAddNewItem");
		solo_.enterText(0, egg);
		solo_.takeScreenshot();
		solo_.clickOnButton(0);
		try{
			Thread.sleep(500);
		}
		catch(InterruptedException e){
		}
		solo_.takeScreenshot();
		ExpandableListView list = (ExpandableListView)solo_.getView(R.id.expandableListView1);
		View v = list.getChildAt(0);
		TextView text = (TextView)v.findViewById(android.R.id.text1);
		String label = text.getText().toString();
		solo_.takeScreenshot();
		System.out.println("label: "+ label);
		Assert.assertTrue("item name", label.startsWith(egg+" "));
		String afterText = solo_.getText(0).getText().toString();
		Assert.assertTrue("after text", "".equals(afterText));
		solo_.stopScreenshotSequence();
	}
}
