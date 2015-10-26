package com.mamewo.stacklr;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.TasksRequest;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.CalendarScopes;

import com.google.api.client.util.DateTime;
import java.util.Date;

import android.util.Log;

import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.graphics.Color;
import android.widget.TextView;
import android.view.View;

import static com.mamewo.stacklr.Constant.*;

public class ExpandableAdapter
	extends BaseExpandableListAdapter
{
	// TODO:Customize?
	//TODO: Design storage
	private List<List<Item>> children_;
	private List<ItemStorage> storageList_;
	private List<Group> groups_;
	private Map<String, Item> name2Item_;
	private StacklrExpActivity activity_;

	//long touch -> history or remove
	public ExpandableAdapter(StacklrExpActivity activity, List<Group> groups){
		activity_ = activity;
		groups_ = groups;
		children_ = new LinkedList<List<Item>>();
		storageList_ = new LinkedList<ItemStorage>();
		name2Item_ = new HashMap<String, Item>();
		File datadir = activity_.getDataDir();
		for (int i = 0; i < groups_.size(); i++) {
			String filename = groupNameToFilename(groups_.get(i).getName());
			storageList_.add(new CSVItemStorage(new File(datadir, filename)));
			children_.add(storageList_.get(i).load(i));
			for(Item child: children_.get(children_.size()-1)){
				name2Item_.put(child.getName(), child);
			}
			//modify group name
		}
	}

	
	/**
	 * @param lst list of gtask for each group
	 */
	public void merge(List<List<com.google.api.services.tasks.model.Task>> lst){
		List<TasksRequest> operationList = new ArrayList<TasksRequest>();
		//debug
		// for(Group group: groups_){
		// 	Log.d(TAG, "merge: "+group.toString());
		// }
		Log.d(TAG, "merge list size: "+lst.size());
		//TODO: lock?
		//remove/move duplicate old items
		for(int nth = 0; nth < lst.size(); nth++){
			List<Item> targetChild = children_.get(nth);
			//TODO: detect removed item
			//move, load new, upload new
			String currentTime = new Date(System.currentTimeMillis()).toString();
			for(Task task: lst.get(nth)){
				String thisName = task.getTitle();
				if(thisName.isEmpty()){
					continue;
				}
				Log.d(TAG, "merge item name: "+thisName);

				Item existing = name2Item_.get(thisName);
				if(existing == null){
					if(!isTaskCompleted(task)){
						//new item
						Item newItem = new Item(task, nth);
						name2Item_.put(newItem.getName(), newItem);
						Util.insertItem(targetChild, newItem, ASCENDING);
					}
				}
				else {
					Log.d(TAG, "gtask exists: " + task + " " + isTaskCompleted(task));
					if(isTaskCompleted(task)){
						//remove existing
						//TODO: check completed time of gtask
						children_.get(existing.getGroup()).remove(existing);
						continue;
					}
					//TODO: update
					if(existing.getGtask() == null){
						existing.setGtask(task);
					}
					DateTime gtaskTime = task.getUpdated();
					if(gtaskTime != null){
						//TODO: handle case that local timestamp is empty
						//net is new or equal
						if(existing.getLastTouchedTime() == gtaskTime.getValue()){
							continue;
						}
						if(existing.getLastTouchedTime() < gtaskTime.getValue()){
							Log.d(TAG, "gtask is new: " + task);
							//remove old item
							existing.update(task);
							Task oldGtask = existing.getGtask();
							//old group id
							if(!oldGtask.getId().equals(task.getId())){
								try{
									String oldGroupId = groups_.get(existing.getGroup()).getGtaskListId();
									operationList.add(activity_.getTasksService().tasks().delete(oldGroupId, oldGtask.getId()));
								}
								catch(IOException e){
									Log.d(TAG, "IOException", e);
								}
							}
							children_.get(existing.getGroup()).remove(existing);
							//TODO: sync group
							List<Item> targetList = targetChild;
							Util.insertItem(targetList, existing, ASCENDING);
						}
						else {
							Log.d(TAG, "gtask is old: " + task);
							//net is old
							//update task
							//TODO: check diff?
							String destId = groups_.get(existing.getGroup()).getGtaskListId();
							if(destId == null){
								continue;
							}
							String oldGroupId = groups_.get(nth).getGtaskListId();
							try{
								//operationList.add(service_.tasks().move(destId, task.getId()));
								//side effect
								//check time
								Task existingGtask = existing.getGtask();
								Task localGtask = existing.toGtask().clone();
								localGtask.setId(null);
								localGtask.setNotes("moved " + currentTime);
								//Log.d(TAG, "feature: move (add): local " + " " + localGtask + " " + destId);
								//Log.d(TAG, "feature: move (add): gtask" + " " + task + " " + destId);
								String oldTaskId = task.getId();
								//TODO: update id?
								//operationList.add(service_.tasks().insert(destId, localGtask));
								operationList.add(activity_.getTasksService().tasks().delete(oldGroupId, oldTaskId));
							}
							catch(IOException e){
								Log.d(TAG, "IOException", e);
							}
						}
					}
				}
			}
		}
		AsyncExecOperationTask.run(activity_, operationList);
	}

	//move to async task
	public void pushEvents(List<Event> events){
		//push
		for(Event e: events){
			String name = e.getSummary();
			EventDateTime start = e.getStart();
			DateTime startDt = start.getDate();
			if(startDt == null){
				startDt = start.getDateTime();
			}			
			Log.d(TAG, "event from calendar(start,dt,date): "+e.getSummary()+" "+start+" "+start.getDateTime()+" "+start.getDate()+" "+startDt.getValue());
			Item existing = name2Item_.get(name);
			Item item;
			//XXX
			if(existing != null){
				if(startDt.getValue() <= existing.getLastTouchedTime()){
					Log.d(TAG, "pushToEvent: skip " + name);
					continue;
				}
				Log.d(TAG, "pushToEvent: pop " + name);
				item = existing;
				item.setLastTouchedTime(startDt.getValue());
			}
			else{
				item = new Item(name, "", startDt.getValue(), Item.ITEM_TYPE_TOP, TO_BUY);
			}
			item.setIsEvent(true);
			pushToBuy(item);
		}
	}

	public void updateGroup(Map<String, TaskList> result, boolean first){
		List<String> absentGroup = new ArrayList<String>();
		for(Group group: groups_){
			TaskList gtasklist = result.get(group.getName());
			if(gtasklist != null){
				Log.d(TAG, "updateGroup: " + gtasklist.getTitle() + " " + gtasklist.getId());
				group.setGtaskListId(gtasklist.getId());
			}
			else {
				//create tasklist
				if(first){
					absentGroup.add(group.getName());
				}
			}
		}
		//TODO: add condition to load
		if(first && !absentGroup.isEmpty()){
			AsyncAddGroupTask.run(activity_, absentGroup);
			return;
		}
		//TODO: modify
		activity_.startLoadTask(true);
	}

	public void moveToNextGroup(int groupPosition, int childPosition){
		//int nextGroupPosition = NEXT_GROUP[groupPosition];
		Item item = children_.get(groupPosition).remove(childPosition);
		int nextGroupPosition = item.nextGroup();
		Log.d(TAG, "moveToNextGroup: "+ item.toString() + " next: " + nextGroupPosition);
		item.setGroup(nextGroupPosition);
		item.setLastTouchedTime(System.currentTimeMillis());
		List<Item> lst = children_.get(nextGroupPosition);
		Util.insertItem(lst, item, ASCENDING);
		notifyDataSetChanged();
	}

	public void moveToGroup(int groupPosition, int childPosition, int nextGroupPosition){
		Item item = children_.get(groupPosition).remove(childPosition);
		item.setGroup(nextGroupPosition);
		item.setLastTouchedTime(System.currentTimeMillis());
		List<Item> lst = children_.get(nextGroupPosition);
		Util.insertItem(lst, item, ASCENDING);
		notifyDataSetChanged();
	}

	public Item search(String itemname){
		for(List<Item> itemlist: children_){
			for(int i = 0; i < itemlist.size(); i++){
				if(itemname.equals(itemlist.get(i).getName())){
					return itemlist.remove(i);
				}
			}
		}
		return null;
	}

	public void pushToBuy(Item item) {
		Item existing = name2Item_.get(item.getName());
		if(existing != null){
			children_.get(existing.getGroup()).remove(existing);
		}
		//xxx
		item.setGroup(TO_BUY);
		//children_.get(TO_BUY).add(0, item);
		List<Item> lst = children_.get(TO_BUY);
		Util.insertItem(lst, item, ASCENDING);

		notifyDataSetChanged();
	}

	public void pushToBuyList(String items) {
		BufferedReader br = new BufferedReader(new StringReader(items));
		String itemname;
		try {
			while ((itemname = br.readLine()) != null) {
				itemname = itemname.trim();
				if(itemname.length() == 0){
					continue;
				}
				//TODO: find existing item
				//TODO: if entered from text box
					
				//TODO: date
				pushToBuy(new Item(itemname, TO_BUY));
			}
		} catch (IOException e) {
			Log.d(TAG, "IOException", e);
		}
	}

	public Item remove(int group, int pos, boolean updateUI) {
		Item item = children_.get(group).remove(pos);
		if(updateUI){
			notifyDataSetChanged();
		}
		return item;
	}

	public Item remove(int group, int pos) {
		return remove(group, pos, true);
	}

	public Item get(int group, int pos){
		return children_.get(group).get(pos);
	}

	public void moveToHistory(int groupPosition, int childPosition){
		Item item = children_.get(groupPosition).remove(childPosition);
		//children_.get(HISTORY).add(0, item);
		//mmmm
		item.setGroup(HISTORY);
		List<Item> lst = children_.get(HISTORY);
		Util.insertItem(lst, item, ASCENDING);
		notifyDataSetChanged();
	}

	public void clearGroup(int groupPos){
		children_.get(groupPos).clear();
	}

	public void save() {
		for (int i = 0; i < storageList_.size(); i++) {
			storageList_.get(i).save(children_.get(i));
		}
	}

	@Override
	public int getGroupCount() {
		return children_.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return children_.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groups_.get(groupPosition).getName();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return children_.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
							 View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = View.inflate(activity_,
									   android.R.layout.simple_expandable_list_item_1, null);
		}
		//TMP?
		if(groupPosition >= groups_.size()){
			return convertView;
		}
		TextView text = (TextView) convertView.findViewById(android.R.id.text1);
		text.setText(groups_.get(groupPosition).getName());
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
							 boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = View.inflate(activity_,
									   android.R.layout.simple_expandable_list_item_2, null);
		}
		// TextView text = (TextView)
		// convertView.findViewById(R.id.item_name);
		TextView text = (TextView) convertView.findViewById(android.R.id.text1);
		//TextView text = (TextView) convertView.findViewById(R.id.item_name);
		Item item = children_.get(groupPosition).get(childPosition);
		String time = "";
		String date = item.lastTouchedDateStr();
		if(date.length() > 0){
			time = " : " + date + String.format(" (%dd)", item.elapsedDays());
		}
		text.setText(item.getName() + time);
		int color = 0;
		switch(item.getType()){
		case Item.ITEM_TYPE_TOP:
			color = Color.rgb(250, 175, 186);
			break;
		case Item.ITEM_TYPE_ARTICLE:
			color = Color.rgb(85, 183, 43);
			break;
		default:
			color = Color.rgb(255, 255, 255);
			break;
		}
		text.setTextColor(color);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	static String groupNameToFilename(String groupName){
		return groupName.replaceAll(" ", "_")+".txt";
	}

	//TODO: 定義するクラスを考える
	static boolean isTaskCompleted(com.google.api.services.tasks.model.Task task){
		return COMPLETED_STATUS.equals(task.getStatus());
	}
}
