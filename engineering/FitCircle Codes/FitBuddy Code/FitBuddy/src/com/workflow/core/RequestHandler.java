package com.workflow.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import com.teamchat.client.annotations.OnAlias;
import com.teamchat.client.annotations.OnKeyword;
import com.teamchat.client.annotations.OnMsg;
import com.teamchat.client.sdk.Chatlet;
import com.teamchat.client.sdk.Form;
import com.teamchat.client.sdk.Room;
import com.teamchat.client.sdk.TeamchatAPI;
import com.teamchat.client.sdk.chatlets.BypassChatletCopy;
import com.teamchat.client.sdk.chatlets.PollChatlet;
import com.teamchat.client.sdk.chatlets.PrimaryChatlet;
import com.teamchat.client.sdk.chatlets.SimpleJsonChatlet;
import com.teamchat.client.sdk.chatlets.SurveyChatlet;
import com.teamchat.client.sdk.chatlets.TextChatlet;
import com.workflow.handlers.DatabaseConfig;
import com.workflow.handlers.JSONTemplateHandler;
import com.workflow.handlers.Logit;
import com.workflow.handlers.Scheduler;

/**
 * 
 * @author rj
 * Handler Servlet
 */
@WebServlet("/handler")
public class RequestHandler extends HttpServlet
{
	private static final long serialVersionUID = 1L;    
	static TeamchatAPI fitbuddy_api, nutritionist_api;
	static final ChatletHandler chatlet_handler = new ChatletHandler();
	DatabaseConfig db_config = new DatabaseConfig();
	static JSONTemplateHandler json_handler= new JSONTemplateHandler(); 	
	Scheduler post_timer = new Scheduler();
	String sender_room_id = null;
    
    public RequestHandler()
    {
    	super();
    }
    
