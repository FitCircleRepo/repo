package com.fitcircle.bot.content.util;

import java.io.*;
import java.text.*;
import java.util.*;

// Logger class to log all eventful occurrences and exceptions to a log folder.

public class Logit {
	static String log_message = null;
	static File log_folder = null;
	public static void logWrite(String file_location, String message)
	{
		log_folder = new File (file_location);
		
		if (!new File(file_location).exists())
		{
			log_folder.mkdir();
			
		}
		
		log_message = message;	
        Date log_time = new Date();
        String current_date = new SimpleDateFormat ("yyyy-MM-dd").format(log_time);
        //System.out.println(log_folder.getAbsolutePath());
        String log_location = log_folder.getAbsolutePath() + "/Logfile for " + current_date + ".txt";
        //System.out.println(log_location);
        try
        {
        	FileWriter log_writer = new FileWriter(log_location, true);
        	String currentTime = new SimpleDateFormat ("hh:mm:ss").format(log_time);
        	log_message = "\n" + currentTime + " - " +message;
        	log_writer.write(log_message);
        	log_writer.flush();
        	log_writer.close();
        }
        catch(Exception io_exception)
        {
        	io_exception.printStackTrace();
        }
        System.out.println(log_message);
	}
	
	public static void logWrite(String file, Exception exception)
	{
		logWrite(file, exception.toString());
		exception.printStackTrace();
	}
	
	public static void logWrite(String message)
	{
		logWrite("", message);
	}
	
	public static void logWrite(Exception exception)
	{
		logWrite("", "An exception has occurred: " + exception.toString() + "\n" + exception.getMessage());
		exception.printStackTrace();
	}
	
}
