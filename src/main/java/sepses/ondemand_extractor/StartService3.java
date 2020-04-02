package sepses.ondemand_extractor;

import sepses.parser.GrokHelper;
import java.io.BufferedReader;
import java.util.UUID;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.jayway.jsonpath.JsonPath;
import com.nflabs.grok.GrokException;
import sepses.parser.JSONRDFParser;
import sepses.parser.Util;

/**
 * Hello world!
 * @author KKurniawan
 * @version 1
 */
public class StartService3
{
	
	private String content;
   


	public StartService3(String qs, String pq, String st, String et) throws IOException, ParseException, InterruptedException, URISyntaxException, org.json.simple.parser.ParseException, GrokException {
//    	System.out.println(qs);
//    	System.out.println(pq);
//		System.out.println(st);
//    	System.out.println(et);

    	//System.exit(0);
		//read json config
		String JsonConfig = new String(Files.readAllBytes(Paths.get("config.json"))); 
		JSONParser jcparser = new JSONParser(); 
		JSONObject jcobject = (JSONObject) jcparser.parse(JsonConfig);
        
		//endpoint
		String sparqlEndpoint = JsonPath.read(jcobject,"$.endpoint.sparqlEndpoint");
        String user = JsonPath.read(jcobject,"$.endpoint.user");
        String pass = JsonPath.read(jcobject,"$.endpoint.pass");
        
        //logsources
        List<String> logSources = JsonPath.read(jcobject, "$.logSources");
        List<String> ltitle = JsonPath.read(jcobject,"$.logSources[*].title");
        List<String> ltype = JsonPath.read(jcobject,"$.logSources[*].type");
        List<String> llogLocation = JsonPath.read(jcobject,"$.logSources[*].logLocation");
        List<String> lmapping = JsonPath.read(jcobject,"$.logSources[*].mapping");
        List<String> lgrokFile = JsonPath.read(jcobject,"$.logSources[*].grokFile");
        List<String> lgrokPattern = JsonPath.read(jcobject,"$.logSources[*].grokPattern");
        List<String> loutputModel = JsonPath.read(jcobject,"$.logSources[*].outputModel");
        List<String> lnamegraph = JsonPath.read(jcobject,"$.logSources[*].namegraph");
        List<String> lregexMeta = JsonPath.read(jcobject,"$.logSources[*].regexMeta");
        List<String> lregexOntology = JsonPath.read(jcobject,"$.logSources[*].regexOntology");
        List<String> lvocabulary = JsonPath.read(jcobject,"$.logSources[*].vocabulary");
        List<String> ltimeRegex = JsonPath.read(jcobject,"$.logSources[*].logTimeRegex");
        List<String> ldateFormat = JsonPath.read(jcobject,"$.logSources[*].logDateFormat");
       // System.out.print(logSources.size());
        //System.exit(0);
		String res="";
		long startTime = System.nanoTime();
		QueryTranslator qt = new QueryTranslator(pq);
		ArrayList prefixes= qt.prefixes;
		//System.out.println(prefixes.get(0));
		
		
		for(int i=0;i<logSources.size();i++) {


			//check prefix
			if(prefixes.contains(lvocabulary.get(i))){
		
						res = parse(llogLocation.get(i), lgrokFile.get(i), lgrokPattern.get(i),
								lmapping.get(i),pq, loutputModel.get(i), 
								lregexMeta.get(i), lregexOntology.get(i),
								sparqlEndpoint, user, pass, lnamegraph.get(i), st, et, ltimeRegex.get(i),
								ldateFormat.get(i));	
					//System.exit(0);
					this.content=res;
					
			}
		}
		
		long elapsedTime = System.nanoTime() - startTime;
		System.out.println("Total time execution :"+elapsedTime/1000000000+" sec");

		
		
    }
 
