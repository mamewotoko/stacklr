package com.mamewo.stacklr;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static com.mamewo.stacklr.Constant.TAG;

import android.util.Log;

public class Group
{
	static final String FILENAME = "_group.csv";
	static final String[] CSV_HEADER = new String[]{"name"};
	private String name_;

	public Group(String name){
		name_ = name;
	}

	public String getName(){
		return name_;
	}


	@Override
	public String toString(){
		return "group: "+name_;
	}
}

