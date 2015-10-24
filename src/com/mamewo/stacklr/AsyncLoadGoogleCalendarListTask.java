package com.mamewo.stacklr;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;

import android.util.Log;

import static com.mamewo.stacklr.Constant.TAG;

import java.io.IOException;
import java.util.List;

public class AsyncLoadGoogleCalendarListTask
	extends CommonAsyncTask
{
	//one week before
	static public DateTime ONE_WEEK_AGO = new DateTime(System.currentTimeMillis()-7*24*60*60*1000);
	static public String CALENDAR_NAME = "stacklr";
	private String calendarId_ = "primary";

	public AsyncLoadGoogleCalendarListTask(StacklrExpActivity activity){
		super(activity);
	}
	
	@Override
	protected void doInBackground()
		throws IOException
	{
		Log.d(TAG, "AsyncLoadGoogleCalendarListTask.doInBackground");
		List<CalendarListEntry> l = calendarClient_
			.calendarList()
			.list()
			.execute()
			.getItems();
		for(CalendarListEntry entry: l){
			if(CALENDAR_NAME.equals(entry.getSummary())){
				calendarId_ = entry.getId();
				return;
			}
		}
	}

	static
	public void run(StacklrExpActivity activity) {
		new AsyncLoadGoogleCalendarListTask(activity).execute();
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		//TODO: save calendarId
		//XXX
		AsyncLoadGoogleCalendarTask.run(activity_, calendarId_);
	}
}