	public void init(ServletConfig config) throws ServletException 
	{
		fitbuddy_api = TeamchatAPI.fromFile("FitBuddyConfig.json").setEmail("rj.perennui+1@gmail.com")
					   .setPassword("abcde123").startReceivingEvents(new FitBuddyBot());
		Logit.logWrite("FitBuddyLogs", "FitBot set");
		nutritionist_api = TeamchatAPI.fromFile("NutritionBotConfig.json").setEmail("rj.perennui+2@gmail.com")
				           .setPassword("Jajajaja1!").startReceivingEvents(new NutritionBot());
		Logit.logWrite("FitBuddyLogs", "NutriBot set");
		post_timer.createScheduledTask(new Runnable()
		{
			public void run()
			{
				try 
				{
					Logit.logWrite("FitBuddyLogs", "Posting daily workout");
					ThreadHandler t1 = new ThreadHandler(json_handler, "QuoteMessage");
					t1.start();
					try
					{
						t1.join();
					}
					catch(Exception exception)
					{
						System.out.println(exception);
					}
				}
				catch(Exception exception)
				{
					System.out.println("Program was terminated. Please see following stacktrace log:");
					exception.printStackTrace();
				}
			}

		}, (WorkflowConstants.dailyworkout_target_hour*60 + WorkflowConstants.dailyworkout_target_minute),
		WorkflowConstants.dailyworkout_interval);




		
		post_timer.createScheduledTask(new Runnable()
		{
			public void run()
			{
				try 
				{
					Logit.logWrite("FitBuddyLogs", "Sending out progress reports to user for the day");
					ThreadHandler progress_threader = new ThreadHandler(json_handler, "ProgressTracker");
					progress_threader.start();
					try{
						progress_threader.join();
					}
					catch(Exception exception)
					{
						System.out.println(exception);
					}
				}
				catch(Exception exception)
				{
					System.out.println("Program was terminated. Please see following stacktrace log:");
					exception.printStackTrace();
				}
			}

		}, (WorkflowConstants.progress_target_hour*60 + WorkflowConstants.progress_target_minute), 
		WorkflowConstants.progress_delay);
		
		
	}

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException 
	{
		response.setContentType("application/json");
		PrintWriter messager = response.getWriter();
		JSONObject response_json = new JSONObject();
		JSONArray target_option;
		String request_identifier = request.getParameter("operation");
		if (request_identifier==null)
		{
			System.out.println("Something went wrong");
			response_json.put("code", "400");
			response_json.put("message", "400 - Bad Request");
			messager.print(response_json.toString());
			messager.flush();
			
		}
		else{
		Logit.logWrite("FitBuddyLogs","RequestHandler: Request received from " + request_identifier);
		switch(request_identifier)
		{
			case "servletStartup":
				Logit.logWrite("FitBuddyLogs","RequestHandler: Started manually");
				break;
				
			case "addAll":
				Logit.logWrite("FitBuddyLogs","RequestHandler: Received 'Add All' request");
				createRoomsForAll();
				break;
			
			case "massFunction":
				Logit.logWrite("FitBuddyLogs", "RequestHandler: Executing mass function");
				executeMassFunction();
				break;
				
			case "doNow":
				//response.setStatus(HttpServletResponse.SC_OK);
				//messager.print("202 - Request Received");
				response_json.put("code", "202");
				response_json.put("message", "202 - Request Received");
				messager.print(response_json.toString());
				messager.flush();
				Logit.logWrite("FitBuddyLogs", "RequestHandler: Executing 'Do Now' function");
				postQuickWorkout(request);
				break;
			
			case "fittest":
				Logit.logWrite("FitBuddyLogs","RequestHandler: Received FitTest page request");
				postFitTestScore(request);
				break;
				
			case "getRunPlanOptions":
				Logit.logWrite("FitBuddyLogs","RequestHandler: Will retrieve run plan options from database "
				+ "to complete plan allocation");
				target_option = null;	
				try 
				{
					target_option = populatePlanList(request);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				response.getWriter().write(target_option.toString());
				break;
						
			case "getGoalPlanOptions":
				Logit.logWrite("FitBuddyLogs","RequestHandler: Will retrieve goal plan options from database to "
				+ "complete plan allocation");
				target_option = null;
				try 
				{
					target_option = populatePlanList(request);
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				response.getWriter().write(target_option.toString());	
				break;
					
			case "getdata":
				Logit.logWrite("FitBuddyLogs", "RequestHandler: Received request to retrieve existing user data to "
				+ "prepare Edit Profile page");
				JSONArray profile_details = null;
				profile_details = updateEditProfilePage(fitbuddy_api, request);
				response.getWriter().write(profile_details.toString());
				break;
						
			case "allocatePlan":
				Logit.logWrite("FitBuddyLogs","RequestHandler: Pushing plan allocation to database");
				try 
				{
					storeUserPlanDetails(request);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				break;
				
			case "createRooms":
				//response.setStatus(HttpServletResponse.SC_OK);
				//messager.print("202 - Request Received");
				response_json.put("code", "202");
				response_json.put("message", "202 - Request Received");
				messager.print(response_json.toString());
				messager.flush();
				String room_member = request.getParameter("emailID");
				String username = request.getParameter("username");
				Logit.logWrite("FitBuddyLogs","RequestHandler: Creating Nutrition Bot and FitBuddy rooms for "
				+ room_member);
				createMainRooms(fitbuddy_api, nutritionist_api, room_member, username);
				break;
				
			case "askNutritionist":
				//response.setStatus(HttpServletResponse.SC_OK);
				//messager.print("202 - Request Received");
				response_json.put("code", "202");
				response_json.put("message", "202 - Request Received");
				messager.print(response_json.toString());
				messager.flush();
				String email = request.getParameter("emailID");
				//String name = request.getParameter("username");
				Logit.logWrite("FitBuddyLogs","RequestHandler: Enacting 'Ask A Nutritionist' functionality for "
				+ email);
				checkNutritionistRoom(email);
				break;
				
			case "saveProf":
				Logit.logWrite("FitBuddyLogs", "RequestHandler: Saving user profile details to database");
				sendProfileData(fitbuddy_api, nutritionist_api, request);
				break;
				
			case "createProf":
				Logit.logWrite("FitBuddyLogs","RequestHandler: Saving user profile details to database");
				createProfileData(fitbuddy_api, request);
				break;	
			
			case "nutritionData":
				Logit.logWrite("FitBuddyLogs", "RequestHandler: Saving user nutrition details to database");
				processNutritionForm(nutritionist_api, request);
				break;
			
			case "progressTrack":
				Logit.logWrite("FitBuddyLogs", "RequestHandler: Saving user progress details to database");
				processProgressData(nutritionist_api, request);
			    break; 
			    
			case "otpMessage":
				Logit.logWrite("FitBuddyLogs", "RequestHandler: Received request for OTP generation and email response");
			    sendOTPMail(request, response, messager, response_json);
			    break;
			    
			default:
				//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request received is incomprehensible");
				//response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				//messager.print("400 - Bad Request");
				response_json.put("code", "400");
				response_json.put("message", "400 - Bad Request");
				messager.print(response_json.toString());
				messager.flush();
				break;
		}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException 
	{
		doGet(request, response);
	}
	/***********************************************************************************************************************************************/
	/************************************************************************************************************************************************
	 * Room Creation
	 * @param api
	 * @param nutritionist_api
	 * @param email_id
	 */
	public void sendOTPMail(HttpServletRequest request, HttpServletResponse response, PrintWriter messager, JSONObject response_json)
	{
		Random otp_gen = new Random();
		int otp = otp_gen.nextInt(9999)+1;
		if (otp <= 999)
		{
			otp += 1000;
		}
		 String data = request.getParameter("data");
	  	 JSONObject json = new JSONObject(data);
		
	  	  String name = json.getString("name");
	      String emailID = json.getString("email");//change accordingly


	      String emailer = "stayfit@fitcircle.in";//change accordingly
	      final String username = "stayfit@fitcircle.in";//change accordingly
	      final String password = "Fitcircle@7!7";//change accordingly

	      
	      String host = "smtp.gmail.com";

	      Properties props = new Properties();
	      props.put("mail.smtp.auth", "true");
	      props.put("mail.smtp.starttls.enable", "true");
	      props.put("mail.smtp.host", host);
	      props.put("mail.smtp.port", "587");

	      // Get the Session object.
	      Session session = Session.getInstance(props,
	      new javax.mail.Authenticator() {
	         protected PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication(username, password);
	         }
	      });

	      try {
	         
	         Message message = new MimeMessage(session);

	         
	         message.setFrom(new InternetAddress(emailer));

	         
	         message.setRecipients(Message.RecipientType.TO,
	         InternetAddress.parse(emailID));

	         // Set Subject: header field
	         message.setSubject("Fitcircle Login OTP: " + otp);

	         // Now set the actual message
	         //message.setText("Hey "+ name + ", your login OTP is: " + otp);
	         message.setContent("<body style = 'font-family:Droid Sans;background-color:#f1f1f1; border-style:2px solid #f1f1f1; box-shadow:#888888;'><img src = 'http://fitcircle.in/fitcircle/images/fitlo1.png' style = 'width:50%;height:25px'/><br /><br /><p style = 'margin-left:1.5em;'><font style='font-size:1em'>Hello " + name + ", your OTP is<br /><br/><font style = 'font-size:2.5em;color:#2BB2E7;'>" + otp + "</font><br/><br/>Please enter it when prompted to, in your app, to access all our cool stuff!<br/><br/><br/>Regards, <br/>Team Fitcircle :)</font></p><br/><br/></body><br/><br/><p align = 'center'> For any doubts or queries, please contact us at stayfit@fitcircle.in", "text/html" );
	         // Send message
	         Transport.send(message);

	         System.out.println("Sent message successfully....");
	         
			response_json.put("status", "200");
			response_json.put("message", "Your four digit code send to your email");
			response_json.put("OTP",otp);
			messager.print(response_json.toString());
			messager.flush();

	      } 
	      catch(Exception ex) {
				response_json.put("status", "403");
				response_json.put("message", "Invalid email or name");
				messager.print(response_json.toString());
				messager.flush();     
	      }
	}
	public void createMainRooms(TeamchatAPI api, TeamchatAPI nutritionist_api, String email_id, String username)
	{
		email_id = email_id.trim();
		JSONArray queried_data = db_config.retrieveQueryRows
								 (db_config.retrieveData(WorkflowConstants.room_id_from_email_query_string(email_id))
								 .toString());
		if (queried_data.isEmpty())
		{
			Room fit_room = api.context().create().add(email_id).setName("TestBuddy");
			api.perform(fit_room.post(new TextChatlet("Hey Buddy! If you are here, you have that desire to live "
			+ "a healthy life. You will love me or you might hate me. But, I will do my best to give"
			+ " you the best tools to stay healthy. Just Trust me and be with me, "
			+ "Love, Your FitBuddy :)")));
			Logit.logWrite("FitBuddyLogs", "Room created with FitBuddy");
			String roomid = fit_room.getId();
			Logit.logWrite("FitBuddyLogs", "FitBuddy RoomID: " + roomid);
			System.out.println(roomid);
			Room nutri_room = nutritionist_api.context().create().add(email_id)
							  ./*add("nutrition_1@dumbelled.com").*/setName("Ask A Nutritionist");
			nutritionist_api.perform(nutri_room.post(new TextChatlet("Hi, I am Priti, "
		    + "Nutritionist's Assistant. Are you looking to chat with the nutritionist?")));
			Logit.logWrite("FitBuddyLogs", "Room created with Nutrition Bot");
			String nutritionRoomID = nutri_room.getId();
			Logit.logWrite("FitBuddyLogs", "NutritionBot RoomID: " + nutritionRoomID);
			System.out.println(nutritionRoomID);
			
			HashMap<String, String> createRoomData = new HashMap<String, String>();
			createRoomData.put("username", username);
			createRoomData.put("email", email_id);
			createRoomData.put("roomid", roomid);
			createRoomData.put("nutritionRoomID", nutritionRoomID);
			Logit.logWrite("FitBuddyLogs", "Room IDs to be sent to userprofilestest:" + createRoomData.toString());
			
			db_config.insertData("userprofilestest", createRoomData);
			Logit.logWrite("FitBuddyLogs", "Room IDs saved to userprofilestest");
			chatlet_handler.postSetupProfileForm(api, roomid);
			Logit.logWrite("FitBuddyLogs", "Post-creation setup chatlet thrown");
			
			JSONArray query = db_config.retrieveQueryRows(db_config.retrieveData("select * from RoomidMap where emailid = '" + email_id +"';").toString());
			if (query.isEmpty())
			{
				HashMap<String, String> roommap_data = new HashMap<String, String>();
				roommap_data.put("emailid", email_id);
				roommap_data.put("name", username);
				roommap_data.put("roomid", roomid);
				roommap_data.put("type", "Fitbuddy");
				db_config.insertData("RoomidMap", roommap_data);
				roommap_data.put("roomid", nutritionRoomID);
				roommap_data.put("type", "Nutrition");
				Logit.logWrite("FitBuddyLogs", "Room IDs and email saved to RoomidMap");
				db_config.insertData("RoomidMap", roommap_data);
			}
		}
	}

	/************************************************************************************************************************************************/
	/***********************************************************************************************************************************************
	 * FitBuddy Offerings (help) section
	 * @param api
	 */
	
	
	
	/***********************************************************************************************************************************************/
	/***********************************************************************************************************************************************
	 * Setup Profile Section
	 * @param api
	 * @throws Exception
	 */
	
	public void sendProfileData(TeamchatAPI api, TeamchatAPI nutritionist_api, HttpServletRequest request)
	{
		Logit.logWrite("FitBuddyLogs", "Capturing profile data from page request");
		String data = request.getParameter("data");
	  	JSONObject json = new JSONObject(data);
		String roomid = json.getString("roomId");
		String name = json.getString("name");
		String gender = json.getString("gender");
		int heightfeet = json.getInt("heightfeet");
		int heightinches = json.getInt("heightinches");
		double height = (heightfeet * 30.48) + (heightinches * 2.54);
		//int weight = json.getInt("weight");
		float weight = Float.parseFloat(json.getString("weight"));
		String phone = json.getString("phone");
		String dob = json.getString("dob");
		String date = getDateForTimeZone("Asia/Calcutta", "dd MMM yyyy HH:mm:ss");
		
		double bmi = (weight)/((height*0.01)*(height*0.01));
		bmi = Double.parseDouble(new DecimalFormat("##.#").format(bmi));
		
		HashMap<String,String> values = new HashMap<String,String>();
		values.put("gender", gender);
		values.put("username", name);
		values.put("height", Double.toString(height));
		values.put("weight", Float.toString(weight));
		values.put("phone", phone);
		values.put("dob", dob);
		values.put("date", date);
		values.put("BMI", Double.toString(bmi));
		
		Logit.logWrite("FitBuddyLogs", "Data received from request: " + values.toString());
		JSONArray queried_rows = db_config.retrieveQueryRows
				                           (db_config.retrieveData("select username, nutritionRoomID "
				                           		      + "from userprofilestest where roomid = '" + roomid + "';")
				                        		     .toString());
		JSONArray result = (JSONArray) queried_rows.get(0);
		String username = (String) result.get(0);
		String nutritionRoomID = (String) result.get(1);
		if (username.equals(""))
		{
			Logit.logWrite("FitBuddyLogs", "No prior user profile data found, initial entry");
			db_config.updateData("userprofilestest", values, "(roomid = '" + roomid + "')");
			Logit.logWrite("FitBuddyLogs", "Profile data updated");
			HashMap<String, String> roommap_data = new HashMap<String, String>();
			roommap_data.put("name", name);
			db_config.updateData("RoomidMap", roommap_data, "(roomid = '" + roomid + "')");
			db_config.updateData("RoomidMap", roommap_data, "(roomid = '" + nutritionRoomID + "')");
			Logit.logWrite("FitBuddyLogs", "User name updated in RoomidMap as well");
			SurveyChatlet surveychatlet = new SurveyChatlet();
			String alias_name = "offer";
			surveychatlet.alias(alias_name);
			Logit.logWrite("FitBuddyLogs", "BMI chatlet alias is " + alias_name);
		if(bmi < 18)
			{
				System.out.println("bmi below 18, bmi" +bmi);
				surveychatlet.setQuestion("Your BMI is "+bmi+". Seems like your weight is low with respect to your "
						      + "height. Let's start with a goal, which one of these would you be most interested in?")
							 .addOption("I would like to Gain Weight")
							 .addOption("I want to Strengthen my Muscles")
							 .addOption("I want to build my stamina")
				             .addOption("I want to improve my flexibility")
				             .addOption("All of the above");
			}
		else if(bmi > 25 && bmi < 30)
			{
				System.out.println("bmi above 25, bmi" +bmi);
				surveychatlet.setQuestion("Your BMI is "+bmi+". Your weight is above normal. Not to worry! "
						      + "Let's start with a goal, which one of these would you be most interested in?")
				             .addOption("I would like to Lose Weight")
				             .addOption("I want to Strengthen my Muscles")
				             .addOption("I want to build my stamina")
				             .addOption("I want to improve my flexibility")
				             .addOption("All of the above");
			}
		else if(bmi > 30)
			{
				System.out.println("bmi above 30, bmi" +bmi);
				surveychatlet.setQuestion("Your BMI is "+bmi+". You need to lose weight and you already seem to "
						      + "be taking steps in the right direction! Don't worry,  lets start with a goal, "
						      + "which one of these would you be most interested in?")
							 .addOption("I would like to Lose Weight")
							 .addOption("I want to Strengthen my Muscles")
							 .addOption("I want to build my stamina")
							 .addOption("I want to improve my flexibility")
							 .addOption("All of the above");
			}
		else{
				System.out.println("bmi in healthy range, bmi" + bmi);
				surveychatlet.setQuestion("Your BMI is "+bmi+". You have a healthy weight. "
						      + "We can focus on several other aspects of fitness. Let's start with a goal,"
						      + " which one of these would you be most interested in?")
							 .addOption("I would like to Lose Weight")
							 .addOption("I want to Strengthen my Muscles")
							 .addOption("I want to build my stamina")
							 .addOption("I want to improve my flexibility")
							 .addOption("All of the above");
			}
		api.perform(api.context().byId(roomid).post(surveychatlet));
		chatlet_handler.postNutritionForm(nutritionist_api, nutritionRoomID);
		Logit.logWrite("FitBuddyLogs", "BMI chatlet thrown");
		}
		
		else
		{
			Logit.logWrite("FitBuddyLogs", "Existing profile data found, updating data");
			db_config.updateData("userprofilestest", values, "(roomid = '" + roomid + "')" );
			api.perform(api.context().byId(roomid).post(new TextChatlet("Thanks, your profile has been edited")));
			api.perform(api.context().byId(roomid).post(new TextChatlet("Please type 'help' to explore "
					                               + "other FitBuddy features!")));
		}
		
		
	}
	
	public void createProfileData(TeamchatAPI api, HttpServletRequest request)
	{
		Logit.logWrite("FitBuddyLogs", "Capturing profile data from page request");
		String data = request.getParameter("data");
		Logit.logWrite("FitBuddyLogs", data);
	  	JSONObject json = new JSONObject(data);
		String nutritionRoomID = json.getString("roomId");
		String name = json.getString("name");
		String gender = json.getString("gender");
		int heightfeet = json.getInt("heightfeet");
		int heightinches = json.getInt("heightinches");
		double height = (heightfeet * 30.48) + (heightinches * 2.54);
		float weight = Float.parseFloat(json.getString("weight"));
		String phone = json.getString("phone");
		String dob = json.getString("dob");
		String date = getDateForTimeZone("Asia/Calcutta", "dd MMM yyyy HH:mm:ss");
		
		double bmi = (weight)/((height*0.01)*(height*0.01));
		bmi = Double.parseDouble(new DecimalFormat("##.#").format(bmi));
		
		HashMap<String,String> values = new HashMap<String,String>();
		values.put("gender", gender);
		values.put("username", name);
		values.put("height", Double.toString(height));
		values.put("weight", Float.toString(weight));
		values.put("phone", phone);
		values.put("dob", dob);
		values.put("date", date);
		values.put("BMI", Double.toString(bmi));
		
		Logit.logWrite("FitBuddyLogs", "Data received from request: " + values.toString());
		JSONArray queried_rows = db_config.retrieveQueryRows(db_config.retrieveData("select username, "
				                  									   + "roomid from userprofilestest "
				                  									   + "where nutritionRoomID = '" + 
				                  									   nutritionRoomID + "';").toString());
		JSONArray result = (JSONArray) queried_rows.get(0);
		String username = (String) result.get(0);
		String fitbuddy_roomid = (String) result.get(1);
		if (username.equals(""))
		{
			Logit.logWrite("FitBuddyLogs", "No prior user profile data found, initial entry");
			db_config.updateData("userprofilestest", values, "(nutritionRoomID = '" + nutritionRoomID + "')");
			Logit.logWrite("FitBuddyLogs", "Profile data updated");
			HashMap<String, String> roommap_data = new HashMap<String, String>();
			roommap_data.put("name", name);
			db_config.updateData("RoomidMap", roommap_data, "(roomid = '" + fitbuddy_roomid + "')");
			db_config.updateData("RoomidMap", roommap_data, "(roomid = '" + nutritionRoomID + "')");
			Logit.logWrite("FitBuddyLogs", "User name updated in RoomidMap as well");
			SurveyChatlet surveychatlet = new SurveyChatlet();
			surveychatlet.alias("offer");
		if(bmi < 18)
			{
				System.out.println("bmi below 18, bmi" +bmi);
				surveychatlet.setQuestion("Your BMI is "+bmi+"\n Seems like your weight is low with respect to your height. \n Lets start with a goal, which one of these would you be most interested in?").addOption("I would like to Gain Weight").addOption("I want to Strengthen my Muscles").addOption("I want to build my stamina").addOption("I want to improve my flexibility").addOption("All of the above");
			}
		else if(bmi > 25 && bmi < 30)
			{
				System.out.println("bmi above 25, bmi" +bmi);
				surveychatlet.setQuestion("Your BMI is "+bmi+"\nYour weight is above normal.\n Not to worry! Lets start with a goal, which one of these would you be most interested in?").addOption("I would like to Lose Weight").addOption("I want to Strengthen my Muscles").addOption("I want to build my stamina").addOption("I want to improve my flexibility").addOption("All of the above");
			}
		else if(bmi > 30)
			{
				System.out.println("bmi above 30, bmi" +bmi);
				surveychatlet.setQuestion("Your BMI is "+bmi+"\n You need to lose weight and you already seem to be taking steps in the right direction!\n Don't worry,  lets start with a goal, which one of these would you be most interested in?").addOption("I would like to Lose Weight").addOption("I want to Strengthen my Muscles").addOption("I want to build my stamina").addOption("I want to improve my flexibility").addOption("All of the above");
			}
		else{
				System.out.println("bmi in healthy range, bmi" + bmi);
				surveychatlet.setQuestion("Your BMI is "+bmi+"\n You have a healthy weight. We can focus on several other aspects of fitness. Lets start with a goal, which one of these would you be most interested in?").addOption("I would like to Lose Weight").addOption("I want to Strengthen my Muscles").addOption("I want to build my stamina").addOption("I want to improve my flexibility").addOption("All of the above");
			}
		
		api.perform(api.context().byId(fitbuddy_roomid).post(surveychatlet));
		nutritionist_api.perform(nutritionist_api.context().byId(nutritionRoomID).post(new TextChatlet("Thanks for setting up your profile! Your FitBuddy will help you out with setting your goal. In the meanwhile, lets look at the nutrition aspect")));
		//chatlet_handler.postNutritionSurvey(nutritionist_api, nutritionRoomID, "nutritionform");
		chatlet_handler.postNutritionForm(nutritionist_api, nutritionRoomID);
		
		Logit.logWrite("FitBuddyLogs", "BMI chatlet thrown");
		}
		
		else
		{
			Logit.logWrite("FitBuddyLogs", "Existing profile data found, updating data");
			db_config.updateData("userprofilestest", values, "(roomid = '" + fitbuddy_roomid + "')" );							
		}
		
		
	}
	/***********************************************************************************************************************************************/
	/************************************************************************************************************************************************
	 * Plan Allocation Section
	 * @param fitbuddy_api
	 * @throws Exception
	 */
	
	public void storeUserPlanDetails(HttpServletRequest request) throws Exception
	{
		String data = request.getParameter("data");
	  	JSONObject json = new JSONObject(data);
		String email = json.getString("sessionid");
		String planType = json.getString("planType");
		String runPlan = json.getString("runPlan");
		String eventEndDate = null;
		String currRunPlan = null;
		if (planType.equals("fi45 Runnerz"))
		{
			currRunPlan = json.getString("currrunPlan");
		}
		else
		{
			currRunPlan = runPlan;
		}
		
		if(json.has("eventDate"))
		{
			eventEndDate = json.getString("eventDate");
			SimpleDateFormat initial_formatter = new SimpleDateFormat("dd/MM/yyyy");
			Date inter_date = initial_formatter.parse(eventEndDate);
			SimpleDateFormat final_formatter = new SimpleDateFormat("MM/dd/yyyy");
			eventEndDate = final_formatter.format(inter_date).toString();
		}

		Logit.logWrite("FitBuddyLogs","User plan details received: Email: " + email + ", Current Plan: " + currRunPlan + ", Plan Type: " + planType + ", Plan Taken: " + runPlan + ", Event Date: " + eventEndDate);
		
		LocalDate today = new LocalDate(DateTimeZone.forOffsetHoursMinutes(5, 30));
		//get monday of this week
		LocalDate monday = today.withDayOfWeek(DateTimeConstants.MONDAY);

		/*String query = "select QuotePlanName,QuotePlanMaxDays from RunPlans where RunningDistance='%s' and RunPlan='%s'";
		query = String.format(query,currRunPlan,runPlan);*/
		
		JSONArray queried_row = db_config.retrieveQueryRows(db_config.retrieveData("select QuotePlanName, QuotePlanMaxDays from RunPlans where RunningDistance = '" + currRunPlan + "' and RunPlan = '" + runPlan + "';").toString());
		JSONArray quotePlanList = (JSONArray) queried_row.get(0);
		Logit.logWrite("FitBuddyLogs","PlanList: " + queried_row.toString());
		
		String quotePlan="";
		int quotePlanMaxDays = 0;
		if(quotePlanList!=null && quotePlanList.size()>0)
		{
			quotePlan = (String) quotePlanList.get(0);
			quotePlanMaxDays = Integer.parseInt((String) quotePlanList.get(1));
		}
		
		LocalDate planEndDate;
		
		if(eventEndDate == null || eventEndDate.length() <= 0)
		{
			eventEndDate = monday.plusDays(quotePlanMaxDays).toString("MM/dd/yyyy");
			planEndDate = getClosestSunday(eventEndDate);
			eventEndDate = planEndDate.toString("MM/dd/yyyy");
		}
		else
		{
			planEndDate = getClosestSunday(eventEndDate);
		
			// if plan end date according to user selection exceeds the max days for the quote plan,
			// then set the planenddate to match the quote plan max days.
			if(Days.daysBetween(today, planEndDate).getDays() > quotePlanMaxDays)
			{
				planEndDate = planEndDate.minusDays(Days.daysBetween(today, planEndDate).getDays() - quotePlanMaxDays);
				planEndDate = getClosestSunday(planEndDate.toString("MM/dd/yyyy"));
				//logger.info("new plan end date accoring to selected plan is : "+planEndDate.toString("dd/MM/yyyy"));
			}
		}
		Logit.logWrite("FitBuddyLogs","Event end date: " + eventEndDate + ", Plan End Date: " + planEndDate);
		
		//logger.info("Event Date : "+eventEndDate);
		//logger.info("Plan End Date (closest sunday) : "+planEndDate.toString("dd/MM/yyyy"));
		
		
		
		//List<String[]> resultList = new ArrayList<String[]>();
		//resultList = readPlanTable(email);
		JSONArray resultList = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.READ_PLAN_TABLE().replaceAll("%s", email)).toString());

		// set existing plans as inactive
		if (resultList != null && resultList.size() > 0)
		{
			// update data call using map
			HashMap<String, String> values = new HashMap<>();
			values.put("isactive", "no");
			db_config.updateData("UserPlanDetails",  values, WorkflowConstants.CRITERIA_EMAIL().replace("%s", email));
			//zohoReportUtility.updateData("UserPlanDetails", values, String.format(QueryConstants.CRITERIA_EMAIL, email));
			//isPlanAllocated = true;
			//zohoReportUtility.updateData("userprofilestest", profMap, String.format(QueryConstants.CRITERIA_EMAIL, profMap.get("email")));
			
		}

		HashMap<String, String> values = new HashMap<>();
		values.put("email", email);
		values.put("plantype", planType);
		values.put("runplan", runPlan);
		values.put("quoteplanname", quotePlan);
		values.put("eventdate", eventEndDate);
		values.put("planenddate", planEndDate.toString("MM/dd/yyyy"));
		values.put("currentrunningdistance", currRunPlan);
		values.put("planstartdate", monday.toString("MM/dd/yyyy"));
		values.put("planallocationdate", getDateForTimeZone("Asia/Calcutta", "MM/dd/yyyy"));
		values.put("isactive", "yes");
		
		Logit.logWrite("FitBuddyLogs","User plan details to be entered into database: " + values.toString());
		
		db_config.insertData("UserPlanDetails", values);
		//zohoReportUtility.addData("UserPlanDetails", values);
		//isPlanAllocated = true;
		//zohoReportUtility.addData("userprofilestest", profMap);
		//return isPlanAllocated;
		JSONArray queried_id_row = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.room_id_from_email_query_string(email)).toString());
		JSONArray email_row = (JSONArray) queried_id_row.get(0);
		String room_id = (String) email_row.get(0);
		Logit.logWrite("FitBuddyLogs","Room ID to post to: " + room_id);
		//chatlet_handler.postUserDashboard(api, room_id);
		chatlet_handler.postPlanDetails(fitbuddy_api, room_id);
		Logit.logWrite("FitBuddyLogs","Plan details posted");
		
		DateTime time = new DateTime(DateTimeZone.forOffsetHoursMinutes(5, 30));
		int hour = time.getHourOfDay();
		if (hour > 15)
		{
			chatlet_handler.postDailyWorkout(fitbuddy_api, room_id);
			Logit.logWrite("FitBuddyLogs","Workout chatlet posted, as user registered for plan post-4PM");
		}
		post_timer.createTaskSeconds(new Runnable(){
			public void run(){fitbuddy_api.perform(fitbuddy_api.context().byId(room_id).post(new TextChatlet("Please type 'help' to explore other FitBuddy features!")));}
		}, 10);
	}
	private LocalDate getClosestSunday(String eventEndDate)
	{
		LocalDate planEndDate;
		LocalDate eventDate = LocalDate.parse(eventEndDate, DateTimeFormat.forPattern("MM/dd/yyyy"));
		LocalDate nextSunday = eventDate.withDayOfWeek(DateTimeConstants.SUNDAY);
		LocalDate lastSunday = eventDate.plusWeeks(-1).withDayOfWeek(DateTimeConstants.SUNDAY);
		
		Period before = new Period(lastSunday,eventDate);
		Period after = new Period(eventDate, nextSunday);
		
		if(before.getDays() < after.getDays())
		{
			planEndDate = lastSunday;
		}
		else
		{
			planEndDate = nextSunday;
		}
		return planEndDate;
	}
	
