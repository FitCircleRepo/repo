package com.workflow.core;

import com.teamchat.client.sdk.TeamchatAPI;
import com.workflow.handlers.*;

public class ThreadHandler extends Thread {
	
	JSONTemplateHandler jsonHandler;
	ChatletHandler chatlet_handler = new ChatletHandler();
	RequestHandler flow_handler = new RequestHandler();
	private Thread t;
	String name="";
	String roomid = "";
	
	ThreadHandler(JSONTemplateHandler json_handler, String threadname){
		name = threadname;
		jsonHandler = json_handler;
	}
	
	ThreadHandler(JSONTemplateHandler json_handler, String threadname, String roomid){
		name = threadname;
		jsonHandler = json_handler;
		this.roomid = roomid;
		
	}
	
	
	
	public void run(){
	
		synchronized(jsonHandler){
		
			switch(name)
				{
				case "QuoteMessage":
					try{
						System.out.println("Inside Thread Handler for Daily Workout");
						chatlet_handler.postDailyWorkout(RequestHandler.fitbuddy_api, null);
					}
					catch(Exception exception)
					{
						System.out.println(exception);
					}
					break;
				
				case "PlanPostForm":
					break;
				
				case "Attendance":
					try{
						System.out.println("Inside Thread Handler for Attendance");
						chatlet_handler.postAttendanceSurvey(RequestHandler.fitbuddy_api, "captureattendanceata");
					}
					catch(Exception exception)
					{
						System.out.println(exception);
					}
					break;
					
				case "ProgressTracker":
					try{
						System.out.println("Inside Thread Handler for Progress Tracker");
						chatlet_handler.postProgressTrackerScheduled(RequestHandler.fitbuddy_api);
					}
					catch(Exception exception)
					{
						System.out.println(exception);
					}
					break;
				
				default:
					break;
				/*case "Recommend":
					System.out.println("andar aaya");
					chatlet_handler.postRecommendation(flow_handler.fitbuddy_api, roomid);*/
				}
		}
	
	}
	
	public void start(){
		if(t == null){
			t= new Thread(this, name);
			t.start();
		}
	}

}