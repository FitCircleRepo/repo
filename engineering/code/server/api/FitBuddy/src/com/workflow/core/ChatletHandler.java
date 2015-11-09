package com.workflow.core;

//import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
//import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;


import com.teamchat.client.sdk.Form;
import com.teamchat.client.sdk.TeamchatAPI;
import com.teamchat.client.sdk.chatlets.BypassChatletCopy;
import com.teamchat.client.sdk.chatlets.PollChatlet;
import com.teamchat.client.sdk.chatlets.PrimaryChatlet;
import com.teamchat.client.sdk.chatlets.SimpleJsonChatlet;
import com.teamchat.client.sdk.chatlets.TextChatlet;
//import com.workflow.config.ConfigReader;
import com.workflow.handlers.*;

public class ChatletHandler {
	
	JSONTemplateHandler json_template = new JSONTemplateHandler();  
	DatabaseConfig db_config = new DatabaseConfig();
	//int character_read = 0;
	//String new_json_content = "";
	//String original_json_content = "";
	JSONArray queried_rooms = null, queried_rows = null;
	SimpleJsonChatlet chatlet_to_post = null;
	//String host_id = ConfigReader.get_host_id();
	Scheduler post_timer = new Scheduler();
	
	public void postDailyWorkout(TeamchatAPI api, String roomid)
	{
		
		json_template.getJSONTemplate("/jsonfolder/QuoteMessage.json");
		Logit.logWrite("FitBuddyLogs/DailyWorkout","Daily Workouts to be posted");
		if (roomid == null)
		{
			queried_rooms = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.room_id_query_string()).toString());
		}
		else
		{
			queried_rooms = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.room_id_query_string(roomid)).toString());
		}
			for (int row_index = 0; row_index < queried_rooms.size(); row_index ++)
			{
				JSONArray row_to_query = (JSONArray)queried_rooms.get(row_index);
				String room_id = (String) row_to_query.get(0);
				queried_rows = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.quote_message_query_string(room_id)).toString());
				for (int row_index_2 = 0; row_index_2 < queried_rows.size(); row_index_2 ++)
				{
				JSONArray row = (JSONArray)queried_rows.get(0);
				SimpleDateFormat date_format = new SimpleDateFormat("MM/dd/yyyy");
				String attendance_date = date_format.format(new Date());
				String workout_quote =  (String)row.get(0);
				String motivational_quote = (String) row.get(1);
				String video_link_icon_image = (String) row.get(2);
				String video_link = (String) row.get(3);
				String video_link_icon_image_2 = (String) row.get(4);
				String video_link_2 = (String) row.get(5);
				String video_link_icon_image_3 = (String) row.get(6);
				String video_link_3 = (String) row.get(7);
				int videolink = video_link.length();
				int videolink2 = video_link_2.length();
				int videolink3 = video_link_3.length();
				
				json_template.insertDataIntoJsons("attendance_date", attendance_date);
				json_template.insertDataIntoJsons("_PlanGoesHere_", workout_quote);
				json_template.insertDataIntoJsons("_MotiMessageGoesHere_", motivational_quote);
				if(videolink == 0)
				{
					json_template.insertDataIntoJsons("_URL_", "");	
				}
				else if(videolink2 == 0)
				{
					json_template.insertDataIntoJsons("_URL_", "<table style='border:20px; padding:30px'><tr><td><a href = '" + video_link + "'><img style='width:100%;' src='" + video_link_icon_image + "'/></a></td></tr></table>");
				}
				else if (videolink3 == 0)
				{
					json_template.insertDataIntoJsons("_URL_", "<table style='border:20px; padding:30px'><tr><td><a href = '" + video_link + "'><img style='width:100%;' src='" + video_link_icon_image + "'/></a></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td><a href = '" + video_link_2 + "'><img style='width:100%;' src='" + video_link_icon_image_2 + "'/></a></td></tr></table>");
				}
				else
				{
					json_template.insertDataIntoJsons("_URL_", "<table style='border:20px; padding:30px'><tr><td><a href = '" + video_link + "'><img style='width:100%;' src='" + video_link_icon_image + "'/></a></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td><a href = '" + video_link_2 + "'><img style='width:100%;' src='" + video_link_icon_image_2 + "'/></a></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td><a href = '" + video_link_3 + "'><img style='width:100%;' src='" + video_link_icon_image_3 + "'/></a></td></tr></table>");
				}
				Logit.logWrite("FitBuddyLogs/DailyWorkout","Room ID: " + room_id + ", Workout Content: " + json_template.new_json_content);
				api.perform(api.context().byId(room_id).post(json_template.chatletToPost(null)));
				}
			}
			
	}
	
	public void postPlanDetails(TeamchatAPI api, String room_id) throws Exception{
		
		json_template.getJSONTemplate("/jsonfolder/PlanPostForm.json");
		//String room_id = api.context().currentRoom().getId();
		queried_rows = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.get_plan_details_query_string(room_id)).toString());
		JSONArray queried_array = (JSONArray) queried_rows.get(0);
		String plan_name = (String) queried_array.get(0);
		String plan_allocation_date = (String) queried_array.get(1);
		String plan_progress = (String) queried_array.get(2);
		
		
		Date date = null;
		DateFormat dateformat = new SimpleDateFormat("MM/dd/yyyy");
		date = dateformat.parse(plan_allocation_date);
		String dateString = dateformat.format(date).toString();
		//SimpleDateFormat date_format = new SimpleDateFormat("MM/dd/yyyy");
		//String plan_date = date_format.parse(plan_allocation_date).toString();
		
		json_template.insertDataIntoJsons("_PlanName_", plan_name);
		json_template.insertDataIntoJsons("_PlanAllocationDate_", dateString);
		json_template.insertDataIntoJsons("_PlanProgress_", plan_progress);
		
		api.perform(api.context().byId(room_id).post(json_template.chatletToPost(null)));
	}
	
