package com.mamewo.stacklr;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.Calendar;
import com.google.api.client.util.DateTime;

import java.util.Date;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static com.mamewo.stacklr.Constant.*;
import android.util.Log;

public class AsyncAddGoogleCalendarEvent
	extends CommonAsyncTask
{
	static final
    private String CHECK_MARK = "\u2714";
	private String calendarId_;
	private List<String> items_;

	public AsyncAddGoogleCalendarEvent(StacklrExpActivity activity, String calendarId, List<String> items){
		super(activity);
		calendarId_ = calendarId;
		items_ = items;
	}

	public AsyncAddGoogleCalendarEvent(StacklrExpActivity activity, String calendarId, String item){
		super(activity);
		calendarId_ = calendarId;
		items_ = new ArrayList<String>();
		items_.add(item);
	}

	@Override
	protected void doInBackground()
		throws IOException
	{
		//TODO: batch?
		
		//5 min
		long EVENT_INTERVAL = 5*60*1000;
		long nowmillis = java.util.Calendar.getInstance().getTimeInMillis();
		EventDateTime nowtime = new EventDateTime().setDateTime(new DateTime(new Date(nowmillis)));
		EventDateTime endtime = new EventDateTime().setDateTime(new DateTime(new Date(nowmillis+EVENT_INTERVAL)));
		for(String item: items_){
			Event event = new Event()
				.setSummary(CHECK_MARK+item)
				.setStart(nowtime)
				.setEnd(endtime);
			calendarClient_.events().insert(calendarId_, event).execute();
		}
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);
	}

	static
	public void run(StacklrExpActivity activity, String calendarId, String item) {
		new AsyncAddGoogleCalendarEvent(activity, calendarId, item).execute();
	}

	static
	public void run(StacklrExpActivity activity, String calendarId, List<String> items) {
		new AsyncAddGoogleCalendarEvent(activity, calendarId, items).execute();
	}
}
