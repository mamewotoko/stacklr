package com.mamewo.stacklr;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.Calendar;

import com.google.api.client.util.DateTime;
import java.io.IOException;
import java.util.List;

import static com.mamewo.stacklr.Constant.TAG;
import android.util.Log;

public class AsyncLoadGoogleCalendarTask
	extends CommonAsyncTask
{
	private String calendarId_;
	private List<Event> events_ = null;
	static public DateTime ONE_WEEK_AGO = new DateTime(System.currentTimeMillis()-7*24*60*60*1000);

	public AsyncLoadGoogleCalendarTask(StacklrExpActivity activity, String calendarId){
		super(activity);
		calendarId_ = calendarId;
	}
	
	@Override
	protected void doInBackground()
		throws IOException
	{
		//TODO: use last loaded time as minTime
		events_ = calendarClient_.events().list(calendarId_)
			.setTimeMin(ONE_WEEK_AGO)
			.setTimeMax(new DateTime(System.currentTimeMillis()))
			.setOrderBy("startTime")
			.setSingleEvents(true)
			.execute()
			.getItems();
		//Log.d(TAG, "AsyncLoadGoogleCalendarTask.doInBackground: " + events_);
	}

	static
		public void run(StacklrExpActivity activity, String calendarId) {
		new AsyncLoadGoogleCalendarTask(activity, calendarId).execute();
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		if(events_ != null){
			activity_.adapter_.pushCalendarEvents(events_);
		}
	}
}
