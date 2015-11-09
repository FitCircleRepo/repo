package com.fitcircle.bot.content.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import com.adventnet.zoho.client.report.ReportClient;
import com.adventnet.zoho.client.report.ServerException;
//import com.workflow.config.ConfigReader;

import org.json.simple.parser.*;
import org.json.simple.*;

//Class to create Zoho connection object and to fetch and add data to the database.

public class DatabaseConfig {

	private static final String DATABASENAME="TEST";
    private static final String LOGINEMAILID="stayfit@dumbelled.com";
    private static final String AUTHTOKEN="26eeac0fd449400437838a673a1fefa5";
    ReportClient report_client;
    
    public DatabaseConfig()
	{
		 report_client = new ReportClient(AUTHTOKEN);
	}

	public void insertData(String table_name, HashMap<String,String> values)
	{
		try {
			report_client.addRow(report_client.getURI(LOGINEMAILID, DATABASENAME, table_name), values,null);
		} catch (IOException | ServerException | com.adventnet.zoho.client.report.ParseException e) {
			e.printStackTrace();
		} 
	}
	
	public JSONObject retrieveData(String sqlQuery)
	{
		ByteArrayOutputStream query2json_buffer = new ByteArrayOutputStream();
		JSONObject result_object = null;
		try {
			report_client.exportDataUsingSQL(report_client.getURI(LOGINEMAILID, DATABASENAME), "JSON", query2json_buffer, sqlQuery, null);
			JSONParser json_parser = new JSONParser();
			JSONObject initial_object = (JSONObject)json_parser.parse(query2json_buffer.toString());
			JSONObject next_object = (JSONObject)initial_object.get("response");
			result_object = (JSONObject)next_object.get("result");
			Logit.logWrite("FitBuddyLogs", "Queried rows retrieved as JSONObject:" + result_object.toString());
		} catch (IOException | ServerException | com.adventnet.zoho.client.report.ParseException | ParseException e) {
			e.printStackTrace();
		}
		return result_object;
	}
	
	public JSONArray retrieveQueryRows(String to_be_parsed_into_object)
	{
		JSONObject received_jsonobject;
		JSONArray return_jsonarray = null;
		try {
			received_jsonobject = (JSONObject)new JSONParser().parse(to_be_parsed_into_object);
			return_jsonarray = (JSONArray)received_jsonobject.get("rows");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return return_jsonarray;
	}
	
	public void updateData(String table_name,  HashMap<String,String> values, /*String email,*/ String Criteria ){
		try {
			report_client.updateData(report_client.getURI(LOGINEMAILID, DATABASENAME, table_name), values, Criteria, null);
		} catch (IOException | ServerException | com.adventnet.zoho.client.report.ParseException e) {
			e.printStackTrace();
		} 
	}
}