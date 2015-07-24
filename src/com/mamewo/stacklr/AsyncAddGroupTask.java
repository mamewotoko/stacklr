package com.mamewo.stacklr;

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.TasksRequest;

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
class AsyncAddGroupTask
	extends CommonAsyncTask
{
	private List<String> groupNames_;

	public AsyncAddGroupTask(StacklrExpActivity activity, List<String> groupNames) {
		super(activity);
		groupNames_ = groupNames;
	}

	@Override
	protected void doInBackground()
		throws IOException
	{
		//TODO: use batch?
		//BatchRequest batch = client_.batch();
		Map<String, TaskList> result = new HashMap<String, TaskList>();
		for(String groupName: groupNames_){
			Log.d(TAG, "add group: " + groupName);
			TaskList lst = new TaskList().setTitle(groupName);
			TaskList newList = client_.tasklists().insert(lst).execute();
			result.put(newList.getTitle(), newList);
		}
		activity_.adapter_.updateGroup(result, false);
	}

	static
	public void run(StacklrExpActivity activity, List<String> lst) {
		new AsyncAddGroupTask(activity, lst).execute();
	}
}