/**	public void postSetupProfileForm(TeamchatAPI api) throws Exception{	
		json_template.getJSONTemplate("/jsonfolder/setup.json");
		api.performPostInCurrentRoom(json_template.chatletToPost(/*"captureprofiledata"null));
}**/
	
	public void postSetupProfileForm(TeamchatAPI api, String roomid){	
		json_template.getJSONTemplate("/jsonfolder/setup.json");
		String url = "http://107.170.203.166:8080/FitBuddy/Dumbelled/registration.html?roomid="+roomid;
		url = createEmbeddedLink(url, "Setup Profile", "http");
		Logit.logWrite("FitBuddyLogs", "Setup URL resolved to webview URL as: " + url);
		json_template.insertDataIntoJsons("_url_", url);
		Logit.logWrite("FitBuddyLogs", "URL inserted into JSON");
		api.perform(api.context().byId(roomid).post(json_template.chatletToPost(/*"captureprofiledata"*/null)));
	}
	
	public void postHelpForm(TeamchatAPI api) throws Exception{
		json_template.getJSONTemplate("/jsonfolder/Help.json");
		api.performPostInCurrentRoom(json_template.chatletToPost(null));
	}
	
	public void postUserDashboard(TeamchatAPI api, String roomid){
		JSONArray queried_rows = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.room_id_query_string(roomid)).toString());
		JSONArray queried_array = (JSONArray) queried_rows.get(0);
		Logit.logWrite("FitBuddyLogs","User profile query: " + queried_rows.toString());
		String email = (String) queried_array.get(1);
		String plan_url = "http://107.170.203.166:8080/FitBuddy/Dumbelled/selectplan.html?sessionid=_email_";
		plan_url = plan_url.replaceAll("_email_", email);
		//plan_url = plan_url.replaceAll("_place_", roomid);
		String web_url = this.createEmbeddedLink(plan_url, "Plan Allocation", "http");
		Logit.logWrite("FitBuddyLogs","Plan Allocation webview URL to be posted: " + web_url);
		String know_more_url = createEmbeddedLink("http://fitcircle.in/Pages/PersonalizedPlans", "How Plans Work", "http");
		json_template.getJSONTemplate("/jsonfolder/UserDashBoard.json");
		json_template.insertDataIntoJsons("_plan_details_", know_more_url);
		json_template.insertDataIntoJsons("_plan_url_", web_url);
		json_template.postChatletByRoomId(api, json_template.chatletToPost(null), roomid);
		Logit.logWrite("FitBuddyLogs","Plan Allocation chatlet thrown");
	}
	
	public void postAttendanceSurvey(TeamchatAPI api, String alias) throws Exception{
		Date now = new Date();
		SimpleDateFormat WeekComplete = new SimpleDateFormat("EEEE");
		String DayOfTheWeek = WeekComplete.format(now);
		System.out.println(DayOfTheWeek);
		String file = DayOfTheWeek.toUpperCase();
		String filename = file + ".json";
		System.out.println(filename);
		json_template.getJSONTemplate(filename);
		
		queried_rooms = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.room_id_query_string()).toString());
		for (int row_index = 0; row_index < queried_rooms.size(); row_index ++)
		{
			JSONArray row_to_query = (JSONArray)queried_rooms.get(row_index);
			String room_id = (String) row_to_query.get(0);
			api.perform(api.context().byId(room_id).post(json_template.chatletToPost("captureattendancedata")));
		}
		
		
	}
	
	public void postBMIAnalysis(TeamchatAPI api, String filename) throws Exception{
		json_template.getJSONTemplate(filename);
		api.performPostInCurrentRoom(json_template.chatletToPost(null));
		
	}
	
	public void manageProfile(TeamchatAPI api, String roomid)
	{
		
		try{
			JSONArray queried_rows = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.complete_profile_query(roomid)).toString());
			JSONArray queried_array = (JSONArray) queried_rows.get(0);
			Logit.logWrite("FitBuddyLogs", "Original profile data received from database: " + queried_rows.toString());
			//System.out.println(queried_rows.toString());
			String name = (String) queried_array.get(1);
			String email = (String) queried_array.get(0);
			String phone = (String) queried_array.get(2);
			String height = (String) queried_array.get(3);
			String weight = (String) queried_array.get(4);
			String url = "http://107.170.203.166:8080/FitBuddy/Dumbelled/vieweditprof.html?roomid="+roomid;
			json_template.getJSONTemplate("/jsonfolder/EditProfile.json");
			url = createEmbeddedLink(url, "Edit Profile", "http");
			Logit.logWrite("FitBuddyLogs", "Setup URL resolved to webview URL as: " + url);
			json_template.insertDataIntoJsons("_name_", name);
			json_template.insertDataIntoJsons("_email_", email);
			json_template.insertDataIntoJsons("_height_", height);
			json_template.insertDataIntoJsons("_weight_", weight);
			json_template.insertDataIntoJsons("_phone_", phone);
			json_template.insertDataIntoJsons("_profile_url_", url);
			
			//api.performPostInCurrentRoom(json_template.chatletToPost("captureprofiledata"));
			api.perform(api.context().byId(roomid).post(json_template.chatletToPost("captureprofiledata")));
			
		}
		catch(Exception exception)
		{
			System.out.println("Exception caught while fetching profile information"+ exception);
								
		}
	}
	
	/*void postNutritionSurvey(TeamchatAPI api, String room_id, String alias)
	{
		try
		{
			//json_template.getJSONTemplate("nutrition.json");
			PrimaryChatlet nutrition_chatlet = new PrimaryChatlet();
			nutrition_chatlet.setQuestionHtml("<img src = 'http://fitcircle.in/fitcircle/images/Fitbuddy/nutrition.jpg' style ='width:100%;height:200px;'  />Please take the survey below to answer a few crucial questions pertinent to your diet.");
			Form nutrition_form = api.objects().form();
			nutrition_form.addField(api.objects().input().name("junk_food_count").label("How often do you eat out in a week?"));
			nutrition_form.addField(api.objects().input().name("current_diet").label("Give me some details about your current diet:"));
			nutrition_form.addField(api.objects().input().name("medical_conditions").label("Mention any medical conditions you may be suffering from, if any:"));
			nutrition_form.addField(api.objects().input().name("goals").label("Tell me about your goals:"));
			nutrition_chatlet.setReplyScreen(nutrition_form);
			nutrition_chatlet.allowComments(false);
			nutrition_chatlet.setReplyLabel("Take Survey");
			nutrition_chatlet.alias(alias);
			System.out.println("Test");
			System.out.println(nutrition_chatlet.toString());
			api.perform(api.context().byId(room_id).post(nutrition_chatlet));
		}
		
		catch (Exception exception)
		{
			System.out.println("Cannot get nutrition questionaire data!");
			exception.printStackTrace();
		}
			
	}*/
	/*
	String postFollowUpChatlet(TeamchatAPI api, TeamchatAPI nutritionist_api, String alias) throws Exception
	{
		String sender_room_id = "";
		try
		{
			PollChatlet follow_up = new PollChatlet();
			HashMap<String, String> nutrition_test_data = new HashMap<String, String>();
			String email_id = api.context().currentReply().senderEmail();
			nutrition_test_data.put("Email", (email_id));
			nutrition_test_data.put("RoomID", (api.context().currentRoom().getId()));
			nutrition_test_data.put("WeeklyJunkCount", (api.context().currentReply().getField("junk_food_count")));
			nutrition_test_data.put("CurrentDietDetails", (api.context().currentReply().getField("current_diet")));
			nutrition_test_data.put("Ailments", (api.context().currentReply().getField("medical_conditions")));
			nutrition_test_data.put("FitnessGoals", (api.context().currentReply().getField("goals")));
			sender_room_id = api.context().currentRoom().getId();
			
			db_config.insertData("NutritionFormData", nutrition_test_data);
			follow_up.setQuestionHtml("Thank you for your patience. Our nutritionist will analyze your data and be with you shortly. Please take care.");
			api.performPostInCurrentRoom(follow_up);
			PrimaryChatlet nutritionist_message = new PrimaryChatlet();
			nutritionist_message.setQuestionHtml("Email is " + nutrition_test_data.get("Email") + ", eats out " + nutrition_test_data.get("WeeklyJunkCount") + " time(s) a week, current diet details are: " + nutrition_test_data.get("CurrentDietDetails") + ". Suffers from " + nutrition_test_data.get("Ailments") + ", aims for: " + nutrition_test_data.get("FitnessGoals")).alias("nutritionreply");
			/*Form nutrition_form = api.objects().form();
			nutrition_form.addField(api.objects().input().name("nutrition_reply").label("Reply to nutrition query:"));
			nutritionist_message.setReplyScreen(nutrition_form);
			nutritionist_message.showDetails(false);
			nutritionist_message.allowComments(false);
			nutritionist_message.setReplyLabel("Reply to query");
			nutritionist_message.alias(alias);*/
		/*	JSONArray queried_data = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.nutrition_room_id_from_email_query_string(email_id)).toString());
			JSONArray queried_roomid = (JSONArray) queried_data.get(0);
			String nutrition_roomid = (String) queried_roomid.get(0);
			nutritionist_api.perform(nutritionist_api.context().byId(nutrition_roomid).post(nutritionist_message));
		}
		
		catch (Exception exception)
		{
			System.out.println("Sorry, there seems to have been some technical error at our end. We are extremely sorry for the inconvenience!");
			exception.printStackTrace();
		}
		return sender_room_id;
		
	}
	
	void postNutritionReply(TeamchatAPI api, String sender_id) throws Exception
	{
		try
		{
			PollChatlet nutrition_reply = new PollChatlet();
			String nutritionist_answer = api.context().currentReply().getField("nutrition_reply"); 
			nutrition_reply.setQuestionHtml("The nutritionist replies:" + nutritionist_answer);
			HashMap<String, String> update_nutrition_reply = new HashMap<String, String>();
			update_nutrition_reply.put("NutritionistReply", nutritionist_answer);
			db_config.updateData("NutritionFormData", update_nutrition_reply, WorkflowConstants.criteria_for_nutrition_response_update(sender_id));
			api.perform(api.context().byId(sender_id).post(nutrition_reply));
		}
		
		catch (Exception exception)
		{
			System.out.println("Sorry, there seems to have been some technical error at our end. We are extremely sorry for the inconvenience!");
			exception.printStackTrace();
		}
		
	}*/
  
  public void postMenu(TeamchatAPI api, String roomid){
	  		
			json_template.getJSONTemplate("/jsonfolder/explore.json");
			api.perform(api.context().byId(roomid).post(json_template.chatletToPost("ResponseToOffering")));
		
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

  public void publishRewardJson(TeamchatAPI api, String roomid)
	{		
	  		json_template.getJSONTemplate("/jsonfolder/Reward.json");
	  		String link = createEmbeddedLink("http://imojo.in/xjv5o", "Reward", "http");
			/*PrimaryChatlet p = new PrimaryChatlet().setQuestionHtml("<html><body><center><b>Workout To Win!</b></center><br />We applaud people who workout consistently and log their workouts, setting a good example for others around them. Log your workouts consistently and you can win rewards. To know more, click the button below.<br /><center><a href='"+link+"'><button style = 'background-color:#006699;color:#FFFFFF;'>Click here</button></a></center></body></html>");
			String roomid = p.getFormId();
			api.performPostInCurrentRoom(p);
			BypassChatletCopy report = new BypassChatletCopy(p);
			report.setFormId("testform");*/
	  		Logit.logWrite("FitBuddyLogs","Rewards webview URL: " + link);
	  		String know_more_url = createEmbeddedLink("http://fitcircle.in/Pages/Rewards", "Rewards", "http");
	  		json_template.insertDataIntoJsons("_rewards_details_", know_more_url);
	  		json_template.insertDataIntoJsons("_mojo_page_", link);
			api.perform(api.context().byId(roomid).post(json_template.chatletToPost(null)));
			Logit.logWrite("FitBuddyLogs","Rewards chatlet thrown");
			post_timer.createTaskSeconds(new Runnable(){
				public void run(){api.perform(api.context().byId(roomid).post(new TextChatlet("Please type 'help' to explore other FitBuddy features!")));}
			}, 10);
			//api.perform(api.context().byId(roomid).post(new TextChatlet("Please type 'help' to explore other FitBuddy features!")));
	}
/*	
  public void postRecommendation(TeamchatAPI api, String roomid){
  	
  		//String roomid = api.context().currentRoom().getId();
			json_template.getJSONTemplate("/jsonfolder/recommend.json");
			System.out.println("json found");
			//json_template.insertDataIntoJsons("_goal_", "I see, you prefer to - ");
			//json_template.insertDataIntoJsons("_plan_", "Looks like you have not taken a plan or nutrition consultation yet, why dont you try them out?");
			System.out.println(roomid);
			api.perform(api.context().byId(roomid).post(json_template.chatletToPost(null)));
		
  }*/
 
  public void postFitTestChatlet(TeamchatAPI api, String room_id)
  {
	  	Logit.logWrite("FitBuddyLogs", "Inside FitTest chatlet");
  		String url = createEmbeddedLink("http://107.170.203.166:8080/FitBuddy/Dumbelled/FitTest.html?roomid=" + room_id, "Fit Test", "http");
		Logit.logWrite("FitBuddyLogs","URL set for chatlet:" + url);
		json_template.getJSONTemplate("/jsonfolder/FitTest.json");
		String know_more_url = createEmbeddedLink("http://fitcircle.in/Pages/FitTest", "FitTest", "http");
		json_template.insertDataIntoJsons("_fittest_details_", know_more_url);
		json_template.insertDataIntoJsons("_fittest_page_", url);
		api.perform(api.context().currentRoom().post(json_template.chatletToPost(null)));
	    Logit.logWrite("FitBuddyLogs","FitTest chatlet posted");
  }
  
  /*public void postFitTestResult(TeamchatAPI api, HttpServletRequest request)
  {
  	String room_id = request.getParameter("room-id");
  	api.perform(api.context().byId(room_id).post(new TextChatlet("Please wait a while while we process your FitTest result!")));
  	String age = request.getParameter("age");
  	String gender = request.getParameter("gender");
  	int height_feet = Integer.parseInt(request.getParameter("height-feet"));
  	int height_inches = Integer.parseInt(request.getParameter("height-inches"));
  	double height = (height_feet * 30.48) + (height_inches * 2.54);
  	float weight = Float.parseFloat(request.getParameter("weight"));
  	String pushups = request.getParameter("pushups");
  	String squats = request.getParameter("squats");
  	int plank_minutes = Integer.parseInt(request.getParameter("low-plank-minutes"));
  	int plank_seconds = Integer.parseInt(request.getParameter("low-plank-seconds"));
  	int low_plank_time = (plank_minutes * 60) + plank_seconds;
  	String globe_jumps = request.getParameter("globe-jump");
  	int mile_minutes = Integer.parseInt(request.getParameter("one-mile-minutes"));
  	int mile_seconds = Integer.parseInt(request.getParameter("one-mile-seconds"));
  	int one_mile_time = (mile_minutes * 60) + mile_seconds;
  	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  	Calendar cal = Calendar.getInstance();
  	String date = dateFormat.format(cal.getTime());
  	String email = "";
  	try {
			JSONArray email_row = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.room_id_query_string(room_id)).toString());
			JSONArray queried_email = (JSONArray) email_row.get(0);
			email = (String) queried_email.get(1);
		}  catch (Exception e) {
			e.printStackTrace();
		}
  	
  	HashMap<String, String> fit_test_data = new HashMap<String, String>();
		fit_test_data.put("Email", email);
		fit_test_data.put("Height", Double.toString(height));
		fit_test_data.put("Weight", Double.toString(weight));
		fit_test_data.put("Pushups", pushups);
		fit_test_data.put("SquatCount", squats);
		fit_test_data.put("Age", age);
		fit_test_data.put("Gender", gender);
		fit_test_data.put("Low Plank", Integer.toString(low_plank_time));
		fit_test_data.put("GlobeJump", globe_jumps);
		fit_test_data.put("1MileTime", Integer.toString(one_mile_time));
		fit_test_data.put("Date", date);
		Logit.logWrite("FitBuddyLogs","FitTest data obtained" + fit_test_data.toString());
		db_config.insertData("FitTestInput", fit_test_data);
		Logit.logWrite("FitBuddyLogs","User fitness details stored in database.");
		//api.perform(api.context().byId(room_id).post(new TextChatlet("Your test has been completed!")));
		//Logit.logWrite("FitBuddyLogs","FitTest completion chatlet posted");
		Logit.logWrite("FitBuddyLogs","FitTest result data being retrieved");
		JSONArray queried_result = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.fit_test_result_query_string(email)).toString());
		JSONArray queried_row = (JSONArray) queried_result.get(0);
		String username = (String) queried_row.get(0);
		String rank = (String) queried_row.get(1);
		String FitScore = (String) queried_row.get(2);
		String FitMsg = (String) queried_row.get(3);
		String BMIScore = (String) queried_row.get(4);
		String BMIMsg = (String) queried_row.get(5);
		String endScore = (String) queried_row.get(6);
		String endMsg = (String) queried_row.get(7);
		String StrengthScore = (String) queried_row.get(8);
		String StrMsg = (String) queried_row.get(9);
		String total_tests_taken = (String) queried_row.get(10);
		Logit.logWrite("FitBuddyLogs","FitTest data received: \n" + "Username: " + username + ", Rank: " +
				rank + ", FitScore: " + FitScore + ", BMIScore: " + BMIScore + ", Endurance Score: " + endScore + ", StrengthScore: " + StrengthScore);
		json_template.getJSONTemplate("/jsonfolder/FitScoreJson.json");
		json_template.insertDataIntoJsons("_name_", username);
		json_template.insertDataIntoJsons("_rank_here_", rank);
		json_template.insertDataIntoJsons("_out_of_", total_tests_taken);
		json_template.insertDataIntoJsons("_fit_score_", FitScore);
		//json_template.insertDataIntoJsons("_fitscore_comment_", FitMsg);
		json_template.insertDataIntoJsons("_bmi_score_", BMIScore);
		//json_template.insertDataIntoJsons("_bmi_comment_", BMIMsg);
		json_template.insertDataIntoJsons("_endurance_score_", endScore);
		//json_template.insertDataIntoJsons("_endurance_comment_", endMsg);
		json_template.insertDataIntoJsons("_str_score_", StrengthScore);
		//json_template.insertDataIntoJsons("_str_comment_", StrMsg);
		String url = createEmbeddedLink("http://fitcircle.in/Pages/FitTest", "Help With FitTest", "http");
		json_template.insertDataIntoJsons("_url_", url);
		api.perform(api.context().byId(room_id).post(json_template.chatletToPost(null)));
		Logit.logWrite("FitBuddyLogs","FitTest result posted");
		api.perform(api.context().byId(room_id).post(new TextChatlet("Please type 'help' to explore other FitBuddy features!")));
  }*/
  
  public JSONArray getPlanOptions(HttpServletRequest request) throws ParseException, Exception
	{
  	String data = request.getParameter("data");
  	JSONObject json = new JSONObject(data);
  		Logit.logWrite("FitBuddyLogs", "Getting plan options, request parameters: " + json.toString());
		String currentRun = json.getString("currentRun");
		int weeks = json.getInt("weeks");
		String query = WorkflowConstants.RUN_PLAN_OPTIONS();
		query = String.format(query, currentRun, weeks);
		JSONArray target_options = db_config.retrieveQueryRows(db_config.retrieveData(query).toString());
		return target_options;
	}

/*public void postWorkoutChatlet(TeamchatAPI api, String sender_room_id) {
	api.perform(api.context().byId(sender_room_id).post(new PollChatlet().setQuestion("Hi, please tap the workout tab at the bottom to select a workout range and group.")));
}*/

public JSONArray getProfileDetails(TeamchatAPI api, HttpServletRequest request) 
{
  		String roomid = request.getParameter("sessionid");
  		Logit.logWrite("FitBuddyLogs", "Getting profile details, roomid passed in request is: " + roomid);
		String query = "select username, dob, phone, gender, height, weight from userprofilestest where roomid = '" + roomid +"';";
		JSONArray profile_details_query = db_config.retrieveQueryRows(db_config.retrieveData(query).toString());
		JSONArray profile_details = (JSONArray) profile_details_query.get(0);
		return profile_details;
}

public void postSetupFormInNutritionRoom(TeamchatAPI api, String roomid){	
	json_template.getJSONTemplate("/jsonfolder/setup.json");
	String url = "http://107.170.203.166:8080/FitBuddy/Dumbelled/registration2.html?roomid="+roomid;
	url = createEmbeddedLink(url, "Setup Profile", "http");
	Logit.logWrite("FitBuddyLogs", "Setup URL resolved to webview URL as: " + url);
	json_template.insertDataIntoJsons("_url_", url);
	Logit.logWrite("FitBuddyLogs", "URL inserted into JSON");
	api.perform(api.context().byId(roomid).post(json_template.chatletToPost(null)));
}
 
/*
 void postNutritionSurvey(TeamchatAPI api, String room_id, String alias)
	{
		try
		{
			//json_template.getJSONTemplate("nutrition.json");
			PrimaryChatlet nutrition_chatlet = new PrimaryChatlet();
			nutrition_chatlet.setXSLTForChatlet("primary-2.7.2");
			nutrition_chatlet.setQuestionHtml("<img src = 'http://fitcircle.in/fitcircle/images/Fitbuddy/nutrition.jpg' style ='width:100%;height:200px;'  />Please tell me a few things about your current diet and eating habits");
			Form nutrition_form = api.objects().form();
			nutrition_form.addField(api.objects().select().name("Goal").label("Goal").addOption("Weight Loss").addOption("Muscle Tone").addOption("Weight Gain").addOption("Detox"));
			nutrition_form.addField(api.objects().select().name("Cuisine").label("Preferred Cuisine").addOption("South Indian").addOption("North Indian"));
			nutrition_form.addField(api.objects().select().name(	 //api.perform(api.context().byId(nutritionRoomID).post(json_template.chatletToPost(null)));"Preference").label("Food Preferences").addOption("Veg").addOption("Non Veg").addOption("Egg"));
			nutrition_form.addField(api.objects().select().name("Activity").label("Current Physical Activity").addOption("Walking").addOption("Running").addOption("Gym").addOption("Yoga").addOption("Sports").addOption("Other").addOption("None"));	
			nutrition_form.addField(api.objects().select().name("Exercise").label("How often do you exercise for 20 minutes or more in a week?").addOption("0 to 1 times").addOption("2 to 3 times").addOption("3 to 5 times").addOption("More than 6 times"));
			nutrition_form.addField(api.objects().select().name("Supplements").label("Do you take Vitamin or Mineral Supplements?").addOption("Vitamin C").addOption("Vitamin D").addOption("Vitamin E").addOption("Calcium").addOption("Combination of above").addOption("Other").addOption("None"));
			nutrition_form.addField(api.objects().select().name("NutritionalSupplements").label("Do you take Nutritional Supplements?").addOption("Omega 3").addOption("Probiotics").addOption("Protien Shakes").addOption("Combination of above").addOption("Other").addOption("None"));
			nutrition_form.addField(api.objects().label().name("filler").label("Last but not the least, help us out with your Current Diet & Medical History so that we can advise you with the best solution "));
			nutrition_form.addField(api.objects().input().name("dietrecall").label("Please tell us your Yesterday's Detail Diet Recall - Everything you ate yesterday"));
			nutrition_form.addField(api.objects().input().name("medicalhistory").label("Medical History, if any "));
			nutrition_form.addField(api.objects().input().name("foodallergies").label("Food Allergies, if any"));
			
			nutrition_chatlet.allowComments(false);
			nutrition_chatlet.setReplyScreen(nutrition_form);
			nutrition_chatlet.setReplyLabel("Take Survey");
			nutrition_chatlet.alias(alias);
			nutrition_chatlet.showDetails(true);
			System.out.println(nutrition_chatlet.toString());
			api.perform(api.context().byId(room_id).post(nutrition_chatlet));
		}
		
		catch (Exception exception)
		{
			System.out.println("Cannot get nutrition questionaire data!");
			exception.printStackTrace();
		}
			
	}*/
 
 public void postNutritionForm(TeamchatAPI api, String nutritionRoomID){
	 json_template.getJSONTemplate("/jsonfolder/nutritionform.json");
		String url = "http://107.170.203.166:8080/FitBuddy/Dumbelled/nutritionform.html?roomid="+nutritionRoomID;
		url = createEmbeddedLink(url, "Nutrition Form", "http");
		Logit.logWrite("FitBuddyLogs", "Setup URL resolved to webview URL as: " + url);
		json_template.insertDataIntoJsons("_nutrition_url_", url);
		Logit.logWrite("FitBuddyLogs", "URL inserted into JSON");
		api.perform(api.context().byId(nutritionRoomID).post(json_template.chatletToPost(null)));
 }
 
 /*public void postProgressForm(TeamchatAPI api, String room_id){
	 json_template.getJSONTemplate("/jsonfolder/prores.json");
	 String replace_content = "<tr><th>Date</th><th colspan = '5'>Measurements</th></tr><tr><td>&nbsp</td><td>Weight</td><td>Arms</td><td>Waist</td><td>Thighs</td></tr>", analysis;
	 String date, weight, arms, waist = "0", thighs, hip = "0", gender = "M";
	 float waist_hip_ratio = 0F;
	 JSONArray queried_rows = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.progress_records(room_id)).toString());
	 
	 if(queried_rows.size()>0)
	 {
		 
			for (int row_index = 0; row_index < queried_rows.size(); row_index ++)
			{
				if (row_index > 2)
				{
					break;
				}
				JSONArray row_to_query = (JSONArray)queried_rows.get(row_index);
				date = (String) row_to_query.get(0);
				weight = (String) row_to_query.get(1);	
				arms = (String) row_to_query.get(2);
				waist = (String) row_to_query.get(3);
				thighs = (String) row_to_query.get(4);
				hip = (String) row_to_query.get(5);
				gender = (String) row_to_query.get(6);
				if(date.length() >0){
					replace_content = replace_content + "<tr><td>"+date+"</td><td>"+weight+"</td><td>"+arms+"</td><td>"+hip+"</td><td>"+waist+"</td><td>"+thighs+"</td></tr>";
					}
					else{
						System.out.println("Inside else");
					}
			}	
			System.out.println("Replace Str -" + replace_content);
			json_template.insertDataIntoJsons("_data_",replace_content);
			waist_hip_ratio = Integer.parseInt(waist)/Integer.parseInt(hip);
			if ((waist_hip_ratio < 0.85)&&(gender.equals("F")) || ((waist_hip_ratio < 1.0)&&(gender.equals("M"))))
			{
				analysis = "<p>Analysis: You are within your ideal weight range.</p>";
				json_template.insertDataIntoJsons("_analysis_", analysis);
			}
			else
			{
				analysis = "<p>Analysis: You are overweight.</p>";
				json_template.insertDataIntoJsons("_analysis_", analysis);
			}
			api.perform(api.context().byId(room_id).post(json_template.chatletToPost(null)));
			
	 }
	 else
	 {
		 json_template.insertDataIntoJsons("_data_", "<tr><td>You can start tracking your progress here</td></tr>");
		 api.perform(api.context().byId(room_id).post(new TextChatlet("You can start keepin track of your stats here")));
		 //api.perform(api.context().byId(nutritionRoomID).post(json_template.chatletToPost(null)));
		
	 }
	 	
		//api.perform(api.context().byId(nutritionRoomID).post(json_template.chatletToPost(null)));
					
 }
*/

 public void postFitTestResult(TeamchatAPI api, HttpServletRequest request)
 {
 	String data = request.getParameter("data");
 	JSONObject json = new JSONObject(data);
 	String room_id = json.getString("roomId");
 	
 	String age = json.getString("age");
 	String gender = json.getString("gender");
 	int height_feet = Integer.parseInt(json.getString("heightfeet"));
 	int height_inches = Integer.parseInt(json.getString("heightinches"));
 	double height = (height_feet * 30.48) + (height_inches * 2.54);
 	float weight = Float.parseFloat(json.getString("weight"));
 	String pushups = json.getString("pushups");
 	String squats = json.getString("squats");
 	int plank_minutes = Integer.parseInt(json.getString("lowplankminutes"));
 	int plank_seconds = Integer.parseInt(json.getString("lowplankseconds"));
 	int low_plank_time = (plank_minutes * 60) + plank_seconds;
 	String globe_jumps = json.getString("globejump");
 	int mile_minutes = Integer.parseInt(json.getString("onemileminutes"));
 	int mile_seconds = Integer.parseInt(json.getString("onemileseconds"));
 	int one_mile_time = (mile_minutes * 60) + mile_seconds;
 	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 	Calendar cal = Calendar.getInstance();
 	String date = dateFormat.format(cal.getTime());
 	String email = "";
 	try {
			JSONArray email_row = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.room_id_query_string(room_id)).toString());
			JSONArray queried_email = (JSONArray) email_row.get(0);
			email = (String) queried_email.get(1);
		}  catch (Exception e) {
			e.printStackTrace();
		}
 	
 	HashMap<String, String> fit_test_data = new HashMap<String, String>();
		fit_test_data.put("Email", email);
		fit_test_data.put("Height", Double.toString(height));
		fit_test_data.put("Weight", Double.toString(weight));
		fit_test_data.put("Pushups", pushups);
		fit_test_data.put("SquatCount", squats);
		fit_test_data.put("Age", age);
		fit_test_data.put("Gender", gender);
		fit_test_data.put("Low Plank", Integer.toString(low_plank_time));
		fit_test_data.put("GlobeJump", globe_jumps);
		fit_test_data.put("1MileTime", Integer.toString(one_mile_time));
		fit_test_data.put("Date", date);
		Logit.logWrite("FitBuddyLogs","FitTest data obtained" + fit_test_data.toString());
		db_config.insertData("FitTestInput", fit_test_data);
		Logit.logWrite("FitBuddyLogs","User fitness details stored in database.");
		api.perform(api.context().byId(room_id).post(new TextChatlet("I've received your FitTest details. Sharing your FitScore in a while.")));
		//api.perform(api.context().byId(room_id).post(new TextChatlet("Your test has been completed!")));
		//Logit.logWrite("FitBuddyLogs","FitTest completion chatlet posted");
		Logit.logWrite("FitBuddyLogs","FitTest result data being retrieved");
		JSONArray queried_result = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.fit_test_result_query_string(email)).toString());
		JSONArray queried_row = (JSONArray) queried_result.get(0);
		String username = (String) queried_row.get(0);
		String rank = (String) queried_row.get(1);
		String FitScore = (String) queried_row.get(2);
		String FitMsg = (String) queried_row.get(3);
		String BMIScore = (String) queried_row.get(4);
		String BMIMsg = (String) queried_row.get(5);
		String endScore = (String) queried_row.get(6);
		String endMsg = (String) queried_row.get(7);
		String StrengthScore = (String) queried_row.get(8);
		String StrMsg = (String) queried_row.get(9);
		String total_tests_taken = (String) queried_row.get(10);
		Logit.logWrite("FitBuddyLogs","FitTest data received: \n" + "Username: " + username + ", Rank: " +
				rank + ", FitScore: " + FitScore + ", BMIScore: " + BMIScore + ", Endurance Score: " + endScore + ", StrengthScore: " + StrengthScore);
		json_template.getJSONTemplate("/jsonfolder/FitScoreJson.json");
		json_template.insertDataIntoJsons("_name_", username);
		json_template.insertDataIntoJsons("_rank_here_", rank);
		json_template.insertDataIntoJsons("_out_of_", total_tests_taken);
		json_template.insertDataIntoJsons("_fit_score_", FitScore);
		//json_template.insertDataIntoJsons("_fitscore_comment_", FitMsg);
		json_template.insertDataIntoJsons("_bmi_score_", BMIScore);
		//json_template.insertDataIntoJsons("_bmi_comment_", BMIMsg);
		json_template.insertDataIntoJsons("_endurance_score_", endScore);
		//json_template.insertDataIntoJsons("_endurance_comment_", endMsg);
		json_template.insertDataIntoJsons("_str_score_", StrengthScore);
		//json_template.insertDataIntoJsons("_str_comment_", StrMsg);
		String url = createEmbeddedLink("http://fitcircle.in/Pages/FitTest", "Help With FitTest", "http");
		json_template.insertDataIntoJsons("_url_", url);
		api.perform(api.context().byId(room_id).post(json_template.chatletToPost(null)));
		Logit.logWrite("FitBuddyLogs","FitTest result posted");
		post_timer.createTaskSeconds(new Runnable(){
			public void run(){api.perform(api.context().byId(room_id).post(new TextChatlet("Please type 'help' to explore other FitBuddy features!")));}
		}, 10);
 }

 void postProgressTrackerScheduled(TeamchatAPI api)
 {
	 	SimpleDateFormat simple_date_format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calender = Calendar.getInstance();
		calender.setTime(new Date());
		calender.add(Calendar.DATE, 0); 
		String today = simple_date_format.format(calender.getTime());
		//System.out.println(today);
		JSONArray today_list = db_config.retrieveQueryRows(db_config.retrieveData("select roomid from userprofilestest where NextTrackerDate like '" + today + "%';").toString());
		for(int list_index = 0; list_index < today_list.size(); list_index ++)
		{
			JSONArray room_row = (JSONArray) today_list.get(list_index);
			String room_id = (String) room_row.get(0);
			postProgressForm(api, room_id);
		}
 }
