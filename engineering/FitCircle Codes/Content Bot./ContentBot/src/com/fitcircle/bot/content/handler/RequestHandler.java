package com.fitcircle.bot.content.handler;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.fitcircle.bot.content.util.DatabaseConfig;
import com.fitcircle.bot.content.util.JSONTemplateHandler;
import com.fitcircle.bot.content.util.Logit;
import com.fitcircle.bot.content.util.Scheduler;
import com.teamchat.client.sdk.Expirable;
import com.teamchat.client.sdk.TeamchatAPI;
import com.teamchat.client.sdk.impl.TeamchatAPIImpl;

/**
 * Servlet implementation class RequestHandler
 */
@WebServlet("/handler")
public class RequestHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static TeamchatAPI content_api;
	Scheduler post_timer = new Scheduler();
	ChatletHandler chatlet_handler = new ChatletHandler(); 
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RequestHandler() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		content_api = TeamchatAPIImpl.fromFile("ContentBotConfig.json").setEmail("vighnesh+content@dumbelled.com").setPassword("Contentbot@1").startReceivingEvents(new ContentBot());
		
		post_timer.createScheduledTask(new Runnable()
		{
				public void run()
				{
					try 
					{
						Logit.logWrite("ContentBotLogs", "Posting daily workout");
						ThreadHandler t1 = new ThreadHandler(chatlet_handler, "workout");
						t1.start();
						try{
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
	
		}, (WorkflowConstants.workout_target_hour*60 + WorkflowConstants.workout_target_minute), WorkflowConstants.workout_target_interval);
		
		
		post_timer.createScheduledTask(new Runnable()
		{
				public void run()
				{
					try 
					{
						Logit.logWrite("ContentBotLogs", "Posting activity logger");
						ThreadHandler t2 = new ThreadHandler(chatlet_handler, "activity");
						t2.start();
						try{
							t2.join();
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
	
		}, (WorkflowConstants.activity_target_hour*60 + WorkflowConstants.activity_target_minute), WorkflowConstants.activity_target_interval);
		
		
		post_timer.createScheduledTask(new Runnable()
		{
				public void run()
				{
					try 
					{
						Logit.logWrite("ContentBotLogs", "Posting leaderboard");
						ThreadHandler t3 = new ThreadHandler(chatlet_handler, "leaderboard");
						t3.start();
						try{
							t3.join();
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
	
		}, (WorkflowConstants.leaderboard_target_hour*60 + WorkflowConstants.leaderboard_target_minute), WorkflowConstants.leaderboard_target_interval);
		
		
		post_timer.createScheduledTask(new Runnable()
		{
				public void run()
				{
					try 
					{
						Logit.logWrite("ContentBotLogs", "Posting rating chatlet");
						ThreadHandler t4 = new ThreadHandler(chatlet_handler, "rating");
						t4.start();
						try{
							t4.join();
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
	
		}, (WorkflowConstants.rating_target_hour*60 + WorkflowConstants.rating_target_minute), WorkflowConstants.rating_target_interval);
		
		
		post_timer.createScheduledTask(new Runnable()
		{
				public void run()
				{
					try 
					{
						Logit.logWrite("ContentBotLogs", "Posting daily dose");
						ThreadHandler t5 = new ThreadHandler(chatlet_handler, "dailydose");
						t5.start();
						try{
							t5.join();
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
	
		}, (WorkflowConstants.dailydose_target_hour*60 + WorkflowConstants.dailydose_target_minute), WorkflowConstants.dailydose_target_interval);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String operation = request.getParameter("operation");
		switch(operation)
		{
		case "leaderboard":
			Logit.logWrite("ContentBotLogs", "Request recieved for leaderboard operation");
			String roomid = request.getParameter("roomid");
			String leaderboard = getLeaderboard("/ContentJSONs/leaderboardpage.txt"); 
			String leaderboard_data = getLeaderboardData(roomid);
			String final_leaderboard = leaderboard.replace("_leaderboard_data_", leaderboard_data); 
			PrintWriter out = response.getWriter();
		    out.write(final_leaderboard);
			
			break;
		
		case "start":
			Logit.logWrite("ContentBotLogs", "Servlet Started");
			response.getWriter().append("Served at: ").append(request.getContextPath());
			break;
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	
	
	public String getLeaderboardData(String roomid){
		DatabaseConfig database_config = new DatabaseConfig();
		String leaderboard_data_string = "";
		JSONArray leaderboard_data = database_config.retrieveQueryRows(database_config.retrieveData(WorkflowConstants.fetch_leaderboard(roomid)).toString());
		Logit.logWrite("ContentBotLogs", leaderboard_data.size()+" leaderboard rows retrieved for Room ID : "+roomid);
		if(leaderboard_data.size()>0)
		{
			leaderboard_data_string = "<tr><th><font style = 'color:#2BB2E7;'>Rank</font></th><th><font style = 'color:#2BB2E7;'>Name</font></th><th><font style = 'color:#2BB2E7;'>Health Points</font></th></tr>";
			for(int row_index = 0; row_index<leaderboard_data.size(); row_index++)
				{
					
					JSONArray leaderboard_row = (JSONArray) leaderboard_data.get(row_index);
					String username = (String) leaderboard_row.get(0);
					String rank = (String) leaderboard_row.get(1);
					String health_points = (String) leaderboard_row.get(2);
					if(username.isEmpty()||health_points.isEmpty()||rank.isEmpty())
						{
						Logit.logWrite("ContentBotLogs", "Empty Row for leaderboard retrieved at :"+row_index);
						}
					else
					{	
						Logit.logWrite("ContentBotLogs", "Recieved Row for leaderboard  :"+rank +"  "+username+"  "+health_points );
						leaderboard_data_string = leaderboard_data_string + "<tr><td>"+rank+"</td><td>"+username+"</td><td>"+health_points+"</td></tr>";
					}
			}
		}
		else
		{
			Logit.logWrite("ContentBotLogs", "No records found for leaderboard for Room ID : "+roomid);
			leaderboard_data_string = "<tr><td colspan=\"3\" align=\"center\"><font style = 'font-size:0.8em; color:#2BB2E7;'>Looks like your group has not started logging yet! What are you waiting for?</font><td></tr>";
		}
		
		return leaderboard_data_string;
	}
	
	
	
	public String getLeaderboard(String file_name)
	{
		String leaderboard_string = "";
		int character_read = 0;
		String original_file_content = "";
		String file_content = "";
		FileReader file_reader;
		try {
			file_reader = new FileReader(file_name);
			while (!((character_read = file_reader.read()) == -1))
			{
				file_content += (char) character_read;
			}
			file_content += "\0";
			original_file_content = file_content;
			file_reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file_content;
	}
	
	
	
}
