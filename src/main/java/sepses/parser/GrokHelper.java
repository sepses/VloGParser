package sepses.parser;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import io.krakens.grok.api.exception.GrokException;

//import io.thekraken.grok.api.Grok;
//import io.thekraken.grok.api.Match;
//import io.thekraken.grok.api.exception.GrokException;



public class GrokHelper {


 
 public static JsonNode parseGrok(InputStream grokfilestream, String grokpattern, String logline) throws IOException  {
	
	    GrokCompiler grokCompiler = GrokCompiler.newInstance();
	    grokCompiler.register(grokfilestream);
		 final Grok grok = grokCompiler.compile(grokpattern);
		 Match gm = grok.match(logline);
		 final Map<String, Object> capture = gm.capture();
		 ObjectMapper mapper = new ObjectMapper();
		 JsonNode jsonNode = mapper.valueToTree(capture);
	 return jsonNode;
	
  }
 
  public static boolean checkIfKeyValueExist(String jsonString, String pkey, String pvalue) throws Exception, ParseException {
	  JSONObject jsonObj = parseJSON(jsonString);
	boolean exist=false;
		if(jsonObj.containsKey(pkey)) {
			if(jsonObj.containsValue(pvalue)) {
				exist=true;
			}else {
				exist=false;
			}
		}else {
			exist=false;
		}
	
	return exist;
	 
  }
  
	public static JSONObject parseJSON(String js) throws Exception, ParseException {
		//read file
		   JSONParser jsonParser = new JSONParser();
		   JSONObject obj = (JSONObject)jsonParser.parse(js);
	        return obj;
	}
	
	
	
  
}