public void postProgressForm(TeamchatAPI api, String room_id){
	 json_template.getJSONTemplate("/jsonfolder/pro.json");
		String url = "http://107.170.203.166:8080/FitBuddy/Dumbelled/progresstrack.html?roomid="+room_id;
		String progress_url = createEmbeddedLink(url, "Track Your Progress", "http");
		Logit.logWrite("FitBuddyLogs", "Progress Track URL resolved to webview URL as: " + progress_url);
		json_template.insertDataIntoJsons("_progress_url_", progress_url);
		Logit.logWrite("FitBuddyLogs", "URL inserted into JSON");
		url = "https://www.youtube.com/watch?v=LUOQtXvtnvg";
		//String info_url = createEmbeddedLink(url, "How To Measure", "http");
		//Logit.logWrite("FitBuddyLogs", "Progress Track URL resolved to webview URL as: " + info_url);
		json_template.insertDataIntoJsons("_info_url_", url);
		Logit.logWrite("FitBuddyLogs", "URL inserted into JSON");
	 //String replace_content = "<tr><th style='text-align:center;'>Date</th><th colspan = '5' style='text-align:center;'>Measurements</th></tr><tr><td>&nbsp</td><td>Weight</td><td>Arms</td><td>Hip</td><td>Waist</td><td>Thighs</td></tr>";
		String replace_content = null;
	 String date, weight, arms, waist , thighs, hip;
	 JSONArray queried_rows = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.progress_records(room_id)).toString());
	 float table_array[][] = new float[5][3];
	 String date_array[][] = new String[1][3];
	 if(queried_rows.size()>0)
	 {
		 
			for (int row_index = 0; row_index < queried_rows.size(); row_index ++)
			{
				JSONArray row_to_query = (JSONArray)queried_rows.get(row_index);
				date = (String) row_to_query.get(0);
				date_array[0][row_index] = date;
				weight = (String) row_to_query.get(1);
				table_array[0][row_index] = Float.parseFloat(weight);
				arms = (String) row_to_query.get(2);
				table_array[1][row_index] = Float.parseFloat(arms);	
				waist = (String) row_to_query.get(3);
				table_array[2][row_index] = Float.parseFloat(waist);
				thighs = (String) row_to_query.get(4);
				table_array[3][row_index] = Float.parseFloat(thighs);
				hip = (String) row_to_query.get(5);
				table_array[4][row_index] = Float.parseFloat(hip);
				
				if(date.length() >0)
				{
					replace_content = "<tr><td colspan = '4'>Your measurements so far</td></tr><tr><td></td></tr><tr><td></td><td><b>"+date_array[0][0]+"</b></td><td><b>"+date_array[0][1]+"</b></td><td><b>"+date_array[0][2]+"</b></td></tr><tr><td><b>WGT</b></td><td>"+table_array[0][0]+"</td><td>"+table_array[0][1]+"</td><td>"+table_array[0][2]+"</td></tr>"+"<tr><td><b>ARM</b></td><td>"+table_array[1][0]+"</td><td>"+table_array[1][1]+"</td><td>"+table_array[1][2]+"</td></tr>"+"<tr><td><b>WST</b></td><td>"+table_array[2][0]+"</td><td>"+table_array[2][1]+"</td><td>"+table_array[2][2]+"</td></tr>"+"<tr><td><b>THG</b></td><td>"+table_array[3][0]+"</td><td>"+table_array[3][1]+"</td><td>"+table_array[3][2]+"</td></tr>"+"<tr><td><b>HIP</b></td><td>"+table_array[4][0]+"</td><td>"+table_array[4][1]+"</td><td>"+table_array[4][2]+"</td></tr><tr><td></td></tr><tr><td></td></tr><tr><td style='color:#22b2e7;' colspan = '4'>Share your progress with the nutritionist</td></tr> ";
				}
				else
				{
						//System.out.println("Inside else");
				}
			}	
			System.out.println("Replace Str -" + replace_content);
			replace_content = replace_content.replaceAll("<td>0.0</td>", "<td>-</td>");
			replace_content = replace_content.replaceAll("<td><b>null</b></td>", "<td><b>N/A</b></td>");
			json_template.insertDataIntoJsons("_data_",replace_content);			
			api.perform(api.context().byId(room_id).post(json_template.chatletToPost(null)));
			
			HashMap<String, String> progress_schedule = new HashMap<String, String>();
			String tracker_date = getDateForTimeZone("Asia/Calcutta", "MM/dd/yyyy");
			String next_tracker_date = addDaysToDate(tracker_date, 7);
			progress_schedule.put("TrackerDate", tracker_date);
			progress_schedule.put("NextTrackerDate", next_tracker_date);
			
			db_config.updateData("userprofilestest", progress_schedule,
											"(roomid = '"+room_id+"')");
			
	 }
	 else
	 {
		 json_template.insertDataIntoJsons("_data_", "<tr><td>Share your measurements so that the Nutritionist can give you feedback on your progress</td></tr>");
		 //api.perform(api.context().byId(room_id).post(new TextChatlet("You can start keepin track of your stats here")));
		 api.perform(api.context().byId(room_id).post(json_template.chatletToPost(null)));
		 HashMap<String, String> progress_schedule = new HashMap<String, String>();
			String tracker_date = getDateForTimeZone("Asia/Calcutta", "MM/dd/yyyy");
			String next_tracker_date = addDaysToDate(tracker_date, 7);
			progress_schedule.put("TrackerDate", tracker_date);
			progress_schedule.put("NextTrackerDate", next_tracker_date);
			
			db_config.updateData("userprofilestest", progress_schedule,
											"(roomid = '"+room_id+"')");
	 }
	 
 }
 
