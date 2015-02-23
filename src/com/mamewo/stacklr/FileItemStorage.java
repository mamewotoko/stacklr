package com.mamewo.stacklr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.LinkedList;

import android.util.Log;
import java.io.File;

import static com.mamewo.stacklr.Constant.TAG;

//Line based text representation
public class FileItemStorage
	implements ItemStorage
{
	protected File file_;

	public FileItemStorage(File f) {
		file_ = f;
	}

	@Override
	public List<Item> load() {
		List<Item> result = new LinkedList<Item>();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(file_));
			String line;
			while((line = br.readLine()) != null){
				result.add(new Item(line));
			}
		}
		catch(IOException e){
			Log.d(TAG, "IOException", e);
		}
		finally {
			if(br != null){
				try{
					br.close();
				}
				catch(Exception e){

				}

			}
		}
		return result;
	}

	@Override
	public void save(List<Item> data) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file_);
			for (Item item : data) {
				pw.println(item.getName());
			}
		}
		catch (IOException e) {
			Log.d(TAG, "IOException", e);
		}
		finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
}
