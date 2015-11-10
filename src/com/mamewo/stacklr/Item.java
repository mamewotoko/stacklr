package com.mamewo.stacklr;

import java.text.SimpleDateFormat;
import java.util.Date;
import static com.mamewo.stacklr.Constant.*;

public class Item
	implements Comparable
{
	static final
	private int[] NEXT_GROUP = new int[]{
		STOCK, //from to buy
		TO_BUY, //from stock, to buy(click) or history list(long)
		TO_BUY, //from history
		HISTORY //from archive
	};

	private String name_;
	private int type_;
	private long lastTouchedTime_;
	static final
	private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
	static final
	private SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private int group_;

	static final
	public int ITEM_TYPE_TOP = 0;
	static final
	public int ITEM_TYPE_FOOD = 1;
	static final
	public int ITEM_TYPE_ARTICLE = 2;
	static final
	public int ITEM_TYPE_BOTTOM = 3;

	public Item(String name, long time, int type, int group){
		name_ = name;
		lastTouchedTime_ = time;
		type_ = type;

		group_ = group;
		//group ~ tasklist
	}

	public long getLastTouchedTime(){
		return lastTouchedTime_;
	}

	public void setLastTouchedTime(long timeMillis){
		lastTouchedTime_ = timeMillis;
	}

	public int getGroup(){
		return group_;
	}

	public void setGroup(int group){
		group_ = group;
	}

	public String getName(){
		return name_;
	}

	public int getType(){
		return type_;
	}

	public void setType(int t){
		type_ = t;
	}
	
	public String lastTouchedDateStr(){
		long time = getLastTouchedTime();
		if(time == 0){
			return "";
		}
		return DATE_FORMAT.format(new Date(time));
	}
	
	public String lastTouchedTimestampStr(){
		if(lastTouchedTime_ == 0){
			return "";
		}
		return TIME_FORMAT.format(new Date(getLastTouchedTime()));
	}

	public int elapsedDays(){
		long now = System.currentTimeMillis();
		return (int)((now-getLastTouchedTime())/((double)1000*60*60*24));
	}

	//ascending order of type
	//descending order of time
	@Override
	public int compareTo(Object other){
		Item item = (Item)other;
		// int diff = type_ - item.type_;
		// if(diff != 0){
		// 	return diff;
		// }
		int typediff = type_ - item.type_;
		if(typediff != 0){
			return -typediff;
		}
		long diff = getLastTouchedTime() - item.getLastTouchedTime();
		if(diff < 0){
			return -1;
		}
		if(diff > 0){
			return 1;
		}
		return 0;
	}

	public int nextGroup(){
		if(group_ == TO_BUY){
			return HISTORY;
		}
		return NEXT_GROUP[group_];
	}

	@Override
	public String toString(){
		return name_ + " " + type_ + " " + group_;
	}
}
