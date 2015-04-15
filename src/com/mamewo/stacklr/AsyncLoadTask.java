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
	//TODO: get id by list API
	static final
	private String TO_BUY_GTASK_ID = "MTMwNzA3NjMyODY2ODMxNjg0NDQ6MjA4MTcxOTAzNzow";

	public AsyncLoadTasks(StacklrExpActivity activity) {
		super(activity);
	}

	@Override
	protected void doInBackground()
		throws IOException
	{
		List<Task> result = new ArrayList<Task>();
		//TODO: get task id, updated time
		List<Task> tasks =
			client_.tasks().list(TO_BUY_GTASK_ID).setFields("items/title,items/updated").execute().getItems();
		if(tasks == null){
			return;
		}
		//filter empty item
		for(Task task: tasks){
			if(task.getTitle().length() > 0){
				result.add(task);
			}
		}
		activity_.adapter_.merge(TO_BUY, result);
	}

	static
	public void run(StacklrExpActivity activity) {
		new AsyncLoadTasks(activity).execute();
	}
}