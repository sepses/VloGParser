package sepses.ondemand_extractor;

import sepses.parser.GrokHelper;


import java.io.BufferedReader;
import java.util.UUID;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;

import sepses.parser.JSONRDFParser;
import sepses.parser.Util;


/**
 * Hello world!
 * @author KKurniawan
 * @version 1
 */
public class StartService
{
	
	private String content;
	private static final Logger log = LoggerFactory.getLogger(StartService.class);
	private long startTime = System.nanoTime();

	public StartService(String qs, String pq, String st, String et) throws Exception {

		String JsonConfig = new String(Files.readAllBytes(Paths.get("config.json"))); 
		JSONParser jcparser = new JSONParser(); 
		JSONObject jcobject = (JSONObject) jcparser.parse(JsonConfig);
        
		//endpoint
		String sparqlEndpoint = JsonPath.read(jcobject,"$.endpoint.sparqlEndpoint");
        String user = JsonPath.read(jcobject,"$.endpoint.user");
        String pass = JsonPath.read(jcobject,"$.endpoint.pass");
        
        //logsources
        List<String> logSources = JsonPath.read(jcobject, "$.logSources");

        String outputDir = JsonPath.read(jcobject,"$.outputDir");
        String hdtrepo = JsonPath.read(jcobject,"$.hdt-repo");
    	String hostname = InetAddress.getLocalHost().getHostName();

		String hdtOutput = outputDir+hostname+".hdt";
        List<String> llogLocation = JsonPath.read(jcobject,"$.logSources[*].logLocation");
        List<String> llogMeta = JsonPath.read(jcobject,"$.logSources[*].logMeta");
        List<String> lmapping = JsonPath.read(jcobject,"$.logSources[*].mapping");
        List<String> lgrokFile = JsonPath.read(jcobject,"$.logSources[*].grokFile");
        List<String> lgrokPattern = JsonPath.read(jcobject,"$.logSources[*].grokPattern");
        List<String> loutputModel = JsonPath.read(jcobject,"$.logSources[*].outputModel");
        List<String> lnamegraph = JsonPath.read(jcobject,"$.logSources[*].namegraph");
        List<String> lregexPattern = JsonPath.read(jcobject,"$.logSources[*].regexPattern");
        List<String> lvocabulary = JsonPath.read(jcobject,"$.logSources[*].vocabulary");
        List<String> ltimeRegex = JsonPath.read(jcobject,"$.logSources[*].logTimeRegex");
        List<String> ldateFormat = JsonPath.read(jcobject,"$.logSources[*].logDateFormat");
        

		String res="";

		 ArrayList<String> prefixes = QueryTranslator2.parsePrefixes(pq);

		
		org.eclipse.rdf4j.model.Model rdf4JM = new LinkedHashModel();
		
		for(int i=0;i<logSources.size();i++) {
				
		
			if(prefixes.contains(lvocabulary.get(i).toString())){

				log.info("parsing start");
				
						res = parse(rdf4JM, llogLocation.get(i), llogMeta.get(i),lgrokFile.get(i), lgrokPattern.get(i),
								lmapping.get(i),pq, loutputModel.get(i), hdtOutput,hdtrepo,lregexPattern.get(i),
								sparqlEndpoint, user, pass, lnamegraph.get(i), st, et, ltimeRegex.get(i),
								ldateFormat.get(i),lvocabulary.get(i));	

					this.content=res;
					
			}
		}
		

		
		
		long elapsedTime = System.nanoTime() - this.startTime;
		System.out.println("Total time execution :"+elapsedTime/1000000+" ms");

		
		
    }
 
