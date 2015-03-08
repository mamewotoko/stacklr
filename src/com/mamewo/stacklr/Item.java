package com.mamewo.stacklr;

import java.text.SimpleDateFormat;
import java.util.Date;

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
	public int ITEM_TYPE_FOOD = 0;
	static final
	public int ITEM_TYPE_ARTICLE = 1;

	public Item(String name){
		this(name, 0, ITEM_TYPE_FOOD);
		name_ = name;
		lastTouchedTime_ = 0;
	}
	
	public Item(String name, long time){
		this(name, time, ITEM_TYPE_FOOD);
	}
	
	public Item(String name, long time, int type){
		name_ = name;
		lastTouchedTime_ = time;
		type_ = type;
	}

	public void setLastTouchedTime(long time){
		lastTouchedTime_ = time;
	}

	public String getName(){
		return name_;
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
		return TIME_FORMAT.format(new Date(lastTouchedTime_));
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
		return (int)(lastTouchedTime_ - item.lastTouchedTime_);
	}
}

