package com.workflow.core;

import java.io.*;
import com.workflow.handlers.*;

//Important SQL queries are picked up from here.

public class WorkflowConstants {
	
	protected static String query_string = null;
	
	public static final int dailyworkout_target_hour = 16;
	public static final int dailyworkout_target_minute = 00;
	public static final int dailyworkout_interval = 1440;
	
	public static final int attendance_target_hour = 17;
	public static final int attendance_target_minute = 20;
	public static final int attendance_interval = 1440;
	
	public static final int recommendation_post_delay = 60;
	
	public static final int progress_target_hour = 7;
	public static final int progress_target_minute = 0;
	public static final int progress_delay = 1440;
	
	public static String RUN_PLAN_OPTIONS()
	{
		query_string = "select RunPlan from RunPlans where RunningDistance = '%s' and MinDuration <= '%s'";
		//Logit.logWrite("Acquiring RUN_PLAN_OPTIONS query string");
		return query_string;
	}
	
	public static String READ_PLAN_TABLE()
	{
		query_string = "select plantype,runplan,planallocationdate,planstartdate,planenddate,email,id,currentrunningdistance,quoteplanname,planinprogress,eventdate,quoteday,isactive from activeuserplandetails where email = '%s'";
		//Logit.logWrite("Acquiring READ_PLAN_TABLE query string");
		return query_string;
	}
	
	public static String criteria_for_stopplan(String email)
	{
		query_string = "(email = '"+email+"') and (isactive ='yes')";
		//Logit.logWrite("Acquiring test query string to fetch data: ");
		return query_string;
	}
	
	public static String CRITERIA_EMAIL()
	{
		query_string = "\"email\" = '%s'";
		//Logit.logWrite("Acquiring CRITERIA_EMAIL criteria string.");
		return query_string;
	}
	
	public static String email_criteria_string(String email)
	{
		query_string = "(email = '"+email+"')";
		//Logit.logWrite("Acquiring email query string to insert data.");
		return query_string;
	}
	
	public static String criteria_for_profile_update(String email, String room_id)
	{
		query_string = "(email = '"+email+"') and (roomid ='"+room_id+"')";
		//Logit.logWrite("Acquiring test query string to fetch data: ");
		return query_string;
	}
		
	public static String criteria_for_nutrition_response_update(String room_id)
	{
		query_string = "(RoomID = '" + room_id + "') and (NutritionistReply ='')";
		//Logit.logWrite("Acquiring profile update criteria to fetch data: ");
		return query_string;
	}
	
	public static String fit_test_result_query_string(String email)
	{
		query_string = "select top 1 a.\"username\", b.\"Rank\", b.\"FitScore\", b.\"FitMsg\", b.\"BMIScore\", b.\"BMIMsg\", b.\"endScore\", b.\"endMsg\", b.\"StrengthScore\", b.\"StrMsg\", b.\"TestTaken\" from userprofilestest a, RankTable b where a.\"email\" = b.\"email\" and a.\"email\" = '" + email + "' order by b.\"Date\" desc;";
		return query_string;
	}
	
	public static String room_id_query_string()
	{
		query_string = "select roomid, email from userprofilestest;";
		return query_string; 
	}
	
	public static String get_total_scorers()
	{
		query_string = "select count(distinct email) as 'TotalCount' from RankTable;";
		return query_string; 
	}
	
	public static String room_id_query_string(String room_id)
	{
		query_string = "select roomid, email from userprofilestest where roomid ='"+room_id+"' ;";
		return query_string; 
	}
	
	public static String room_id_from_email_query_string(String email_id)
	{
		query_string = "select roomid, email, username from userprofilestest where email ='"+email_id+"' ;";
		return query_string; 
	}
	
	public static String nutrition_room_id_from_email_query_string(String email_id)
	{
		query_string = "select nutritionRoomID from userprofilestest where email ='"+email_id+"' ;";
		return query_string; 
	}
	
	public static String complete_profile_query(String roomid)
	{
		query_string = "select email, username, phone, height, weight from userprofilestest where roomid ='"+roomid+"' ;";
		return query_string; 
	}
	
	/*public static String get_fitscore_query(String room_id) throws Exception
	{
		query_string = "Select r.FitScore, r.FitMsg, r.endScore, r.endMsg, r.BMIScore, r.BMIMsg, r.StrengthScore, r.StrMsg, r.Rank, u.username, r.RoomId from RankTable as r,userprofilestest as u where r.RoomId='"+room_id+"'and r.RoomId=u.roomid and u.email=r.email;";
		return query_string; 
	}*/
	
	public static String quote_message_query_string(String room_id)
	{
		System.out.println(room_id);
		query_string = "select workoutquote, motivationalquote, imagelink, videolink, imagelink1, videolink1, imagelink2, videolink2 from dailyquotes where roomId ='"+room_id+"'";
		//Logit.logWrite("Acquired query to fetch quote details: ");
		return query_string;
	}
	
	
	public static String get_plan_details_query_string(String room_id) {
		query_string = "select plantype, planallocationdate, planinprogress from activeuserplandetails where email in (select email from userprofilestest where roomid ='" + room_id +"');";
		return query_string;
	}
	
	public static String complete_profile_query_nutrition(String nutritionRoomID, String email)
	{
		query_string = "select email, username, phone, height, weight, FirstConsultation from userprofilestest where nutritionRoomID ='"+nutritionRoomID+"' and email ='"+email+"' ;";
		return query_string; 
	}
	
		
	public static String first_consultation(String roomid, String email){
		query_string = "(nutritionRoomID = '"+roomid+"') and (email = '"+email+"')";
		return query_string;
	}
	
	public static String nutrition_chatlet_id(String email, String date){
		query_string = "(Email = '"+email+"') and (Date = '"+date+"')";
		return query_string;
	}
	
	public static String progress_tracker_criteria(String roomid, String email){
		query_string = "(nutritionRoomID = '"+roomid+"') and (email = '"+email+"')";
		return query_string;
	}
	
	public static String progress_records(String roomid){
		query_string = "select top 3 \"Date\", WEIGHT, ARMS, WAIST, THIGHS, HIP from ProgressData where RoomID ='"+roomid+"' ORDER BY \"Date\" desc;";
		return query_string;
	}
	
	public static String nutrition_room_id_query_string(String nutrition_roomid)
	{
		query_string = "select email, username, height, weight, gender, BMI, dob from userprofilestest where nutritionRoomID = '"+nutrition_roomid+"' ;";
		return query_string; 
	}
	
	public static String fetch_roomidmap_data(String roomid, String room_type)
	{
		query_string = "select roomid, emailid, name, type, FBWorkout from RoomidMap where roomid = '"+roomid+"' and type = '" +room_type+ "';";
		return query_string; 
	}
	
	public static String fetch_quickworkout_data(String workout_name)
	{
		query_string = "select CircuitName, Name1, Exercise1, Inst11, Inst12, Name2, Exercise2, Inst21, Inst22, Name3, Exercise3, Inst31, Inst32, Name4, Exercise4, Inst41, Inst42, Name5, Exercise5, Inst51, Inst52 from Content where CircuitName = '" + workout_name + "';";
		return query_string; 
	}

}