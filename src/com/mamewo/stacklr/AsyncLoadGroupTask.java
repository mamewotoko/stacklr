package com.mamewo.stacklr;

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import static com.mamewo.stacklr.Constant.*;

import android.util.Log;

/**
 * Asynchronously load the tasks.
 * 
 */
class AsyncListTask
	extends CommonAsyncTask
{
	public AsyncListTask(StacklrExpActivity activity) {
		super(activity);
	}

	@Override
	protected void doInBackground()
		throws IOException
	{
		Log.d(TAG, "startLinkTask.doInBackground");

		Map<String, TaskList> result = new HashMap<String, TaskList>();
		//TODO: get task id, updated time
		List<TaskList> tasklists =
			client_.tasklists().list().setFields("items/title,items/id").execute().getItems();
		if(tasklists == null){
			return;
		}
		//filter empty item

		for(TaskList tasklist: tasklists){
			result.put(tasklist.getTitle(), tasklist);
		}
		Log.d(TAG, "startLinkTask tasklist: " + result.toString());

		activity_.adapter_.updateGroup(result);
	}

	static
	public void run(StacklrExpActivity activity) {
		new AsyncListTask(activity).execute();
	}
}
