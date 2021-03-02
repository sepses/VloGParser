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
	private String grokfile;
	private String grokpattern;

 public static void main(String[] args) throws GrokException, Exception, ParseException {
	 String logline ="Dec 31 20:40:19 KABULHOST sshd[7855]: Invalid user mike from 87.106.50.214";
	 String grokfile ="experiment/pattern/pattern.grok";
	 String grokpattern="%{SYSLOGBASE} %{GREEDYDATA:message}";
	 String jsonParam = "{\"logsource\":\"KABULHOST\",\"program\":\"ssh\"}";

	 GrokHelper gh = new GrokHelper(grokfile, grokpattern);
//	 String rs = gh.parseGrok(logline);
//	 
//	 //System.out.println(rs);
//	 if(checkIfKeyValueExist(rs, "logsource", "KABULHOST")) {
//		 System.out.print("True");
//	 }else {
//		 System.out.print("False");
//		 }
	 

}
 
 public GrokHelper(String grokfile,String grokpattern) {
	 this.grokfile=grokfile;
	 this.grokpattern=grokpattern;
 }
 
 public JsonNode parseGrok(String logline) throws GrokException, IOException {

	 /* Create a new grokCompiler instance */
	    File initialFile = new File(this.grokfile);
	    InputStream targetStream = new FileInputStream(initialFile);
	 GrokCompiler grokCompiler = GrokCompiler.newInstance();
	 grokCompiler.register(targetStream);

	 /* Grok pattern to compile, here httpd logs */
	 final Grok grok = grokCompiler.compile(this.grokpattern);

	 /* Line of log to match */
	
	 Match gm = grok.match(logline);
//	 Grok g = new Grok();
//		g.addPatternFromFile(this.grokfile);
//		g.compile(this.grokpattern);
//		//System.out.println(logline);
//		Match gm = g.match(logline);;
	 
	 final Map<String, Object> capture = gm.capture();
	 ObjectMapper mapper = new ObjectMapper();
	 JsonNode jsonNode = mapper.valueToTree(capture);
//		//System.out.println(gm.toJson());
//		//See the result
//	 System.out.println(json);
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
