package com.mamewo.stacklr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.LinkedList;

import android.util.Log;
import static com.mamewo.stacklr.StacklrActivity.TAG;

//Line based text representation
public class FileItemStorage
	implements ItemStorage
{
    private String filename_;

    public FileItemStorage(String filename) {
        filename_ = filename;
    }

    @Override
    public List<String> load() {
		List<String> result = new LinkedList<String>();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(filename_));
			String line;
			while((line = br.readLine()) != null){
				result.add(line);
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
    public void save(List<String> data) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename_);
            for (String line : data) {
                pw.println(line);
            }
        } catch (IOException e) {
            Log.d(TAG, "IOException", e);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
}
