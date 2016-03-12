package com.mamewo.stacklr;

import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.mamewo.stacklr.Constant.*;

import android.util.Log;

/**
 * Asynchronously insert tasks.
 * 
 */
class AsyncAddTask
	extends CommonAsyncTask
{
	private Map<Group,List<Item>> group2Items_;

	public AsyncAddTask(StacklrExpActivity activity, Map<Group,List<Item>> group2Items){
		super(activity);
		group2Items_ = group2Items;
	}

	@Override
	protected void doInBackground()
		throws IOException
	{
		//TODO: use batch?
		//BatchRequest batch = client_.batch();
		for(Map.Entry<Group, List<Item>> entry: group2Items_.entrySet()){
			Group group = entry.getKey();
			List<Item> items = entry.getValue();
			for(Item item: items){
				Log.d(TAG, "upload task: " + group.getName() + item.getName() + " " + item.toGtask());
				//TaskList lst = new TaskList();
				//lst.setTitle(item.getName());
				//lst.setUpdated(new DateTime(item.lastTouchedTime()));
				//client_.tasks().insert(lst).execute();
				Task gtask = client_.tasks()
					.insert(group.getGtaskListId(), item.toGtask())
					.execute();
				item.setGtask(gtask);
				Log.d(TAG, "upload end: " + group.getName() + item.getName());
			}
		}
	}

	static
	public void run(StacklrExpActivity activity, Map<Group,List<Item>> group2Items) {
		new AsyncAddTask(activity, group2Items).execute();
	}
}
