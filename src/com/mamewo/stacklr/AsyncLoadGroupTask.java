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
 * list groups, fetch group id
 * 
 */
class AsyncLoadGroupTask
	extends CommonAsyncTask
{
	public AsyncLoadGroupTask(StacklrExpActivity activity) {
		super(activity);
	}

	@Override
	protected void doInBackground()
		throws IOException
	{
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
		activity_.adapter_.updateGroup(result, true);
	}

	static
	public void run(StacklrExpActivity activity) {
		new AsyncLoadGroupTask(activity).execute();
	}
}
