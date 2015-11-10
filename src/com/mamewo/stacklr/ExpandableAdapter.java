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

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

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
	//TODO:Customize?
	private List<List<Item>> children_;
	private List<ItemStorage> storageList_;
	private List<Group> groups_;
	private Map<String, Item> name2Item_;
	private StacklrExpActivity activity_;
	private long lastModifiedTime_;
	private long lastSavedTime_;

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
			Log.d(TAG, "ExpandableAdapter: load "+ filename + " "+children_.get(children_.size()-1).size());
			for(Item child: children_.get(children_.size()-1)){
				name2Item_.put(child.getName(), child);
			}
			//modify group name
		}
		lastModifiedTime_ = System.currentTimeMillis();
	}

	public void moveToNextGroup(int groupPosition, int childPosition){
 		Item item = children_.get(groupPosition).get(childPosition);
 		int nextGroupPosition = item.nextGroup();
		moveToGroup(groupPosition, childPosition, nextGroupPosition);
	}

	public void moveToGroup(int groupPosition, int childPosition, int nextGroupPosition){
		Log.d(TAG, "moveToGroup" + groupPosition + " "+childPosition);
		Item item = children_.get(groupPosition).remove(childPosition);
		item.setGroup(nextGroupPosition);
		item.setLastTouchedTime(System.currentTimeMillis());
		List<Item> lst = children_.get(nextGroupPosition);
		Util.insertItem(lst, item, ASCENDING);
		notifyDataSetChanged();
	}

	public Item search(String itemname){
		return name2Item_.get(itemname);
	}

	//TODO: remove this method
	public void pushToBuy(Item item) {
		Item existing = name2Item_.get(item.getName());
		if(existing != null){
			children_.get(existing.getGroup()).remove(existing);
		}
		else {
			name2Item_.put(item.getName(), item);
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
				pushToBuy(new Item(itemname, System.currentTimeMillis(), Item.ITEM_TYPE_FOOD, TO_BUY));
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

	public void save() {
		for (int i = 0; i < storageList_.size(); i++) {
			storageList_.get(i).save(children_.get(i));
		}
		lastSavedTime_ = System.currentTimeMillis();
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
		//int name_id = android.R.id.text1;
		int name_id = R.id.item_name;
		
		if (convertView == null) {
			// convertView = View.inflate(activity_,
			// 						   android.R.layout.simple_expandable_list_item_2, null);
			convertView = View.inflate(activity_, R.layout.exp_item, null);
		}
		// TextView text = (TextView) convertView.findViewById(R.id.item_name);
		TextView text = (TextView) convertView.findViewById(name_id);
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
		//XXX
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	static String groupNameToFilename(String groupName){
		return groupName.replaceAll(" ", "_")+".txt";
	}
}
