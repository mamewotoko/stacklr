package com.mamewo.stacklr;

import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static com.mamewo.stacklr.Constant.*;

/**
 * Asynchronously load the tasks.
 * 
 */
class AsyncLoadTasks
	extends CommonAsyncTask
{
	List<String> groupIdList_;

	public AsyncLoadTasks(StacklrExpActivity activity, List<String> groupIdList) {
		super(activity);
		groupIdList_ = groupIdList;
	}

	@Override
	protected void doInBackground()
		throws IOException
	{
		List<List<Task>> result = new ArrayList<List<Task>>();
		//TODO: get task id, updated time
		for(String groupId: groupIdList_){
			List<Task> tasklist = null;
			if(groupId.length() > 0){
				tasklist = client_.tasks().list(groupId).setFields("items/id,items/title").execute().getItems();
			}
			else {
				tasklist = new ArrayList<Task>();
			}
			result.add(tasklist);
		}
		activity_.adapter_.merge(result);
	}

	static
	public void run(StacklrExpActivity activity, List<String> groupIdList) {
		new AsyncLoadTasks(activity, groupIdList).execute();
	}
}
