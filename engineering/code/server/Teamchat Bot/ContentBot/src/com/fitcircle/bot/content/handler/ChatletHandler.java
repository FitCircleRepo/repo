package com.fitcircle.bot.content.handler;



import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import com.teamchat.client.sdk.Chatlet;
import com.teamchat.client.sdk.Expirable;
import com.teamchat.client.sdk.Form;
import com.teamchat.client.sdk.TeamchatAPI;
import com.teamchat.client.sdk.chatlets.BypassChatletCopy;
import com.teamchat.client.sdk.chatlets.PollChatlet;
import com.teamchat.client.sdk.chatlets.PrimaryChatlet;
import com.teamchat.client.sdk.chatlets.SimpleJsonChatlet;
import com.teamchat.client.sdk.chatlets.TextChatlet;
import com.fitcircle.bot.content.util.*;

public class ChatletHandler {
	
	JSONTemplateHandler json_template = new JSONTemplateHandler();  
	DatabaseConfig db_config = new DatabaseConfig();
	JSONArray queried_rooms = null, queried_rows = null;
	SimpleJsonChatlet chatlet_to_post = null;
	DatabaseConfig database_config = new DatabaseConfig();
	
     
    ChatletHandler chatlet_handler = this;
	
	
	public void postActivityLogger(TeamchatAPI api, String roomid){
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
		Calendar cal = Calendar.getInstance();
	  	String date = formatter.format(cal.getTime());
	       
		Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Posting activity logger for Room ID :"+roomid);	
		
		SimpleJsonChatlet activity_logger;
		try {
			json_template.getJSONTemplate("/ContentJSONs/ActivityLogger.json");
			json_template.insertDataIntoJsons("_date_", date);
			activity_logger = json_template.chatletToPost("activity");
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Activity Logger content retrieved as :"+activity_logger);
			api.perform(api.context().byId(roomid).post(activity_logger));
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Activity logger posted in Room  :"+roomid);
			HashMap<String, String> logger_id = new HashMap<String, String>();
			logger_id.put("LoggerChatlet", activity_logger.getFormId());
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Activity logger form ID retrieved as :"+activity_logger.getFormId());
			database_config.updateData("RoomData", logger_id, WorkflowConstants.logger_chatlet_criteria(roomid));
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Updated LoggerChatlet Field in RoomData table with value :"+activity_logger.getFormId());
		} 
		catch (Exception exception) {
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while posting Activity Logger"+exception.toString());
		}
				
	
		
		
	}	
	
	
	public void expireActivityLogger(TeamchatAPI api, String roomid){
		
		try {
			JSONArray queried_data = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.fetch_logger_chatlet_id(roomid)).toString());
			Logit.logWrite("ContentBotLogs/Posts//", "Retrieved Logger Chatlet ID data as :"+queried_data.toString());
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Expiring Logger Chatlet for Room ID :"+roomid);
			
			if(queried_data.size()>0)
			{
				JSONArray chatlet_id_value = (JSONArray) queried_data.get(0);
				String chatlet_id = (String) chatlet_id_value.get(0);
				Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Chatlet ID for activity logger recieved as :"+chatlet_id);
				if(chatlet_id.isEmpty())
				{
					Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Empty Chatlet ID recieved for activity logger for Roomd ID : "+roomid);
				}
				else
				{	
					Expirable remove = (Expirable) api.context().chatletById(chatlet_id);
					api.perform(api.context().byId(roomid).reply(remove.expireNow()));
					Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Activity Logger "+chatlet_id+" expired for Roomd ID : "+roomid);
				}
			}
		} catch (Exception exception) {
			
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while expiring Activity Logger for Room ID : "+roomid+" Exception : "+exception);
		}
		
	}
	
	
	
