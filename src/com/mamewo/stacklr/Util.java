package com.mamewo.stacklr;

import java.util.List;

public class Util {
	static
	public<T> void insertItem(List<T> lst, T item){
		insertItem(lst, item, 0, lst.size()-1);
	}

	static
	public<T> void insertItem(List<T> lst, T item, int from, int to){
		if(lst.size() == 0){
			lst.add(item);
		}
		else {
			int med = (from+to)/2;
			int diff = ((Comparable)item).compareTo(lst.get(med));
			if(from == to){
				if(diff <= 0){
					lst.add(from, item);
				}
				else {
					lst.add(from+1, item);
				}
			}
			else {
				if(diff < 0){
					insertItem(lst, item, from, Math.max(from, med-1));
				}
				//TODO: insert to head of run
				else if (diff == 0){
					lst.add(med, item);
				}
				else {
					insertItem(lst, item, Math.min(med+1,to), to);
				}
			}
		}
	}
}
