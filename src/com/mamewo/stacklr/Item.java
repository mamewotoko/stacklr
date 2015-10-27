package com.mamewo.stacklr;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.api.services.tasks.model.Task;
import com.google.api.client.util.DateTime;
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

	private Task gtask_;
	private String gtaskId_;
	private boolean isEvent_;

	public Item(String name, String gtaskId, long time, int type, int group){
		name_ = name;
		lastTouchedTime_ = time;
		type_ = type;

		gtaskId_ = gtaskId;
		group_ = group;
		//group ~ tasklist
		gtask_ = null;
		//umm...
		if(gtaskId_ != null && !gtaskId_.isEmpty()){
			gtask_ = toGtask();
		}
		isEvent_ = false;
	}

	//TODO: save item type in gtask
	//TODO: remove task dependency
	public Item(Task gtask, int group){
		name_ = gtask.getTitle();
		DateTime time = gtask.getUpdated();
		lastTouchedTime_ = 0;
		type_ = ITEM_TYPE_FOOD;
		if(time != null){
			lastTouchedTime_ = time.getValue();
		}
		gtask_ = gtask;
		group_ = group;
		isEvent_ = false;
	}

	public void setIsEvent(boolean isEvent){
		isEvent_ = isEvent;
	}
	
	public boolean isEvent(){
		return isEvent_;
	}

	public long getLastTouchedTime(){
		if(gtask_ != null){
			DateTime time = gtask_.getUpdated();
			if(time == null){
				gtask_.setUpdated(new DateTime(System.currentTimeMillis()));
				return System.currentTimeMillis();
			}
			return time.getValue();
		}
		return lastTouchedTime_;
	}

	public void setLastTouchedTime(long timeMillis){
		if(gtask_ != null){
			gtask_.setUpdated(new DateTime(timeMillis));
			return;
		}
		lastTouchedTime_ = timeMillis;
	}

	public void setGtask(Task gtask){
		gtask_ = gtask;
	}

	public com.google.api.services.tasks.model.Task getGtask(){
		return gtask_;
	}

	public int getGroup(){
		return group_;
	}

	public void setGroup(int group){
		group_ = group;
	}

	public Task toGtask(){
		if(gtask_ == null){
			gtask_ = new Task().setTitle(name_)
				.setUpdated(new DateTime(lastTouchedTime_));
			if(gtaskId_ != null && !gtaskId_.isEmpty()){
				gtask_.setId(gtaskId_);
			}
		}
		return gtask_;
	}

	public void update(Task gtask){
		DateTime date = gtask.getUpdated();
		long time;
		if(date == null){
			time = System.currentTimeMillis();
		}
		else {
			time = date.getValue();
		}
		setLastTouchedTime(time);
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
		if(isEvent_ && group_ == TO_BUY){
			return HISTORY;
		}
		return NEXT_GROUP[group_];
	}

	@Override
	public String toString(){
		return name_ + " " + type_ + " " + isEvent_ + " " + group_;
	}
}