	public String parse(String logfile, String grokfile, String grokpattern, 
			String RMLFile, String parsedQuery, String outputModel, String regexMeta, 
			String regexOntology,String sparqlEndpoint, String user, String pass, 
			String namegraph,String startTime, String endDate, String dateTimeRegex,
			String dateFormat) throws IOException, org.json.simple.parser.ParseException, ParseException, GrokException {
//		System.out.print(endDate);
//		System.exit(0);
		//=======translate the query ===========
    	//delete existing output file
    	//deleteFile(outputResult);
		String response="";
    	deleteFile(outputModel);
        QueryTranslator qt = new QueryTranslator(parsedQuery);

         Model m = qt.loadRegexModel(regexMeta, regexOntology);
          qt.parseJSONQuery(m);
	     // System.exit(0);
	      List<FilterRegex> filterRegex= qt.filterregex;
		  List<RegexPattern> regexPattern= qt.regexpattern;
		 
    	SimpleDateFormat sdfl = new SimpleDateFormat(dateFormat);
    	SimpleDateFormat sdf = new SimpleDateFormat("MMM d HH:mm:ss");
    	Date startt = sdf.parse(startTime);
    	Date endt = sdf.parse(endDate);
    	
    	FileInputStream fis = new FileInputStream(logfile);
    	BufferedReader in = new BufferedReader(new InputStreamReader(fis));
    	
    	Integer logdata = 0;
    	JSONRDFParser jp = new JSONRDFParser(RMLFile);
    	Model model = ModelFactory.createDefaultModel();
    	try {
  		
    		GrokHelper gh = new GrokHelper(grokfile, grokpattern);
    		

			 String jsondata = "";
			 String jsondataTemp="";
			 
			int co=0;
			JSONArray alljson = new JSONArray();
    		while (in.ready()) {
    			co++;
    			String line = in.readLine();
    			
    			String dt0 = parseRegex(line,dateTimeRegex);
    			 Date dt1 = sdfl.parse(dt0);
				
				 //break after reaching the end of true line
				if(!dt1.before(endt)){
					System.out.println("break, true line is reached!, total read: "+co);
					break;
				}
				

    			 if(dt1.after(startt) && dt1.before(endt)) {
					
    				 jsondataTemp = gh.parseGrok(line);

    				 if(filterRegex.size()!=0) {
						boolean c = checkAllFilter(filterRegex, jsondataTemp);
						 if(c) {
							 jsondata=jsondataTemp;
							
 							
 						 }else {
 							jsondata="";
 						 }
    				 }else {
    					 jsondata=jsondataTemp;
//    					 System.out.println(jsondata);
    				 }
    			  } else{
    				 jsondata="";
    			 }	
     		
     			if(jsondata!=""){
     				logdata++;     			
					JSONObject jd = addUUID(jsondata);
					alljson.add(jd);
				}
				
			}
			JSONObject alljsObj = new JSONObject();
			alljsObj.put("logEntry",alljson);
			//System.out.println(alljsObj.toString());
			model  = jp.Parse(alljsObj.toString());
			System.out.println("logdata :"+logdata);
			
    	 }finally {
			   try {
		    	   in.close();
            	   Util ut = new Util();
            	   ut.saveModel(model, outputModel);
            	   ut.storeFileInRepo(outputModel, sparqlEndpoint, namegraph, user, pass);
            	   //this.content = ut.executeQuery(queryString, model);
            	   response = "{\"content\":\"success\",\"endopoint\":\""+sparqlEndpoint+"\"}";
            	   //ut.relodLDF(ldflog);
       			   model.close();
               }
               catch (IOException closeException) {
                   // ignore
               }
		}
		
		return response;
    	

	}
	public boolean checkFilterJsonWithVariableRegex(String jsondata, String variable, String regex) throws org.json.simple.parser.ParseException {
		JSONParser parser = new JSONParser(); 
		JSONObject json = (JSONObject) parser.parse(jsondata);
		if(json.get(variable)==null){
			return true;
		}else{
			if(checkRegexExist(json.get(variable).toString(),regex)){
				return true;
			}else {
				return false;
			}
		}
	}
	