	public String parse(org.eclipse.rdf4j.model.Model JModel, String logfolder, String logmeta, String grokfile, String grokpattern, 
			String RMLFile, String parsedQuery, String outputModel, String hdtOutput, String hdtrepo,String regexPattern, String sparqlEndpoint, String user, String pass, 
			String namegraph,String startTime, String endDate, String dateTimeRegex,
			String dateFormat, String vocab) throws Exception {
	   

		String response="";
    	deleteFile(outputModel);
    	
    	String generalGrokPattern = QueryTranslator2.parseGeneralRegexPattern(regexPattern,vocab);
    	 
    	 
    	
		ArrayList<String> regexPatterns = new ArrayList<String>();
    	if(generalGrokPattern==null) {
			  regexPatterns= QueryTranslator2.parseRegexPattern(parsedQuery,regexPattern);  
			
			  
		  }

		  ArrayList<String> filterRegex = QueryTranslator2.parseFilter(parsedQuery);

		
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    	Date startt = sdf.parse(startTime);
    	Date endt = sdf.parse(endDate);

    	 
    	
    	
    	
    	
    	
    	Integer logdata = 0;
    	JSONRDFParser jp = new JSONRDFParser(RMLFile);
    	
    	try {
  		
    		JsonNode jsondata=null;
			 JsonNode jsondataTemp=null;
			 
			int co=0;
			JSONArray alljson = new JSONArray();

			
			long timereading=0;
			
			ArrayList<String> files = findRespectedLogFile(startTime,endDate,logmeta);
			
			
	    	if(files.size()!=0) {
			
		     for (String file : files) {
		    	   
		    	 	System.out.println("processing file: "+logfolder+file);
		    	 	String logfile = logfolder+file;
		    
			
		//optionI	
		BufferedReader in = new BufferedReader(new FileReader(logfile));
		//option II
	//	BufferedInputStream bf= new BufferedInputStream(new FileInputStream(logfile));
	//	BufferedReader in = new BufferedReader(new InputStreamReader(bf, StandardCharsets.UTF_8));

		//option III
	//	BufferedReader in = Files.newBufferedReader(Paths.get(logfile), StandardCharsets.UTF_8);
		
	
    	        while (in.ready()) {
                String line = in.readLine();
             	
    			String dt0 = parseRegex(line,dateTimeRegex);
    			
    		
    			
    			Date dt1 = null;
    			if(!dateFormat.contains("epoch")) {
    				SimpleDateFormat sdfl = new SimpleDateFormat(dateFormat);
    				
    				 dt1 = sdfl.parse(dt0);
    					
    				 
    				}else {
    				 dt1 = new Date(Long.parseLong(dt0)*1000);
    				
    				}
    			
    			 
					 //break after reaching the end of true line
				if(!dt1.before(endt)){
					log.info("break, true line is reached!, total read: "+co);
					break;
				}
				
				if(logdata.equals(1)){
					timereading = System.nanoTime() - this.startTime;
					
				}
				
    			 if(dt1.after(startt) && dt1.before(endt)) {
    				 
					if(generalGrokPattern!=null) {
					 jsondataTemp = GrokHelper.parseGeneralGrok(grokfile,generalGrokPattern,line);
//					 System.out.println(jsondataTemp);
//					 System.exit(0);
					}else {
    				 jsondataTemp = GrokHelper.parseGrok(grokfile, regexPatterns, line);
//    				  System.out.print(jsondataTemp);
//    					 System.exit(0);
    				 
					}
    				 
    				 if(filterRegex.size()!=0) {
    					 
						boolean c = checkAllFilter(filterRegex, jsondataTemp);
						 if(c) {
							 jsondata=jsondataTemp;
							 
 							
 						 }
    				 }else {
						 jsondata=jsondataTemp;
						
    				 }
    			  } 
     		
     			if(jsondata!=null){
     				logdata++;     			
					JsonNode jd = addUUID(jsondata);
					alljson.add(jd);
				}
				co++;
			 
			
		      }
    	        in.close();
		     }    
		 	log.info("filtering finished");
		 	
			JSONObject alljsObj = new JSONObject();
		if(alljson.size()<0)  {
			log.info("filtered log line is empty! ");
		}
			alljsObj.put("logEntry",alljson);
			long timeextracting = System.nanoTime() - this.startTime;

			org.eclipse.rdf4j.model.Model rdf4jmodel  = jp.Parse(alljsObj.toString());
	
			long parsingtime = System.nanoTime()-this.startTime;
			log.info("parsing finished");
			JModel.addAll(rdf4jmodel);
			Util.saveRDF4JModel(rdf4jmodel, outputModel);
			log.info("delete previously indexed hdt file..");
			Util.deleteFile(hdtOutput+".index.v1-1");
			Util.generateHDTFile(namegraph, outputModel, "TURTLE", hdtOutput);
			//Util.MapHDTFile(hdtOutput);
			long compressingtime = System.nanoTime()-this.startTime;
			log.info("compression (hdt) finished..");
			Util.storeHDTFile(hdtOutput, hdtrepo);
			long uploadingtime = System.nanoTime()-this.startTime;
			response = "{\"content\":\"success\",\"endopoint\":\""+sparqlEndpoint+"\"}";
			System.out.println("===========SUMMARY============");
			System.out.println("read line :"+co);
			System.out.println("extracted line :"+logdata);
			System.out.println("parsed line :"+alljson.size());
			System.out.println("===========RUN-TIME============");
			System.out.println("reading time :"+timereading/1000000+" ms");
			System.out.println("extraction time :"+(timeextracting-timereading)/1000000+" ms");
			System.out.println("parsing time :"+(parsingtime-timeextracting)/1000000+" ms");
			System.out.println("Compressing time :"+(compressingtime-parsingtime)/1000000+" ms");
			System.out.println("Uploading time :"+(uploadingtime-compressingtime)/1000000+" ms");

	    		}
    			}
               catch (Exception closeException) {
          	}
		
		return response;
    	

	}
	private ArrayList<String> findRespectedLogFile(String startt, String endt, String logmeta) throws ParseException {
		
		//query log meta based on start and end date
		
		Model metaModel = RDFDataMgr.loadModel(logmeta) ;
		//create sparql query to select data based on date
        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
        		"select ?fid  where {\r\n" + 
        		"    ?s  <http://w3id.org/sepses/asset#startDate> ?sd.\r\n" + 
        		"    ?s  <http://w3id.org/sepses/asset#endDate> ?ed.\r\n" + 
        		"    ?s <http://w3id.org/sepses/asset#fileID> ?fid.\r\n" + 
        		"    FILTER(\""+startt+"\" >= ?sd && \""+startt+"\" <= ?ed  )\r\n" + 
        		"} \r\n" ; 

       

        QueryExecution qe = QueryExecutionFactory.create(query, metaModel);
        ResultSet rs = qe.execSelect();
        Integer c = null;
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            RDFNode co = qs.get("?fid");
            c = co.asLiteral().getInt();
        }       
        
        
        String query2 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
        		"select ?fid  where {\r\n" + 
        		"    ?s  <http://w3id.org/sepses/asset#startDate> ?sd.\r\n" + 
        		"    ?s  <http://w3id.org/sepses/asset#endDate> ?ed.\r\n" + 
        		"    ?s <http://w3id.org/sepses/asset#fileID> ?fid.\r\n" + 
        		"    FILTER(\""+endt+"\" >= ?sd && \""+endt+"\" <= ?ed  )\r\n" + 
        		"} \r\n"; 


       
        QueryExecution qe2 = QueryExecutionFactory.create(query2,metaModel);
        ResultSet rs2 = qe2.execSelect();
        Integer c2 = null;
        while (rs2.hasNext()) {
            QuerySolution qs2 = rs2.nextSolution();
            RDFNode co2 = qs2.get("?fid");
            c2 = co2.asLiteral().getInt();
        }
        
