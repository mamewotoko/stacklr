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
class AsyncAddTasks
	extends CommonAsyncTask
{
	private String gid_;
	private List<Item> lst_;

	public AsyncAddTasks(StacklrExpActivity activity, String gid, List<Item> lst) {
		super(activity);
		lst_ = lst;
		gid_ = gid;
	}

	@Override
	protected void doInBackground()
		throws IOException
	{
		//TODO: use batch?
		//BatchRequest batch = client_.batch();
		for(Item item: lst_){
			client_.tasks().insert(gid_, item.toGtask()).execute();
		}
	}

	static
	public void run(StacklrExpActivity activity, String gid, List<Item> lst) {
		new AsyncAddTasks(activity, gid, lst).execute();
	}
}