	public void translateQuery(String pq,String rm,String ro) throws IOException, org.json.simple.parser.ParseException{
		QueryTranslator qt = new QueryTranslator(pq);
	      Model m = qt.loadRegexModel(rm, ro);
	      qt.parseJSONQuery(m);
	      
        
	}
    private boolean checkRegexExist(String Line,String regex) {
		String uri = parseRegex(Line,regex);
		if(uri!=null) {
			return true;
		}else {
			return false;
		}
	}

	private boolean checkAllFilter(List<FilterRegex> filterRegex, String jsondataTemp ) throws org.json.simple.parser.ParseException{
		ArrayList resFilter = new ArrayList<Boolean>();
		
		 for (int k=0;k<filterRegex.size();k++){
			// System.out.print(filterRegex.get(k).variable+"|"+filterRegex.get(k).regex);
    					 	  Boolean  cf = checkFilterJsonWithVariableRegex(jsondataTemp,filterRegex.get(k).variable,filterRegex.get(k).regex);
							resFilter.add(cf);
							
						}
		//System.out.println(jsondataTemp);
		for (int k=0;k<resFilter.size();k++){
					//		   System.out.print(resFilter.get(k)+"|");
							   
							
						}
		//System.out.println("");			
		if(resFilter.contains(false)){
			return false;
		}else{
			return true;
		}
	}
    
    private boolean checkRegexVariableExist(String Line,String variable,String regex) {
		String uri = parseRegex(Line,regex);
		if(uri!=null) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean checkRegexExistForObject(String Line,String regex, String Object) {
		String uri = parseRegex(Line,regex);
		if(uri!=null) {
			if(uri.contains(Object)) {
				return true;
			}else {
			    return false;
			}
		}else {
			return false;
		}
	}


	public static String parseRegex(String logline,String regex) {

    	Pattern pattern = Pattern.compile(regex);
    	Matcher matcher = pattern.matcher(logline);
    	String dt = null;
    	if (matcher.find())
    	{
    	    dt= matcher.group(0);
    	}
		return dt;  
	
    
}
	public static void deleteFile(String file) throws IOException {
		File f = new File(file);
		f.delete();
	}
    
	public boolean isFileExist(String outputResult) {
		File file = new File(outputResult); 
		if(file.exists()) {
			return true;
		}else {
			return false;
		}
	}

	public String getContents(String outputResult) throws IOException, InterruptedException {
		//System.out.println("getContent: "+outputResult);
		String r = null;
		File file = new File(outputResult); 
		while (!file.exists()) {
			Thread.sleep(10);
		}
		//System.out.println("getContent: "+outputResult);
		  BufferedReader br = new BufferedReader(new FileReader(file)); 
		  String st; 
		  while ((st = br.readLine()) != null) {
			  if(r==null) {
				  r=st;
			  }else {
				  r=r+"\r\n"+st;
			  }
		    //System.out.println(st); 
		  }
		  br.close();
		  file.delete();
		  return r;
		}
		
		

	public String getContent() {
		return this.content;
	}
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        return randomUUIDString;
	}
	public static void main( String[] args ) throws IOException, ParseException, InterruptedException, URISyntaxException, GrokException, org.json.simple.parser.ParseException
  {
		String parsedQueryFile = "experiment0/input/query.json";
		String parsedQuery = new String(Files.readAllBytes(Paths.get(parsedQueryFile))); 
		String queryStringFile = "experiment0/input/query2.sparql";
		String queryString = new String(Files.readAllBytes(Paths.get(queryStringFile))); 
		String startTime = "May 30 19:09:00";
    	String endDate = "May 30 19:10:00";
    	
			StartService3 ss = new StartService3(queryString,parsedQuery, startTime, endDate);
		
		
 	}
	
	 JSONObject addUUID(String jsondata) throws org.json.simple.parser.ParseException{
		UUID ui = UUID.randomUUID();
		org.json.simple.parser.JSONParser jp = new org.json.simple.parser.JSONParser();
		JSONObject json = (JSONObject) jp.parse(jsondata);
		json.put("id", ui);
		//System.out.print(json.toString());
		return json;
			
		
	}

	
}
