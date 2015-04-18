package com.mamewo.stacklr.tests;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;
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
		solo_ = new Solo(getInstrumentation(), getActivity());
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

	@Smoke
	public void testAddNewItem(){
		String egg = "Egg";
		solo_.enterText(0, egg);
		solo_.clickOnButton("Push");
		ExpandableListView list = (ExpandableListView)solo_.getView(R.id.expandableListView1);
		View v = list.getChildAt(0);
		TextView text = (TextView)v.findViewById(android.R.id.text1);
		String label = text.getText().toString();
		Assert.assertTrue("item name", label.startsWith(egg+" "));
		String afterText = solo_.getText(0).getText().toString();
		Assert.assertTrue("after text", "".equals(afterText));
	}
}
