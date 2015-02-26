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

import static com.mamewo.stacklr.Constant.TAG;

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
	private String TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

	public CSVItemStorage(File f) {
		super(f);
	}

	@Override
	public List<Item> load() {
		CSVReader reader = null;
		List<Item> result = new LinkedList<Item>();
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
		if(!header[0].equals(TIMESTAMP_COLUMN)){
			try{
				reader.close();
			}
			catch (IOException e){
				Log.d(TAG, "IOException", e);
			}
			return super.load();
		}
		try{
			String[] row;
			SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
			while((row = reader.readNext()) != null){
				String name = row[1];
				if(row[0].length() == 0){
					result.add(new Item(name));
				}
				else {
					long timestamp = sdf.parse(row[0]).getTime();
					result.add(new Item(name, timestamp));
				}
			}
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
		String[] header = new String[]{ TIMESTAMP_COLUMN, NAME_COLUMN };
		try{
			writer =  new CSVWriter(new FileWriter(file_));
			writer.writeNext(header);
			for (Item item : data) {
				writer.writeNext(new String[] { item.lastTouchedTimestampStr(),
												item.getName() });
			}
			writer.close();
		}
		catch(IOException e){
			Log.d(TAG, "IOException", e);
		}
	}
}
