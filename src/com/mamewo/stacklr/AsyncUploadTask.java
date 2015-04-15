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
	//TODO: get id by list API
	static final
	private String TO_BUY_GTASK_ID = "MTMwNzA3NjMyODY2ODMxNjg0NDQ6MjA4MTcxOTAzNzow";

	private List<Item> lst_;

	public AsyncUploadTasks(StacklrExpActivity activity, List<Item> lst) {
		super(activity);
		lst_ = lst;
	}

	@Override
	protected void doInBackground()
		throws IOException
	{
		
	}
	
}
