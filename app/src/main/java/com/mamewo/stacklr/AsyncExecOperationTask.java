package com.mamewo.stacklr;

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.TasksRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static com.mamewo.stacklr.Constant.*;
import android.util.Log;

/**
 * Asynchronously load the tasks.
 * 
 */
class AsyncExecOperationTask
	extends CommonAsyncTask
{
	private List<TasksRequest<?>> lst_;

	public AsyncExecOperationTask(StacklrExpActivity activity, List<TasksRequest<?>> lst) {
		super(activity);
		lst_ = lst;
	}

	@Override
	protected void doInBackground()
	{
		//TODO: use batch?
		//BatchRequest batch = client_.batch();
		int i = 0;
		for(TasksRequest<?> operation: lst_){
			Log.d(TAG, "execute: " +(i++)+ " " +operation.getClass().getName() + " " + operation.toString());
			try{
				operation.execute();
			}
			catch(IOException e){
				Log.d(TAG, "AsyncExecOperationTask", e);
			}
		}
	}

	static
	public void run(StacklrExpActivity activity, List<TasksRequest<?>> lst) {
		new AsyncExecOperationTask(activity, lst).execute();
	}
}
