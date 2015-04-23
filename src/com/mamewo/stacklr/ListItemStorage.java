package com.mamewo.stacklr;

import java.util.List;

public class ListItemStorage 
	implements ItemStorage
{
	private List<Item> list_;

	public ListItemStorage(List<Item> lst){
		list_ = lst;
	}

	public List<Item> load(int group){
		return list_;
	}

	public void save(List<Item> data){
		//nop....
	}
	
}