public void postProgressResult(TeamchatAPI fit_api, TeamchatAPI nut_api, String room_id, String nutrition_roomid){
	 json_template.getJSONTemplate("/jsonfolder/prores.json");
	 String replace_content = null;
	 String date, weight, arms, waist , thighs, hip;
	 JSONArray queried_rows = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.progress_records(room_id)).toString());
	 float table_array[][] = new float[5][3];
	 String date_array[][] = new String[1][3];
	 if(queried_rows.size()>0)
	 {
		 
			for (int row_index = 0; row_index < queried_rows.size(); row_index ++)
			{
				JSONArray row_to_query = (JSONArray)queried_rows.get(row_index);
				date = (String) row_to_query.get(0);
				date_array[0][row_index] = date;
				weight = (String) row_to_query.get(1);
				table_array[0][row_index] = Float.parseFloat(weight);
				arms = (String) row_to_query.get(2);
				table_array[1][row_index] = Float.parseFloat(arms);	
				waist = (String) row_to_query.get(3);
				table_array[2][row_index] = Float.parseFloat(waist);
				thighs = (String) row_to_query.get(4);
				table_array[3][row_index] = Float.parseFloat(thighs);
				hip = (String) row_to_query.get(5);
				table_array[4][row_index] = Float.parseFloat(hip);
				
				if(date.length() >0){
					replace_content = "<tr><td colspan = '4'>Your measurements so far</td></tr><tr><td></td></tr><tr><td></td><td><b>"+date_array[0][0]+"</b></td><td><b>"+date_array[0][1]+"</b></td><td><b>"+date_array[0][2]+"</b></td></tr><tr><td><b>WGT</b></td><td>"+table_array[0][0]+"</td><td>"+table_array[0][1]+"</td><td>"+table_array[0][2]+"</td></tr>"+"<tr><td><b>ARM</b></td><td>"+table_array[1][0]+"</td><td>"+table_array[1][1]+"</td><td>"+table_array[1][2]+"</td></tr>"+"<tr><td><b>WST</b></td><td>"+table_array[2][0]+"</td><td>"+table_array[2][1]+"</td><td>"+table_array[2][2]+"</td></tr>"+"<tr><td><b>THG</b></td><td>"+table_array[3][0]+"</td><td>"+table_array[3][1]+"</td><td>"+table_array[3][2]+"</td></tr>"+"<tr><td><b>HIP</b></td><td>"+table_array[4][0]+"</td><td>"+table_array[4][1]+"</td><td>"+table_array[4][2]+"</td></tr>";
					}
					else{
						//System.out.println("Inside else");
					}
			}	
			
			replace_content = replace_content.replaceAll("<td>0.0</td>", "<td>-</td>");
			replace_content = replace_content.replaceAll("null", "N/A");
			System.out.println("Replace Str -" + replace_content);
			json_template.insertDataIntoJsons("_data_",replace_content);			
			//api.perform(api.context().byId(room_id).post(json_template.chatletToPost(null)));
			nut_api.perform(nut_api.context().byId(nutrition_roomid).post(json_template.chatletToPost(null)));
	 }
	 else
	 {
		 json_template.insertDataIntoJsons("_data_", "<tr><td>Share your measurements so that the Nutritionist can give you feedback on your progress</td></tr>");
		 nut_api.perform(nut_api.context().byId(nutrition_roomid).post(new TextChatlet("You can start keepin track of your stats here")));	
	 }
	 		
}

