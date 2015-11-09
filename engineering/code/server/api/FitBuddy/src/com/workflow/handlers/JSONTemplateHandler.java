package com.workflow.handlers;

//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

//import org.apache.commons.codec.binary.Base64;
//import org.json.JSONException;
import org.json.JSONObject;
//import org.json.simple.JSONArray;

import com.teamchat.client.sdk.TeamchatAPI;
import com.teamchat.client.sdk.chatlets.SimpleJsonChatlet;

public class JSONTemplateHandler {
	
	//DatabaseConfig db_config = new DatabaseConfig();
	int character_read = 0;
	public String new_json_content = "";
	String original_json_content = "";
	SimpleJsonChatlet chatlet_to_post = null;
	

	public void getJSONTemplate(String file_name)
	{
		new_json_content = "";
		FileReader json_reader;
		try {
			json_reader = new FileReader(file_name);
			while (!((character_read = json_reader.read()) == -1))
			{
				new_json_content += (char) character_read;
			}
			new_json_content += "\0";
			original_json_content = new_json_content;
			json_reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//return new_json_content;
	}
	
	public SimpleJsonChatlet chatletToPost (/*String json_content,*/ String alias)
	{
		JSONObject json_object = new JSONObject (new_json_content); 
		SimpleJsonChatlet json_chatlet = new SimpleJsonChatlet(json_object);
		if(!(alias == null))
		{
			json_chatlet.alias(alias);
		}
		new_json_content = original_json_content;
		return json_chatlet;
	}
	
	public void insertDataIntoJsons(String substring_to_replace, String substring_replacement){
		new_json_content = new_json_content.replaceAll(substring_to_replace, substring_replacement);
	}
	
	public void postChatletByRoomId(TeamchatAPI api, SimpleJsonChatlet jsonchatlet, String Roomid){
		api.perform(api.context().byId(Roomid).post(jsonchatlet));
	}
	
	public void postChatletInCurrentRoom(TeamchatAPI api, SimpleJsonChatlet jsonchatlet){
		api.performPostInCurrentRoom(jsonchatlet);
	}
	
	
}