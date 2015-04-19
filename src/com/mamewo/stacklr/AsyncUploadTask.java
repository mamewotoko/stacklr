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
class AsyncUploadTasks
	extends CommonAsyncTask
{
	private List<Item> lst_;

	public AsyncUploadTasks(StacklrExpActivity activity, List<Item> lst) {
		super(activity);
		lst_ = lst;
	}

	@Override
	protected void doInBackground()
		throws IOException
	{
		//TODO: use batch?
		//BatchRequest batch = client_.batch();
		for(Item item: lst_){
			client_.tasks().insert(TO_BUY_GTASK_ID, item.toGtask()).execute();
		}
	}

	static
	public void run(StacklrExpActivity activity, List<Item> lst) {
		new AsyncUploadTasks(activity, lst).execute();
	}
}