        ArrayList<String> c3 = new ArrayList<String>();

        if(c!=null && c2!=null) {
        	System.out.println("take between "+c+" and "+c2);
          //default (c2 & c !=null): take between
          c3 = takeBetween(c, c2, metaModel);   	
        }else if(c==null && c2!=null) {
    	 //take under = c2
        	System.out.println("take under "+c2);
    	  c3 = takeUnder(c2, metaModel);
       }else if(c!=null && c2==null) {
    	   System.out.println("take above "+c);
    	   
    	  c3 = takeAbove(c, metaModel);
      }else {
    	 
    	   if(checkIfDateInScope(startt, endt, metaModel)) {
    		    
    		  //take between 0 and metamodel.size()
    		   int countMeta =  getCountLogMeta(metaModel);
    		
    		  c3= takeBetween(0,countMeta-1,metaModel);
    	  }else {
    		  System.out.println("Date is out of range");
    	  }
      }
       			return c3;
	}

	
	private ArrayList<String> takeBetween(int c, int c2, Model metaModel) {
		ArrayList<String> res = new ArrayList<String>();
        String query3 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
        		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
        		"select ?l  where {\r\n" + 
        		"    ?s rdfs:label ?l.\r\n" + 
        		"    ?s  <http://w3id.org/sepses/asset#fileID> ?fid.\r\n" + 
        		"    \r\n" + 
        		"    FILTER(  ?fid <="+c2+" && ?fid >="+c+" )\r\n" + 
        		"} ORDER BY ASC(?fid)\r\n" + 
        		"";
        
        QueryExecution qe3 = QueryExecutionFactory.create(query3,metaModel);
        ResultSet rs3 = qe3.execSelect();
        while (rs3.hasNext()) {
            QuerySolution qs3 = rs3.nextSolution();
            RDFNode co3 = qs3.get("?l");
            res.add(co3.toString());
        }
        
		return res;
		
	}
	
	private ArrayList<String> takeUnder(int c2, Model metaModel) {
		ArrayList<String> res = new ArrayList<String>();
        String query3 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
           		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
           		"select ?l  where {\r\n" + 
           		"    ?s rdfs:label ?l.\r\n" + 
           		"    ?s  <http://w3id.org/sepses/asset#fileID> ?fid.\r\n" + 
           		"    \r\n" + 
           		"    FILTER(  ?fid <="+c2+")\r\n" + 
           		"} ORDER BY ASC(?fid)\r\n" + 
           		"";
        
        QueryExecution qe3 = QueryExecutionFactory.create(query3,metaModel);
        ResultSet rs3 = qe3.execSelect();
        while (rs3.hasNext()) {
            QuerySolution qs3 = rs3.nextSolution();
            RDFNode co3 = qs3.get("?l");
            res.add(co3.toString());
        }
        
		return res;
		
	}
	
	private ArrayList<String> takeAbove(int c, Model metaModel) {
		ArrayList<String> res = new ArrayList<String>();
        String query3 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
           		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
           		"select ?l  where {\r\n" + 
           		"    ?s rdfs:label ?l.\r\n" + 
           		"    ?s  <http://w3id.org/sepses/asset#fileID> ?fid.\r\n" + 
           		"    \r\n" + 
           		"    FILTER(  ?fid >="+c+")\r\n" + 
           		"} ORDER BY ASC(?fid)\r\n" + 
           		"";
        
        QueryExecution qe3 = QueryExecutionFactory.create(query3,metaModel);
        ResultSet rs3 = qe3.execSelect();
        while (rs3.hasNext()) {
            QuerySolution qs3 = rs3.nextSolution();
            RDFNode co3 = qs3.get("?l");
            res.add(co3.toString());
        }
        
		return res;
		
	}
	private int getCountLogMeta(Model metaModel) {
		int c = 0;
        String query3 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
           		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
           		"select (COUNT(?s) as ?c)  where {\r\n" + 
           		"    ?s rdfs:label ?l.\r\n" +  
           		"}";
   
        QueryExecution qe3 = QueryExecutionFactory.create(query3,metaModel);
        ResultSet rs3 = qe3.execSelect();
        while (rs3.hasNext()) {
            QuerySolution qs3 = rs3.nextSolution();
            RDFNode co3 = qs3.get("?c");
            
            c = co3.asLiteral().getInt();
           
        }
        
        //metaModel.write(System.out,"TURTLE");
		return c;
		
	}
	private HashMap<String, String> getMinMaxLogTime(Model metaModel) {
		HashMap<String, String> minMax = new HashMap<String, String>();
	
    String query3 = "select (MIN(?sd) as ?sdt) (MAX(?ed) as ?edt) where { \r\n" + 
    		"			?s <http://w3id.org/sepses/asset#startDate> ?sd .\r\n" + 
    		"		    ?s <http://w3id.org/sepses/asset#endDate> ?ed .\r\n" + 
    		"		}";
    
    QueryExecution qe3 = QueryExecutionFactory.create(query3,metaModel);
    ResultSet rs3 = qe3.execSelect();
    while (rs3.hasNext()) {
        QuerySolution qs3 = rs3.nextSolution();
        RDFNode sdt = qs3.get("?sdt");
        RDFNode edt = qs3.get("?edt");
        minMax.put("sdt", sdt.toString());
        minMax.put("edt", edt.toString());
    }

	return minMax;

	}
	
	
	private Boolean checkIfDateInScope(String startt,String endt, Model metaModel) throws ParseException {
		 //System.out.println(startt+" "+endt);
	HashMap<String, String> minMax = getMinMaxLogTime(metaModel);
  	  String df = "yyyy-MM-dd'T'hh:mm:ss";
  	  Long gsdt =  Util.datetimeToLong(startt, df);
  	  Long gedt = Util.datetimeToLong(endt, df);
  	  Long sdt =  Util.datetimeToLong(minMax.get("sdt"), df);
  	  Long edt = Util.datetimeToLong(minMax.get("edt"), df);
  	  if(gsdt<sdt && gedt>edt) {
  		
  		  return true;
  	  }else {
  		  return false;
  	  }
  	  
	}
	public boolean checkFilterJsonWithVariableRegex(JsonNode json, String variable, String regex) throws org.json.simple.parser.ParseException {
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
	
	public void translateQuery(String pq,String rm,String ro) throws Exception, org.json.simple.parser.ParseException{
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

	private boolean checkAllFilter(List<String> filterRegex, JsonNode jsondataTemp ) throws org.json.simple.parser.ParseException{
		ArrayList<Boolean> resFilter = new ArrayList<Boolean>();
		
		 for (int k=0;k<filterRegex.size();k++){
			 String[] fr = filterRegex.get(k).split("=");
						 	  Boolean  cf = checkFilterJsonWithVariableRegex(jsondataTemp,fr[0],fr[1]);
							resFilter.add(cf);
							
						}
		for (int k=0;k<resFilter.size();k++){
							
						}
		if(resFilter.contains(false)){
			return false;
		}else{
			return true;
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
	public static void deleteFile(String file) throws Exception {
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

	public String getContents(String outputResult) throws Exception, InterruptedException {
		//System.out.println("getContent: "+outputResult);
		String r = null;
		File file = new File(outputResult); 
		while (!file.exists()) {
			Thread.sleep(10);
		}
		  BufferedReader br = new BufferedReader(new FileReader(file)); 
		  String st; 
		  while ((st = br.readLine()) != null) {
			  if(r==null) {
				  r=st;
			  }else {
				  r=r+"\r\n"+st;
			  }
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
	
	public static void main( String[] args ) throws Exception
  {    
		// =======================audit=============================
//				String parsedQueryFile = "experiment/input/query-audit2.json";
//				String queryStringFile = "experiment/input/query-audit2.sparql";
//				String startTime = "2020-02-29T01:00:05";
//		    	String endDate = "2020-02-29T01:00:13";
		// =======================apache=============================
		String parsedQueryFile = "experiment/example_query/query-apache.json";
		String queryStringFile = "experiment/example_query/query-apache.sparql";
		String startTime = "2020-02-02T08:49:36";
		String endDate = "2020-03-01T11:40:14";
		// ========================auth===============================
//		String parsedQueryFile = "experiment/example_query/query-apache-error.json";
//		String queryStringFile = "experiment/example_query/query-apache-error.sparql";
//		String startTime = "2020-03-01T06:28:15";
//    	String endDate = "2020-03-05T06:55:10";

		String parsedQuery = new String(Files.readAllBytes(Paths.get(parsedQueryFile))); 
		String queryString = new String(Files.readAllBytes(Paths.get(queryStringFile))); 
	    new StartService(queryString,parsedQuery, startTime, endDate);
		
		
 	}
	
	 JsonNode addUUID(JsonNode json) throws org.json.simple.parser.ParseException{
		UUID ui = UUID.randomUUID();
		((ObjectNode) json).put("id", ui.toString());
		return json;
			
		
	}

}