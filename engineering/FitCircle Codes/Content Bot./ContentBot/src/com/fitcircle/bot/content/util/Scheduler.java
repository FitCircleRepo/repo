package com.fitcircle.bot.content.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;

//Chatlet-timer class.

public class Scheduler
{
	public void createScheduledTask(Runnable task,int target_minute,int interval_in_minutes)
	{
		Logit.logWrite("ContentBotLogs", "Scheduler Initialized");
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		
		Calendar calendar = Calendar.getInstance();
   	 	int current_minute=calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE); 
   	 	int delay = (current_minute<target_minute) ?target_minute-current_minute:interval_in_minutes-(current_minute-target_minute);
	    scheduler.scheduleAtFixedRate(task,delay,interval_in_minutes, TimeUnit.MINUTES);
	    Logit.logWrite("ContentBotLogs", "Tasks Scheduled");
	}
	
	public void createTask(Runnable task,int minute_delay)
	{
		System.out.println("Inside createTask");
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		
		//Calendar calendar = Calendar.getInstance();
		//int hour = calendar.get(Calendar.HOUR_OF_DAY)*60;
		//int minute = calendar.get(Calendar.MINUTE);
   	 	//int current_minute= hour + minute; 
   	 	//int delay = current_minute + minute_delay;
	    scheduler.schedule(task, minute_delay, TimeUnit.MINUTES);
	    System.out.println("Task Scheduled");
	}
}