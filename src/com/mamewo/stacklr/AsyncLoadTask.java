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
	public AsyncLoadTasks(StacklrExpActivity activity) {
		super(activity);
	}

	@Override
	protected void doInBackground()
		throws IOException
	{
		List<Task> result = new ArrayList<Task>();
		//TODO: get task id, updated time
		List<Task> tasklists =
			client_.tasks().list(TO_BUY_GTASK_ID).setFields("items/id,items/title").execute().getItems();
		activity_.adapter_.merge(TO_BUY, result);
	}

	static
	public void run(StacklrExpActivity activity) {
		new AsyncLoadTasks(activity).execute();
	}
}
