package com.mamewo.stacklr;
public class Constant {
	static final
	protected String TAG = "stacklr";
	static final protected int REQUEST_GOOGLE_PLAY_SERVICES = 0;
	static final protected int REQUEST_AUTHORIZATION = 1;
	static final protected int REQUEST_ACCOUNT_PICKER = 2;

	//order of groups
	static protected final int TO_BUY = 0;
	static protected final int STOCK = 1;
	static protected final int HISTORY = 2;
	static protected final int ARCHIVE = 3;
	//static protected final int REMOVE = -1;

	// static final
	// protected String TO_BUY_GTASK_ID = "MTMwNzA3NjMyODY2ODMxNjg0NDQ6MjA4MTcxOTAzNzow";
	static String COMPLETED_STATUS = "completed";
	static final public boolean ASCENDING = false;

	static public final int[] NEXT_GROUP = new int[]{
		STOCK, //from to buy
		TO_BUY, //from stock, to buy(click) or history list(long)
		TO_BUY, //from history
		HISTORY //from archive
	};
}
