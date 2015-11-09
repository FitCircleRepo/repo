package com.fitcircle.bot.content.handler;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.json.simple.JSONArray;

import com.fitcircle.bot.content.util.DatabaseConfig;
import com.teamchat.client.annotations.OnAlias;
import com.teamchat.client.annotations.OnKeyword;
import com.teamchat.client.sdk.Form;
import com.teamchat.client.sdk.TeamchatAPI;
import com.teamchat.client.sdk.chatlets.PrimaryChatlet;

public class ContentBot {
	
	TeamchatAPI api = RequestHandler.content_api; 
	ChatletHandler chatlet_handler = new ChatletHandler();
	DatabaseConfig database_config = new DatabaseConfig();
	
	@OnAlias("activity")
	public void processActivityData(TeamchatAPI api){
				
		//String challenge_score = "";
		String workout1_score = "";
		String workout2_score = "";
		String date_format = "dd MMM yyyy";
		String dateInString =new SimpleDateFormat(date_format).format(new Date());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
		LocalDate date = LocalDate.parse(dateInString, formatter);
		
		String email = api.context().currentReply().senderEmail();
		String roomid = api.context().currentRoom().getId();
		
		
		
		HashMap<String, String> activity_form_data = new HashMap<String, String>();
	
		
		
		
		/*switch(api.context().currentReply().getField("Challenge").toString())
		{		
			case "Done!":
				challenge_score = "100";
				break;
			
			case "Missed Out!":
				challenge_score = "0";
				break;
				
			default:
				challenge_score = "0";
				break;
									
		}*/
		
		if(api.context().currentReply().getField("Workout1").isEmpty())
			{
				workout1_score = "0";
			}
		else
			{
				workout1_score = "100";
			}
		
		
		if(api.context().currentReply().getField("Workout2").isEmpty())
			{
				workout2_score = "0";
			}
		else
			{
				workout2_score = "100";
			}
			
		
		
		activity_form_data.put("EmailID", email);
		activity_form_data.put("RoomID", roomid);
		activity_form_data.put("Date", dateInString);
		//activity_form_data.put("Challenge", "");
		activity_form_data.put("Workout1", api.context().currentReply().getField("Workout1"));
		activity_form_data.put("Workout1Desc", api.context().currentReply().getField("Workout1Desc"));
		activity_form_data.put("Workout2", api.context().currentReply().getField("Workout2"));
		activity_form_data.put("Workout2Desc", api.context().currentReply().getField("Workout2Desc"));
		//activity_form_data.put("ChallengeScore", "0");
		activity_form_data.put("Workout1Score", workout1_score);
		activity_form_data.put("Workout2Score", workout2_score);
				
		JSONArray queried_data = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.check_logger_record(email, roomid, date)).toString());
		
		
		if(queried_data.size()>0)
		{
			JSONArray room_data = (JSONArray) queried_data.get(0);
			String diet_score = (String) room_data.get(10);
			String challenge = (String) room_data.get(8);
			if(diet_score.isEmpty())
			{
				activity_form_data.put("DietScore", "0");
				activity_form_data.put("StressScore", "0");
				database_config.updateData("Logger", activity_form_data, WorkflowConstants.update_logger_data_criteria(email, roomid, date));
				System.out.println("Updated row");
			}
			else
			{
				database_config.updateData("Logger", activity_form_data, WorkflowConstants.update_logger_data_criteria(email, roomid, date));
				System.out.println("Updated row");
			}
			if(challenge.isEmpty())
			{
				activity_form_data.put("ChallengeScore", "0");
				database_config.updateData("Logger", activity_form_data, WorkflowConstants.update_logger_data_criteria(email, roomid, date));
			}
		}
		else
		{
			activity_form_data.put("ChallengeScore", "0");
			activity_form_data.put("DietScore", "0");
			activity_form_data.put("StressScore", "0");
			database_config.insertData("Logger", activity_form_data);
			System.out.println("Row inserted");
		}
	}
	
	
	@OnAlias("rate")
	public void processRating(TeamchatAPI api){
		
		String diet_score = "";
		String stress_score = "";
		
		String email = api.context().currentReply().senderEmail();
		String roomid = api.context().currentRoom().getId();
		
		String date_format = "dd MMM yyyy";
		String dateInString =new SimpleDateFormat(date_format).format(new Date());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
		LocalDate date = LocalDate.parse(dateInString, formatter);
		
		HashMap<String, String> rating_form_data = new HashMap<String, String>();
		
		switch(api.context().currentReply().getField("Diet").toString())
		{		
			case "Skipped Meals, Ate Unhealthy!":
				diet_score = "25";
				break;
			
			case "Skipped Meals, Ate Healthy":
				diet_score = "50";
				break;
				
			case "Ate after Good Intervals, Ate Unhealthy":
				diet_score = "75";
				break;
			
			case "A Bliss! Very Healhy Diet at Regular Intervals":
				diet_score = "100";
				break;
				
			default:
				diet_score = "0";
				break;	
		}
	
		switch(api.context().currentReply().getField("Stress").toString())
		{		
			case "Today was - Aaargh!":
				stress_score = "25";
				break;
			
			case "Stressful but Managed it!":
				stress_score = "50";
				break;
				
			case "A Little Stressful!":
				stress_score = "75";
				break;
			
			case "Completely Chilled Out!":
				stress_score = "100";
				break;	
			
			default:
				stress_score = "0";
				break;
				
		}
	
		rating_form_data.put("EmailID", email);
		rating_form_data.put("RoomID", roomid);
		rating_form_data.put("Date", dateInString);
		rating_form_data.put("Diet", api.context().currentReply().getField("Diet"));
		rating_form_data.put("Stress", api.context().currentReply().getField("Stress"));
		rating_form_data.put("DietScore", diet_score);
		rating_form_data.put("StressScore", stress_score);
		
		JSONArray queried_data = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.check_logger_record(email, roomid, date)).toString());
		
		if(queried_data.size()>0)
		{
			JSONArray room_data = (JSONArray) queried_data.get(0);
			String workout1_score = (String) room_data.get(8);
			String challenge = (String) room_data.get(8);
			if (workout1_score.isEmpty())
			{ 
				rating_form_data.put("Workout1Score", "0");
				rating_form_data.put("Workout2Score", "0");	
				database_config.updateData("Logger", rating_form_data, WorkflowConstants.update_logger_data_criteria(email, roomid, date));
				System.out.println("Updated row");
			}
			else
			{
				database_config.updateData("Logger", rating_form_data, WorkflowConstants.update_logger_data_criteria(email, roomid, date));
				System.out.println("Updated row");
			}
			
			if(challenge.isEmpty())
			{
				rating_form_data.put("ChallengeScore", "0");
				database_config.updateData("Logger", rating_form_data, WorkflowConstants.update_logger_data_criteria(email, roomid, date));
			}
			
		}
		else
		{
			rating_form_data.put("ChallengeScore", "0");
			rating_form_data.put("Workout1Score", "0");
			rating_form_data.put("Workout2Score", "0");	
			database_config.insertData("Logger", rating_form_data);
			System.out.println("Row inserted");
		}
		
		
		
		
	}
	
	
}
