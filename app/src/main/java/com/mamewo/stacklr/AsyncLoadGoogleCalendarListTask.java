package com.mamewo.stacklr;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import java.util.Map;
import java.util.HashMap;

import android.widget.Toast;
import android.util.Log;

import static com.mamewo.stacklr.Constant.TAG;

import java.io.IOException;
import java.util.List;

public class AsyncLoadGoogleCalendarListTask
	extends CommonAsyncTask
{
	//one week before
	static public DateTime ONE_WEEK_AGO = new DateTime(System.currentTimeMillis()-7*24*60*60*1000);
	private String calendarName_;
	private String calendarId_;
	static private Map<String, String> calendarName2Id_ = new HashMap<String, String>();
	private CalendarIdRunnable post_;
	
	public AsyncLoadGoogleCalendarListTask(StacklrExpActivity activity,
										   String calendarName,
										   CalendarIdRunnable post){
		super(activity);
		calendarName_ = calendarName;
		post_ = post;
		calendarId_ = null;
	}
	
	@Override
	protected void doInBackground()
		throws IOException
	{
		//default
		calendarId_ = "primary";

		Log.d(TAG, "AsyncLoadGoogleCalendarListTask.doInBackground");
		List<CalendarListEntry> l = calendarClient_
			.calendarList()
			.list()
			.execute()
			.getItems();
		synchronized(calendarName2Id_){
			for(CalendarListEntry entry: l){
				calendarName2Id_.put(entry.getSummary(), entry.getId());
			}
		}
	}

	static
	public void run(StacklrExpActivity activity,
					String calendarName,
					CalendarIdRunnable post) {
		//use cache data
		if(calendarName2Id_.size() > 0){
			String calendarId = calendarName2Id_.get(calendarName);
			if(null != calendarId){
				post.run(calendarName, calendarId);
			}
			else {
				//calendar not found
				String errorMessage = String.format(activity.getString(R.string.error_calendar_not_found),
													calendarName);
				Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
			}
			return;
		}
		new AsyncLoadGoogleCalendarListTask(activity, calendarName, post).execute();
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		//TODO: save calendarId
		//XXX
		//AsyncLoadGoogleCalendarTask.run(activity_, calendarId_);
		if(success){
			String calendarId = calendarName2Id_.get(calendarName_);
			if(null != calendarId){
				post_.run(calendarName_, calendarName2Id_.get(calendarName_));
			}
			else {
				//calendar not found
				String errorMessage = String.format(activity_.getString(R.string.error_calendar_not_found),
													calendarName_);
				Toast.makeText(activity_, errorMessage, Toast.LENGTH_LONG).show();
			}
		}
	}

	interface CalendarIdRunnable {
		public void run(String calendarName, String calendarId);
	}
}
