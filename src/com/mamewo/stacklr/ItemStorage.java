package com.mamewo.stacklr;

import java.util.List;

//TODO: add method to sync with storage
//TODO: add meta information (save serializable)
//preserve item order
public interface ItemStorage
{
	public List<Item> load();
	public void save(List<Item> data);
}
