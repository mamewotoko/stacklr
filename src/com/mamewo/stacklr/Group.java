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
	static final String[] CSV_HEADER = new String[]{"name", "gid"};
	private String name_;

	public Group(String name, String gtaskListId){
		name_ = name;
	}

	public String getName(){
		return name_;
	}

	static
	public void save(File dataDir, List<Group> groupList){
		CSVWriter writer = null;
		File f = new File(dataDir, FILENAME);
		try {
			writer = new CSVWriter(new FileWriter(f));
			writer.writeNext(CSV_HEADER);
			for(Group group: groupList){
				writer.writeNext(new String[]{ group.getName(),
											   });
			}
		}
		catch(IOException e){
			Log.d(TAG, "IOException", e);
		}
		finally {
			if(writer != null){
				try{
					writer.close();
				}
				catch(IOException e){
					Log.d(TAG, "IOException", e);
				}
			}
		}
	}

	@Override
	public String toString(){
		return "group: "+name_;
	}

	static
	public List<Group> load(File dataDir){
		File f = new File(dataDir, FILENAME);
		if(!f.exists()){
			return null;
		}
		List<Group> result = new ArrayList<Group>();
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(f));
			String[] header = reader.readNext();
			Map<String, Integer> resolv = new HashMap<String, Integer>();
			for(int i = 0; i < header.length; i++){
				String col = header[i];
				resolv.put(col, Integer.valueOf(i));
			}
			String[] row;
			while((row = reader.readNext()) != null){
				String name = row[resolv.get("name")];
				String gid = row[resolv.get("gid")];
				if(gid.length() == 0){
					gid = null;
				}
				result.add(new Group(name, gid));
			}
		}
		catch(IOException e){
			Log.d(TAG, "IOException", e);
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
}