	public String getDateForTimeZone(String timeZone, String format) // Also called in sendProfileData
	{
		Date date = new Date();
		DateFormat dateformat = new SimpleDateFormat(format);
		dateformat.setTimeZone(TimeZone.getTimeZone(timeZone));
		String dateString = dateformat.format(date).toString();
		return dateString;
	}
	
	/*public String getCurrentProgressTrackerDate() //Not using
	{
		SimpleDateFormat simple_date_format = new SimpleDateFormat("MM/dd/yyyy");
		Calendar calender = Calendar.getInstance();
		calender.setTime(new Date()); // Now use today date.
		calender.add(Calendar.DATE, 0); 
		String output = simple_date_format.format(calender.getTime());
		System.out.println(output);
		return output;
	}*/
	
	/*public String getNextProgressTrackerDate(int days){   //Not using
		SimpleDateFormat simple_date_format = new SimpleDateFormat("MM/dd/yyyy");
		Calendar calender = Calendar.getInstance();
		calender.setTime(new Date()); // Now use today date.
		calender.add(Calendar.DATE, days); // Adding 7 days
		String output = simple_date_format.format(calender.getTime());
		System.out.println(output);
		return output;
	}*/
	
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
	
	public JSONArray populatePlanList(HttpServletRequest request) throws ParseException, Exception
	{	
		Logit.logWrite("FitBuddyLogs","User plan options returned");
		return chatlet_handler.getPlanOptions(request);
	}
	/***********************************************************************************************************************************************/
	/********************************************************
	 * Workout Section
	 * @param api
	 * @throws Exception
	 */
	/************************************************************************************************************************************************/
	/***********************************************************************************************************************************************
	 * FitTest Section
	 * @param fitbuddy_api
	 */
	
