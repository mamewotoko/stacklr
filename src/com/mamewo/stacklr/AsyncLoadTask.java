package com.mamewo.stacklr;

import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static com.mamewo.stacklr.Constant.*;
import android.util.Log;

/**
 * Asynchronously load the tasks.
 * 
 */
class AsyncLoadTask
	extends CommonAsyncTask
{
	List<String> groupIdList_;

	public AsyncLoadTask(StacklrExpActivity activity, List<String> groupIdList) {
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
				//tasklist is null, if list is empty
				tasklist = client_.tasks().list(groupId).setFields("items/id,items/title,items/updated,items/status").execute().getItems();
			}
			result.add(tasklist);
		}
		activity_.adapter_.merge(result);
	}

	static
	public void run(StacklrExpActivity activity, List<String> groupIdList) {
		new AsyncLoadTask(activity, groupIdList).execute();
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		if (success) {
			activity_.refreshView();
		}
		activity_.uploadTasks();
	}
}