public void postRatingForm(TeamchatAPI api, String roomid){
	SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
	Calendar cal = Calendar.getInstance();
  	String date = formatter.format(cal.getTime());
       
	
		try {
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Posting Rating Form for Room ID :"+roomid);
			json_template.getJSONTemplate("/ContentJSONs/RatingForm.json");
			json_template.insertDataIntoJsons("_date_", date);
			SimpleJsonChatlet rating_logger = json_template.chatletToPost("rate");
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Rating Form content retrieved as :"+rating_logger);
			api.perform(api.context().byId(roomid).post(rating_logger));
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Rating Form posted in Room  :"+roomid);
			HashMap<String, String> logger_id = new HashMap<String, String>();
			logger_id.put("RatingChatlet", rating_logger.getFormId());
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Updated RatingChatlet Field in RoomData table with value :"+rating_logger.getFormId());
			database_config.updateData("RoomData", logger_id, WorkflowConstants.logger_chatlet_criteria(roomid));
		} catch (Exception exception) {
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while posting Rating Form for Room ID : "+roomid+" Exception : "+exception);
		}
	}

	
	
	public void postWorkout(TeamchatAPI api, String roomid){
		Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Posting Workouts to Room ID :"+roomid);
				
		JSONArray queried_rows = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.fetch_room_data(roomid)).toString());
		if(queried_rows.size()>0)
		{
			JSONArray room_data = (JSONArray) queried_rows.get(0);
			String day = (String) room_data.get(0);
			String circle_name = (String) room_data.get(1);
			String chatletid = (String) room_data.get(2);
					
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Workout Chatlets retrieved for Room :"+roomid);
			
			if(chatletid.isEmpty())
			{
				HashMap<String, String> chatlet_id_map = new HashMap<String, String>();
				chatlet_id_map.put("workoutchatlet1", circle_name+"workout1");
							
				
				database_config.updateData("RoomData", chatlet_id_map, WorkflowConstants.update_chatlet_id_criteria(roomid));
				Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Workout Chatlets updated for Room : :"+roomid);
			}
			
			if(day.isEmpty())
			{
				Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Day value empty for Room : :"+roomid);
			}
			else
			{	
				System.out.println("Current Day is :"+day);	
				JSONArray workout_data = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.workout_query(day)).toString());
				Logit.logWrite("/ContentBotLogs/Posts/", "Fetching workouts for day "+day+" to be posted in Room :"+roomid);
				JSONArray workout_row = (JSONArray) workout_data.get(0);
				if(workout_row.size()>0)
				{
					Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Workout row retrieved for day "+day+" to be posted in Room :"+roomid);
					
					try {
						json_template.getJSONTemplate("/ContentJSONs/workout.json");
						json_template.insertDataIntoJsons("_circuit_name_", (String) workout_row.get(1));
						json_template.insertDataIntoJsons("_exercise_name_1_", (String) workout_row.get(2));
						json_template.insertDataIntoJsons("_image_link_1_", (String) workout_row.get(3));
						json_template.insertDataIntoJsons("_instructions_1_1_", (String) workout_row.get(4));
						json_template.insertDataIntoJsons("_instructions_1_2_", (String) workout_row.get(5));
								
						json_template.insertDataIntoJsons("_exercise_name_2_", (String) workout_row.get(6));
						json_template.insertDataIntoJsons("_image_link_2_", (String) workout_row.get(7));
						json_template.insertDataIntoJsons("_instructions_2_1_", (String) workout_row.get(8));
						json_template.insertDataIntoJsons("_instructions_2_2_", (String) workout_row.get(9));
														
						json_template.insertDataIntoJsons("_exercise_name_3_", (String) workout_row.get(10));
						json_template.insertDataIntoJsons("_image_link_3_", (String) workout_row.get(11));
						json_template.insertDataIntoJsons("_instructions_3_1_", (String) workout_row.get(12));
						json_template.insertDataIntoJsons("_instructions_3_2_", (String) workout_row.get(13));
										
						json_template.insertDataIntoJsons("_exercise_name_4_", (String) workout_row.get(14));
						json_template.insertDataIntoJsons("_image_link_4_", (String) workout_row.get(15));
						json_template.insertDataIntoJsons("_instructions_4_1_", (String) workout_row.get(16));
						json_template.insertDataIntoJsons("_instructions_4_2_", (String) workout_row.get(17));
											
						json_template.insertDataIntoJsons("_exercise_name_5_", (String) workout_row.get(18));
						json_template.insertDataIntoJsons("_image_link_5_", (String) workout_row.get(19));
						json_template.insertDataIntoJsons("_instructions_5_1_", (String) workout_row.get(20));
						json_template.insertDataIntoJsons("_instructions_5_2_", (String) workout_row.get(21));
						SimpleJsonChatlet workout = json_template.chatletToPost(null);
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Workout Chatlet Retrieved as :"+workout.toString());
						BypassChatletCopy workout_post = new BypassChatletCopy(workout);
						workout_post.setFormId(chatletid);
						
						api.perform(api.context().byId(roomid).post(workout_post));
					} catch (Exception exception) {
						
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while posting Workout Circuit for Room ID : "+roomid+" Exception : "+exception.getStackTrace());
					}
					
										
				}
				else
				{
					Logit.logWrite("ContentBotLogs/Posts/"+roomid, "No workouts found for day "+day+" to be posted in Room :"+roomid);
				}
				
			}
		}
	}
	
	public void expireRating(TeamchatAPI api, String roomid){
		
		try {
			JSONArray rating_data = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.fetch_rating_chatlet_id(roomid)).toString());
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Retrieved Rating Chatlet data as :"+rating_data.toString());
			
			if(rating_data.size()>0)
			{
				JSONArray rating_id_value = (JSONArray) rating_data.get(0);
				String chatlet_id = (String) rating_id_value.get(0);
				Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Chatlet ID for Rating Form Retrieved :"+chatlet_id);
				if(chatlet_id.isEmpty())
				{
					Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Chatlet ID for Rating Form is empty");
				}
				else
				{	
					Expirable remove = (Expirable) api.context().chatletById(chatlet_id);
					api.perform(api.context().byId(roomid).reply(remove.expireNow()));
					Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Chatlet ID "+chatlet_id+" expired for Rating Form.");
				}
			}
			else
			{
				Logit.logWrite("Row not found for Room ID" + roomid);
			}
		} catch (Exception exception) {
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while expiring Rating Form for Room ID : "+roomid+" Exception : "+exception);
			
		}
		
	}
	
	
	
	public void expireDailyDose(TeamchatAPI api, String roomid){
		
		try {
			JSONArray queried_data = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.fetch_dailydose_chatlet_data(roomid)).toString());
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Daily Dose data retrieved as."+queried_data+" for Room ID :"+roomid);
			
			if(queried_data.size()>0)
			{
				JSONArray chatlet_id_value = (JSONArray) queried_data.get(0);
				String day = (String) chatlet_id_value.get(0);
				String chatlet_id = (String) chatlet_id_value.get(1);
				String post_type = (String) chatlet_id_value.get(2);
				String quiz_answer = (String) chatlet_id_value.get(3);
				Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Chatlet ID "+chatlet_id+" retrieved for Daily Dose for room :"+roomid);
				if(chatlet_id.isEmpty())
				{
					
					Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Empty Chatlet ID recieved for Daily Dose  for Roomd ID : "+roomid);
											
				}
				else
				{
					if(post_type.equals("Quiz"))
					{	
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Previous Daily Dose was quiz for Roomd ID : "+roomid);
						if(!quiz_answer.isEmpty())
						{	
							Chatlet chatlet = api.context().chatletById(chatlet_id);
							api.perform(api.context().currentRoom().comment(chatlet.createComment().addComment("Answer is : "+quiz_answer)));
							Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Posted Quiz answer in comments for Roomd ID : "+roomid);
						}
						Expirable remove = (Expirable) api.context().chatletById(chatlet_id);
						api.perform(api.context().byId(roomid).reply(remove.expireNow()));
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Daily Dose (Quiz) Chatlet Expired for Room ID : "+roomid);
					}
					else
					{
						Expirable remove = (Expirable) api.context().chatletById(chatlet_id);
						api.perform(api.context().byId(roomid).reply(remove.expireNow()));
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Daily Dose Chatlet Expired for Room ID : "+roomid);
					}
				}
			}
		} catch (Exception exception) {
			
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while expiring Daily Dose for Room ID : "+roomid+" Exception : "+exception);
		}
	}
	
	
	public void postDailyDose(TeamchatAPI api, String roomid){
		try {
			
			SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
			Calendar cal = Calendar.getInstance();
		  	String date = formatter.format(cal.getTime());
		       
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Posting Daily Dose Chatlet to room :"+roomid);
			SimpleJsonChatlet daily_dose = null;
			HashMap<String, String> dailydose_id = new HashMap<String, String>();
					
			JSONArray queried_data = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.fetch_dailydose_chatlet_data(roomid)).toString());
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Daily Dose data retrieved as."+queried_data+" for Room ID :"+roomid);
			
			if(queried_data.size()>0)
			{
				JSONArray chatlet_id_value = (JSONArray) queried_data.get(0);
				String day = (String) chatlet_id_value.get(0);
						
				if(day.isEmpty())
						{
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Day value for daily dose found empty for Roomd ID : "+roomid);
						}
					else
						{	
							Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Day value for daily dose retrieved as - "+day);
							JSONArray dailydose_data = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.fetch_dailydose_post(day)).toString());
							
							if(dailydose_data.size()>0)
							{	
								JSONArray dailydose_row = (JSONArray) dailydose_data.get(0);
								
									
								switch((String) dailydose_row.get(3))
								{
									case "Quiz":
										json_template.getJSONTemplate("/ContentJSONs/Quiz.json");
										json_template.insertDataIntoJsons("_date_", date);
										json_template.insertDataIntoJsons("_question_", (String) dailydose_row.get(4));
										json_template.insertDataIntoJsons("_option_1_", (String) dailydose_row.get(6));
										json_template.insertDataIntoJsons("_option_2_", (String) dailydose_row.get(7));
										json_template.insertDataIntoJsons("_option_3_", (String) dailydose_row.get(8));
										json_template.insertDataIntoJsons("_option_4_", (String) dailydose_row.get(9));
										daily_dose = json_template.chatletToPost(null);
										
										api.perform(api.context().byId(roomid).post(daily_dose));
										
										dailydose_id.put("DailyDoseChatlet", daily_dose.getFormId());
										dailydose_id.put("PostType", (String) dailydose_row.get(3));
										dailydose_id.put("QuizAnswer", (String) dailydose_row.get(5));
										
										break;
										
									default:
										
										json_template.getJSONTemplate("/ContentJSONs/dailydose.json");
										json_template.insertDataIntoJsons("_date_", date);
										json_template.insertDataIntoJsons("_image_link_", (String) dailydose_row.get(1));
										json_template.insertDataIntoJsons("_title_", (String) dailydose_row.get(3));
										json_template.insertDataIntoJsons("_content_", (String) dailydose_row.get(2));
										daily_dose = json_template.chatletToPost(null);
										
										api.perform(api.context().byId(roomid).post(daily_dose));
										
										dailydose_id.put("DailyDoseChatlet", daily_dose.getFormId());
										dailydose_id.put("PostType", "");
										dailydose_id.put("PostType", "");																				
										break;
								}
							
								database_config.updateData("RoomData", dailydose_id, WorkflowConstants.dailydose_chatlet_criteria(roomid));
						}
				}
					
			}
			else
			{
				System.out.println("No row found");
			}
		} catch (Exception exception) {
			
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while posting Daily Dose for Room ID : "+roomid+" Exception : "+exception);
		}
		
			
	}
	
	public void postLeaderboardChatlet(TeamchatAPI api, String roomid){
		try {
			String base_URL = "http://192.241.227.158:8080/ContentBot/handler?operation=leaderboard&roomid="+roomid;
			
			json_template.getJSONTemplate("/ContentJSONs/leaderboard.json");
			String URL = createEmbeddedLink(base_URL, "Leaderboard", "http");
			json_template.insertDataIntoJsons("_url_", URL);
			
			SimpleJsonChatlet leaderboard = json_template.chatletToPost(null);
			BypassChatletCopy leaderboard_post = new BypassChatletCopy(leaderboard);
			
			JSONArray leaderboard_data = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.leaderboard_form_id(roomid)).toString());
			JSONArray leaderboard_row = (JSONArray) leaderboard_data.get(0);
			String form_id = (String) leaderboard_row.get(0);
			String room_name = (String) leaderboard_row.get(0);
				
				if(form_id.isEmpty())
				{
					HashMap<String, String> leaderboard_form_id = new HashMap<String, String>();
					leaderboard_form_id.put("leaderboardchatlet", room_name+"leaderboard");
					database_config.updateData("RoomData", leaderboard_form_id, WorkflowConstants.leaderboard_chatlet_criteria(roomid));
									
					leaderboard_post.setFormId(room_name+"leaderboard");
				}
				else
				{
					leaderboard_post.setFormId(form_id);
				}
			
				api.perform(api.context().byId(roomid).post(leaderboard_post));
		} catch (Exception exception) {
			
			Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while posting Leaderboard for Room ID : "+roomid+" Exception : "+exception);
		}		
	}
	
	
	public String createEmbeddedLink(String url, String title, String protocol) throws JSONException
	  {
	  		JSONObject object = new JSONObject();
	  		JSONObject web = new JSONObject();
	  		web.put("title", title);
	  		web.put("cancelBtnName", "Back");
	  		web.put("minWidth", "200");
	  		web.put("draggable", "true");
	  		web.put("newWindow", "true");
	  		web.put("url", url);
	  		object.put("web", web);
	  		System.out.println(object.toString());
	  		byte[] byteArray = Base64.encodeBase64(object.toString().getBytes());
	  		String encodedString = new String(byteArray);
	  		String fUrl = protocol + "://teamchat:data=" + encodedString;

	  		return fUrl;

	  }
	
	
	
	public void postToAll(TeamchatAPI api, String post){
		JSONArray roomid_data = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.fetch_all_roomids()).toString());
		
		if(roomid_data.size()>0)
		{
			for(int row_index = 0; row_index<roomid_data.size(); row_index++)
			{
			JSONArray roomid_row = (JSONArray) roomid_data.get(row_index);
			String roomid = (String) roomid_row.get(0);
			
				switch(post)
				{
				case "activity":
					postActivityLogger(api, roomid);
					break;
				
				case "workout":
					
					try {
						expireActivityLogger(api, roomid);
					} catch (Exception exception) { 
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while expiring Activity Logger for Room ID : "+roomid+" Exception : "+exception);
					}
					
					try {
						expireRating(api, roomid);
					} catch (Exception exception) {
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while expiring Rating Form for Room ID : "+roomid+" Exception : "+exception);
					}
					
					try {
						expireDailyDose(api, roomid);
					} catch (Exception exception) {
						
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while expiring Daily Dose for Room ID : "+roomid+" Exception : "+exception);
					}
					
					try {
						postWorkout(api, roomid);
					} catch (Exception exception) {
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while posting workout for Room ID : "+roomid+" Exception : "+exception);
					}
					
					break;
					
				case "leaderboard":
					try {
						postLeaderboardChatlet(api, roomid);
					} catch (Exception exception) {
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while posting Leaderboard for Room ID : "+roomid+" Exception : "+exception);
					}
					break;
					
				case "dailydose":
					try {
						postDailyDose(api, roomid);
					} catch (Exception exception) {
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while posting Daily Dose for Room ID : "+roomid+" Exception : "+exception);					
					}
					break;
					
				case "rating":
					try {
						postRatingForm(api, roomid);
					} catch (Exception exception) {
						// TODO Auto-generated catch block
						Logit.logWrite("ContentBotLogs/Posts/"+roomid, "Exception caught while posting Rating Form for Room ID : "+roomid+" Exception : "+exception);
					}
					break;	
				}
			}
		}
		
	}
	
	
}