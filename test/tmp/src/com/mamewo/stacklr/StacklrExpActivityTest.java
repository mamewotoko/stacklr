package com.mamewo.stacklr;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.mamewo.stacklr.StacklrExpActivityTest \
 * com.mamewo.stacklr.tests/android.test.InstrumentationTestRunner
 */
public class StacklrExpActivityTest extends ActivityInstrumentationTestCase2<StacklrExpActivity> {

    public StacklrExpActivityTest() {
        super("com.mamewo.stacklr", StacklrExpActivity.class);
    }

}