	private void postFitTestScore(HttpServletRequest request) {
		String data = request.getParameter("data");
		JSONObject json = new JSONObject(data);
		System.out.println(json.toString());
		chatlet_handler.postFitTestResult(fitbuddy_api, request);
		//return new JSONArray();
	}
	/***********************************************************************************************************************************************/
	/***********************************************************************************************************************************************
	 * Attendance Section
	 * @param api
	 * @throws Exception
	 */
	
	/***********************************************************************************************************************************************/
	/************************************************************************************************************************************************
	 * Nutrition Section
	 * @param api
	 */
	
	
	/***********************************************************************************************************************************************/
	/***********************************************************************************************************************************************
	 * Rewards Section
	 * @param api
	 */
	
	
	public void postRewards(TeamchatAPI api){
		String room_id = api.context().currentRoom().getId();			//Removed Keyword
		chatlet_handler.publishRewardJson(api, room_id);			
	}
	
	/***********************************************************************************************************************************************/
	/***********************************************************************************************************************************************
	 * Recommendation Section
	 * @param api
	 */
		
	public JSONArray updateEditProfilePage(TeamchatAPI api, HttpServletRequest request)
	{
		return chatlet_handler.getProfileDetails(api, request);
	}
	
	private void createRoomsForAll() 
	{
		JSONArray queried_rows = db_config.retrieveQueryRows(db_config.retrieveData("select email from testemail;").toString());
		Logit.logWrite("FitBuddyLogs", "Test email data: " + queried_rows.toString());
		//JSONArray queried_row_array = (JSONArray) queried_rows.get(0);
		for (int row_index = 0; row_index < queried_rows.size(); row_index ++)
		{
			JSONArray queried_email_record = (JSONArray) queried_rows.get(row_index);
			String email = (String) queried_email_record.get(0);
			email = email.trim();
			Logit.logWrite("FitBuddyLogs", "Current email: " + email);
			JSONArray userprofilestest_data = db_config.retrieveQueryRows(db_config.retrieveData("select nutritionRoomID, roomid, username from userprofilestest where email = '" + email + "';").toString());
			if (!(userprofilestest_data.isEmpty()))
			{
				Logit.logWrite("FitBuddyLogs", "Data exists for this user in userprofilestest, updating with new room ids");
				JSONArray userprofilestest_nroomid_row = (JSONArray) userprofilestest_data.get(0);
				String nutrition_room_id = (String) userprofilestest_nroomid_row.get(0);
				String username = (String) userprofilestest_nroomid_row.get(2);
				if (nutrition_room_id.isEmpty())
				{
					Logit.logWrite("FitBuddyLogs", "No nutrition room id found, creating new rooms");
					Room fit_room = fitbuddy_api.context().create().add(email);
					fitbuddy_api.perform(fit_room.post(new TextChatlet("Hey Buddy! If you are here, you have that desire to live a healthy life. You will love me or you might hate me. But, I will do my best to give you the best tools to stay healthy. Just Trust me and be with me, Love, Your FitBuddy :)")));
					String new_room_id = fit_room.getId();
					Logit.logWrite("FitBuddyLogs", "FitBuddy room created, id: " + new_room_id);
					Room nutrition_room = nutritionist_api.context().create().add(email).add("nutrition_1@dumbelled.com").setName("Ask A Nutritionist");
					nutritionist_api.perform(nutrition_room.post(new TextChatlet("Hi, I am Priti, Nutritionist's Assistant. Are you looking to chat with the nutritionist?")));
					nutrition_room_id = nutrition_room.getId();
					Logit.logWrite("FitBuddyLogs", "Nutrition room created, id: " + nutrition_room_id);
					String old_room_id = (String) userprofilestest_nroomid_row.get(1);
					
					HashMap<String, String> createRoomData = new HashMap<String, String>();
					createRoomData.put("roomid", new_room_id);
					createRoomData.put("OldRoom", old_room_id);
					createRoomData.put("nutritionRoomID", nutrition_room_id);
					Logit.logWrite("FitBuddyLogs", "Data to update in userprofilestest: " + createRoomData.toString());
					db_config.updateData("userprofilestest", createRoomData, WorkflowConstants.email_criteria_string(email));
					Logit.logWrite("FitBuddyLogs", "userprofilestest updated");
					if (username.isEmpty())
					{
						chatlet_handler.postSetupProfileForm(fitbuddy_api, new_room_id);
					}
					else 
					{
						chatlet_handler.postMenu(fitbuddy_api, new_room_id);
					}
					Logit.logWrite("FitBuddyLogs", "Setup chatlet thrown for " + email);
				}
			}
			else
			{
				Logit.logWrite("FitBuddyLogs", "No existing data found for user in userprofilestest");
				Room fit_room = fitbuddy_api.context().create().add(email);
				fitbuddy_api.perform(fit_room.post(new TextChatlet("Hey Buddy! If you are here, you have that desire to live a healthy life. You will love me or you might hate me. But, I will do my best to give you the best tools to stay healthy. Just Trust me and be with me, Love, Your FitBuddy :)")));
				String fit_room_id = fit_room.getId();
				Logit.logWrite("FitBuddyLogs", "FitBuddy room created, id: " + fit_room_id);
				Room nutrition_room = nutritionist_api.context().create().add(email).add("nutrition_1@dumbelled.com").setName("Ask A Nutritionist");
				nutritionist_api.perform(nutrition_room.post(new TextChatlet("Hi, I am Priti, Nutritionist's Assistant. Are you looking to chat with the nutritionist?")));
				String nutrition_room_id = nutrition_room.getId();
				Logit.logWrite("FitBuddyLogs", "Nutrition room created, id: " + nutrition_room_id);
				
				HashMap<String, String> createRoomData = new HashMap<String, String>();
				createRoomData.put("roomid", fit_room_id);
				createRoomData.put("email", email);
				createRoomData.put("nutritionRoomID", nutrition_room_id);
				Logit.logWrite("FitBuddyLogs", "Data to update in userprofilestest: " + createRoomData.toString());
				db_config.insertData("userprofilestest", createRoomData);
				Logit.logWrite("FitBuddyLogs", "userprofilestest updated");
				chatlet_handler.postSetupProfileForm(fitbuddy_api, fit_room_id);
				Logit.logWrite("FitBuddyLogs", "Setup chatlet thrown for " + email);
			}
		}
	}
	
