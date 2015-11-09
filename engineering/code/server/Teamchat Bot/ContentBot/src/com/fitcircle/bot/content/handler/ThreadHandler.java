package com.fitcircle.bot.content.handler;

import com.fitcircle.bot.content.util.*;

public class ThreadHandler extends Thread {
	
	JSONTemplateHandler jsonHandler;
	ChatletHandler chatletHandler;
	//RequestHandler flow_handler = new RequestHandler();
	private Thread t;
	String name="";
	String roomid = "";
	
	ThreadHandler(ChatletHandler chatlet_handler, String threadname){
		name = threadname;
		chatletHandler = chatlet_handler;
	}
	
	ThreadHandler(ChatletHandler chatlet_handler, String threadname, String roomid){
		name = threadname;
		chatletHandler = chatlet_handler;
		this.roomid = roomid;
		
	}
	
	
	
	public void run(){
	
		synchronized(chatletHandler){
		
			switch(name)
				{
				case "activity":
					chatletHandler.postToAll(RequestHandler.content_api, "activity");
					break;
					
				case "workout":
					chatletHandler.postToAll(RequestHandler.content_api, "workout");
					break;		
				
				case "leaderboard":
					chatletHandler.postToAll(RequestHandler.content_api, "leaderboard");
					break;		
								
				case "dailydose":
					chatletHandler.postToAll(RequestHandler.content_api, "dailydose");
					break;
					
				case "rating":
					chatletHandler.postToAll(RequestHandler.content_api, "rating");
					break;	
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