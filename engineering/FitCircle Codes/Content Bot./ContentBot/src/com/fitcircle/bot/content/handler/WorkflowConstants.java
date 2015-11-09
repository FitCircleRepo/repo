package com.fitcircle.bot.content.handler;

import java.io.*;
import java.time.LocalDate;
import java.util.Date;

import com.fitcircle.bot.content.handler.*;

//Important SQL queries are picked up from here.

public class WorkflowConstants {
	
	public static final int workout_target_hour = 10;
	public static final int workout_target_minute = 18;
	public static final int workout_target_interval = 10;
	
	public static final int activity_target_hour = 10;
	public static final int activity_target_minute = 19;
	public static final int activity_target_interval = 10;
	
	
	public static final int leaderboard_target_hour = 13;
	public static final int leaderboard_target_minute = 00;
	public static final int leaderboard_target_interval = 1440;
	
	public static final int dailydose_target_hour = 15;
	public static final int dailydose_target_minute = 00;
	public static final int dailydose_target_interval = 1440;
	

	public static final int rating_target_hour = 18;
	public static final int rating_target_minute = 00;
	public static final int rating_target_interval = 1440;
	
	
	public static String check_logger_record(String email, String roomid, LocalDate date){
		String query_string = "select * from Logger where EmailID = '"+email+"' and RoomID = '"+roomid+"' and \"Date\" = '"+date+"';";
		return query_string;
	}
	
		
	public static String update_logger_data_criteria(String email, String roomid,  LocalDate date){
		String criteria_string = "(EmailID = '" + email + "') and (RoomID ='"+roomid+"') and (\"Date\" = '"+date+"')";
		return criteria_string;
	}
	
	public static String logger_chatlet_criteria(String roomid){
		String criteria_string = "(RoomID ='"+roomid+"')";
		return criteria_string;
	}
	
	public static String fetch_logger_chatlet_id(String roomid){
		String query_string = "select LoggerChatlet from RoomData where RoomID = '"+roomid+"';";
		return query_string;
	}
	
	public static String fetch_rating_chatlet_id(String roomid){
		String query_string = "select RatingChatlet from RoomData where RoomID = '"+roomid+"';";
		return query_string;
	}
	
	public static String fetch_all_roomids(){
		String query_string = "select roomid from RoomData;";
		return query_string;
	}
	
	public static String workout_query(String day){
		String query_string = "select Day, CircuitName, Name1, Exercise1, Inst11, Inst12, Name2, Exercise2, Inst21, Inst22, Name3, Exercise3, Inst31, Inst32, Name4, Exercise4, Inst41, Inst42, Name5, Exercise5, Inst51, Inst52 from Content where Day = '"+day+"';";
		return query_string;
	}
	
	public static String fetch_room_data(String roomid){
		String query_string = "select CurrentDay, CircleName, workoutchatlet1, workoutchatlet2, workoutchatlet3, workoutchatlet4, workoutchatlet5 from RoomData where roomid = '"+roomid+"';";
		return query_string;
	}
	
	public static String update_chatlet_id_criteria(String roomid){
		String query_string = "(RoomID ='"+roomid+"')";
		return query_string;
	}
	
	public static String fetch_dailydose_chatlet_data(String roomid){
		String query_string = "select CurrentDay, DailyDoseChatlet, PostType, QuizAnswer from RoomData where RoomID = '"+roomid+"';";
		return query_string;
	}
	
	public static String dailydose_chatlet_criteria(String roomid){
		String query_string = "(RoomID ='"+roomid+"')";
		return query_string;
	}
	
	public static String leaderboard_chatlet_criteria(String roomid){
		String query_string = "(RoomID ='"+roomid+"')";
		return query_string;
	}
	
	public static String fetch_dailydose_post(String day){
		String query_string = "select * from DailyDose where Day = '"+day+"';";
		return query_string;
	}
	
	public static String fetch_leaderboard(String roomid){
		String query_string = "select Name, Rank, HealthPoints from CircleLBoard where Roomid = '"+roomid+"';";
		return query_string;
	}
	
	public static String leaderboard_form_id(String roomid){
		String query_string = "select leaderboardchatlet from RoomData where roomid = '"+roomid+"';";
		return query_string;
	}
}