public void postWorkoutLogger(TeamchatAPI api, String roomid, String workout_name)
{
	try
	{
		PrimaryChatlet quick_logger = new PrimaryChatlet();
		quick_logger.setXSLTForChatlet("primary-2.7.2");
		quick_logger.setQuestionHtml("<img src = 'http://fitcircle.in/fitcircle/images/Fitbuddy/nutrition.jpg' style ='width:100%;height:200px;'  /> Hey, log your workout records here to win Health Points!");
		Form logger_form = api.objects().form();
		logger_form.addField(api.objects().select().name("didWorkout").label("Have you completed all the workouts?").addOption("Yes").addOption("No"));
		//nutrition_form.addField(api.objects().label().name("filler").label("Last but not the least, help us out with your Current Diet & Medical History so that we can advise you with the best solution "));
		//logger_form.addField(api.objects().input().name("dietrecall").label("Please tell us your Yesterday's Detail Diet Recall - Everything you ate yesterday"));
		logger_form.addField(api.objects().hidden().name("workout").value(workout_name));
		logger_form.addField(api.objects().input().name("ex1score").label("Please enter your number of reps for the first workout (in numbers)").addRegexValidation("\\d+","Only numbers are allowed."));
		logger_form.addField(api.objects().input().name("ex2score").label("Please enter your number of reps for the second workout (in numbers)").addRegexValidation("\\d+","Only numbers are allowed."));
		logger_form.addField(api.objects().input().name("ex3score").label("Please enter your number of reps for the third workout (in numbers)").addRegexValidation("\\d+","Only numbers are allowed."));
		logger_form.addField(api.objects().input().name("ex4score").label("Please enter your number of reps for the fourth workout (in numbers)").addRegexValidation("\\d+","Only numbers are allowed."));
		logger_form.addField(api.objects().input().name("ex5score").label("Please enter your number of reps for the fifth workout (in numbers)").addRegexValidation("\\d+","Only numbers are allowed."));
	
		quick_logger.allowComments(false);
		quick_logger.setReplyScreen(logger_form);
		quick_logger.setReplyLabel("Log Your Workout");
		quick_logger.alias("quickLoggerResponse");
		quick_logger.showDetails(true);
		api.perform(api.context().byId(roomid).post(quick_logger));
	}

	catch (Exception exception)
	{
		System.out.println("Cannot get nutrition questionaire data!");
		exception.printStackTrace();
	}
}

public String getDateForTimeZone(String timeZone, String format)
{
	Date date = new Date();
	DateFormat dateformat = new SimpleDateFormat(format);
	dateformat.setTimeZone(TimeZone.getTimeZone(timeZone));
	String dateString = dateformat.format(date).toString();
	return dateString;
}

public String addDaysToDate(String current_date, int days)
{
	//SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat date_formatter = new SimpleDateFormat("MM/dd/yyyy");
	Calendar calendar = Calendar.getInstance();
	try {
		calendar.setTime(date_formatter.parse(current_date));
	} catch (java.text.ParseException e) {
		Logit.logWrite("FitBuddyLogs", e);
	}
	calendar.add(Calendar.DATE, days); 
	String next_date = date_formatter.format(calendar.getTime());
	return next_date;
}

}