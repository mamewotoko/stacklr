package com.mamewo.stacklr;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Item 
{
	private String name_;
	private long lastTouchedTime_;
	SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy/MM/dd");

	public Item(String name){
		name_ = name;
		lastTouchedTime_ = 0;
	}
	
	public Item(String name, long time){
		name_ = name;
		lastTouchedTime_ = time;
	}

	public void setLastTouchedTime(long time){
		lastTouchedTime_ = time;
	}

	public String getName(){
		return name_;
	}
	
	public String getLastTouchedTimeStr(){
		if(lastTouchedTime_ == 0){
			return "";
		}
		return FORMAT.format(new Date(lastTouchedTime_));
	}
	
	public int elapsedDays(){
		long now = System.currentTimeMillis();
		return (int)((now-lastTouchedTime_)/((double)1000*60*60*24));
	}
}
