package com.workflow.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import com.teamchat.client.annotations.OnAlias;
import com.teamchat.client.annotations.OnKeyword;
import com.teamchat.client.sdk.TeamchatAPI;
import com.teamchat.client.sdk.chatlets.PollChatlet;
import com.teamchat.client.sdk.chatlets.TextChatlet;
import com.workflow.handlers.DatabaseConfig;
import com.workflow.handlers.JSONTemplateHandler;
import com.workflow.handlers.Logit;

public class FitBuddyBot {
	TeamchatAPI api = RequestHandler.fitbuddy_api;
	DatabaseConfig db_config = new DatabaseConfig();
	static final ChatletHandler chatlet_handler = new ChatletHandler();
	JSONTemplateHandler json_handler = new JSONTemplateHandler();
	RequestHandler request_handler = new RequestHandler();
	
	/*@OnKeyword(value = "Explore", description = "Check out what we have got to offer")
	public void postExploreMenu(TeamchatAPI api){
		Logit.logWrite("FitBuddyLogs", "\'Explore\' keyword used");
		String roomid = api.context().currentRoom().getId();
		chatlet_handler.postMenu(api, roomid);
		Logit.logWrite("FitBuddyLogs", "\'Explore\' chatlet thrown");
	}*/
	
	@OnKeyword("help")
	public void postExploreMenu(TeamchatAPI api){
		Logit.logWrite("FitBuddyLogs", "\'Help\' keyword used");
		String roomid = api.context().currentRoom().getId();
		chatlet_handler.postMenu(api, roomid);
		Logit.logWrite("FitBuddyLogs", "\'Help\' chatlet thrown");
	}
	
	@OnAlias("offer")										// Called immediately after BMI, after creating a profile.
	public void postOfferingSurvey(TeamchatAPI api)
	{
		/*SurveyChatlet offering_survey = new SurveyChatlet().setQuestion("That's great! I will get back to you with the best way to achieve your goal. In the meanwhile, you can try some of these cool things that can help you on your fitness journey.").addOption("Get Your Fitness Evaluation").addOption("Get a Personalized Fitness Plan").addOption("Get Diet and Nutrition Advice").addOption("Get a Quick Workout").addOption("Get Rewards for working out");
		offering_survey.alias("ResponseToOffering");
		api.performPostInCurrentRoom(offering_survey);*/
		Logit.logWrite("FitBuddyLogs", "BMI reply received");
		//api.performPostInCurrentRoom(new TextChatlet("I'll get back to you soon with the best way to achieve your goal!"));
		api.performPostInCurrentRoom(new TextChatlet("Alright, I will help you with your goal, in the meanwhile, let's get started."));
		postExploreMenu(api);
		Logit.logWrite("FitBuddyLogs", "\'Explore\' chatlet thrown");
	}
	
	@OnAlias("ResponseToOffering")
	public void responseToOffering(TeamchatAPI api){
		Logit.logWrite("FitBuddyLogs", "\'Explore\' response received");
		String response = api.context().currentReply().getField("resp");
		String roomid = api.context().currentRoom().getId();
		Logit.logWrite("FitBuddyLogs", "Response received: " + response);
		Logit.logWrite("FitBuddyLogs", "Current room ID: " + roomid);
		switch(response)
		{
		
		case "fittest":
			Logit.logWrite("FitBuddyLogs", "\'FitTest\' option selected");
			chatlet_handler.postFitTestChatlet(api, roomid);
			break;
			
		case "Plan":
				Logit.logWrite("FitBuddyLogs", "\'Plan\' option selected");
				chatlet_handler.postUserDashboard(api, roomid);
			break;
			
		case "Nutrition":
			Logit.logWrite("FitBuddyLogs","\'Nutrition\' option selected");
			//chatlet_handler.postNutritionSurvey(api, roomid, "nutritionresponse");
			api.performPostInCurrentRoom(new TextChatlet("Please go to your nutrition chatroom for your nutrition consultation"));
			break;
			
			/*case "QuickWorkout":
			
			SurveyChatlet workout_survey_options = new SurveyChatlet();
			workout_survey_options.setQuestion("Choose level of Intensity/Difficulty for your workout").addOption("Beginner").addOption("Intermediate").addOption("Advanced");	
			api.performPostInCurrentRoom(workout_survey_options);
			
			Logit.logWrite("FitBuddyLogs","\'Workout\' option selected");
			chatlet_handler.postWorkoutChatlet(api, roomid);
			break;*/
		
		case "Rewards":
			//PrimaryChatlet reward_chatlet = new PrimaryChatlet();
			//String link = chatlet_handler.createEmbeddedLink("http://imojo.in/xjv5o", "Reward", "http");
			//System.out.println(link);
			//api.performPostInCurrentRoom(new PollChatlet().setQuestionHtml("<a href= "+link+">Shame to Ryan</a>"));
			Logit.logWrite("FitBuddyLogs","\'Rewards\' option selected");
			chatlet_handler.publishRewardJson(api, roomid);
			break;
			
		case "EditProf":
			Logit.logWrite("FitBuddyLogs","\'Edit Profile\' option selected");
			chatlet_handler.manageProfile(api, roomid);
			
		}
	}
	
	@OnKeyword("test")
	public void test(TeamchatAPI api)
	{
		String roomid = api.context().currentRoom().getId();
		chatlet_handler.postDailyWorkout(api, roomid);
	}
	
	@OnAlias("quickLoggerResponse")
	public void saveQuickWorkoutDetails(TeamchatAPI api)
	{
		String roomid = api.context().currentRoom().getId();
		String email = api.context().currentReply().senderEmail();
		String ex1score = "";
		String ex2score = "";
		String ex3score = "";
		String ex4score = "";
		String ex5score = "";
		
		Date date = new Date();
		DateFormat dateformat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		dateformat.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
		String dateString = dateformat.format(date).toString();
		
		if(api.context().currentReply().getField("ex1score").isEmpty())
		{
			ex1score = "0";
		}
			else
		{
			ex1score = "25";
		}
		
		if(api.context().currentReply().getField("ex2score").isEmpty())
		{
			ex2score = "0";
		}
		else
		{
			ex2score = "25";
		}
		
		if(api.context().currentReply().getField("ex3score").isEmpty())
		{
			ex3score = "0";
		}
	else
		{
			ex3score = "25";
		}
		
		if(api.context().currentReply().getField("ex4score").isEmpty())
		{
			ex4score = "0";
		}
	else
		{
			ex4score = "25";
		}
		
		if(api.context().currentReply().getField("ex5score").isEmpty())
		{
			ex5score = "0";
		}
	else
		{
			ex5score = "25";
		}
		
		HashMap<String, String> workout_details = new HashMap<String, String>();
		workout_details.put("Date", dateString);
		workout_details.put("roomid", roomid);
		workout_details.put("email", email);
		workout_details.put("ex1score", ex1score);
		workout_details.put("ex2score", ex2score);
		workout_details.put("ex3score", ex3score);
		workout_details.put("ex4score", ex4score);
		workout_details.put("ex5score", ex5score);
		
		db_config.insertData("qwloggertest", workout_details);
	}
		
	/*@OnKeyword("prog")
	public void boo(TeamchatAPI api)
	{
		chatlet_handler.postProgressTrackerScheduled(RequestHandler.fitbuddy_api);
	}*/
	
	
}
