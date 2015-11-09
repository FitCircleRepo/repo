package com.workflow.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONArray;

import com.teamchat.client.annotations.OnAlias;
import com.teamchat.client.annotations.OnKeyword;
import com.teamchat.client.annotations.OnMsg;
import com.teamchat.client.sdk.TeamchatAPI;
import com.teamchat.client.sdk.chatlets.PollChatlet;
import com.teamchat.client.sdk.chatlets.TextChatlet;
import com.workflow.handlers.DatabaseConfig;
import com.workflow.handlers.JSONTemplateHandler;
import com.workflow.handlers.Logit;

public class NutritionBot {

	TeamchatAPI api = RequestHandler.nutritionist_api;
	DatabaseConfig db_config = new DatabaseConfig();
	static final ChatletHandler chatlet_handler = new ChatletHandler();
	JSONTemplateHandler json_handler = new JSONTemplateHandler();
	//static ArrayList<String> user_buffer = new ArrayList<String>();
	ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	//boolean blocker;
	
	@OnMsg()
	public void msgReceived(TeamchatAPI api){
		String nightReply;
		String sender_email = api.context().currentSender().getEmail();
		System.out.println("Received Message from - " + sender_email);
		if(sender_email.equals("asst_nutritionist@fitcircle.in")||(sender_email.equals("nutrition_1@dumbelled.com"))||(sender_email.equals("rj.perennui+2@gmail.com"))){
			System.out.println("Bot/Subiyah messaged");
			if (sender_email.equals("nutrition_1@dumbelled.com"))
			{
				String nutrition_roomid = api.context().currentRoom().getId();
				HashMap<String, String> night_reply = new HashMap<String, String>();
				night_reply.put("nightReply", "");
				db_config.updateData("NutritionFormData", night_reply, "RoomID='" + nutrition_roomid + "'");
			}
		}
		else
		{	
			try{
				DateTime time = new DateTime(DateTimeZone.forOffsetHoursMinutes(5, 30));
				int hour = time.getHourOfDay();
				
				System.out.println("Received Message");
				String nutrition_roomid = api.context().currentRoom().getId();
				JSONArray queried_rows = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.complete_profile_query_nutrition(nutrition_roomid, sender_email)).toString());
				JSONArray queried_array = (JSONArray) queried_rows.get(0);
				Logit.logWrite("FitBuddyLogs", "Original profile data received from database: " + queried_rows.toString() + RequestHandler.fitbuddy_api.data());
				String name = (String) queried_array.get(1);
				String email = (String) queried_array.get(0);
				String phone = (String) queried_array.get(2);
				String height = (String) queried_array.get(3);
				String weight = (String) queried_array.get(4);
				String first_consultation = (String) queried_array.get(5);
				JSONArray req_row = db_config.retrieveQueryRows(db_config.retrieveData("select nightReply from NutritionFormData where RoomID = '" + nutrition_roomid + "';").toString());
				if (!(req_row.isEmpty()))
				{
					JSONArray req_array = (JSONArray) req_row.get(0);
					nightReply = (String) req_array.get(0);
				}
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				int day_of_week = c.get(Calendar.DAY_OF_WEEK);
				
				if(first_consultation.equals("done"))
				{
					System.out.println("first consultation is done");
					/*if ((((hour > 17) || (hour < 9)) || (((hour > 18) || (hour < 9)))) || day_of_week == 1)
					{
						System.out.println("It is after-hours or Sunday.");
						if (nightReply.isEmpty())
						{
							System.out.println("First time user is messaging during off hours");
							api.performPostInCurrentRoom(new TextChatlet("Thanks, The nutritionist is available from 9 am to 7 pm IST. I have booked your consultation for tomorrow :)"));
							System.out.println("Off-hours message sent");
							HashMap<String, String> night_reply = new HashMap<String, String>();
							night_reply.put("nightReply", "y");
							db_config.updateData("NutritionFormData", night_reply, "RoomID='" + nutrition_roomid + "'");
							System.out.println("NutritionFormData table updated with nightReply");
						}
					}
					else
					{
						api.performPostInCurrentRoom(new TextChatlet("Hi "+name+"! The nutritionist will be with you shortly!"));
					}*/
				}
				else
				{	
					System.out.println("Checking for profile");
					int phone_check = phone.length();
					int weight_length = weight.length();
					int height_length = height.length();
					
					if(phone_check > 0 && weight_length > 0 && height_length > 0)
					{
						System.out.println("Profile exists");
						chatlet_handler.postNutritionForm(api, nutrition_roomid);
					}
					else
					{
						System.out.println("Profile does not exist");
						api.performPostInCurrentRoom(new TextChatlet("Hey, it looks like you haven't set up your profile yet. Please set up your profile so we can help you better!"));
						chatlet_handler.postSetupFormInNutritionRoom(api, nutrition_roomid);
												
					}
				}						
			}
			catch(Exception exception)
			{
				System.out.println("Exception caught while fetching profile information"+ exception);
									
			}
		}	
		
		
	}
	
	@OnKeyword("reset")
	public void resetConsultation(TeamchatAPI api)
	{
		String sender_email = api.context().currentSender().getEmail();
		/*if(sender_email.equals("nutrition_1@dumbelled.com"))
		{*/
			Logit.logWrite("FitBuddyLogs", "Resetting user's consultation marker");
			String room_id = api.context().currentRoom().getId();
			HashMap<String, String> consult_reset = new HashMap<String, String>();
			consult_reset.put("FirstConsultation", "");
			db_config.updateData("userprofilestest", consult_reset, "(roomid='"+room_id+"')");
			Logit.logWrite("FitBuddyLogs", "Reset done.");
		///}
	}

	/*Runnable runner = new Runnable(){ 
		public void run(){
			System.out.println("Starting to reply to patient buffer");
			for (String roomid:user_buffer)
			{
				RequestHandler.nutritionist_api.perform(RequestHandler.nutritionist_api.context().byId(roomid).post(new TextChatlet("Thanks, The nutritionist is available from 9 am to 7 pm IST. I have booked your consultation for tomorrow :)")));
			    System.out.println("Posted unavailability message in room " + roomid);
			}
			user_buffer.clear();
			System.out.println("Buffer cleared");
			blocker = false;
			System.out.println("Queue unblocked!");
			}
		};*/
}
