package com.mamewo.stacklr;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.api.services.tasks.model.Task;
import com.google.api.client.util.DateTime;

public class Item
	implements Comparable
{
	private String name_;
	private int type_;
	private long lastTouchedTime_;
	static final
	private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
	static final
	private SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	static final
	public int ITEM_TYPE_TOP = 0;
	static final
	public int ITEM_TYPE_FOOD = 1;
	static final
	public int ITEM_TYPE_ARTICLE = 2;
	static final
	public int ITEM_TYPE_BOTTOM = 3;

	private Task gtask_;

	public Item(String name, long time, int type, Task gtask){
		name_ = name;
		lastTouchedTime_ = time;
		type_ = type;
		gtask_ = gtask;
	}
	
	public Item(String name){
		this(name, 0, ITEM_TYPE_FOOD, null);
	}
	
	public Item(String name, long time){
		this(name, time, ITEM_TYPE_FOOD, null);
	}
	
	//TODO: save item type in gtask
	public Item(Task gtask){
		//TODO: fix time
		this(null, 0, ITEM_TYPE_FOOD, gtask);
	}

	public long getLastTouchedTime(){
		if(gtask_ != null){
			DateTime time = gtask_.getUpdated();
			if(time == null){
				gtask_.setUpdated(new DateTime(System.currentTimeMillis()/1000));
				return System.currentTimeMillis();
			}
			return time.getValue();
		}
		return lastTouchedTime_;
	}

	public void setLastTouchedTime(long timeMillis){
		if(gtask_ != null){
			gtask_.setUpdated(new DateTime(timeMillis/1000));
			return;
		}
		lastTouchedTime_ = timeMillis;
	}

	public void setGtask(Task gtask){
		gtask_ = gtask;
	}

	public String getName(){
		if(gtask_ != null){
			return gtask_.getTitle();
		}
		return name_;
	}

	public int getType(){
		return type_;
	}

	public void setType(int t){
		type_ = t;
	}
	
	public String lastTouchedDateStr(){
		if(lastTouchedTime_ == 0){
			return "";
		}
		return DATE_FORMAT.format(new Date(lastTouchedTime_));
	}
	
	public String lastTouchedTimestampStr(){
		if(lastTouchedTime_ == 0){
			return "";
		}
		return TIME_FORMAT.format(new Date(getLastTouchedTime()));
	}

	public int elapsedDays(){
		long now = System.currentTimeMillis();
		return (int)((now-lastTouchedTime_)/((double)1000*60*60*24));
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
}
