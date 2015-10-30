package com.mamewo.stacklr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.LinkedList;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import android.util.Log;
import java.io.File;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import static com.mamewo.stacklr.Constant.*;

//csv representation
//timestamp, itemname
public class CSVItemStorage
	extends FileItemStorage
{
	static final
	private String TIMESTAMP_COLUMN = "timestamp";
	static final
	private String NAME_COLUMN = "name";
	static final
	private String TYPE_COLUMN = "type";
	static final
	private String GTASK_ID_COLUMN = "gtask_id";
	static final
	private String IS_EVENT_COLUMN = "is_event";

	static final
	private String[] CSV_HEADER = new String[]{ TIMESTAMP_COLUMN,
												NAME_COLUMN,
												TYPE_COLUMN,
												GTASK_ID_COLUMN,
												IS_EVENT_COLUMN };

	static final
	private String TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

	public CSVItemStorage(File f) {
		super(f);
	}

	//TODO: simplify (use CSV only)
	@Override
	public List<Item> load(int group) {
		CSVReader reader = null;
		List<Item> result = new LinkedList<Item>();
		if(!file_.exists()){
			return result;
		}
		try {
			reader = new CSVReader(new FileReader(file_));
		}
		catch(IOException e){
			Log.d(TAG, "IOException", e);
			return result;
		}
		String[] header = null;
		try{
			header = reader.readNext();
		}
		catch(IOException e){
			Log.d(TAG, "IOException", e);
			return result;
		}
		if(header == null){
			return result;
		}
		try{
			String[] row;
			SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
			//TODO: move method definition
			while((row = reader.readNext()) != null){
				long timestamp = 0;
				timestamp = sdf.parse(row[0]).getTime();
				String name = row[1];
				int itemtype = Integer.valueOf(row[2]);
				String gtaskId = "";
				if(row.length >= 4){
					gtaskId = row[3];
				}
				boolean isEvent = false;
				if(row.length >= 5){
					Boolean.valueOf(row[4]);
				}
				//XXX
				Item item = new Item(name, gtaskId, timestamp, itemtype, group);
				item.setIsEvent(isEvent);
				Util.insertItem(result, item, ASCENDING);
			}
			//sort result by timestamp
		}
		catch(IOException e){
			Log.d(TAG, "IOException", e);
		}
		catch(ParseException e2){
			Log.d(TAG, "IOException", e2);
		}
		finally {
			if(reader != null){
				try{
					reader.close();
				}
				catch(IOException e){
					Log.d(TAG, "IOException", e);
				}
			}
		}
		return result;
	}

	@Override
	public void save(List<Item> data) {
		CSVWriter writer = null;
		try{
			writer =  new CSVWriter(new FileWriter(file_));
			writer.writeNext(CSV_HEADER);
			for (Item item: data) {
				String gtaskId = "";
				if(item.getGtask() != null && item.getGtask().getId() != null){
					gtaskId = item.getGtask().getId();
				}
				//TODO: move to Item.java
				writer.writeNext(new String[] { item.lastTouchedTimestampStr(),
												item.getName(),
												String.valueOf(item.getType()),
												gtaskId,
												Boolean.toString(item.isEvent())});
			}
			writer.close();
		}
		catch(IOException e){
			Log.d(TAG, "IOException", e);
		}
	}
}