	public void processNutritionForm(TeamchatAPI api, HttpServletRequest request){
		String data = request.getParameter("data");
		JSONObject json = new JSONObject(data);
		String nutritionRoomID = json.getString("roomId");
		Logit.logWrite("FitBuddyLogs", "Nutrition data received, data: " + json.toString() + ", nutrition room ID: " + nutritionRoomID);
		
		JSONArray queried_rows = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.nutrition_room_id_query_string(nutritionRoomID)).toString());
		JSONArray queried_array = (JSONArray) queried_rows.get(0);
		String email = (String) queried_array.get(0);
		String height = (String) queried_array.get(2);
		String weight = (String) queried_array.get(3);
		String gender = (String) queried_array.get(4);
		String name = (String) queried_array.get(1);
		String BMI = (String) queried_array.get(5);
		String dob = (String) queried_array.get(6);
		
		String consultation_date = getDateForTimeZone("Asia/Calcutta", "dd MMM yyyy HH:mm:ss");
		
		HashMap<String, String> nutrition_data = new HashMap<String, String>();
		nutrition_data.put("Email", email);
		nutrition_data.put("RoomID", nutritionRoomID);
		nutrition_data.put("Goal", json.getString("goal"));
		//nutrition_data.put("Cuisine", json.getString("preferred_cuisine"));
		nutrition_data.put("Preference", json.getString("preference"));
		nutrition_data.put("Activity", json.getString("current_physical_activity"));
		nutrition_data.put("Exercise", json.getString("exercise_frequency"));
		nutrition_data.put("Supplements", json.getString("supplements"));
		nutrition_data.put("NutritionalSupplements", json.getString("nutritional_supplements"));
		nutrition_data.put("diet_recall", json.getString("current_diet"));
		nutrition_data.put("medical_history", json.getString("medical_history"));
		nutrition_data.put("food_allergies", json.getString("food_allergy"));
		nutrition_data.put("Date", consultation_date);
		
		Logit.logWrite("FitBuddyLogs", "Data to be entered into NutritionFormData:" + nutrition_data.toString());
		db_config.insertData("NutritionFormData", nutrition_data);
		Logit.logWrite("FitBuddyLogs", "Data pushed to NutritionFormData, preparing nutrition profile chatlet");
		
		PollChatlet basic_profile = new PollChatlet();
		basic_profile.setQuestionHtml("<h1>"+name+"'s Basic Profile</h1><br /><table style = 'align:center; width: 100%;'><tr><td>Gender</td><td>"+gender+"</td></tr><tr><td>Height</td><td>"+height+"</td></tr><tr><td>Weight</td><td>"+weight+"</td></tr><tr><td>Goal</td><td>"+json.getString("goal")+"</td></tr><tr><td>BMI</td><td>"+BMI+"</td></tr></table><br />Thanks for giving us the details, the nutritionist will analyze your data and get in touch with you shortly");
		api.perform(api.context().byId(nutritionRoomID).post(basic_profile));
		Logit.logWrite("FitBuddyLogs", "Nutrition profile chatlet posted");
		String chatlet_id = basic_profile.getFormId();
		String comment_string = "Preference :"+json.getString("preference")+"\r\nActivity :"+json.getString("current_physical_activity")+"\r\nExercise :"+json.getString("exercise_frequency")+"\r\nSupplements :"+json.getString("supplements")+"\r\nNutritionalSupplements :"+json.getString("nutritional_supplements")+"\r\n Yesterday's Diet :"+json.getString("current_diet")+"\r\nMedical History :"+json.getString("medical_history")+"\r\nFood Allergies :"+json.getString("food_allergy")+"\r\nDate Of Birth :"+dob;
		
		Chatlet chatlet = api.context().chatletById(chatlet_id);
		api.perform(api.context().byId(nutritionRoomID).comment(chatlet.createComment()
			.addComment(comment_string)));
		Logit.logWrite("FitBuddyLogs", "Comment added to nutrition profile chatlet");
		HashMap<String, String> first_consultation = new HashMap<String, String>();
		first_consultation.put("FirstConsultation", "done");
		System.out.println("Update row for email "+email+" and room id "+ nutritionRoomID);
		System.out.println(db_config.retrieveQueryRows(db_config.retrieveData("select * from "
				   + "userprofilestest where nutritionRoomID ='"+nutritionRoomID+"';").toString()));
		//marker#1
		db_config.updateData("userprofilestest", first_consultation, 
				             WorkflowConstants.first_consultation(nutritionRoomID, email));
		Logit.logWrite("FitBuddyLogs", "'userprofilestest' updated, first consultation is done");
		HashMap<String, String> progress_schedule = new HashMap<String, String>();
		String tracker_date = getDateForTimeZone("Asia/Calcutta", "MM/dd/yyyy");
		String next_tracker_date = addDaysToDate(tracker_date, 7);
		progress_schedule.put("TrackerDate", tracker_date);
		progress_schedule.put("NextTrackerDate", next_tracker_date);
		db_config.updateData("userprofilestest", progress_schedule,
				             WorkflowConstants.progress_tracker_criteria(nutritionRoomID, email));
	}
	
	public void processProgressData(TeamchatAPI api, HttpServletRequest request){
		String data = request.getParameter("data");
		JSONObject json = new JSONObject(data);
		String room_id = json.getString("roomId");
		String consultation_date = getDateForTimeZone("Asia/Calcutta", "dd MMM yyyy");
		
		JSONArray queried_rows = db_config.retrieveQueryRows(db_config.retrieveData("select email, "
				                                                                    + "nutritionRoomID from "
				                                                                    + "userprofilestest where "
				                                                                    + "roomid = '" + room_id + "';"
				                                                                    )
																	  .toString()
															);
		JSONArray queried_array = (JSONArray) queried_rows.get(0);
		String email = (String) queried_array.get(0);
		String nutrition_roomid = (String) queried_array.get(1);
		
		HashMap<String, String> progress_data = new HashMap<String, String>();
		progress_data.put("Email", email);
		progress_data.put("RoomID", room_id);
		progress_data.put("ARMS", json.getString("arms"));
		progress_data.put("HIP", json.getString("hip"));
		progress_data.put("WAIST", json.getString("waist"));
		progress_data.put("THIGHS", json.getString("thighs"));
		progress_data.put("WEIGHT", json.getString("weight"));
		progress_data.put("Date", consultation_date);
		progress_data.put("GENDER", json.getString("gender"));
				
		db_config.insertData("ProgressData", progress_data);
		
		//HashMap<String, String> progress_schedule = new HashMap<String, String>();
		//String tracker_date = getDateForTimeZone("Asia/Calcutta", "MM/dd/yyyy");
		//String next_tracker_date = addDaysToDate(tracker_date, 7);
		//progress_schedule.put("TrackerDate", tracker_date);
		//progress_schedule.put("NextTrackerDate", next_tracker_date);
		
		//db_config.updateData("userprofilestest", progress_schedule,
		//		             WorkflowConstants.progress_tracker_criteria(nutrition_roomid, email));
		
		String waist = json.getString("waist"), hip = json.getString("hip");//, gender = json.getString("gender"), analysis;
		float waist_hip_ratio = Float.parseFloat(waist)/Float.parseFloat(hip);
		/*if ((waist_hip_ratio < 0.85)&&(gender.equals("F")) || ((waist_hip_ratio < 1.0)&&(gender.equals("M"))))
		{
			analysis = "Analysis: You are within your ideal weight range.";
			
		}
		else
		{
			analysis = "Analysis: You are overweight.";
		}*/
		
		fitbuddy_api.perform(fitbuddy_api.context().byId(room_id).post(new TextChatlet("Thank you for taking the "
																					   + "measurements. I'll share "
																					   + "your data with the nutritionist."
																					   )
																	   )
				            );
		nutritionist_api.perform(nutritionist_api.context().byId(nutrition_roomid)
				        		 .post(new TextChatlet("Based on the measurements shared by you, your waist-hip"
				        		                       + " ratio is " + String.format("%.2f", waist_hip_ratio) +", the nutritionist will"
				        		                       + " analyze this information and get back to you. Below is"
				        		                       + " your detailed statistic."
				        		                       )
				        			   )
				        		 );
		chatlet_handler.postProgressResult(fitbuddy_api, nutritionist_api, room_id, nutrition_roomid);
		
	}
	
	private void executeMassFunction()
	{
		JSONArray rows_to_act_on = db_config.retrieveQueryRows(db_config.retrieveData("select roomid from testemail").toString());
		for (int row_count = 0; row_count < rows_to_act_on.size(); row_count ++)
		{
			JSONArray individual_rows = (JSONArray) rows_to_act_on.get(row_count);
			String individual_id = (String) individual_rows.get(0);
			System.out.println(individual_id);
			json_handler.getJSONTemplate("/jsonfolder/masstest.json");
			fitbuddy_api.perform(fitbuddy_api.context().byId(individual_id).post(json_handler.chatletToPost(null)));
		}
	}
	
	public void postQuickWorkout(HttpServletRequest request)
	{
		String email = request.getParameter("emailID");
		String workout_name = request.getParameter("workoutName");
		Logit.logWrite("FitBuddyLogs", "Quick workout request received from: " + email);
		JSONArray roomid_rows = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.room_id_from_email_query_string(email)).toString());	
		JSONArray rows = (JSONArray) roomid_rows.get(0);
		String roomid = (String) rows.get(0);
		Logit.logWrite("FitBuddyLogs", "RoomID received: " + roomid);
		JSONArray queried_rows = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.fetch_roomidmap_data(roomid, "Fitbuddy")).toString());
		if(queried_rows.size()>0)
		{
			Logit.logWrite("FitBuddyLogs", email + " has a FitBuddy row in RoomidMap");
			JSONArray room_data = (JSONArray) queried_rows.get(0);
			String chatletid = (String) room_data.get(4);
			
			if(chatletid.isEmpty())
			{
				Logit.logWrite("FitBuddyLogs", email + " does not have any prior bypass workout chatlet, throwing one");
				HashMap<String, String> chatlet_id_map = new HashMap<String, String>();
				chatletid = roomid.substring(roomid.length()-10) + "fb1";
				Logit.logWrite("FitBuddyLogs", email + "'s new quick workout chatlet ID is " + chatletid);
				chatlet_id_map.put("FBWorkout", chatletid);
				db_config.updateData("RoomidMap", chatlet_id_map, "(roomid = '"+roomid+"') and (type = 'Fitbuddy')");
				Logit.logWrite("FitBuddyLogs/WorkoutPosts/", "Workout Chatlet ID updated for Room :"+roomid+", user: "+email);
			}
				
			JSONArray workout_data = db_config.retrieveQueryRows(db_config.retrieveData(WorkflowConstants.fetch_quickworkout_data(workout_name)).toString());
			Logit.logWrite("FitBuddyLogs/WorkoutPosts/", "Fetching workouts to be posted for :"+email);
			JSONArray workout_row = (JSONArray) workout_data.get(0);
			if(workout_row.size()>0)
			{
				Logit.logWrite("FitBuddyLogs/WorkoutPosts/", "Workout row retrieved to be posted in Room :"+roomid);
					
				try 
				{
					json_handler.getJSONTemplate("/jsonfolder/workout.json");
					json_handler.insertDataIntoJsons("_circuit_name_", (String) workout_row.get(0));
					json_handler.insertDataIntoJsons("_exercise_name_1_", (String) workout_row.get(1));
					json_handler.insertDataIntoJsons("_image_link_1_", (String) workout_row.get(2));
					json_handler.insertDataIntoJsons("_instructions_1_1_", (String) workout_row.get(3));
					json_handler.insertDataIntoJsons("_instructions_1_2_", (String) workout_row.get(4));
							
					json_handler.insertDataIntoJsons("_exercise_name_2_", (String) workout_row.get(5));
					json_handler.insertDataIntoJsons("_image_link_2_", (String) workout_row.get(6));
					json_handler.insertDataIntoJsons("_instructions_2_1_", (String) workout_row.get(7));
					json_handler.insertDataIntoJsons("_instructions_2_2_", (String) workout_row.get(8));
													
					json_handler.insertDataIntoJsons("_exercise_name_3_", (String) workout_row.get(9));
					json_handler.insertDataIntoJsons("_image_link_3_", (String) workout_row.get(10));
					json_handler.insertDataIntoJsons("_instructions_3_1_", (String) workout_row.get(11));
					json_handler.insertDataIntoJsons("_instructions_3_2_", (String) workout_row.get(12));
									
					json_handler.insertDataIntoJsons("_exercise_name_4_", (String) workout_row.get(13));
					json_handler.insertDataIntoJsons("_image_link_4_", (String) workout_row.get(14));
					json_handler.insertDataIntoJsons("_instructions_4_1_", (String) workout_row.get(15));
					json_handler.insertDataIntoJsons("_instructions_4_2_", (String) workout_row.get(16));
										
					json_handler.insertDataIntoJsons("_exercise_name_5_", (String) workout_row.get(17));
					json_handler.insertDataIntoJsons("_image_link_5_", (String) workout_row.get(18));
					json_handler.insertDataIntoJsons("_instructions_5_1_", (String) workout_row.get(19));
					json_handler.insertDataIntoJsons("_instructions_5_2_", (String) workout_row.get(20));
					SimpleJsonChatlet workout = json_handler.chatletToPost(null);
					Logit.logWrite("FitBuddyLogs/WorkoutPosts/", "Quick Workout Chatlet Retrieved as :"+workout.toString());
					BypassChatletCopy workout_post = new BypassChatletCopy(workout);
					workout_post.setFormId(chatletid);
						
					fitbuddy_api.perform(fitbuddy_api.context().byId(roomid).post(workout_post));
					Logit.logWrite("FitBuddyLogs", "Quick workout chatlet posted in room " + roomid + " for user " + email);
				} 
				catch (Exception exception) 
				{	
					Logit.logWrite("FitBuddyLogs/WorkoutPosts/"+roomid, "Exception caught while posting Workout Circuit: "+roomid+", Exception : "+exception.getStackTrace());
				}						
			}
			else
			{
				Logit.logWrite("FitBuddyLogs/WorkoutPosts/", "No workouts found to be posted in Room :"+roomid);
			}
			
			chatlet_handler.postWorkoutLogger(fitbuddy_api, roomid, workout_name);
			
		}
	}
	
	
	public void checkNutritionistRoom(String email)
	{
		email = email.trim();
		Logit.logWrite("FitBuddyLogs", "Checking if user " + email + " has an existing Nutritionist room");
		JSONArray nutrition_query = db_config.retrieveQueryRows(db_config.retrieveData("select nutritionRoomID from userprofilestest where email = '" + email + "';").toString());
		JSONArray nutrition_row = (JSONArray) nutrition_query.get(0);
		String nutrition_id = (String) nutrition_row.get(0);
		if (nutrition_id.isEmpty()) 
		{
			Logit.logWrite("FitBuddyLogs", "No nutritionist roomid found for " + email + ", creating new room");
			Room nutri_room = nutritionist_api.context().create().add(email)
			./*add("nutrition_1@dumbelled.com").*/setName("Ask A Nutritionist");
			nutritionist_api.perform(nutri_room.post(new TextChatlet("Hi, I am Priti, Nutritionist's Assistant. Are you looking to chat with the nutritionist?")));
			Logit.logWrite("FitBuddyLogs", "Room created with Nutrition Bot for " + email);
			String nutritionRoomID = nutri_room.getId();
			Logit.logWrite("FitBuddyLogs", "NutritionBot RoomID: " + nutritionRoomID);
			
			HashMap<String, String> putNutritionID = new HashMap<String, String>();
			putNutritionID.put("nutritionRoomID", nutritionRoomID);
			db_config.updateData("userprofilestest", putNutritionID, "(email = '" + email + "')");
			Logit.logWrite("FitBuddyLogs", email + "'s userprofile row updated with new nutrition roomid");
		}
	}
	
}