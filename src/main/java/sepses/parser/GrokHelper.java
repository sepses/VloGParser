package sepses.parser;

import java.io.IOException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nflabs.grok.Grok;
import com.nflabs.grok.GrokException;
import com.nflabs.grok.Match;


public class GrokHelper {
	private String grokfile;
	private String grokpattern;

 public static void main(String[] args) throws GrokException, IOException, ParseException {
	 String logline ="Dec 31 20:40:19 KABULHOST sshd[7855]: Invalid user mike from 87.106.50.214";
	 String grokfile ="experiment/input/pattern.grok";
	 String grokpattern="%{SYSLOGBASE} %{GREEDYDATA:message}";
	 String jsonParam = "{\"logsource\":\"KABULHIST\",\"program\":\"ssh\"}";

	 GrokHelper gh = new GrokHelper(grokfile, grokpattern);
	 String rs = gh.parseGrok(logline);
	 //System.out.println(rs);
	 if(checkIfKeyValueExist(rs, "logsource", "KABULHOST")) {
		 System.out.print("True");
	 }else {
		 System.out.print("False");
		 }
	 

}
 
 public GrokHelper(String grokfile,String grokpattern) {
	 this.grokfile=grokfile;
	 this.grokpattern=grokpattern;
 }
 
 public String parseGrok(String logline) throws GrokException {

	 Grok g = new Grok();
		g.addPatternFromFile(this.grokfile);
		g.compile(this.grokpattern);
		//System.out.println(logline);
		Match gm = g.match(logline);
		gm.captures();
		//System.out.println(gm.toJson());
		//See the result
	 return gm.toJson();
  }
 
  public static boolean checkIfKeyValueExist(String jsonString, String pkey, String pvalue) throws IOException, ParseException {
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
  
	public static JSONObject parseJSON(String js) throws IOException, ParseException {
		//read file
		   JSONParser jsonParser = new JSONParser();
		   JSONObject obj = (JSONObject)jsonParser.parse(js);
	        return obj;
	}
	
	
	
  
}
