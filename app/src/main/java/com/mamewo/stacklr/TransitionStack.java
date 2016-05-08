package com.mamewo.stacklr;

import java.util.List;
import java.util.LinkedList;
import java.io.Serializable;

public class TransitionStack
	implements Serializable
{
	public class Transition
		implements Serializable
	{
		private Item item_;
		private int from_;
		private int to_;
		private long origTime_;

		public Transition(Item item, int from, int to, long origTime){
			item_ = item;
			from_ = from;
			to_ = to;
			origTime_ = origTime;
		}

		public Item getItem(){
			return item_;
		}

		public int getFrom(){
			return from_;
		}

		public int getTo(){
			return to_;
		}

		public long getOrigTime(){
			return origTime_;
		}
	}

	private List<Transition> stack_;
	public TransitionStack(){
		stack_ = new LinkedList<Transition>();
	}
	
	public void push(Item item, int from, int to, long origTime){
		stack_.add(0, new Transition(item, from, to, origTime));
	}

	public Transition remove(int nth){
		return stack_.remove(nth);
	}

	public Transition get(int nth){
		return stack_.get(nth);
	}
	
	public int size(){
		return stack_.size();
	}
}
